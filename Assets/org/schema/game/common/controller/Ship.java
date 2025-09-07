package org.schema.game.common.controller;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.FastMath;
import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.manager.ingame.BuildCallback;
import org.schema.game.client.controller.manager.ingame.BuildInstruction;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.SymmetryPlanes;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.buildhelper.BuildHelper;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.HpTrigger.HpTriggerType;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.AIShipConfiguration;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.stealth.StealthAddOn.StealthLvl;
import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;
import org.schema.game.common.controller.generator.EmptyCreatorThread;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.*;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.NetworkShip;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.AudioEntity;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

public class Ship extends ManagedUsableSegmentController<Ship> implements AudioEntity {

	public static final Vector3i core = new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
	public static final float MAX_TURN = 4.0f;
	public static final float BASE_TURN = 6.2f;
	//	public static final float DIVIDER = 1.1f;
	public static final float MASS_CONTRIB = 0.0055f;
	public static final float ROLL_EXTRA = 2.5f;
	private final AIGameConfiguration<ShipAIEntity, Ship> aiConfiguration;
	private final ShipManagerContainer shipManagerContainer;
	private final Vector3f orientationForce = new Vector3f();
	private final Vector3f veloTmp = new Vector3f();
	private String nameTag = "";

	private Vector3b tmpLocalPos = new Vector3b();
	private int lastFactionId;
	private long lastEmptyCheck;
	private BlueprintInterface spawnedFrom;
	private String blueprintSpawnedBy;
	private boolean modifiedBleuprintTriggered;
	private boolean modifiedBleuprintTriggeredExecuted;
	private Faction lastFaction;
	private boolean flagNameChange;
	private boolean modifiedBleuprintModifiable = true;

