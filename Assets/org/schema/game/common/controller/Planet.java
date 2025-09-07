package org.schema.game.common.controller;


import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.data.gamemap.GameMap;
import org.schema.game.client.data.gamemap.requests.GameMapRequest;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.AIPlanetConfiguration;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.PlanetManagerContainer;
import org.schema.game.common.controller.generator.PlanetCreatorThread;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.space.PlanetCore;
import org.schema.game.network.objects.NetworkPlanet;
import org.schema.game.server.ai.PlanetAIEntity;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.linearmath.Transform;

public class Planet extends ManagedUsableSegmentController<Planet>  {

	private final PlanetManagerContainer planetManagerContainer;

	public int fragmentId = -1;
	private AIPlanetConfiguration aiConfiguration;
	private PlanetType planetType = PlanetType.EARTH;
	private PlanetCore core;
	private String planetCoreUID = "none";
	private Vector3f blownOff;
	private boolean clientBlownOff;
	private boolean hasClientBlownOff;
	private boolean blownOffDebug;
	private boolean transientMoved;
	private boolean checkEmpty;

	public Planet(StateInterface state) {
		super(state);
		planetManagerContainer = new PlanetManagerContainer(state, this);
		aiConfiguration = new AIPlanetConfiguration(state, this);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.PLANET_SEGMENT;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#affectsGravityOf(org.schema.game.common.data.world.SimpleTransformableSendableObject)
	 */
	@Override
	protected boolean affectsGravityOf(
			SimpleTransformableSendableObject<?> target) {
		if (target instanceof AbstractCharacter<?> && ((AbstractCharacter<?>) target).getOwnerState().isSitting()) {
			return false;
		}
		if(FactionManager.isNPCFaction(target.getFactionId()) ){
			//no gravity for NPC ships
			return false;
		}
		return target.getSectorId() == getSectorId() && 
				(target.getMass() > 0 || target instanceof AbstractCharacter<?>) &&
				checkGravityDownwards(target);
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.PLANET;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getGravityAABB(javax.vecmath.Vector3f, javax.vecmath.Vector3f)
	 */
	@Override
	public void getGravityAABB(Vector3f minOut, Vector3f maxOut) {
		super.getGravityAABB(minOut, maxOut);
		maxOut.y += 32;
	}

	@Override
	public void getGravityAABB(Transform t,
	                           Vector3f minOut, Vector3f maxOut) {
		super.getGravityAABB(t, minOut, maxOut);
		maxOut.y += 32;
	}

	@Override
	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyPlanet);
			case FRIEND -> out.set(ColorPalette.allyPlanet);
			case NEUTRAL -> out.set(ColorPalette.neutralPlanet);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionPlanet);
		}
		out.x += select;
		out.y += select;
		out.z += select;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		//		deathStarAIEntity = new DeathStarAIEntity(this);
		setMass(0);

		setRealName("Planet");
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#isGravitySource()
	 */
	@Override
	public boolean isGravitySource() {
		return true;
	}

	@Override
	public boolean isHomeBase() {
		return super.isHomeBase() || isAnyPlanetSegmentHomebase();
	}

	@Override
	public void onSectorInactiveClient() {
		super.onSectorInactiveClient();
		planetManagerContainer.getShoppingAddOn().onSectorInactiveClient();

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#cleanUpOnEntityDelete()
	 */
	@Override
	public void cleanUpOnEntityDelete() {

		super.cleanUpOnEntityDelete();

		planetManagerContainer.getShoppingAddOn().cleanUp();

	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();

		// Update map for deleted planet
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
	}

	@Override
	public String toNiceString() {
		String nc = "PlanetSegment(" + getRealName() + ");";
		if (getFactionId() != 0) {
			nc += ("[");
			Faction f = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
			if (f != null) {
				nc += (f.getName());
			} else {
				nc += ("factionUnknown");
				nc += (getFactionId());
			}
			nc += ("]");
		}
		return nc;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
	
		if (!isOnServer()) {
			planetCoreUID = getNetworkObject().planetUid.get();
			setSeed(getNetworkObject().seed.getLong());
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);

		if (!isOnServer()) {
			clientBlownOff = ((NetworkPlanet) super.getNetworkObject()).blownOff.get();
			planetCoreUID = getNetworkObject().planetUid.get();
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		if (isOnServer()) {
			getNetworkObject().planetUid.set(planetCoreUID);
			getNetworkObject().seed.set(getSeed());
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToNetworkObject()
	 */
	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		if (isOnServer()) {
			getNetworkObject().planetUid.set(planetCoreUID);
		}
	}

	@Override
	public void onRename(String oldName, String newName) {

		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());

		Vector3i playerPos = new Vector3i();
		if (isOnServer()) {
			for (PlayerState state : ((GameServerState) getState()).getPlayerStatesByName().values()) {
				StellarSystem.getPosFromSector(new Vector3i(state.getCurrentSector()), playerPos);
				if (playerPos.equals(sysPos)) {
					((GameServerState) getState()).getGameMapProvider().addRequestServer(new GameMapRequest(GameMap.TYPE_SYSTEM, sysPos), state.getClientChannel());
				}
			}
		}
	}

	@Override
	public boolean hasStructureAndArmorHP() {
		return false;
	}

	@Override
	public boolean isHomeBaseFor(int forFactionId) {

		return super.isHomeBaseFor(forFactionId) || isAnyPlanetSegmentHomebase();

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		assert (tag.getName().equals("Planet"));

		Tag[] subTags = (Tag[]) tag.getValue();
		if (subTags[0].getType() == Type.BYTE && subTags[1].getType() == Type.STRING) {
			fragmentId = (Byte) subTags[0].getValue() - 1;
			planetCoreUID = (String) subTags[1].getValue();
			super.fromTagStructure(subTags[2]);
		} else {
			//old version (the byte was a wild card in the old one)
			super.fromTagStructure(subTags[1]);
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#setFactionId(int)
	 */
	@Override
	public void setFactionId(int factionId) {
		if(blownOffDebug){
			System.err.println("[PLANET] Cannot set faction on blown up planet");
			factionId = 0;
		}
		super.setFactionId(factionId);
		if (core != null) {
			core.setFactionAll(factionId);
		}

	}

	//	public void notifyElementChanged() {
	//		this.setChanged();
	//		this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);
	//	}

	@Override
	public int getCreatorId() {
		return planetType.ordinal();
	}


	@Override
	public void onRemovedElementSynched(short removedType, int oldSize, byte x, byte y, byte z, byte oldOrientation, Segment segment, boolean preserveControl, long time) {
		planetManagerContainer.onRemovedElementSynched(removedType, oldSize, x, y, z, segment, preserveControl);
		super.onRemovedElementSynched(removedType, oldSize, x, y, z, oldOrientation, segment, preserveControl, time);
	}

	@Override
	public void setCreatorId(int id) {
		planetType = PlanetType.values()[id];
	}


	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {

		return new Tag(Type.STRUCT, "Planet",
				new Tag[]{
						new Tag(Type.BYTE, null, (byte) (fragmentId + 1)),
						new Tag(Type.STRING, null, planetCoreUID),
						super.toTagStructure(),
						FinishTag.INST});

	}

	@Override
	public boolean isRankAllowedToChangeFaction(int targetFactionId, PlayerState player, byte rank) {

		/*
		 * only faction members with homebase permission may reset
		 * the faction signature of a planet that has a homebase plate
		 */
		if (targetFactionId == 0 && ((FactionState) getState()).getFactionManager().existsFaction(getFactionId()) && isHomeBase()) {

			if (((FactionState) getState()).getFactionManager().existsFaction(player.getFactionId())) {
				if (!((FactionState) getState()).getFactionManager().getFaction(player.getFactionId()).getRoles().hasHomebasePermission(rank)) {

					return false;
				}
			}
		}
		return super.isRankAllowedToChangeFaction(targetFactionId, player, rank);
	}




	//	/* (non-Javadoc)
	//	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#hasVirtual()
	//	 */
	//	@Override
	//	protected boolean hasVirtual() {
	//		return false;
	//	}
	@Override
	public AIGameConfiguration<PlanetAIEntity, Planet> getAiConfiguration() {
		return aiConfiguration;
	}

	
	@Override
	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i where, Vector3i parameter) {
		super.onAttachPlayer(playerState, detachedFrom, where, parameter);
		if (!isOnServer() && ((GameClientState) getState()).getPlayer() == playerState) {

			GameClientState s = (GameClientState) getState();
			if (s.getPlayer() == playerState) {
				PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().
						getPlayerGameControlManager();
				playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().setActive(true);
				System.err.println("Entering space stationc ");

			}
		}
	}

	@Override
	public void onDetachPlayer(PlayerState playerState, boolean hide, Vector3i parameter) {
		if (!isOnServer()) {

			GameClientState s = (GameClientState) getState();
			if (s.getPlayer() == playerState && ((GameClientState) getState()).getPlayer() == playerState) {
				PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().
						getPlayerGameControlManager();
				playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().setActive(false);

				//				if(getShipAudioContainer().inShipSound != null){
				//					getShipAudioContainer().inShipSound.pause();
				//				}
				//				if(getShipAudioContainer().coreSound != null){
				//					getShipAudioContainer().coreSound.resume();
				//				}
			}
		}
		//		refreashNameTag();
		Starter.modManager.onSegmentControllerPlayerDetached(this);
	}


	@Override
	protected short getCoreType() {
		return ElementKeyMap.DEATHSTAR_CORE_ID;
	}

	@Override
	public NetworkPlanet getNetworkObject() {
		return (NetworkPlanet) super.getNetworkObject();
	}

	@Override
	protected String getSegmentControllerTypeString() {
		return "Planet";
	}



	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkPlanet(getState(), this));
	}

	@Override
	public void onAddedElementSynched(short type, byte orientation, byte x, byte y, byte z, Segment segment, boolean updateSegementBuffer, long absIndex, long time, boolean revalidate) {

		planetManagerContainer.onAddedElementSynched(type, segment, absIndex, time, revalidate);
		super.onAddedElementSynched(type, orientation, x, y, z, segment, updateSegementBuffer, absIndex, time, revalidate);

	}

	@Override
	protected void onCoreDestroyed(Damager from) {

	}

	@Override
	public void onDamageServerRootObject(float actualDamage, Damager from) {
		super.onDamageServerRootObject(actualDamage, from);
		aiConfiguration.onDamageServer(actualDamage, from);
		planetManagerContainer.getShoppingAddOn().onHit(from);
	}

	@Override
	public void startCreatorThread() {
		if (getCreatorThread() == null) {
			setCreatorThread(new PlanetCreatorThread(this, planetType));
		}
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#toString()
	 */
	@Override
	public String toString() {
		return "Planet(" + getId() + ")[s" + getSectorId() + "]" + getPlanetInfo();
	}

	@Override
	protected boolean canObjectOverlap(Physical sendable) {

		boolean ret = super.canObjectOverlap(sendable);

		if (sendable instanceof PlayerCharacter)
		{
			for (PlayerState state : getAttachedPlayers())
			{
				if (state.getAssingedPlayerCharacter() != null &&
						state.getAssingedPlayerCharacter().equals(sendable))
				{
					return false;
				}
			}
		}

		return ret;
	}

	/* (non-Javadoc)
		 * @see org.schema.game.common.controller.EditableSendableSegmentController#update(org.schema.schine.graphicsengine.core.Timer)
		 */
	@Override
	public void updateLocal(Timer timer) {

		super.updateLocal(timer);

		if (isOnServer() && getTotalElements() <= 0 &&
				System.currentTimeMillis() - getTimeCreated() > 50000
				&& isEmptyOnServer()) {
			if(getSegmentBuffer().isFullyLoaded()){
				System.err.println("[SERVER][Planet] Empty planet section: deleting " + this);
				this.setMarkedForDeleteVolatile(true);
			}
		}

		if (!isOnServer() && core == null && !planetCoreUID.equals("none")) {
			// its already ste on server under guarantee
			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(planetCoreUID);
			if (sendable != null) {
				core = (PlanetCore) sendable;
			}

		}
		if (core != null && !getRealName().equals("Planet") && !core.getRealName().equals(getRealName())) {

			core.setRealNameToAll(getRealName());
		}

		if (isOnServer() && blownOff != null) {
			((NetworkPlanet) super.getNetworkObject()).blownOff.set(true);

			doBlowOff();

			blownOff = null;
			System.err.println("SERVER PLANET CORE EXPLOSION: " + this);

		} else if (clientBlownOff && !hasClientBlownOff) {
			doBlowOff();

			System.err.println("CLIENT PLANET CORE EXPLOSION: " + this);
			hasClientBlownOff = true;
		}
		if (blownOffDebug) {
			//			System.err.println(this.getState()+" PLANET IS BLOWN OFF: "+this);
			assert (((RigidBodySegmentController) getPhysicsDataContainer().getObject()).isCollisionException());

			Vector3f linearVelocity = ((RigidBodySegmentController) getPhysicsDataContainer().getObject()).getLinearVelocity(new Vector3f());
			if (linearVelocity.lengthSquared() <= 0.01) {
				blownOffDebug = false;

				if (isOverlapping()) {
					System.err.println("STILL OVERLAPPING: PUSHING FURTHER");
					doBlowOff();
				} else {
					reinstate();
				}
			}

		}

		if (isOnServer() && checkEmpty) {
			if (getTotalElements() <= 0) {
				destroy();
			}
			checkEmpty = false;
		}


		Starter.modManager.onSegmentControllerUpdate(this);

	}

	@Override
	public PlanetManagerContainer getManagerContainer() {
		return planetManagerContainer;
	}

	@Override
	public SegmentController getSegmentController() {
		return this;
	}


	/**
	 * @return the planetType
	 */
	public PlanetType getPlanetType() {
		return planetType;
	}

	@Override
	public boolean isSalvagableFor(Salvager harvester,
	                               String[] cannotHitReason, Vector3i position) {
		AbstractOwnerState p = harvester.getOwnerState();
		if(harvester.getFactionId() == getFactionId() && 
				((p != null && p instanceof PlayerState && !allowedToEdit((PlayerState) p)) || harvester.getOwnerFactionRights() < getFactionRights())){
			cannotHitReason[0] = Lng.str("Cannot salvage: insufficient faction rank");
			return false;
		}
		if (harvester.getFactionId() == getFactionId()) {
			return true;
		}

		if (isHomeBase() ||
				(getDockingController().getDockedOn() != null && getDockingController().getDockedOn().to.getSegment().getSegmentController().isHomeBaseFor(getFactionId()))) {

			cannotHitReason[0] = Lng.str("Cannot salvage: home base protected");
			return false;
		}
		return true;
	}

	public boolean isHomebaseSingle(int forFactionId) {
		return super.isHomeBaseFor(forFactionId);
	}

	/**
	 * @return true, if this planet has another segment that is the faction id and is homebase
	 */
	private boolean isAnyPlanetSegmentHomebase() {
		FactionManager factionManager = ((FactionState) getState()).getFactionManager();
		if (factionManager.existsFaction(getFactionId())) {
			synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (s instanceof Planet) {
						Planet p = (Planet) s;
						if (p.core != null && core != null) {
							assert (p.core.getUniqueIdentifier() != null) : p;
							assert (core.getUniqueIdentifier() != null) : this;
							if (p.core.getUniqueIdentifier().equals(core.getUniqueIdentifier())) {
								if (getFactionId() == p.getFactionId()) {

									if (p.isHomebaseSingle(p.getFactionId())) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private String getPlanetInfo() {
		if ("none".equals(planetCoreUID)) {
			return "none";
		}
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(planetCoreUID);
		if (sendable != null) {
			return ((PlanetCore) sendable).toNiceString();
		} else {
			//			System.err.println(getState().getLocalAndRemoteObjectContainer().getUidObjectMap());
		}
		return planetCoreUID + "(unloaded)";
	}

	private void doBlowOff() {
		assert (!blownOffDebug);
	
		
		System.err.println("[PLANET] "+getState()+" DO BLOW OFF "+this);
		if(isOnServer()){
			setFactionId(0);
		}
		onPhysicsRemove();
		getPhysicsDataContainer().setObject(null);
		setMass(0.1f);
		initPhysics();
		((RigidBodySegmentController) getPhysicsDataContainer().getObject()).setCollisionException(true);
		onPhysicsAdd();

		Vector3f direction = new Vector3f(getWorldTransform().origin);
		direction.normalize();
		direction.scale(15);
		((RigidBodySegmentController) getPhysicsDataContainer().getObject()).applyCentralImpulse(direction);
		direction.scale(2);
		((RigidBodySegmentController) getPhysicsDataContainer().getObject()).applyTorqueImpulse(direction);

		blownOffDebug = true;

		assert (((RigidBodySegmentController) getPhysicsDataContainer().getObject()).isCollisionException());
		//resetMass to 0
		setMass(0.0f);
	}

	private void reinstate() {
		System.err.println("[PLANET] REINSTATE");
		onPhysicsRemove();
		getPhysicsDataContainer().setObject(null);
		setMass(0.0f);
		initPhysics();
		((RigidBodySegmentController) getPhysicsDataContainer().getObject()).setCollisionException(false);
		onPhysicsAdd();
		setMass(0.0f);
		blownOffDebug = false;
	}

	public PlanetCore getCore() {
		return core;
	}

	/**
	 * @return the planetCoreUID
	 */
	public String getPlanetCoreUID() {
		return planetCoreUID;
	}

	/**
	 * @param planetCoreUID the planetCoreUID to set
	 */
	public void setPlanetCoreUID(String planetCoreUID) {
		this.planetCoreUID = planetCoreUID;
	}

	public void setPlanetCore(PlanetCore planetCore) {
		this.core = planetCore;
	}

	public void setBlownOff(Vector3f blownOff) {
		this.blownOff = blownOff;
	}


	


	@Override
	public boolean isMoved() {
		return transientMoved;
	}

	@Override
	public void setMoved(boolean b) {
		if (b != this.transientMoved) {
			setChangedForDb(true);
		}
		transientMoved = b;
		setMoved(b);
	}

	@Override
	public void onPlayerDetachedFromThis(PlayerState pState,
			PlayerControllable newAttached) {
		
	}
	@Override
	public boolean isStatic() {
		return true;
	}
	
}
