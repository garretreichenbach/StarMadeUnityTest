package org.schema.game.common.controller;

import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.HpTrigger.HpTriggerType;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.AISpaceStationConfiguration;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.SpaceStationManagerContainer;
import org.schema.game.common.controller.generator.SpaceStationCreatorThread;
import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.player.inventory.ShopInventory;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.network.objects.NetworkSpaceStation;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.server.ai.SpaceStationAIEntity;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Set;

public class SpaceStation extends ManagedUsableSegmentController<SpaceStation> implements ShopInterface {
	
	//	private SpaceStationAIEntity spaceStationAIEntity;
	private final SpaceStationManagerContainer spaceStationManagerContainer;
	private AISpaceStationConfiguration aiConfiguration;
	private SpaceStationType creatorType = SpaceStationType.EMPTY;
	private boolean transientMoved;
	
	



	public SpaceStation(StateInterface state) {
		super(state);
		spaceStationManagerContainer = new SpaceStationManagerContainer(state, this);
		aiConfiguration = new AISpaceStationConfiguration(state, this);
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.SPACE_STATION;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.SPACE_STATION;
	}

	@Override
	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyStation);
			case FRIEND -> out.set(ColorPalette.allyStation);
			case NEUTRAL -> out.set(ColorPalette.neutralStation);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionStation);
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
	}

	@Override
	public void onSectorInactiveClient() {
		super.onSectorInactiveClient();
		spaceStationManagerContainer.getShoppingAddOn().onSectorInactiveClient();

	}
	
	@Override
	protected void onFullyLoaded() {
		if(isOnServer()){
			if(ServerConfig.REMOVE_ENTITIES_WITH_INCONSISTENT_BLOCKS.isOn()){
				for(short s : ElementKeyMap.keySet){
					if(!allowedTypeNormal(s) && getElementClassCountMap().get(s) > 0){
						
						String msg = Lng.str("Ship %s\nused a block type that is not allowed on space stations:\n%s\nIt will be removed!",  this,  ElementKeyMap.toString(s));
						((GameServerState)getState()).getController().broadcastMessageAdmin(Lng.astr("Ship %s\nused a block type that is not allowed on space stations:\n%s\nIt will be removed!",  this,  ElementKeyMap.toString(s)), ServerMessage.MESSAGE_TYPE_ERROR);
						System.err.println("[SERVER] "+msg);
						LogUtil.log().fine(msg);
						
						markForPermanentDelete(true);
						setMarkedForDeleteVolatile(true);
						
					}
				}
				if(railController.isDockedAndExecuted()){
					String msg = Lng.str("Invalid station. A station can't be docked and will be removed.");
					((GameServerState)getState()).getController().broadcastMessageAdmin(Lng.astr("Invalid station. A station can't be docked and will be removed."), ServerMessage.MESSAGE_TYPE_ERROR);
					System.err.println("[SERVER] "+msg);
					LogUtil.log().fine(msg);
					
					markForPermanentDelete(true);
					setMarkedForDeleteVolatile(true);
				}
			}
			
		}
		super.onFullyLoaded();
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#allowedType(short)
	 */
	@Override
	public boolean allowedType(short type) {

		if (isScrap() && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).getType().hasParent("terrain")) {
			return false;
		}

		return super.allowedType(type);
	}
	public boolean allowedTypeNormal(short type) {
		return super.allowedType(type);
	}

	@Override
	protected short getCoreType() {
		return ElementKeyMap.DEATHSTAR_CORE_ID;
	}

	@Override
	public NetworkSpaceStation getNetworkObject() {
		return (NetworkSpaceStation) super.getNetworkObject();
	}

	@Override
	protected String getSegmentControllerTypeString() {
		return "Station";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#handleBeingSalvaged(org.schema.game.common.controller.elements.BeamState, org.schema.game.common.controller.BeamHandlerContainer, javax.vecmath.Vector3f, org.schema.game.common.data.physics.CubeRayCastResult, int)
	 */
	@Override
	public void handleBeingSalvaged(
			BeamState hittingBeam,
			BeamHandlerContainer<? extends SimpleTransformableSendableObject> container,
			Vector3f to, SegmentPiece hitPiece, int beamHitsForReal) {
		super.handleBeingSalvaged(hittingBeam, container, to, hitPiece,
				beamHitsForReal);

		if (isScrap() && container.getHandler().getBeamShooter().isClientOwnObject()) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("The blocks on this station are\nworn and decayed!\nYou can only salvage scrap!\n(press %s on the station to repair)",  KeyboardMappings.SPAWN_SPACE_STATION.getKeyChar()), 0);
		}
	}

	

	

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkSpaceStation(getState(), this));
	}

	@Override
	public void onAddedElementSynched(short type, byte orientation, byte x, byte y, byte z, Segment segment, boolean updateSegementBuffer, long absIndex, long time, boolean revalidate) {

		spaceStationManagerContainer.onAddedElementSynched(type, segment, absIndex, time, revalidate);
		super.onAddedElementSynched(type, orientation, x, y, z, segment, updateSegementBuffer, absIndex, time, revalidate);

	}

	@Override
	protected void onCoreDestroyed(Damager from) {

	}

	@Override
	public void onDamageServerRootObject(float actualDamage, Damager from) {
		super.onDamageServerRootObject(actualDamage, from);
		aiConfiguration.onDamageServer(actualDamage, from);

		spaceStationManagerContainer.getShoppingAddOn().onHit(from);
	}

	@Override
	public void startCreatorThread() {
		if (getCreatorThread() == null) {
			setCreatorThread(new SpaceStationCreatorThread(this, creatorType));

		}
	}

	@Override
	public String toString() {
		return "SpaceStation[" + getUniqueIdentifier() + "(" + getId() + ")]";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);


		if (isOnServer() && getTotalElements() <= 0 &&
				System.currentTimeMillis() - getTimeCreated() > 50000 &&
				isEmptyOnServer() && !isWrittenForUnload()) {
			if(getSegmentBuffer().isFullyLoaded()){
				System.err.println("[SERVER][SPACESTATION] " + this + " Empty station: deleting!!!!!!!!!!!!!!!!!!! " + this);
				this.markForPermanentDelete(true);
				this.setMarkedForDeleteVolatile(true);
			}
		}

		Starter.modManager.onSegmentControllerUpdate(this);
	}

	@Override
	public AIGameConfiguration<SpaceStationAIEntity, SpaceStation> getAiConfiguration() {
		return aiConfiguration;
	}



	

	@Override
	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i where, Vector3i parameter) {
		super.onAttachPlayer(playerState, detachedFrom, where, parameter);
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
				playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().setActive(true);
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
			}
		}
		Starter.modManager.onSegmentControllerPlayerDetached(this);
	}

	//	public void notifyElementChanged() {
	//		this.setChanged();
	//		this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);
	//	}



	@Override
	public SpaceStationManagerContainer getManagerContainer() {
		return spaceStationManagerContainer;
	}

	@Override
	public SegmentController getSegmentController() {
		return this;
	}

	@Override
	public boolean isHandleHpCondition(HpTriggerType type) {
		return type == HpTriggerType.OVERHEATING;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		assert (tag.getName().equals("SpaceStation"));
		Tag[] subTags = (Tag[]) tag.getValue();

		super.fromTagStructure(subTags[1]);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#setFactionId(int)
	 */
	@Override
	public void setFactionId(int factionId) {
		if (isOnServer() && factionId == 0 && (getFactionId() < 0)) {
			//turn stations of NPCs to scrap if faction block is removed
			setScrap(true);
			for (ElementDocking a : getDockingController().getDockedOnThis()) {
				a.from.getSegment().getSegmentController().setScrap(true);
			}

			railController.getRoot().railController.setAllScrap(true);

		}
		super.setFactionId(factionId);
	}

	@Override
	public int getCreatorId() {
		return creatorType.ordinal();
	}



	@Override
	public void onRemovedElementSynched(short removedType, int oldSize, byte x, byte y, byte z, byte oldOrientation, Segment segment, boolean preserveControl, long time) {
		spaceStationManagerContainer.onRemovedElementSynched(removedType, oldSize, x, y, z, segment, preserveControl);
		super.onRemovedElementSynched(removedType, oldSize, x, y, z, oldOrientation, segment, preserveControl, time);
	}

	@Override
	public void setCreatorId(int id) {
		creatorType = SpaceStationType.values()[id];
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {

		return new Tag(Type.STRUCT, "SpaceStation",
				new Tag[]{spaceStationManagerContainer.toTagStructure(), super.toTagStructure(), FinishTag.INST});

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#isVulnerable()
	 */
	@Override
	public boolean isVulnerable() {
		if (getFactionId() == FactionManager.TRAIDING_GUILD_ID) {
			return false;
		}
		return super.isVulnerable();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#isMinable()
	 */
	@Override
	public boolean isMinable() {
		if (getFactionId() == FactionManager.TRAIDING_GUILD_ID) {
			return false;
		}
		return super.isMinable();
	}


	@Override
	public boolean isSalvagableFor(Salvager harvester,
	                               String[] cannotHitReason, Vector3i position) {

		if (!isMinable()) {
			cannotHitReason[0] = Lng.str("Cannot take block\nStructure is not minable");
			return false;
		}

		if (!(harvester instanceof AbstractCharacter<?>) && ((FactionState) getState()).getFactionManager().existsFaction(getFactionId())) {
			cannotHitReason[0] = Lng.str("Faction owned station not salvagable\nDestroy faction block first");
			return false;
		}
		AbstractOwnerState p = harvester.getOwnerState();
		if(harvester.getFactionId() == getFactionId() && 
				((p != null && p instanceof PlayerState && !allowedToEdit((PlayerState) p)) || harvester.getOwnerFactionRights() < getFactionRights())){
			cannotHitReason[0] = Lng.str("Cannot salvage: insufficient faction rank");
			return false;
		}
		if(hasActiveReactors() && spaceStationManagerContainer.getPowerInterface().isAnyRebooting()){
			cannotHitReason[0] = Lng.str("Cannot salvage while reactor is booting.");
			return false;
		}
		if (isHomeBase() ||
				(getDockingController().getDockedOn() != null && getDockingController().getDockedOn().to.getSegment().getSegmentController().isHomeBaseFor(getFactionId()))) {
			if (!(harvester instanceof AbstractCharacter<?>) || (harvester.getFactionId() != getFactionId())) {
				
				
					
				
				if (harvester.getFactionId() == getFactionId()) {
					//own faction allows salvaging even on homebase
				} else {
					cannotHitReason[0] = Lng.str("Cannot salvage: home base protected");
					return false;
				}
			}

		}
		return true;
	}

	public void setProspectedElementCount(ElementCountMap elementMap) {
	}

	
	@Override
	public boolean canAttack(Damager from) {
		Faction faction = ((FactionState)getState()).getFactionManager().getFaction(getFactionId());
		if(faction != null && faction.isNPC() && !((NPCFaction)faction).canAttackStations()){
			from.sendClientMessage(Lng.str("Target doesn't seem to take any damage!"), ServerMessage.MESSAGE_TYPE_WARNING);
			return false;
		}
		return super.canAttack(from);
	}
	@Override
	public boolean isNPCHomeBase() {
		return spaceStationManagerContainer.isNPCHomeBase();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#cleanUpOnEntityDelete()
	 */
	@Override
	public void cleanUpOnEntityDelete() {

		super.cleanUpOnEntityDelete();

		spaceStationManagerContainer.getShoppingAddOn().cleanUp();

	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();

		// Update map for deleted space station
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
	}

	@Override
	public String toNiceString() {
		String r;
		if (getFactionId() != 0) {
			Faction f = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
			if (f != null) {
				r = getRealName() + "[" + f.getName() + "]";
			} else {
				r = getRealName() + Lng.str("[UnknownFaction %d]", getFactionId());
			}
		} else {
			r = getRealName();
		}
		if (isOnServer()) {
			return r;
		} else {
			return r + " " + (isScrap() ? Lng.str("(decayed)") : "");
		}
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
	}

	public enum SpaceStationType {
		RANDOM,
		EMPTY,
		PIRATE,
		FACTION,
	}

	@Override
	public void fillInventory(boolean send, boolean full)
			throws NoSlotFreeException {
		spaceStationManagerContainer.fillInventory(send, full);
	}

	@Override
	public long getCredits() {
		return spaceStationManagerContainer.getCredits();
	}

	@Override
	public long getPermissionToPurchase() {
		return spaceStationManagerContainer.getPermissionToPurchase();
	}

	@Override
	public long getPermissionToTrade() {
		return spaceStationManagerContainer.getPermissionToTrade();
	}


	@Override
	public ShopInventory getShopInventory() {
		return spaceStationManagerContainer.getShopInventory();
	}

	@Override
	public Set<String> getShopOwners() {
		return spaceStationManagerContainer.getShopOwners();
	}

	@Override
	public ShoppingAddOn getShoppingAddOn() {
		return spaceStationManagerContainer.getShoppingAddOn();
	}

	@Override
	public void modCredits(long i) {
		spaceStationManagerContainer.modCredits(i);
	}

	@Override
	public boolean isInfiniteSupply() {
		return spaceStationManagerContainer.isInfiniteSupply();
	}

	@Override
	public boolean isAiShop() {
		return spaceStationManagerContainer.isAiShop();
	}

	@Override
	public boolean isTradeNode() {
		return spaceStationManagerContainer.isTradeNode();
	}

	@Override
	public TradeNode getTradeNode() {
		return spaceStationManagerContainer.getTradeNode();
	}

	@Override
	public boolean isValidShop() {
		return spaceStationManagerContainer.isValidShop();
	}
	@Override
	public int getPriceString(ElementInformation info, boolean purchasePrice) {
		return getShoppingAddOn().getPriceString(info, purchasePrice);
	}

	@Override
	public TradePriceInterface getPrice(short type, boolean buy) {
		return getShoppingAddOn().getPrice(type, buy);
	}

	@Override
	public boolean wasValidTradeNode() {
		return spaceStationManagerContainer.wasValidTradeNode();
	}
	@Override
	public boolean isStatic() {
		return true;
	}
	public boolean isExtraAcidDamageOnDecoBlocks() {
		return true;
	}
}
