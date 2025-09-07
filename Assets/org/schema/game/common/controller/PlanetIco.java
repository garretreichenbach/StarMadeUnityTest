package org.schema.game.common.controller;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.data.gamemap.GameMap;
import org.schema.game.client.data.gamemap.requests.GameMapRequest;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.ai.AIPlanetIcoConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.elements.PlanetIcoManagerContainer;
import org.schema.game.common.controller.elements.PulseHandler;
import org.schema.game.common.controller.elements.beam.harvest.SalvageElementManager;
import org.schema.game.common.controller.generator.PlanetIcoCreatorThread;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.network.objects.NetworkPlanetIco;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.AIConfigurationInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlanetIco extends EditableSendableSegmentController implements PulseHandler, InventoryHolder, Salvager, SegmentControllerAIInterface, ManagedSegmentController<PlanetIco>, PlayerControllable, TransientSegmentController, CelestialBodyGravityHandler {

	private final PlanetIcoManagerContainer planetManagerContainer;
	private final AIPlanetIcoConfiguration aiConfiguration;
	private final ArrayList<PlayerState> attachedPlayer = new ArrayList<>();
	private final Transform initialTransform = new Transform();
	private final DamageBeamHitHandler damageBeamHitHandler = new DamageBeamHitHandlerSegmentController();
	public float radius;
	private SectorInformation.PlanetType planetType;
	public int planetGenSeed;
	private boolean transientTouched;
	private boolean transientMoved;
	private float salvageDamage;
	private long lastSalvage;
	private byte sideId = -1;
	private TerrainGenerator terrainGenerator;
	private String planetCoreUID;
	private PlanetIcoCore planetCore;

	public PlanetIco(StateInterface state) {
		super(state);
		planetManagerContainer = new PlanetIcoManagerContainer(state, this);
		aiConfiguration = new AIPlanetIcoConfiguration(state, this);
	}

	public String getPlanetCoreUID() {
		return planetCoreUID;
	}

	public void setPlanetCoreUID(String planetCoreUID) {
		this.planetCoreUID = planetCoreUID;
	}

	@Override
	public SendableType getSendableType() {
		return SendableTypes.PLANET_ICO;
	}

	public void setTerrainGenerator(TerrainGenerator terrainGenerator) {
		this.terrainGenerator = terrainGenerator;
	}

	@Override
	public void activateAI(boolean active, boolean send) {
	}

	@Override
	public AIConfigurationInterface getAiConfiguration() {
		return aiConfiguration;
	}

	@Override
	public void sendHitConfirm(byte damageType) {
		if(getState().getUpdateTime() - lastSendHitConfirm > 300) {
			for(int i = 0; i < getAttachedPlayers().size(); i++) {
				getAttachedPlayers().get(i).sendHitConfirm(damageType);
			}
			lastSendHitConfirm = getState().getUpdateTime();
		}
	}

	@Override
	public AbstractOwnerState getOwnerState() {
		return !attachedPlayer.isEmpty() ? attachedPlayer.getFirst() : null;
	}
//	Probably not needed... I hope
//	@Override
//	public boolean isHomeBase() {
//		return super.isHomeBase() || planetCore.isHomeBase();
//	}

	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public List<PlayerState> getAttachedPlayers() {
		return attachedPlayer;
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit u, KeyboardMappings mapping, Timer timer) {
		if(u == null) return;
		ControllerStateUnit unit = u;
		if(unit.parameter instanceof Vector3i) {
			if(getPhysicsDataContainer().isInitialized()) planetManagerContainer.handleKeyEvent(unit, mapping, timer);
		}
	}

	@Override
	public void handleKeyPress(Timer timer, ControllerStateInterface u) {
		if(!(u instanceof ControllerStateUnit unit)) return;
		if(unit.parameter instanceof Vector3i) {
			if(getPhysicsDataContainer().isInitialized()) planetManagerContainer.handleKeyPress(unit, timer);
		}
	}

	@Override
	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i where, Vector3i parameter) {
		if(!isOnServer() && ((GameClientState) getState()).getPlayer() == playerState) {
			GameClientState s = (GameClientState) getState();
			if(s.getPlayer() == playerState) {
				PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
				playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().setActive(true);
			}
		}
		Starter.modManager.onSegmentControllerPlayerAttached(this);
	}

	@Override
	public void onDetachPlayer(PlayerState playerState, boolean hide, Vector3i parameter) {
		if(!isOnServer()) {
			GameClientState s = (GameClientState) getState();
			if(s.getPlayer() == playerState && ((GameClientState) getState()).getPlayer() == playerState) {
				PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
				playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().setActive(false);
			}
		}
		Starter.modManager.onSegmentControllerPlayerDetached(this);
	}

	@Override
	public boolean hasSpectatorPlayers() {
		for(PlayerState s : getAttachedPlayers()) {
			if(s.isSpectator()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onPlayerDetachedFromThis(PlayerState pState, PlayerControllable newAttached) {
	}

	@Override
	public boolean isSalvagableFor(Salvager harvester, String[] cannotHitReason, Vector3i position) {
		if(harvester.getFactionId() == getFactionId()) {
			//own faction allows salvaging even on homebase
			return true;
		}
		if(isHomeBase()) {
			cannotHitReason[0] = Lng.str("Cannot salvage: home base protected");
			return false;
		}
		return true;
	}

	@Override
	public void startCreatorThread() {
		if(getCreatorThread() == null) setCreatorThread(new PlanetIcoCreatorThread(this, terrainGenerator));
	}

	@Override
	public boolean isEmptyOnServer() {
		return false;
	}

	@Override
	public NetworkPlanetIco getNetworkObject() {
		return (NetworkPlanetIco) super.getNetworkObject();
	}

	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		if(isOnServer() && getTotalElements() <= 0 && System.currentTimeMillis() - getTimeCreated() > 50000 && isEmptyOnServer()) {
			System.err.println("[SERVER][Planet] Empty planet section: deleting " + this);
			setMarkedForDeleteVolatile(true);
		}
		planetManagerContainer.updateLocal(timer);
		if(!isOnServer() && planetCore == null && planetCoreUID != null && !"none".equals(planetCoreUID)) {
			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(planetCoreUID);
			if(sendable != null) planetCore = (PlanetIcoCore) sendable;
		}
		Starter.modManager.onSegmentControllerUpdate(this);
		if(getPhysicsDataContainer().getObject() != null) getPhysicsDataContainer().getObject().setCollisionFlags(CollisionFlags.KINEMATIC_OBJECT);
	}

	@Override
	public void newNetworkObject() {
		setNetworkObject(new NetworkPlanetIco(getState(), this));
	}

	@Override
	protected String getSegmentControllerTypeString() {
		return "Planet";
	}

	@Override
	public void cleanUpOnEntityDelete() {
		super.cleanUpOnEntityDelete();
		planetManagerContainer.getShoppingAddOn().cleanUp();
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
	public String toString() {
		return "PlanetIco(" + getId() + ")[s" + getSectorId() + "]";
	}

	@Override
	public boolean isTouched() {
		return transientTouched;
	}

	@Override
	public void setTouched(boolean b, boolean checkEmpty) {
		transientTouched = b;
	}

	@Override
	public boolean isMoved() {
		return transientMoved;
	}

	@Override
	public void setMoved(boolean b) {
		transientMoved = b;
	}

	@Override
	public boolean needsTagSave() {
		return true;
	}

	@Override
	public boolean hasStructureAndArmorHP() {
		return false;
	}

	@Override
	public void initPhysics() {
		super.initPhysics();
	}

	@Override
	public void onAddedElementSynched(short type, byte orientation, byte x, byte y, byte z, Segment segment, boolean updateSegementBuffer, long absIndex, long time, boolean revalidate) {
		planetManagerContainer.onAddedElementSynched(type, segment, absIndex, time, revalidate);
		super.onAddedElementSynched(type, orientation, x, y, z, segment, updateSegementBuffer, absIndex, time, revalidate);
	}

	@Override
	public void onRemovedElementSynched(short removedType, int oldSize, byte x, byte y, byte z, byte oldOrientation, Segment segment, boolean preserveControl, long time) {
		planetManagerContainer.onRemovedElementSynched(removedType, oldSize, x, y, z, segment, preserveControl);
		super.onRemovedElementSynched(removedType, oldSize, x, y, z, oldOrientation, segment, preserveControl, time);
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public boolean isClientOwnObject() {
		return !isOnServer() && getAttachedPlayers().contains(((GameClientState) getState()).getPlayer());
	}

	@Override
	public void fromTagStructure(Tag tag) {
		assert ("PlanetIco".equals(tag.getName()));
		Tag[] subTags = tag.getStruct();
		sideId = subTags[0].getByte();
		radius = subTags[1].getFloat();
		planetGenSeed = subTags[2].getInt();
		sideId = subTags[3].getByte();
		TerrainGenerator.TerrainGeneratorTypeI type = TerrainGenerator.TerrainGeneratorTypeI.getFromId(subTags[4].getInt());
		planetCoreUID = (String) subTags[5].getValue();
		planetType = SectorInformation.PlanetType.valueOf(subTags[6].getString());
		super.fromTagStructure(subTags[7]);
		terrainGenerator = type.inst(planetGenSeed, radius);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#setFactionId(int)
	 */
	@Override
	public void setFactionId(int factionId) {
		if(railController.isDocked()) railController.getRoot().setFactionId(factionId);
		else super.setFactionId(factionId);
		if(planetCore != null) planetCore.setFactionId(factionId);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		return new Tag(Tag.Type.STRUCT, "PlanetIco", new Tag[]{
				new Tag(Tag.Type.BYTE, null, (sideId)),
				new Tag(Tag.Type.FLOAT, null, radius),
				new Tag(Tag.Type.INT, null, planetGenSeed),
				new Tag(Tag.Type.BYTE, null, sideId),
				new Tag(Tag.Type.INT, null, terrainGenerator.getType().getId()),
				new Tag(Tag.Type.STRING, null, planetCoreUID),
				new Tag(Tag.Type.STRING, null, planetType.name()),
				super.toTagStructure(), FinishTag.INST});
	}

	@Override
	public boolean isGravitySource() {
		return true;
	}

	@Override
	public boolean isRankAllowedToChangeFaction(int targetFactionId, PlayerState player, byte rank) {
		/*
		 * only faction members with homebase permission may reset
		 * the faction signature of a planet that has a homebase plate
		 */
		if(targetFactionId == 0 && ((FactionState) getState()).getFactionManager().existsFaction(getFactionId()) && isHomeBase()) {
			if(((FactionState) getState()).getFactionManager().existsFaction(player.getFactionId())) {
				if(!((FactionState) getState()).getFactionManager().getFaction(player.getFactionId()).getRoles().hasHomebasePermission(rank)) {
					return false;
				}
			}
		}
		return super.isRankAllowedToChangeFaction(targetFactionId, player, rank);
	}

	@Override
	public boolean canBeRequestedOnClient(int x, int y, int z) {
		//when someone built on a planet the potential size might go beyond the shape limits of a icosahedron
		return super.canBeRequestedOnClient(x, y, z) && (transientTouched || IcosahedronHelper.isSegmentInSide(x, y, z)) && !isInPlanetCore(x, y, z); //isSegmentInSideApprox might be broken?
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		setRealName("Planet");
		initialTransform.set(getWorldTransform());
	}

	/**
	 * Checks if the given coordinates are inside the planet core. If they are, nothing should generate there.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 *
	 * @return true if the coordinates are inside the planet core
	 */
	public boolean isInPlanetCore(float x, float y, float z) {
		return FastMath.carmackLength(x, y, z) < getCore().getRadius() - 50.0f; //Have it generate slightly inside the core just to be safe
		//Todo: Ideally, this should actually use an Icosahedron to check if the point is inside the core, rather than a sphere... but this should work fine for now.
	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();
		if(getCore() != null) getCore().destroyPersistent();
		// Update map for deleted planet
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		planetManagerContainer.initFromNetworkObject(getNetworkObject());
		planetCoreUID = getNetworkObject().coreUID.get();
		radius = getNetworkObject().radius.get();
		planetCore = (PlanetIcoCore) getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(getNetworkObject().coreUID.get());
		sideId = getNetworkObject().sideId.get();
	}

	@Override
	public String toNiceString() {
		String r;
		if(getFactionId() != 0) {
			Faction f = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
			if(f != null) r = getRealName() + "[" + f.getName() + "]";
			else r = getRealName() + Lng.str("[UnknownFaction %d]", getFactionId());
		} else r = getRealName();
		r += Lng.str(" (Radius: %d)", (int) radius);
		return r;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		planetManagerContainer.updateFromNetworkObject(o, senderId);
		planetCoreUID = getNetworkObject().coreUID.get();
		radius = getNetworkObject().radius.get();
		planetCore = (PlanetIcoCore) getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(getNetworkObject().coreUID.get());
		sideId = getNetworkObject().sideId.get();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		planetManagerContainer.updateToFullNetworkObject(getNetworkObject());
		if(planetCoreUID != null) getNetworkObject().coreUID.set(planetCoreUID);
		if(radius > 0) getNetworkObject().radius.set(radius);
		if(sideId != -1) getNetworkObject().sideId.set(sideId);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToNetworkObject()
	 */
	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		planetManagerContainer.updateToNetworkObject(getNetworkObject());
		if(planetCoreUID != null) getNetworkObject().coreUID.set(planetCoreUID);
		if(radius > 0) getNetworkObject().radius.set(radius);
		if(sideId != -1) getNetworkObject().sideId.set(sideId);
	}

	@Override
	public void onRename(String oldName, String newName) {
		if(railController.isDocked()) {
			((PlanetIco) railController.getRoot()).onRename(oldName, newName);
		} else {
			Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
			Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
			Vector3i playerPos = new Vector3i();
			if(isOnServer()) {
				for(PlayerState state : ((GameServerState) getState()).getPlayerStatesByName().values()) {
					StellarSystem.getPosFromSector(new Vector3i(state.getCurrentSector()), playerPos);
					if(playerPos.equals(sysPos)) {
						((GameServerState) getState()).getGameMapProvider().addRequestServer(new GameMapRequest(GameMap.TYPE_SYSTEM, sysPos), state.getClientChannel());
					}
				}
			}
		}
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public PlanetIcoManagerContainer getManagerContainer() {
		return planetManagerContainer;
	}

	@Override
	public SegmentController getSegmentController() {
		return this;
	}

	@Override
	public int handleSalvage(BeamState beam, int beamHits, BeamHandlerContainer<?> container, Vector3f to, SegmentPiece hitPiece, Timer timer, Collection<Segment> updatedSegments) {
		float salvageDamage = (beamHits * beam.getPower());
		if(System.currentTimeMillis() - lastSalvage > 10000) {
			this.salvageDamage = 0;
		}
		this.salvageDamage += salvageDamage;
		lastSalvage = System.currentTimeMillis();
		if(isOnServer() && beamHits > 0 && this.salvageDamage >= SalvageElementManager.SALVAGE_DAMAGE_NEEDED_PER_BLOCK) {
			setTouched(true, true);
			short type = hitPiece.getType();
			byte orientation = (hitPiece.getOrientation());
			boolean removeElement = hitPiece.getSegment().removeElement(hitPiece.getPos(tmpLocalPos), false);
			if(removeElement) {
				onSalvaged(container);
				updatedSegments.add(hitPiece.getSegment());
				((RemoteSegment) hitPiece.getSegment()).setLastChanged(System.currentTimeMillis());
				hitPiece.refresh();
				assert (hitPiece.getType() == Element.TYPE_NONE);
				if(hitPiece.getSegment().getSegmentController().isScrap()) {
					if(Universe.getRandom().nextFloat() > 0.5f) {
						type = ElementKeyMap.SCRAP_ALLOYS;
					} else {
						type = ElementKeyMap.SCRAP_COMPOSITE;
					}
				}
				hitPiece.setHitpointsByte(1);
				//				segmentPiece.getSegment().getSegmentController().sendBlockMod(new RemoteSegmentPiece(segmentPiece, getNetworkObject()));
				hitPiece.getSegment().getSegmentController().sendBlockSalvage(hitPiece);
				Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> short2ObjectOpenHashMap = getControlElementMap().getControllingMap().get(ElementCollection.getIndex(beam.controllerPos));
				LongOpenHashSet longOpenHashSet;
				if(short2ObjectOpenHashMap != null && (longOpenHashSet = short2ObjectOpenHashMap.get(ElementKeyMap.STASH_ELEMENT)) != null && longOpenHashSet.size() > 0) {
					LongIterator iterator = longOpenHashSet.iterator();
					while(iterator.hasNext()) {
						long chestPos = iterator.nextLong();
						Inventory inventory = planetManagerContainer.getInventory(ElementCollection.getPosFromIndex(chestPos, new Vector3i()));
						if(inventory != null && inventory.canPutIn(type, 1)) {
							int slot = inventory.incExistingOrNextFreeSlot(type, 1);
							planetManagerContainer.sendInventoryDelayed(inventory, slot);
							int miningBonus = getMiningBonus(hitPiece.getSegment().getSegmentController());
							if(ElementKeyMap.hasResourceInjected(type, orientation) && inventory.canPutIn(ElementKeyMap.orientationToResIDMapping[orientation], miningBonus)) {
								int slotOre = inventory.incExistingOrNextFreeSlot(ElementKeyMap.orientationToResIDMapping[orientation], miningBonus);
								planetManagerContainer.sendInventoryDelayed(inventory, slotOre);
							}
							break;
						}
					}
				} else if(getAttachedPlayers().size() > 0) {
					PlayerState playerState = getAttachedPlayers().get(0);
					playerState.modDelayPersonalInventory(type, 1);
					if(ElementKeyMap.hasResourceInjected(type, orientation)) {
						int miningBonus = getMiningBonus(hitPiece.getSegment().getSegmentController());
						playerState.modDelayPersonalInventory(ElementKeyMap.orientationToResIDMapping[orientation], miningBonus);
					}
				}
			}
		}
		return beamHits;
	}

	@Override
	public InventoryMap getInventories() {
		return planetManagerContainer.getInventories();
	}

	@Override
	public Inventory getInventory(long pos) {
		return planetManagerContainer.getInventory(pos);
	}

	@Override
	public NetworkInventoryInterface getInventoryNetworkObject() {
		return getNetworkObject();
	}

	@Override
	public String printInventories() {
		return planetManagerContainer.printInventories();
	}

	@Override
	public void sendInventoryModification(IntCollection changedSlotsOthers, long parameter) {
		planetManagerContainer.sendInventoryModification(changedSlotsOthers, parameter);
	}

	@Override
	public void sendInventoryModification(int slot, long parameter) {
		planetManagerContainer.sendInventoryModification(slot, parameter);
	}

	@Override
	public void sendInventorySlotRemove(int slot, long parameter) {
		planetManagerContainer.sendInventorySlotRemove(slot, parameter);
	}

	@Override
	public double getCapacityFor(Inventory inventory) {
		return planetManagerContainer.getCapacityFor(inventory);
	}

	@Override
	public void volumeChanged(double volumeBefore, double volumeNow) {
		planetManagerContainer.volumeChanged(volumeBefore, volumeNow);
	}

	@Override
	public void sendInventoryErrorMessage(Object[] astr, Inventory inv) {
		planetManagerContainer.sendInventoryErrorMessage(astr, inv);
	}

	@Override
	public EntityType getType() {
		return EntityType.PLANET_ICO;
	}

	@Override
	protected boolean affectsGravityOf(SimpleTransformableSendableObject<?> target) {
		if(target instanceof AbstractCharacter<?> && ((AbstractCharacter<?>) target).getOwnerState().isSitting()) return false;
		if(FactionManager.isNPCFaction(target.getFactionId())) return false; //No gravity for NPC ships in order to prevent them from ramming into planets
		return checkGravityDownwards(target);
	}

	@Override
	public void getRelationColor(FactionRelation.RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyOther);
			case FRIEND -> out.set(ColorPalette.allyOther);
			case NEUTRAL -> out.set(ColorPalette.neutralOther);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionOther);
		}
		out.x += select;
		out.y += select;
		out.z += select;
	}

	public byte getSideId() {
		return sideId;
	}

	public void setSideId(byte sideId) {
		this.sideId = sideId;
	}

	public DamageBeamHitHandler getDamageBeamHitHandler() {
		return damageBeamHitHandler;
	}

	public PlanetIcoCore getCore() {
		if(planetCore == null && planetCoreUID != null && !"none".equals(planetCoreUID)) {
			planetCore = (PlanetIcoCore) getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(planetCoreUID);
		}
		return planetCore;
	}

	public void setPlanetCore(PlanetIcoCore planetCore) {
		this.planetCore = planetCore;
		if(planetCore != null) planetCoreUID = planetCore.getUniqueIdentifier();
	}

	public SectorInformation.PlanetType getPlanetType() {
		if(planetType == null) {
			System.err.println("[ERROR][PLANET] Planet type is null for " + this);
			return SectorInformation.PlanetType.BARREN;
		}
		return planetType;
	}

	public void setPlanetType(SectorInformation.PlanetType planetType) {
		this.planetType = planetType;
	}

	@Override
	public Vector3f getGravityVector(SimpleTransformableSendableObject<?> target) {
		Vector3f gravity = new Vector3f(0, -GravityState.G, 0);
		if(!isFullyLoaded() || target instanceof Ship || (target instanceof PlayerCharacter character && character.getOwnerState().isSitting())) gravity.set(0, 0, 0);
		Vector3f targetPos = new Vector3f(target.getPosition());
		Vector3f platePos = new Vector3f(getPosition());
		targetPos.sub(platePos);
		targetPos.absolute();
		if(targetPos.length() > 500) gravity.set(0, 0, 0);
		return gravity;
	}
}
