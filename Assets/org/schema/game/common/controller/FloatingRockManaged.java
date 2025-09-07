package org.schema.game.common.controller;

import api.element.block.Blocks;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.beam.harvest.SalvageElementManager;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.Universe;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.network.objects.NetworkSpaceStation;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FloatingRockManaged extends FloatingRock implements ManagedSegmentController<FloatingRockManaged>, InventoryHolder, Salvager, PulseHandler, PlayerControllable {

	private final ArrayList<PlayerState> attachedPlayers = new ArrayList<PlayerState>();
	private final FloatingRockManagerContainer asteroidManagerContainer;
	private long lastSalvage;
	private float salvageDamage;
	private AsteroidOuterMaterialProvider surfaceMaterials;

	public FloatingRockManaged(StateInterface state) {
		super(state);
		asteroidManagerContainer = new FloatingRockManagerContainer(state, this);
	}

	@Override
	public ManagerContainer<FloatingRockManaged> getManagerContainer() {
		return asteroidManagerContainer;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.ASTEROID_MANAGED;
	}
	@Override
	public SegmentController getSegmentController() {
		return this;
	}
	@Override
	public void sendHitConfirm(byte damageType) {
		if (this instanceof PlayerControllable) {
			if(getState().getUpdateTime() - lastSendHitConfirm > 300){
				for (int i = 0; i < ((PlayerControllable) this).getAttachedPlayers().size(); i++) {
					((PlayerControllable) this).getAttachedPlayers().get(i).sendHitConfirm(damageType);
				}
				lastSendHitConfirm = getState().getUpdateTime();
			}
		}
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit u, KeyboardMappings mapping, Timer timer) {
		if(!(u instanceof ControllerStateUnit)) {
			return;
		}
		ControllerStateUnit unit = (ControllerStateUnit)u;
		if (unit.parameter instanceof Vector3i) {
			if (getPhysicsDataContainer().isInitialized()) {
				getManagerContainer().handleKeyEvent(unit, mapping, timer);
			}
		}
	}
	@Override
	public void handleKeyPress(Timer timer, ControllerStateInterface u) {
		if(!(u instanceof ControllerStateUnit)) {
			return;
		}
		ControllerStateUnit unit = (ControllerStateUnit)u;
		if (unit.parameter instanceof Vector3i) {
			if (getPhysicsDataContainer().isInitialized()) {
				getManagerContainer().handleKeyPress(unit, timer);
			}
		}
	}
	@Override
	public void onAddedElementSynched(short type, byte orientation, byte x, byte y, byte z, Segment segment, boolean updateSegementBuffer, long absIndex, long time, boolean revalidate) {
		getManagerContainer().onAddedElementSynched(type, segment, absIndex, time, revalidate);
		super.onAddedElementSynched(type, orientation, x, y, z, segment, updateSegementBuffer, absIndex, time, revalidate);
	}

	@Override
	public String toString() {
		return "ManagedAsteroid(" + getId() + ")sec[" + getSectorId() + "]" + (isTouched() ? "(!)" : "");
	}

	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);

		if (isOnServer()) {
			getManagerContainer().updateLocal(timer);
		} else {
			getManagerContainer().updateLocal(timer);
		}
	}

	@Override
	public boolean needsTagSave() {
		return true;
	}

	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		getManagerContainer().initFromNetworkObject(getNetworkObject());
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		getManagerContainer().updateFromNetworkObject(o, senderId);
	}

	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		getManagerContainer().updateToFullNetworkObject(getNetworkObject());
	}

	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		getManagerContainer().updateToNetworkObject(getNetworkObject());
	}

	@Override
	public void onRemovedElementSynched(short removedType, int oldSize, byte x, byte y, byte z, byte oldOrientation, Segment segment, boolean preserveControl, long time) {
		getManagerContainer().onRemovedElementSynched(removedType, oldSize, x, y, z, segment, preserveControl);
		super.onRemovedElementSynched(removedType, oldSize, x, y, z, oldOrientation, segment, preserveControl, time);
	}

	@Override
	public void fromTagStructure(Tag tag) {
		assert (tag.getName().equals("FloatingRockManaged"));
		Tag[] subTags = (Tag[]) tag.getValue();
		super.fromTagStructure(subTags[1]);
		if(subTags[2].getType() == Tag.Type.BYTE_ARRAY && subTags[3].getType() == Tag.Type.BYTE_ARRAY)
			setOres(subTags[2].getByteArray(),subTags[3].getByteArray());
		else setOres(COMMON_ORES,FALLBACK_ORES_FREQ);
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Tag.Type.STRUCT, "FloatingRockManaged",
				new Tag[]{
					getManagerContainer().toTagStructure(),
					super.toTagStructure(),
					new Tag(Tag.Type.BYTE_ARRAY, "ores", getOres()),
					new Tag(Tag.Type.BYTE_ARRAY, "oreFreqs", getOreFrequencies()),
					new Tag(Tag.Type.FINISH, null, null)
				}
			);
	}

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkSpaceStation(getState(), this));
	}

	@Override
	public int handleSalvage(BeamState beam, int beamHits, BeamHandlerContainer<?> container, Vector3f to, SegmentPiece hitPiece, Timer timer, Collection<Segment> updatedSegments) {

		//		if(isOnServer()){
		//			System.err.println("SHIP HITTING: "+segmentPiece);
		//		}
		//
		float salvageDamage = (beamHits * beam.getPower());

		if (System.currentTimeMillis() - lastSalvage > 10000) {
			this.salvageDamage = 0;
		}
		this.salvageDamage += salvageDamage;

		lastSalvage = System.currentTimeMillis();

		if (isOnServer() && beamHits > 0 && this.salvageDamage >= SalvageElementManager.SALVAGE_DAMAGE_NEEDED_PER_BLOCK) {
			setTouched(true, true);
			short type = hitPiece.getType();
			
			if(ElementKeyMap.isValidType(type) && ElementKeyMap.isValidType(ElementKeyMap.getInfoFast(type).getSourceReference())){
				type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
			}
			
			byte orientation = (hitPiece.getOrientation());

			boolean removeElement = hitPiece.getSegment().removeElement(hitPiece.getPos(tmpLocalPos), false);
			if (removeElement) {
				
				onSalvaged(container);
				updatedSegments.add(hitPiece.getSegment());
				((RemoteSegment) hitPiece.getSegment()).setLastChanged(System.currentTimeMillis());
				hitPiece.refresh();
				assert (hitPiece.getType() == Element.TYPE_NONE);
				if (hitPiece.getSegment().getSegmentController().isScrap()) {
					if (Universe.getRandom().nextFloat() > 0.5f) {
						type = ElementKeyMap.SCRAP_ALLOYS;
					} else {
						type = ElementKeyMap.SCRAP_COMPOSITE;
					}
				}
				hitPiece.setHitpointsByte(1);

				
				hitPiece.getSegment().getSegmentController().sendBlockSalvage(hitPiece);

				Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> short2ObjectOpenHashMap = getControlElementMap().getControllingMap().get(ElementCollection.getIndex(beam.controllerPos));
				LongOpenHashSet longOpenHashSet;
				if (short2ObjectOpenHashMap != null && ((longOpenHashSet = short2ObjectOpenHashMap.get(ElementKeyMap.STASH_ELEMENT)) != null || (longOpenHashSet = short2ObjectOpenHashMap.get(Blocks.LOCK_BOX.getId())) != null) && !longOpenHashSet.isEmpty()) {
					LongIterator iterator = longOpenHashSet.iterator();
					while (iterator.hasNext()) {
						long chestPos = iterator.nextLong();
						Inventory inventory = getManagerContainer().getInventory(ElementCollection.getPosFromIndex(chestPos, new Vector3i()));
						if (inventory != null && inventory.canPutIn(type, 1)) {
							int slot = inventory.incExistingOrNextFreeSlot(type, 1);
							getManagerContainer().sendInventoryDelayed(inventory, slot);
							int miningBonus = getMiningBonus(hitPiece.getSegment().getSegmentController());
							if (ElementKeyMap.hasResourceInjected(type, orientation) && inventory.canPutIn(ElementKeyMap.orientationToResIDMapping[orientation], miningBonus)) {
								int slotOre = inventory.incExistingOrNextFreeSlot(ElementKeyMap.orientationToResIDMapping[orientation], miningBonus);
								getManagerContainer().sendInventoryDelayed(inventory, slotOre);
							}
							break;
						}
					}

				} else if (getAttachedPlayers().size() > 0) {
					PlayerState playerState = getAttachedPlayers().get(0);
					playerState.modDelayPersonalInventory(type, 1);

					if (ElementKeyMap.hasResourceInjected(type, orientation)) {
						int miningBonus = getMiningBonus(hitPiece.getSegment().getSegmentController());
						playerState.modDelayPersonalInventory(ElementKeyMap.orientationToResIDMapping[orientation], miningBonus);
					}
				}

			}
		}
		return beamHits;
	}

	@Override
	public boolean isNewPowerSystemNoReactor() {
		return !isUsingOldPower() && !hasAnyReactors() && !railController.isDockedAndExecuted();
	}

	@Override
	public boolean isNewPowerSystemNoReactorOverheatingCondition() {
		if(isFullyLoaded()){
			ElementCountMap blocks = getElementClassCountMap();
			int unhittableBlocks = 0;
			unhittableBlocks += blocks.get(ElementKeyMap.CARGO_SPACE);
			unhittableBlocks += blocks.get(ElementKeyMap.PICKUP_AREA);
			unhittableBlocks += blocks.get(ElementKeyMap.PICKUP_RAIL);
			unhittableBlocks += blocks.get(ElementKeyMap.EXIT_SHOOT_RAIL);
			unhittableBlocks += blocks.get(ElementKeyMap.SIGNAL_TRIGGER_AREA); //beams don't detect them
			//TODO add anything else here
			if(unhittableBlocks > 0 && unhittableBlocks >= this.getTotalElements()) return true;
		}
		return false;
	}

	@Override
	public List<PlayerState> getAttachedPlayers() {
		return attachedPlayers;
	}

	@Override
	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i where, Vector3i parameter) {
		if (!isOnServer() && ((GameClientState) getState()).getPlayer() == playerState) {
			GameClientState s = (GameClientState) getState();
			if (s.getPlayer() == playerState) {
				PlayerGameControlManager playerGameControlManager = s.getGlobalGameControlManager().getIngameControlManager().
						getPlayerGameControlManager();
				playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().setActive(true);
				System.err.println("Entering asteroid");
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
				playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().setActive(false);
			}
		}
		Starter.modManager.onSegmentControllerPlayerDetached(this);
	}

	@Override
	public boolean hasSpectatorPlayers() {
		for (PlayerState s : attachedPlayers) {
			if (s.isSpectator()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isTouched() {
		return true;
	}

	@Override
	public boolean isMoved() {
		return true;
	}
	
	
	@Override
	public void onPlayerDetachedFromThis(PlayerState pState,
			PlayerControllable newAttached) {
		
	}


	@Override
	public InventoryMap getInventories() {
		return getManagerContainer().getInventories();
	}

	@Override
	public Inventory getInventory(long pos) {
		return getManagerContainer().getInventory(pos);
	}
	@Override
	public double getCapacityFor(Inventory inventory) {
		CargoCollectionManager cargoCollectionManager = getManagerContainer().getCargo().getCollectionManagersMap().get(inventory.getParameterIndex());
		return cargoCollectionManager != null ? cargoCollectionManager.getCapacity() : 0;
	}
	@Override
	public void volumeChanged(double volumeBefore, double volumeNow) {
		getManagerContainer().volumeChanged(volumeBefore, volumeNow);
	}
	@Override
	public void sendInventoryErrorMessage(Object[] astr, Inventory inv) {
		getManagerContainer().sendInventoryErrorMessage(astr, inv);
	}
	@Override
	public NetworkInventoryInterface getInventoryNetworkObject() {
		return getManagerContainer().getInventoryNetworkObject();
	}

	@Override
	public String printInventories() {
		return getManagerContainer().printInventories();
	}

	@Override
	public void sendInventoryModification(
			IntCollection changedSlotsOthers, long parameter) {
		getManagerContainer().sendInventoryModification(changedSlotsOthers, parameter);
	}

	@Override
	public void sendInventoryModification(int slot, long parameter) {
		getManagerContainer().sendInventoryModification(slot, parameter);
	}
	@Override
	public void sendInventorySlotRemove(int slot, long parameter) {
		getManagerContainer().sendInventorySlotRemove(slot, parameter);
	}

	public void setSurfaceMaterials(AsteroidOuterMaterialProvider surfaceMaterials) {
		this.surfaceMaterials = surfaceMaterials;
	}

	@Override
	public AsteroidOuterMaterialProvider getSurfaceMaterials() {
		return surfaceMaterials; //dynamic for managed asteroids.
		// shouldn't ever be needed tbh, as the blocks are already generated and saved.
		// but just in case it's relevant for scanning or something, we'll have to make a permanent/saved implementor of that interface
	}
}