	public long lastPickupAreaUsed = Long.MIN_VALUE;
	private long lastPickupAreaUsedBefore = Long.MIN_VALUE;
	private String copiedFromUID;
	public float lastSpeed;
	private boolean coreDestroyedFlag;
@Override
	public SendableType getSendableType() {
		return SendableTypes.SHIP;
	}
	public Ship(StateInterface state) {
		super(state);

		shipManagerContainer = new ShipManagerContainer(state, this);
		aiConfiguration = new AIShipConfiguration(state, this);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.SHIP;
	}

//	/* (non-Javadoc)
//	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getRelationColor(org.schema.game.common.data.player.faction.FactionRelation.RType, javax.vecmath.Vector4f, float)
//	 */
//	@Override
//	public void getRelationColor(RType relation, Vector4f out, float select) {
//		if (isCoreOverheating()) {
//			out.x = 0.0f;
//			out.y = 0.8f + select;
//			out.z = 0.7f + select;
//
//			return;
//		}
//		if (((GameClientState) getState()).getPlayer().getFactionId() != this.getFactionId() &&
//				((GameClientState) getState()).getFactionManager().existsFaction(this.getFactionId()) &&
//				((GameClientState) getState()).getFactionManager().getFaction(this.getFactionId()).getFactionMode() != 0) {
//			Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(this.getFactionId());
//			out.x = faction.getColor().x;
//			out.y = faction.getColor().y;
//			out.z = faction.getColor().z;
//
//			return;
//		}
//		if (((GameClientState) getState()).getPlayer().getFactionId() == this.getFactionId() &&
//				((GameClientState) getState()).getFactionManager().existsFaction(this.getFactionId()) &&
//				((GameClientState) getState()).getFactionManager().getFaction(this.getFactionId()).isFactionMode(Faction.MODE_FIGHTERS_FFA)) {
//			//always enemy
//			out.x = 1f + select;
//			out.y = select;
//			out.z = select;
//			return;
//		}
//		switch (relation) {
//			case ENEMY:
//				out.x = 1f + select;
//				out.y = select;
//				out.z = select;
//				break;
//
//			case FRIEND:
//				out.x = select;
//				out.y = 1f + select;
//				out.z = select;
//				break;
//
//			case NEUTRAL:
//				if (getAttachedPlayers().isEmpty()) {
//					out.x = 0.5f + select;
//					out.y = 0.7f + select;
//					out.z = 0.9f + select;
//				} else {
//					out.x = 0.3f + select;
//					out.y = 0.5f + select;
//					out.z = 0.7f + select;
//				}
//				break;
//		}
//	}
	@Override
	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {


		if (((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive() &&
				((GameClientState) getState()).getPlayer().getFactionId() != this.getFactionId() &&
				((GameClientState) getState()).getFactionManager().existsFaction(this.getFactionId()) &&
				((GameClientState) getState()).getFactionManager().getFaction(this.getFactionId()).getFactionMode() != 0) {
			Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(this.getFactionId());
			out.x = faction.getColor().x;
			out.y = faction.getColor().y;
			out.z = faction.getColor().z;

			return;
		}

		if (isCoreOverheating()) {
			switch(relation) {
				case ENEMY -> out.set(ColorPalette.enemyOverheating);
				case FRIEND -> out.set(ColorPalette.allyOverheating);
				case NEUTRAL -> out.set(ColorPalette.neutralOverheating);
			}
			if(sameFaction) {
				out.set(ColorPalette.factionOverheating);
			}
			out.x += pulse * 0.25f;
			out.y += pulse * 0.25f;
			out.z += pulse * 0.25f;
		}else if(!railController.isRoot() && !railController.isPowered()) {
			switch(relation) {
				case ENEMY -> out.set(ColorPalette.allyUnpoweredDock);
				case FRIEND -> out.set(ColorPalette.allyUnpoweredDock);
				case NEUTRAL -> out.set(ColorPalette.neutralUnpoweredDock);
			}
			if(sameFaction) {
				out.set(ColorPalette.factionUnpoweredDock);
			}
		}else if(railController.isTurretDocked()) {
			switch(relation) {
				case ENEMY -> out.set(ColorPalette.enemyTurret);
				case FRIEND -> out.set(ColorPalette.allyTurret);
				case NEUTRAL -> out.set(ColorPalette.neutralTurret);
			}
			if(sameFaction) {
				out.set(ColorPalette.factionTurret);
			}
		}else if(railController.isDocked()) {
			switch(relation) {
				case ENEMY -> out.set(ColorPalette.enemyDock);
				case FRIEND -> out.set(ColorPalette.allyDock);
				case NEUTRAL -> out.set(ColorPalette.neutralDock);
			}
			if(sameFaction) {
				out.set(ColorPalette.factionDock);
			}
		}else {
			switch(relation) {
				case ENEMY -> out.set(ColorPalette.enemyShip);
				case FRIEND -> out.set(ColorPalette.allyShip);
				case NEUTRAL -> out.set(ColorPalette.neutralShip);
			}
			if(sameFaction) {
				out.set(ColorPalette.factionShip);
			}
		}

		out.x += select;
		out.y += select;
		out.z += select;
	}



	@Override
	public void onRevealingAction() {
		shipManagerContainer.getStealthElementManager().stopStealth(1);

		shipManagerContainer.onRevealingAction();
	}

	@Override
	public void onFTLJump() {
		super.onFTLJump();
		try {
			shipManagerContainer.getJumpDrive().getElementManager().onJumpComplete();
		} catch (Exception ex){
			System.err.println("!!! Unexpected condition ending FTL jump for ship: " + getName());
			ex.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#allowedType(short)
	 */
	@Override
	public boolean allowedType(short type) {
		if(true){
			return true;
		}
		boolean a = !ElementKeyMap.getInfo(type).getType().hasParent("spacestation");

		if(ElementKeyMap.getInfo(type).getType().hasParent("factory")) {
			a = (((GameStateInterface)getState()).getGameState()).isAllowFactoryOnShips();
		}
		a = a || ElementKeyMap.getInfo(type).getType().hasParent("ship");
		if (!a && !isOnServer()) {
			((GameClientController) getState().getController()).popupAlertTextMessage(
					Lng.str("Cannot place\n%s\non a ship!",  ElementKeyMap.getInfo(type).getName()), 0);
		}
		return super.allowedType(type) && a;
	}

	@Override
	public boolean checkCore(SegmentPiece pointUnsave) {
		return pointUnsave.getType() == ElementKeyMap.CORE_ID;
	}
	@Override
	public void checkInitialPositionServer(Transform t){
		assert(isOnServer());

		avoid(getInitialTransform(), true);


	}

	@Override
	public int getNearestIntersection(short type, Vector3f fromRay, Vector3f toRay, BuildCallback callback, int elementOrientation, boolean activateBlock, DimensionFilter dimensionFilter, Vector3i size, int count, float editDistance, SymmetryPlanes symmetryPlanes, BuildHelper posesFilter, BuildInstruction buildInstruction) throws ElementPositionBlockedException, BlockedByDockedElementException, BlockNotBuildTooFast {
		if (symmetryPlanes.getPlaceMode() == 0 && type == ElementKeyMap.AI_ELEMENT && aiConfiguration.getControllerBlock() != null) {
			if (!isOnServer()) {
				((GameClientState) getState()).getController().popupAlertTextMessage(
						Lng.str("ERROR\nOnly one AI block is permitted\nper ship!"), 0);
			}
			return 0;
		}

		if (symmetryPlanes.getPlaceMode() == 0 && type == ElementKeyMap.CORE_ID) {
			if (!isOnServer()) {
				((GameClientState) getState()).getController().popupAlertTextMessage(
						Lng.str("ERROR\nShip Cores cannot be placed,\nthey are used to spawn new ships."), 0);
			}
			return 0;
		}
		//		try {
		return super.getNearestIntersection(type, fromRay, toRay, callback, elementOrientation, activateBlock, dimensionFilter, size, count, editDistance, symmetryPlanes, posesFilter, buildInstruction);
		//		} catch (ElementPositionBlockedException e) {
		//			((GameClientState)getState()).getController().popupAlertTextMessage(
		//					"Cannot build here!\n" +
		//					"Something is blocking this position!", 0);
		//		} catch (BlockedByDockedElementException e) {
		//			((GameClientState)getState()).getController().popupAlertTextMessage("Cannot build here!\nPosition blocked\nby active docking area!", 0);
		//		}
		//		return 0;
	}

	@Override
	public NetworkShip getNetworkObject() {
		return (NetworkShip) super.getNetworkObject();
	}

	@Override
	protected void onFullyLoaded() {
		if(isOnServer()){
			if(ServerConfig.REMOVE_ENTITIES_WITH_INCONSISTENT_BLOCKS.isOn()){
				for(short s : ElementKeyMap.keySet){
					if(s != ElementKeyMap.CORE_ID && !allowedType(s) && getElementClassCountMap().get(s) > 0){

						Object[] msg = Lng.astr("Ship %s\nused a block type that is not allowed on ships:\n%s\nIt will be removed!", this, ElementKeyMap.toString(s));
						((GameServerState)getState()).getController().broadcastMessage(msg, ServerMessage.MESSAGE_TYPE_ERROR);
						System.err.println("[SERVER] "+msg);
						LogUtil.log().fine(String.format("Ship %s\nused block type that is not allowed on ships:\n%s\nIt will be removed!",this,ElementKeyMap.toString(s)));

						markForPermanentDelete(true);
						setMarkedForDeleteVolatile(true);

					}
				}
			}
		}
		super.onFullyLoaded();
	}

	@Override
	protected String getSegmentControllerTypeString() {
		return "Ship";
	}

//	@Override
//	public ParticleHitCallback handleHit(ParticleHitCallback callback, Damager from, float damage, float damageBeforeShield, Vector3f startPos, Vector3f endPos, boolean shieldAbsorbed, long weaponId) {
//
//		shipManagerContainer.onHitNotice();
//
//		super.handleHit(callback, from, damage, damageBeforeShield, startPos, endPos, shieldAbsorbed, weaponId);
//
//		if (callback.hasDoneDamage()) {
//			for (int i = 0; i < getAttachedPlayers().size(); i++) {
//				getAttachedPlayers().get(i).onVesselHit(this);
//			}
//		}
//		return callback;
//	}

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkShip(getState(), this));
	}

	//	public void notifyElementChanged() {
	//		this.setChanged();
	//		this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);
	//	}
	@Override
	public void onAddedElementSynched(short type, byte orientation, byte x, byte y, byte z, Segment segment, boolean updateSegementBuffer, long absIndex, long time, boolean revalidate) {

		shipManagerContainer.onAddedElementSynched(type, segment, absIndex, time, revalidate);

		super.onAddedElementSynched(type, orientation, x, y, z, segment, updateSegementBuffer, absIndex, time, revalidate);
	}

	@Override
	protected void onCoreDestroyed(Damager from) {

		if (isOnServer()) {
			if (ServerConfig.USE_STRUCTURE_HP.isOn()) {

				getHpController().onHullDamage(from, 255, ElementKeyMap.CORE_ID, DamageDealerType.PROJECTILE);

				if(isNewPowerSystemNoReactor() || (railController.isDockedAndExecuted() && (!railController.getRoot().isUsingOldPower()))){
					coreDestroyedFlag = true;
				}
			} else {
				// OLD SYSTEM: Kill all when core destroyed and start overheating
				for (int i = 0; i < getAttachedPlayers().size(); i++) {
					getAttachedPlayers().get(i).handleServerHealthAndCheckAliveOnServer(0, from);
				}
				aiConfiguration.onCoreDestroyed(from);

				startCoreOverheating(from);
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#onCoreHitAlreadyDestroyed(float)
	 */
	@Override
	protected void onCoreHitAlreadyDestroyed(float damage) {
		super.onCoreHitAlreadyDestroyed(damage);
//		if(isOnServer() && isCoreOverheating()){
//			coreTimerDuration = (long) Math.max(60000, coreTimerDuration - damage * 10);
//		}
	}

	@Override
	public void onDamageServerRootObject(float actualDamage, Damager from) {
		super.onDamageServerRootObject(actualDamage, from);
		aiConfiguration.onDamageServer(actualDamage, from);

	}

	@Override
	public void startCreatorThread() {
		if (getCreatorThread() == null) {
			setCreatorThread(new EmptyCreatorThread(this));
		}
	}

	@Override
	public void startCoreOverheating(Damager from) {
		coreDestroyedFlag = false;
		super.startCoreOverheating(from);

	}

	@Override
	public String toString() {
		return "Ship[" + getRealName() + "](" + getId() + ")";
	}

	@Override
	public boolean isNewPowerSystemNoReactorOverheatingCondition() {
		return coreDestroyedFlag;
	}
	@Override
	public boolean isNewPowerSystemNoReactor() {
		return !isUsingOldPower() && !hasAnyReactors() && !railController.isDockedAndExecuted();
		//return coreDestroyedFlag || super.isNewPowerSystemNoReactorOverheatingCondition(); //release
	}
	@Override
	public void updateLocal(Timer timer) {

		getState().getDebugTimer().start(this, "ShipUpdate");

		long time = System.currentTimeMillis();

		if (!isOnServer()) {
			if (!getAttachedPlayers().isEmpty() && getAttachedPlayers().get(0).isClientOwnPlayer()) {
				getRemoteTransformable().useSmoother = false;
				getRemoteTransformable().setSendFromClient(true);
			} else {
				getRemoteTransformable().useSmoother = true;
				getRemoteTransformable().setSendFromClient(false);
			}
		}
		if(isOnServer() &&
				getFactionId() >= 0 &&
				aiConfiguration.isActiveAI() && getConfigManager().apply(StatusEffectType.AI_DISABLE, false)){
			sendControllingPlayersServerMessage(Lng.astr("---WARNING---\nEffects on this ship disabled AI functions"), ServerMessage.MESSAGE_TYPE_ERROR);
			activateAI(false, true);
		}
		if (((GameStateInterface) getState()).getGameState().getFrozenSectors().contains(getSectorId())) {
			CollisionObject object = getPhysicsDataContainer().getObject();
			if (object != null && object instanceof RigidBodySegmentController) {
				((RigidBodySegmentController) object).setLinearVelocity(new Vector3f());
				((RigidBodySegmentController) object).setAngularVelocity(new Vector3f());
			}
		}

		float tRot = shipManagerContainer.getThrusterElementManager().rotationBalance
				*ThrusterElementManager.THRUST_ROT_PERCENT_MULT;

		orientationForce.set(
				Math.min(ThrusterElementManager.MAX_ROTATIONAL_FORCE_X, Math.max(0.001f, ((ThrusterElementManager.BASE_ROTATIONAL_FORCE_X+tRot)/Math.max(1f, FastMath.pow(railController.railedInertia.x, ThrusterElementManager.INTERTIA_POW))))),
				Math.min(ThrusterElementManager.MAX_ROTATIONAL_FORCE_Y, Math.max(0.001f, ((ThrusterElementManager.BASE_ROTATIONAL_FORCE_Y+tRot)/Math.max(1f, FastMath.pow(railController.railedInertia.y, ThrusterElementManager.INTERTIA_POW))))),
				Math.min(ThrusterElementManager.MAX_ROTATIONAL_FORCE_Z, Math.max(0.001f, ((ThrusterElementManager.BASE_ROTATIONAL_FORCE_Z+tRot)/Math.max(1f, FastMath.pow(railController.railedInertia.z, ThrusterElementManager.INTERTIA_POW))))));
//		getOrientationForce().set(
//				Math.min(10f, Math.max(0.001f, ((10f+tRot)/Math.max(1f, FastMath.pow(getPhysicsDataContainer().inertia.x, ThrusterElementManager.INTERTIA_POW))))),
//				Math.min(10f, Math.max(0.001f, ((10f+tRot)/Math.max(1f, FastMath.pow(getPhysicsDataContainer().inertia.y, ThrusterElementManager.INTERTIA_POW))))),
//				Math.min(10f, Math.max(0.001f, ((10f+tRot)/Math.max(1f, FastMath.pow(getPhysicsDataContainer().inertia.z, ThrusterElementManager.INTERTIA_POW))))));
//		if(isClientOwnObject()){
//			System.err.println("FORCE ::: "+getOrientationForce()+"; "+getPhysicsDataContainer().inertia);
//		}
//		getOrientationForce().set(
//				1f/Math.max(1, 0),
//				1f/Math.max(1, 0),
//				1f/Math.max(1, 0));

		if (getDockingController().isDocked()) {
			shipManagerContainer.getThrusterElementManager().getVelocity().set(0, 0, 0);
		} else {
			//this is just for the plum
			if (shipManagerContainer.getThrusterElementManager().getLastUpdateNum() < getState().getNumberOfUpdate() - 10) {
				//			System.err.println("ALLALL "+shipManagerContainer.getThrusterElementManager().getLastUpdateNum()+"; "+(getState().getUpdateNumber() - 10));
				shipManagerContainer.getThrusterElementManager().getVelocity().scale(Math.max(0, 1f - timer.getDelta()));

			}
		}

		//		long t0 = System.currentTimeMillis();
		super.updateLocal(timer);



		//		long tookSuper = System.currentTimeMillis() - t0;
		//		if(tookSuper > 5){
		//			System.err.println("[SHIP] "+getState()+" "+this+" super update took "+tookSuper);
		//		}

		//		t0 = System.currentTimeMillis();
		if (!getDockingController().isDocked() && !railController.isDocked() && flagUpdateMass) {
			if (isOnServer()) {
				boolean updateMass = updateMassServer();
				if (updateMass) {
					flagUpdateMass = false;
				}
			} else {
				getPhysicsDataContainer().updateMass(getMass(), true);
				railController.calculateInertia();
				if(getPhysicsDataContainer().getObject() instanceof RigidBody && railController.isRail()){
//					System.err.println("CC RAILED::: "+railController.railedInertia);
					((RigidBody) getPhysicsDataContainer().getObject()).setMassProps(getMass(), railController.railedInertia);
				}
				flagUpdateMass = false;
			}
//			System.err.println(getState()+"; "+this+" SET DAMP; "+getLinearDamping()+"; "+getRotationalDamping());
			((RigidBodySegmentController)getPhysicsDataContainer().getObject()).setDamping(getLinearDamping(), getRotationalDamping());

		}
		//		long tookMass = System.currentTimeMillis() - t0;
		//		if(tookMass > 5){
		//			System.err.println("[SHIP] "+getState()+" "+this+" mass udpate took "+tookMass);
		//		}

		getSlotAssignment().update();

		if (isOnServer()) {

			if (modifiedBleuprintModifiable && modifiedBleuprintTriggered && !modifiedBleuprintTriggeredExecuted) {

				System.err.println("[SERVER] WARNING: Possible modified Blueprint: " + spawnedFrom.getName() + " spawned by " + getSpawner() + ": " + getLastModifier());

				Object[] eText = Lng.astr("MODIFIED BLUEPRINT WARNING\n%s spawned blueprint\n%s\nBB-Price: %s\nPrice of actual spawned: %s\n",
						blueprintSpawnedBy,  spawnedFrom.getName(),  spawnedFrom.getElementMap().getCurrentPrice(), getElementClassCountMap().getCurrentPrice());

				System.err.println(eText);
				((GameServerState) getState()).getController().broadcastMessage(eText, ServerMessage.MESSAGE_TYPE_ERROR);
				((GameServerState) getState()).announceModifiedBlueprintUsage(spawnedFrom, blueprintSpawnedBy);

				this.destroy();
				modifiedBleuprintTriggeredExecuted = true;
			}
			if (((SegmentBufferManager) getSegmentBuffer()).isFullyLoaded()) {
				if (isEmptyOnServer()) {
					SegmentPiece pointUnsave = getSegmentBuffer().getPointUnsave(new Vector3i(Ship.core));//autorequest true previously
					if (pointUnsave != null && pointUnsave.getType() != ElementKeyMap.CORE_ID) {
						System.err.println("[SERVER][SHIP] WARNING ::::::::::::::::::: Empty SHIP (no core): deleting " + this);
						this.markForPermanentDelete(true);
						this.setMarkedForDeleteVolatile(true);
					}
				}
				lastEmptyCheck = System.currentTimeMillis();
			}
		}


		if (flagNameChange || nameTag.isEmpty() || lastFactionId != getFactionId() || (getFactionId() != 0 && lastFaction != ((FactionState) getState()).getFactionManager().getFaction(getFactionId()))) {
			Faction f = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
			refreshNameTag();
			lastFactionId = getFactionId();
			lastFaction = f;
			flagNameChange = false;
		}


		Starter.modManager.onSegmentControllerUpdate(this);

		getState().getDebugTimer().end(this, "ShipUpdate");
	}

	@Override
	public void onBlockSinglePlacedOnServer() {
		modifiedBleuprintModifiable = false;
		this.spawnedFrom = null;
		this.blueprintSpawnedBy = null;
		modifiedBleuprintTriggered = false;
	}

	@Override
	public void cleanUpOnEntityDelete() {
		super.cleanUpOnEntityDelete();

	}
	@Override
	public void destroyPersistent() {
		super.destroyPersistent();

		// Update map for deleted ship
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos;
		if(sector != null){
			sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		}else{
			sysPos = transientSectorPos;
			System.err.println("[SERVER][SHIP] WARNING: Entity wasn't in a loaded sector: "+this+" in sector ID: "+getSectorId());
		}
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
	}

	@Override
	public String toNiceString() {
		Fleet fleet;
		if (getNetworkObject() != null && !getNetworkObject().getDebugState().get().isEmpty()) {
			String cloak = "(stealth:"+getStealthStrength()+")";
			return nameTag + cloak+(isScrap() ? " (decayed)" : "") + ((fleet = getFleet()) != null ? " FLT["+fleet.getName()+"]" : "") + getNetworkObject().getDebugState().get() + "\n[CLIENTAI " +
					(aiConfiguration.isActiveAI() ? "ACTIVE" : "INACTIVE") + "] " + aiConfiguration.getAiEntityState();
		} else {
			if (!isOnServer()) {
				return nameTag + (isScrap() ? " " + Lng.str("(decayed)") : "") + ((fleet = getFleet()) != null ? " " + Lng.str("FLT[%s]", fleet.getName()) : "") + (isVirtualBlueprint() ? " " + Lng.str("(design)") : "");
			} else {
				return nameTag + (isScrap() ? " " + Lng.str("(decayed)") : "") + (isVirtualBlueprint() ? " " + Lng.str("(design)") : "");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		if (!isOnServer()) {



			aiConfiguration.updateFromNetworkObject(from);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);

		aiConfiguration.updateFromNetworkObject(o);

		for(int i = 0; i < getNetworkObject().lastPickupAreaUsed.getReceiveBuffer().size(); i++){
			lastPickupAreaUsed = getNetworkObject().lastPickupAreaUsed.getReceiveBuffer().getLong(i);
		}
		if (!isOnServer()) {

			if (!((NetworkShip) o).onHitNotices.getReceiveBuffer().isEmpty()) {
				shipManagerContainer.onHitNotice();
			}


		}
	}


	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		if (isOnServer()) {
			aiConfiguration.updateToFullNetworkObject(getNetworkObject());

			getNetworkObject().lastPickupAreaUsed.add(lastPickupAreaUsed);

		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToNetworkObject()
	 */
	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		if (isOnServer()) {
			getNetworkObject().stealthActive.set(shipManagerContainer.getStealth().getElementManager().isActive());

			aiConfiguration.updateToNetworkObject(getNetworkObject());

			if(lastPickupAreaUsed != lastPickupAreaUsedBefore){
				getNetworkObject().lastPickupAreaUsed.add(lastPickupAreaUsed);
				lastPickupAreaUsedBefore = lastPickupAreaUsed;
			}
		}
	}

	@Override
	public AIGameConfiguration<ShipAIEntity, Ship> getAiConfiguration() {
		return aiConfiguration;
	}

	@Override
	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i where, Vector3i parameter) {


		super.onAttachPlayer(playerState, detachedFrom, where, parameter);
		shipManagerContainer.thrustConfiguration.onAttachPlayer(playerState);

		if (!isOnServer()) {
			GameClientState s = (GameClientState) getState();
			if (s.getPlayer() == playerState) {
				PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().
						getPlayerGameControlManager();
				SegmentPiece entered = playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().getEntered();
				if(entered == null || entered.getSegmentController() != this) {
					SegmentPiece p = getSegmentBuffer().getPointUnsave(Ship.core);
					playerGameControlManager.getPlayerIntercationManager().setEntered(p);
				}

				playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().setActive(true);
			}
		}

		Starter.modManager.onSegmentControllerPlayerAttached(this);

	}

	@Override
	public void onDetachPlayer(PlayerState playerState, boolean hide, Vector3i parameter) {
		if (!isOnServer()) {

			GameClientState s = (GameClientState) getState();
			if (s.getPlayer() == playerState && ((GameClientState) getState()).getPlayer() == playerState) {
				PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().
						getPlayerGameControlManager();
				playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().setActive(false);

			}
		}else{

		}
		shipManagerContainer.thrustConfiguration.onDetachPlayer(playerState, getAttachedPlayers());
		refreshNameTag();
		Starter.modManager.onSegmentControllerPlayerDetached(this);
	}



	@Override
	public String getInsideSound() {
		return "0022_ambience loop - interior cockpit (loop)";
	}

	@Override
	public float getInsideSoundPitch() {
		return 1;
	}

	@Override
	public float getInsideSoundVolume() {
		return 0.7f;
	}

	@Override
	public String getOutsideSound() {
		return "0022_spaceship user - small engine thruster loop";
	}

	@Override
	public float getOutsideSoundPitch() {
		return 1;
	}

	@Override
	public float getOutsideSoundVolume() {
		return 0.3f;
	}

	@Override
	public float getSoundRadius(){
		return getBoundingBox().maxSize();
	}

	public void stopCoreOverheating() {
		if(isOnServer()) {
			SegmentPiece pointUnsave = getSegmentBuffer().getPointUnsave(core);
			if(pointUnsave != null && pointUnsave.getType() == ElementKeyMap.CORE_ID) {
//				final int maxHP = pointUnsave.getInfo().getMaxHitPointsByte();
				pointUnsave.setHitpointsByte(1);
				try {
					pointUnsave.getSegment().getSegmentData().applySegmentData(pointUnsave, getState().getUpdateTime());

				} catch (SegmentDataWriteException e) {
					SegmentData replaceData = SegmentDataWriteException.replaceData(pointUnsave.getSegment());

					try {
						replaceData.applySegmentData(pointUnsave, getState().getUpdateTime());
					} catch (SegmentDataWriteException e1) {
						e1.printStackTrace();
					}

				}
				sendBlockHpByte(pointUnsave, (short)1);

			}
		}
		super.stopCoreOverheating();
	}
	@Override
	public boolean isOwnPlayerInside() {
		return isClientOwnObject();
	}

	@Override
	public ShipManagerContainer getManagerContainer() {
		return shipManagerContainer;
	}

	@Override
	public SegmentController getSegmentController() {
		return this;
	}


	/**
	 * Get the root ship of the chain of entities
	 * @return A ship object
	 */
	public Ship getRootShip() {
		if(railController.isDockedOrDirty() && railController.getRoot() != this) {
			if (railController.getRoot() instanceof Ship) {
				return ((Ship)railController.getRoot());
			}
		}
		return this;
	}

	/**
	 * @return Max speed of the entity whether or not the entity is docked
	 */
	public float getMaxServerSpeed() {
		return shipManagerContainer.getThrusterElementManager().getMaxVelocity(veloTmp);
	}

	/**
	 * @return the maxVelocity
	 */
	public float getCurrentMaxVelocity() {
		if(railController.isDockedOrDirty() && railController.getRoot() != this) {
			if (railController.getRoot() instanceof Ship) {
				return ((Ship)railController.getRoot()).getCurrentMaxVelocity();
			} else {
				return 0;
			}
		}
		if(getPhysicsDataContainer().getObject() != null && getPhysicsDataContainer().getObject() instanceof RigidBody){
			return shipManagerContainer.getThrusterElementManager().getMaxVelocity(((RigidBody)getPhysicsDataContainer().getObject()).getLinearVelocity(veloTmp));
		} else {
			return 0;
		}
	}
	/**
	 * @return the maxVelocity
	 */
	public float getMaxSpeedAbsolute() {
		if(railController.isDockedOrDirty() && railController.getRoot() != this){
			if(railController.getRoot() instanceof Ship){
				return ((Ship)railController.getRoot()).getMaxSpeedAbsolute();
			}else{
				return 0;
			}
		}
		return shipManagerContainer.getThrusterElementManager().getMaxSpeedAbsolute();
	}

	/**
	 * @return the orientationForce
	 */
	public Vector3f getOrientationForce() {
		return orientationForce;
	}



	/**
	 * @return the velocity
	 */
	public Vector3f getVelocity() {
		return shipManagerContainer.getThrusterElementManager().getVelocity();
	}

	@Override
	public boolean isHandleHpCondition(HpTriggerType type) {
		return true;
	}

	@Override
	public void onProximity(SegmentController segmentController) {
		super.onProximity(segmentController);
		aiConfiguration.onProximity(segmentController);

	}

	@Override
	public void onRemovedElementSynched(short removedType, int oldSize, byte x, byte y, byte z, byte oldOrientation, Segment segment, boolean preserveControl, long time) {

		shipManagerContainer.onRemovedElementSynched(removedType, oldSize, x, y, z, segment, preserveControl);
		super.onRemovedElementSynched(removedType, oldSize, x, y, z, oldOrientation, segment, preserveControl, time);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#readExtraTagData(org.schema.schine.resource.tag.Tag)
	 */
	@Override
	protected void readExtraTagData(Tag t) {
		if (t.getType() == Type.STRUCT) {

			Tag[] ts = (Tag[]) t.getValue();

			if(ts[0].getType() == Type.LONG){
				/*
				 * for compatibility. segemntControllers
				 * now have the times saved in SegmentController.toTagStructure
				 */
				long timeLeft = (Long) ts[0].getValue();

				if (timeLeft >= 0) {
					coreTimerStarted = System.currentTimeMillis();
					coreTimerDuration = timeLeft;
				}
			}


			if(ts.length > 1 && ts[1].getType() == Type.BYTE){
				//old version
				shipManagerContainer.thrustConfiguration.readFromOldTag(ts);

			}

		}
	}
	@Override
	protected Tag getExtraTagData() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, (byte) 0), //placeholder for compatibility
				FinishTag.INST,
		});
	}
	/**
	 * @param realName the realName to set
	 */
	@Override
	public void setRealName(String realName) {
		if (!realName.equals(this.getRealName())) {
			super.setRealName(realName);
			flagNameChange = true;
		}
	}

	@Override
	public boolean updateMassServer() {
		if (isOnServer()) {
			if (getPhysicsDataContainer().isInitialized() && getPhysicsDataContainer().getShape() != null ) {

				float mass = calculateMass(); //includes inventoryMass

				setMass(mass);


				boolean updateMass = getPhysicsDataContainer().updateMass(mass, false);


				railController.calculateInertia();
				if(getPhysicsDataContainer().getObject() instanceof RigidBody && railController.isRail() && getPhysicsDataContainer().getObject() != null){
					((RigidBody) getPhysicsDataContainer().getObject()).setMassProps(mass, railController.railedInertia);
				}

				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onSegmentAddedSynchronized(Segment s) {

		if (!modifiedBleuprintTriggered && spawnedFrom != null && isOnServer() && getLastModifier().length() == 0) {
			double f = ServerConfig.MODIFIED_BLUEPRINT_TOLERANCE.getFloat();
			long blueprintPrice = spawnedFrom.getElementMap().getCurrentPrice();
			if ((blueprintPrice + (blueprintPrice * f)) < getElementClassCountMap().getCurrentPrice()) {
				System.err.println("[SHIP] " + this + " MODIFIED BB WARNING: " + spawnedFrom.getElementMap().getCurrentPrice() + " < " + getElementClassCountMap().getCurrentPrice());
				if (!modifiedBleuprintTriggered) {
					this.modifiedBleuprintTriggered = true;
				}
			} else {
				if ((blueprintPrice) > getElementClassCountMap().getCurrentPrice() - (blueprintPrice * f)) {
					shipManagerContainer.getShieldAddOn().flagCompleteLoad();
				}
			}
		}
	}

	@Override
	public boolean isCloakedFor(SimpleTransformableSendableObject<?> viewer) {

		if(railController.getRoot().isUsingPowerReactors()){
			if(railController.isRoot()){
				if(viewer != null){
					return !viewer.canSeeStructure(this, true);
				}else{
					return this.hasStealth(StealthLvl.CLOAKING);
				}
			}else{
				return railController.getRoot().isCloakedFor(viewer);
			}
		}

		if (getNetworkObject().stealthActive.get()) {
			return this.hasStealth(StealthLvl.JAMMING);
		} else if (railController.isDockedAndExecuted()) {
			//recusively check the chain
			return railController.previous.rail.getSegmentController().isCloakedFor(viewer);
		} else if (getDockingController().isDocked() && getDockingController().getDockedOn().to.getSegment().getSegmentController() instanceof Ship) {
			//recusively check the chain
			return ((Ship) getDockingController().getDockedOn().to.getSegment().getSegmentController()).isCloakedFor(viewer);
		}
		return false;
	}

	@Override
	public boolean isJammingFor(SimpleTransformableSendableObject<?> viewer) {

		if(railController.getRoot().isUsingPowerReactors()){
			if(railController.isRoot()){
				if(viewer != null){
					return !viewer.canSeeIndicator(this, true);
				}else{
					return this.hasStealth(StealthLvl.JAMMING);
				}
			}else{
				return railController.getRoot().isJammingFor(viewer);
			}
		}

		if (getNetworkObject().stealthActive.get()) {
			return this.hasStealth(StealthLvl.CLOAKING);
		} else if (railController.isDockedAndExecuted()) {
			//recusively check the chain
			return railController.previous.rail.getSegmentController().isJammingFor(viewer);
		} else if (getDockingController().isDocked() && getDockingController().getDockedOn().to.getSegment().getSegmentController() instanceof Ship) {
			//recusively check the chain
			return ((Ship) getDockingController().getDockedOn().to.getSegment().getSegmentController()).isJammingFor(viewer);
		}
		return false;


	}



	/**
	 * @return the flagNameChange
	 */
	public boolean isFlagNameChange() {
		return flagNameChange;
	}

	/**
	 * @param flagNameChange the flagNameChange to set
	 */
	public void setFlagNameChange(boolean flagNameChange) {
		this.flagNameChange = flagNameChange;
	}

	@Override
	public boolean isSalvagableFor(Salvager harvester, String[] cannotHitReason, Vector3i position) {

		if (isVirtualBlueprint()) {
			cannotHitReason[0] = Lng.str("Cannot mine design!");
			return false;
		}
		if(isInTestSector()){
			cannotHitReason[0] = Lng.str("Cannot mine in test sector!");
			return false;
		}
		if (!isMinable()) {
			cannotHitReason[0] = Lng.str("Cannot take block\nStructure is not minable");
			return false;
		}
		if (!(harvester instanceof AbstractCharacter<?>) && !isCoreOverheating()) {
			cannotHitReason[0] = Lng.str("You can only salvage overheating ships.");
			return false;
		}
		if (Ship.core.equals(position)) {
			cannotHitReason[0] = Lng.str("Can't salvage core! Please Pick up manually.");
			return false;
		}
		if(hasActiveReactors() && shipManagerContainer.getPowerInterface().isAnyRebooting()){
			cannotHitReason[0] = Lng.str("Cannot salvage while reactor is booting.");
			return false;
		}
		AbstractOwnerState p = harvester.getOwnerState();
		if(harvester.getFactionId() == getFactionId() &&
				((p != null && p instanceof PlayerState && !allowedToEdit((PlayerState) p)) || harvester.getOwnerFactionRights() < getFactionRights())){
			cannotHitReason[0] = Lng.str("Cannot salvage: insufficient faction rank");
			return false;
		}
		//		if(	controller.getFactionId() != 0  &&
		//				controller.getFactionId() != getState().getPlayer().getFactionId() &&
		//				getState().getFactionManager().getFaction(controller.getFactionId()) != null
		//				) {
		//
		//		}
		if(railController.isDockedAndExecuted() && railController.getRoot() instanceof ShopSpaceStation){
			return false;
		}
		if (isHomeBase() ||
				(getDockingController().isDocked() && getDockingController().getDockedOn().to.getSegment().getSegmentController().isHomeBaseFor(getFactionId()))) {
			if (harvester.getFactionId() == getFactionId()) {
				//own faction allows salvaging even on homebase
			} else {
				cannotHitReason[0] = Lng.str("Cannot salvage: home base protected");
				return false;
			}
		}
		if (!(harvester instanceof AbstractCharacter<?>) && aiConfiguration.isActiveAI()) {
			cannotHitReason[0] = Lng.str("Can only salvage defeated AI ships!");
			return false;
		}
		if (getAttachedPlayers().isEmpty()) {
			return true;
		} else {
			cannotHitReason[0] = Lng.str("Can only salvage empty ships!");
			return false;
		}
	}


	public Ship copy(String name, int sector, Transform tr, String spawner, int factionId, List<Ship> spawned, EntityCopyData d) {
		assert(isOnServer());
		float[] mat = new float[16];
		tr.getOpenGLMatrix(mat);
		final long time = System.currentTimeMillis();
		String UID = EntityType.SHIP.dbPrefix+name+(d.spawnCount > 0 ? d.spawnCount : "");

		d.spawnCount++;

		for(Ship sh : spawned){
			assert(!UID.equals(sh.getUniqueIdentifier())):UID;
		}
		System.err.println("[SHIP][COPY] COPYING SHIP ::: "+UID+"; from "+getUniqueIdentifier()+"; "+railController.isDockedAndExecuted());
		if(name.length() > 70){
			System.err.println("[SERVER][ERROR] Exception: tried to add ship with a name that is too long: "+name);
			return null;
		}

		final Ship newShip = EntityRequest.getNewShip(
				(ServerStateInterface) getState(),
				UID,
				sector,
				name,
				mat,
				-2, -2, -2,
				2, 2, 2,
				spawner, false);
		newShip.setTouched(true, false);
		newShip.getControlElementMap().setFromMap(this.getControlElementMap().getControllingMap());
		//since the design unloads, we can use the exiting segments

		newShip.getTextBlocks().addAll(this.getTextBlocks());
		newShip.getTextMap().putAll(this.getTextMap());

		newShip.aiConfiguration.setFrom(this.aiConfiguration);

		Tag tModules = shipManagerContainer.getModuleTag();

		Tag tInTag = shipManagerContainer.getInventoryTag();


		newShip.copiedFromUID = this.getUniqueIdentifier();

		SegmentBufferManager segBuf = (SegmentBufferManager) this.getSegmentBuffer();
//		segBuf.setSegmentController(newShip);
//		for (SegmentBufferInterface sb : segBuf.getBuffer().values()) {
//			sb.setSegmentController(newShip);
//			((SegmentBuffer)sb).setLastChanged(System.currentTimeMillis());
//		}

		segBuf.iterateOverNonEmptyElement((s, lastChanged) -> {
			SegmentData segmentData = s.getSegmentData();


			if(segmentData != null){

				RemoteSegment segCopy = new RemoteSegment(newShip);

				segCopy.absPos.set(s.absPos);
				segCopy.pos.set(s.pos);

				SegmentData dataCopy = new SegmentData4Byte(!isOnServer());
				segmentData.copyTo(dataCopy);
				dataCopy.assignData(segCopy);

				dataCopy.revalidateData(time, isStatic());

				newShip.getSegmentBuffer().addImmediate(segCopy);


				segCopy.setLastChanged(time);
				segCopy.dataChanged(true);
			}
			return true;
		}, false);

		assert(newShip.getSegmentBuffer().getPointUnsave(core) != null);
		assert(newShip.getSegmentBuffer().getPointUnsave(core).getType() == ElementKeyMap.CORE_ID):newShip.getSegmentBuffer().getPointUnsave(core).getType();
//		newShip.setSegmentBuffer(segBuf);

		newShip.getSegmentBuffer().setLastChanged(new Vector3i(), time);

		newShip.setFactionId(factionId);

		//only load filters...
		newShip.shipManagerContainer.loadInventoriesFromTag = false;

		newShip.shipManagerContainer.fromTagModule(tModules, 0);

		newShip.shipManagerContainer.fromTagInventory(tInTag);


		Tag aiTag = aiConfiguration.toTagStructure();

		newShip.aiConfiguration.fromTagStructure(aiTag);

		spawned.add(newShip);

		String rlStr;
		if(railController.isRoot() || (railController.isDockedAndExecuted() && railController.previous.rail.getType() == ElementKeyMap.SHIPYARD_CORE_POSITION)){
			rlStr = "-rl";
		}else{
			rlStr = "";
		}
		int i = 0;
		for(RailRelation r : railController.next){

			Ship copyOfDock = ((Ship)r.docked.getSegmentController()).copy(
					name+rlStr+i, sector, tr, spawner, factionId, spawned, d);

			Tag tag = r.docked.getSegmentController().railController.getTag();

			if(copyOfDock != null){

				boolean loadExpectedToDock = false; //dont need expected here as expected are already in the sector
				//read tag like we would when loading ship from disk
				copyOfDock.railController.fromTag(tag, 0, loadExpectedToDock);

				copyOfDock.railController.railRequestCurrent.ignoreCollision = true;

				//replace the segment controllers to the copied ones
				VoidUniqueSegmentPiece rl = new VoidUniqueSegmentPiece(copyOfDock.railController.railRequestCurrent.rail);
				rl.setSegmentController(newShip);
				rl.uniqueIdentifierSegmentController = newShip.getUniqueIdentifier();
				copyOfDock.railController.railRequestCurrent.rail = rl;

				VoidUniqueSegmentPiece dk = new VoidUniqueSegmentPiece(copyOfDock.railController.railRequestCurrent.docked);
				dk.setSegmentController(copyOfDock);
				dk.uniqueIdentifierSegmentController = copyOfDock.getUniqueIdentifier();
				copyOfDock.railController.railRequestCurrent.docked = dk;

			}
			i++;

		}


		newShip.handleCopyUIDTranslation();

		return newShip;
	}
	private void handleCopyUIDTranslation() {
		shipManagerContainer.getTransporter().getElementManager().handleCopyUIDTranslation();
		for(RailRelation r : railController.next){
			((Ship)r.docked.getSegmentController()).handleCopyUIDTranslation();
		}
	}
	@Override
	public void refreshNameTag() {
		if (getRealName().equals("undef")) {
			return;
		}
		StringBuffer b = new StringBuffer();
		b.append(getRealName());

		if (!getAttachedPlayers().isEmpty()) {
			b.append(" <");
			for (int i = 0; i < getAttachedPlayers().size(); i++) {
				try {
					b.append(getAttachedPlayers().get(i).getName());
					if (i < getAttachedPlayers().size() - 1) {
						b.append(", ");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			b.append(">");
		} else {
		}

		if (getFactionId() != 0) {
			b.append("[");
			Faction f = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
			if (f != null) {
				b.append(f.getName());
			} else {
				b.append("factionUnknown");
				b.append(getFactionId());
			}
			b.append("]");
		}
		nameTag = b.toString();
	}

	public void setProspectedElementCount(BlueprintInterface en, String spawner) {
		this.spawnedFrom = en;
		this.blueprintSpawnedBy = spawner;
	}

	public float calculateMass() {
		return Math.max(0.01f, railController.calculateRailMassIncludingSelf());
	}

	@Override
	public boolean canAttack(Damager from) {
		Faction faction = ((FactionState)getState()).getFactionManager().getFaction(getFactionId());
		if(faction != null && faction.isNPC() && !((NPCFaction)faction).canAttackShips()){
			from.sendClientMessage(Lng.str("Target doesn't seem to take any damage!"), ServerMessage.MESSAGE_TYPE_WARNING);
			return false;
		}
		return super.canAttack(from);
	}

	/**
	 * @return the spawnedFrom
	 */
	public BlueprintInterface getSpawnedFrom() {
		return spawnedFrom;
	}

	/**
	 * @param spawnedFrom the spawnedFrom to set
	 */
	public void setSpawnedFrom(BlueprintInterface spawnedFrom) {
		this.spawnedFrom = spawnedFrom;
	}




	@Override
	public boolean isMoved() {
		return true;
	}


	@Override
	public void setMoved(boolean b) {
	}

	public boolean isAutomaticReactivateDampeners() {
		return shipManagerContainer.thrustConfiguration.isAutomaticReacivateDampeners();
	}
	public boolean isAutomaticDampeners() {
		return shipManagerContainer.thrustConfiguration.isAutomaticDampeners();
	}

	public void requestThrustSharing(boolean ts) {
		getNetworkObject().thrustSharingReq.add(new RemoteBoolean(ts, getNetworkObject()));
	}
	public void requestAutomaticDampeners(boolean automaticDampeners) {
		getNetworkObject().automaticDampenersReq.add(new RemoteBoolean(automaticDampeners, getNetworkObject()));
	}
	public void requestAutomaticDampenersReactivate(boolean automaticDampeners) {
		getNetworkObject().automaticDampenersReactivateReq.add(new RemoteBoolean(automaticDampeners, getNetworkObject()));
	}

	// Only dampen velocity/rotation if not providing thrust yourself so acceleration is never hampered
	@Override
	public float getLinearDamping() {

		float dampen = getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_DAMPENING, 1f);

		if((dampen > 1f || shipManagerContainer.thrustConfiguration.isAutomaticDampeners())
			&& !shipManagerContainer.getThrusterElementManager().isUsingThrust()){
			return Math.max(dampen-1f, super.getLinearDamping());
		}else{
			return 0.0f;
		}
	}
	@Override
	public float getRotationalDamping() {
		float dampen = getSegmentController().getConfigManager().apply(StatusEffectType.THRUSTER_DAMPENING, 1f);

		if((dampen > 1f || shipManagerContainer.thrustConfiguration.isAutomaticDampeners())
			&& !shipManagerContainer.getThrusterElementManager().isUsingThrust()){
			return Math.max(dampen-1f, super.getRotationalDamping());
		}else{
			return 0.0f;
		}
	}



	@Override
	public void onSectorSwitchServer(Sector newSector) {
		Fleet fleet = getFleet();
		if(fleet != null){
			fleet.onSectorChangedLoaded(this, newSector);
		}
	}

	public String getCopiedFromUID() {
		return copiedFromUID;
	}

	public void setCopiedFromUID(String copiedFromUID) {
		this.copiedFromUID = copiedFromUID;
	}
	@Override
	public boolean isNPCFactionControlledAI() {
//		return getAttachedPlayers().size() > 0;
		return FactionManager.isNPCFaction(getFactionId()) && aiConfiguration.isActiveAI();
	}

	@Override
	public boolean isStatic() {
		return false;
	}
	public boolean isExtraAcidDamageOnDecoBlocks() {
		return true;
	}
}
