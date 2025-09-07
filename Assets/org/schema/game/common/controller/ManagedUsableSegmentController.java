package org.schema.game.common.controller;

import api.element.block.Blocks;
import api.listener.events.block.SegmentPieceSalvageEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.BlockBuffer;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.damage.projectile.ProjecileDamager;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.elements.PulseHandler;
import org.schema.game.common.controller.elements.beam.harvest.SalvageElementManager;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.faction.FactionInterface;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.Universe;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.physics.Physical;

import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.*;

public abstract class ManagedUsableSegmentController<E extends ManagedUsableSegmentController<E>> extends EditableSendableSegmentController implements PlayerControllable, ManagedSegmentController<E>, InventoryHolder, FactionInterface, PulseHandler, SegmentControllerAIInterface, Physical, Salvager, ShopperInterface, TransientSegmentController, ProjecileDamager {
	private final List<PlayerState> attachedPlayers = new ObjectArrayList<PlayerState>();
	private float salvageDamage;
	private long lastSalvage;
	private boolean transientTouched;
	private Set<ShopInterface> shopsInDistance = new HashSet<>();

	private BlockBuffer blockKillRecorder;

	public ManagedUsableSegmentController(StateInterface state) {
		super(state);
		this.blockKillRecorder = new BlockBuffer();
	}

	@Override
	public final List<PlayerState> getAttachedPlayers() {
		return attachedPlayers;
	}

	public int getSelectedAIControllerIndex() {
		long sStart = ((GameStateInterface) getState()).getGameState().getNetworkObject().serverStartTime.get();
		long sNow = getState().getUpdateTime() - getState().getServerTimeDifference();

		long timeSinceStart = (sNow - sStart);
		//		System.err.println("TIME SICE START: "+timeSinceStart);
		long diff = timeSinceStart / ((GameStateInterface) getState()).getGameState().getAIWeaponSwitchDelayMS();

		diff += (long) getId();

		int index = (int) (diff % 1000000L);

		return index;
	}

	@Override
	public final boolean isClientOwnObject() {
		return !isOnServer() && attachedPlayers.contains(((GameClientState) getState()).getPlayer());
	}

	@Override
	public final void activateAI(boolean active, boolean send) {
		if(getElementClassCountMap().get(ElementKeyMap.AI_ELEMENT) > 0) {
			((AIConfiguationElements<Boolean>) getAiConfiguration().get(Types.ACTIVE)).setCurrentState(active, send);
			getAiConfiguration().applyServerSettings();
		}
	}

	public boolean isAIControlled() {
		return getAiConfiguration().isActiveAI();
	}

	public void onDockingChanged(boolean docked) {
		getManagerContainer().onDockingChanged(docked);
	}

	@Override
	public float getDamageTakenMultiplier(DamageDealerType damageType) {
		float effectVal = getConfigManager().apply(StatusEffectType.DAMAGE_TAKEN, damageType, 1f) - 1f;
		float reactorVal = getManagerContainer().getPowerInterface().getExtraDamageTakenFromStabilization();
		return 1f + effectVal + reactorVal;
	}

	@Override
	public void sendHitConfirm(byte damageType) {
		if(getState().getUpdateTime() - lastSendHitConfirm > 300) {
			for(int i = 0; i < attachedPlayers.size(); i++) {
				attachedPlayers.get(i).sendHitConfirm(damageType);
			}
			lastSendHitConfirm = getState().getUpdateTime();
		}
	}

	@Override
	public void onBlockKill(SegmentPiece p, Damager from) {
		super.onBlockKill(p, from);
		if(isOnServer()) {
			SegmentPiece sp = new SegmentPiece();
			sp.setByValue(p);
			assert (sp.getType() != Element.TYPE_NONE) : sp + "; " + p;
			blockKillRecorder.recordRemove(sp);
			//			System.err.println("RECORDED KILL: "+blockKillRecorder.size()+"; "+p);
		}
	}

	@Override
	public void onBlockAddedHandled() {
		blockKillRecorder.clear();
	}

	@Override
	public void onPlayerDetachedFromThis(PlayerState pState, PlayerControllable newAttached) {
		if(railController.getRoot() == this) {
			getManagerContainer().onPlayerDetachedFromThisOrADock(this, pState, newAttached);
		} else {
			if(railController.getRoot() instanceof ManagedSegmentController<?>) {
				((ManagedSegmentController<?>) railController.getRoot()).getManagerContainer().onPlayerDetachedFromThisOrADock(this, pState, newAttached);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onPhysicsAdd()
	 */
	@Override
	public void onPhysicsAdd() {
		super.onPhysicsAdd();
		getManagerContainer().getPowerInterface().onPhysicsAdd();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onPhysicsRemove()
	 */
	@Override
	public void onPhysicsRemove() {
		super.onPhysicsRemove();
		getManagerContainer().getPowerInterface().onPhysicsRemove();
	}

	@Override
	public void updateLocal(Timer timer) {
		getState().getDebugTimer().start(this, "ManagedUsableSegControllerUpdate");
		super.updateLocal(timer);
		getManagerContainer().updateLocal(timer);

		long t0 = System.currentTimeMillis();
		long tookMan = System.currentTimeMillis() - t0;
		if(tookMan > 40) {
			System.err.println("[SEGMENTCONTROLLER] " + getState() + " " + this + " manager update took " + tookMan + " ms");
		}
		t0 = System.currentTimeMillis();
		getAiConfiguration().update(timer);
		long tookAI = System.currentTimeMillis() - t0;
		if(tookAI > 21) {
			System.err.println("[SHIP] " + getState() + " " + this + " AI udpate took " + tookAI);

		}
		getState().getDebugTimer().end(this, "ManagedUsableSegControllerUpdate");
	}

	@Override
	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i where, Vector3i parameter) {
		refreshNameTag();

		if(detachedFrom instanceof AbstractCharacter<?>) {
			if(((AbstractCharacter<?>) detachedFrom).getGravity().source != null && ((AbstractCharacter<?>) detachedFrom).getGravity().source != this) {
				System.err.println("[SHIP] removing gravity due to entering a ship != current gravity entity " + detachedFrom + " -> " + this + "; current: " + ((AbstractCharacter<?>) detachedFrom).getGravity().source);
				((AbstractCharacter<?>) detachedFrom).removeGravity();
			}
		}
		if(isOnServer()) {
			/*
			 * when entering a virtual object on a sector border
			 * e.g. a turret that is in a sector
			 * while the mother is still is still an another
			 */
			if(playerState.getCurrentSectorId() != this.getSectorId()) {
				System.err.println("[SERVER][ONATTACHPLAYER] entering! " + this + " in a different sector");

				Vector3i secPos;
				if(isOnServer()) {
					secPos = ((GameServerState) getState()).getUniverse().getSector(this.getSectorId()).pos;
				} else {
					secPos = ((RemoteSector) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(this.getSectorId())).clientPos();
				}

				playerState.setCurrentSector(new Vector3i(secPos));
				playerState.setCurrentSectorId(this.getSectorId());

				PlayerCharacter assingedPlayerCharacter = playerState.getAssingedPlayerCharacter();

				if(assingedPlayerCharacter != null) {
					System.err.println("[SERVER][SEGMENTCONTROLLER][ONATTACHPLAYER] entering! Moving along playercharacter " + this + " in a different sector");
					assingedPlayerCharacter.setSectorId(this.getSectorId());
				} else {
					System.err.println("[SERVER][SEGMENTCONTROLLER] WARNING NO PLAYER CHARACTER ATTACHED TO " + playerState);
				}

			}
		}

		Starter.modManager.onSegmentControllerPlayerAttached(this);
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(unit.parameter instanceof Vector3i) {
			if(getPhysicsDataContainer().isInitialized()) {
				getManagerContainer().handleKeyEvent(unit, mapping, timer);
			}
		}
	}

	@Override
	public void handleKeyPress(Timer timer, ControllerStateInterface u) {
		if(!(u instanceof ControllerStateUnit)) {
			return;
		}
		ControllerStateUnit unit = (ControllerStateUnit) u;
		if(((GameStateInterface) getState()).getGameState().getFrozenSectors().contains(getSectorId())) {
			CollisionObject object = getPhysicsDataContainer().getObject();
			if(object != null && object instanceof RigidBodySegmentController) {
				((RigidBodySegmentController) object).setLinearVelocity(new Vector3f());
				((RigidBodySegmentController) object).setAngularVelocity(new Vector3f());
			}

			if(isClientOwnObject()) {
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Sector frozen!\nCannot use entities!"), 0);
			}
			return;
		}

		if(unit.parameter instanceof Vector3i) {

			if(getPhysicsDataContainer().isInitialized()) {

				assert (unit.getPlayerState() != null);
				getManagerContainer().handleKeyPress(unit, timer);
			}
		}
	}

	@Override
	public final Set<ShopInterface> getShopsInDistance() {
		return shopsInDistance;

	}

	@Override
	public final boolean hasSpectatorPlayers() {
		for(PlayerState s : attachedPlayers) {
			if(s.isSpectator()) {
				return true;
			}
		}
		return false;
	}

	public void refreshNameTag() {
	}

	@Override
	public void setTouched(boolean b, boolean checkEmpty) {
		if(b != this.transientTouched) {
			setChangedForDb(true);
		}
		if(b && isLoadByBlueprint()) {
			blueprintIdentifier = null;
			blueprintSegmentDataPath = null;
		}
		transientTouched = b;
	}

	@Override
	public boolean isEmptyOnServer() {

		if(!transientTouched) {
			/*
			 * untouched asteroids are alwas not-empty. checking this will prevent
			 * the server from loading in every single asteroid, which puts
			 * immense load on the server (Simplex)
			 */
			return false;
		}

		return super.isEmptyOnServer();
	}

	@Override
	public boolean isTouched() {
		return transientTouched;
	}

	private final SegmentPiece segmentPiece = new SegmentPiece();

	@Override
	public int handleSalvage(BeamState beam, int beamHits, BeamHandlerContainer<?> container, Vector3f to, SegmentPiece hitPiece, Timer timer, Collection<Segment> updatedSegments) {
		segmentPiece.setByReference(hitPiece.getSegment(), hitPiece.x, hitPiece.y, hitPiece.z);

		//		if(isOnServer()){
		//			System.err.println("SHIP HITTING: "+segmentPiece);
		//		}
		//

		float salvageDamage = (beamHits * beam.getPower());

		if(System.currentTimeMillis() - lastSalvage > 10000) {
			this.salvageDamage = 0;
		}
		this.salvageDamage += salvageDamage;
		//		if(isOnServer()){
		//			System.err.println("SALVAGE DAMAGE. power: "+beam.getPower()+"; hits: "+beamHits+": dmg/tot "+salvageDamage+" / "+this.salvageDamage);
		//		}
		lastSalvage = System.currentTimeMillis();

		if(isOnServer() && beamHits > 0 && this.salvageDamage >= SalvageElementManager.SALVAGE_DAMAGE_NEEDED_PER_BLOCK) {
			if(this instanceof TransientSegmentController) {
				((TransientSegmentController) this).setTouched(true, true);
			}
			this.salvageDamage -= SalvageElementManager.SALVAGE_DAMAGE_NEEDED_PER_BLOCK;

			short type = segmentPiece.getType();
			if(ElementKeyMap.isValidType(type) && ElementKeyMap.isValidType(ElementKeyMap.getInfoFast(type).getSourceReference())) {
				type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
			}
			byte orientation = (segmentPiece.getOrientation());
			boolean removeElement = segmentPiece.getSegment().removeElement(segmentPiece.getPos(tmpLocalPos), false);

			if(removeElement) {
				//INSERTED CODE @368
				//Note that this currently only calls for the server.
				SegmentPieceSalvageEvent event = new SegmentPieceSalvageEvent(beam, (int) salvageDamage, to, segmentPiece, updatedSegments, this, orientation);
				StarLoader.fireEvent(SegmentPieceSalvageEvent.class, event, this.isOnServer());
				if(event.isCanceled()) {
					return beamHits;
				}
				///

				onSalvaged(container);
				if(ElementKeyMap.isValidType(type)) {
					segmentPiece.getSegmentController().getHpController().onManualRemoveBlock(ElementKeyMap.getInfo(type));

					if(ElementKeyMap.isValidType(ElementKeyMap.getInfoFast(type).getSourceReference())) {
						type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
					}
				}

				updatedSegments.add(hitPiece.getSegment());
				((RemoteSegment) segmentPiece.getSegment()).setLastChanged(System.currentTimeMillis());
				segmentPiece.refresh();
				assert (segmentPiece.getType() == Element.TYPE_NONE);
				if(segmentPiece.getSegment().getSegmentController().isScrap()) {
					if(Universe.getRandom().nextFloat() > 0.5f) {
						type = ElementKeyMap.SCRAP_ALLOYS;
					} else {
						type = ElementKeyMap.SCRAP_COMPOSITE;
					}
				}
				segmentPiece.setHitpointsByte(1); //only scatter on destruction

				//				segmentPiece.getSegment().getSegmentController().sendBlockMod(new RemoteSegmentPiece(segmentPiece, getNetworkObject()));
				segmentPiece.getSegment().getSegmentController().sendBlockSalvage(segmentPiece);
				if(!event.isCancelBlockGive()) {
					Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> short2ObjectOpenHashMap = getControlElementMap().getControllingMap().get(ElementCollection.getIndex(beam.controllerPos));
					LongOpenHashSet longOpenHashSet;
					if(short2ObjectOpenHashMap != null && ((longOpenHashSet = short2ObjectOpenHashMap.get(ElementKeyMap.STASH_ELEMENT)) != null || (longOpenHashSet = short2ObjectOpenHashMap.get((Blocks.LOCK_BOX))) != null) && longOpenHashSet.size() > 0) {
						LongIterator iterator = longOpenHashSet.iterator();
						while(iterator.hasNext()) {
							long chestPos = iterator.nextLong();
							Inventory inventory = getManagerContainer().getInventory(ElementCollection.getPosIndexFrom4(chestPos));
							if(inventory != null && inventory.canPutIn(type, 1)) {
								int slot = inventory.incExistingOrNextFreeSlot(type, 1);
								getManagerContainer().sendInventoryDelayed(inventory, slot);
								int miningBonus = getMiningBonus(segmentPiece.getSegment().getSegmentController());
								if(ElementKeyMap.hasResourceInjected(type, orientation) && inventory.canPutIn(ElementKeyMap.orientationToResIDMapping[orientation], 1 * miningBonus)) {

									int slotOre = inventory.incExistingOrNextFreeSlot(ElementKeyMap.orientationToResIDMapping[orientation], 1 * miningBonus);
									getManagerContainer().sendInventoryDelayed(inventory, slotOre);
								}
								break;
							}
						}

					} else if(attachedPlayers.size() > 0) {
						PlayerState playerState = attachedPlayers.get(0);

						playerState.modDelayPersonalInventory(type, 1);

						if(ElementKeyMap.hasResourceInjected(type, orientation)) {
							int miningBonus = getMiningBonus(segmentPiece.getSegment().getSegmentController());
							playerState.modDelayPersonalInventory(ElementKeyMap.orientationToResIDMapping[orientation], 1 * miningBonus);
						}

					}
				}

			}
		}
		segmentPiece.reset();
		return beamHits;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		getManagerContainer().initFromNetworkObject(from);
		getAiConfiguration().initFromNetworkObject(from);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		getManagerContainer().updateFromNetworkObject(o, senderId);

		getAiConfiguration().updateFromNetworkObject(o);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {

		super.updateToFullNetworkObject();
		getManagerContainer().updateToFullNetworkObject(getNetworkObject());

		getAiConfiguration().updateToFullNetworkObject(getNetworkObject());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateToNetworkObject()
	 */
	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		getManagerContainer().updateToNetworkObject(getNetworkObject());
		getAiConfiguration().updateToNetworkObject(getNetworkObject());
	}

	@Override
	public final boolean needsTagSave() {
		return true;
	}

	@Override
	public final InventoryMap getInventories() {
		return getManagerContainer().getInventories();
	}

	@Override
	public final Inventory getInventory(long pos) {
		return getManagerContainer().getInventory(pos);
	}

	@Override
	public final void volumeChanged(double volumeBefore, double volumeNow) {
		getManagerContainer().volumeChanged(volumeBefore, volumeNow);
	}

	@Override
	public final void sendInventoryErrorMessage(Object[] astr, Inventory inv) {
		getManagerContainer().sendInventoryErrorMessage(astr, inv);
	}

	@Override
	public final double getCapacityFor(Inventory inventory) {
		CargoCollectionManager cargoCollectionManager = getManagerContainer().getCargo().getCollectionManagersMap().get(inventory.getParameterIndex());
		return cargoCollectionManager != null ? cargoCollectionManager.getCapacity() : 0;
	}

	@Override
	public final NetworkInventoryInterface getInventoryNetworkObject() {
		return getManagerContainer().getInventoryNetworkObject();
	}

	@Override
	public final String printInventories() {
		return getManagerContainer().printInventories();
	}

	@Override
	public final void sendInventoryModification(IntCollection changedSlotsOthers, long parameter) {
		getManagerContainer().sendInventoryModification(changedSlotsOthers, parameter);
	}

	@Override
	public final void sendInventoryModification(int slot, long parameter) {
		getManagerContainer().sendInventoryModification(slot, parameter);
	}

	@Override
	public final void sendInventorySlotRemove(int slot, long parameter) {
		getManagerContainer().sendInventorySlotRemove(slot, parameter);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public final AbstractOwnerState getOwnerState() {
		AbstractOwnerState o = null;
		for(int i = 0; i < attachedPlayers.size(); i++) {
			AbstractOwnerState p = attachedPlayers.get(i);
			if(o == null || p.isControllingCore()) {
				o = p;
			}
		}
		return o;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return getManagerContainer().getAttackEffectSet(weaponId, damageDealerType);
	}

	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return getManagerContainer().getMetaWeaponEffect(weaponId, damageDealerType);
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

	private DamageBeamHitHandler damageBeamHitHandler = new DamageBeamHitHandlerSegmentController();

	public DamageBeamHitHandler getDamageBeamHitHandler() {
		return damageBeamHitHandler;
	}

	@Override
	protected void addReceivedBeamLatch(long beamId, int objId, long blockPos) {
		getManagerContainer().addReceivedBeamLatch(beamId, objId, blockPos);
	}

	public BlockBuffer getBlockKillRecorder() {
		return blockKillRecorder;
	}

	public void setBlockKillRecorder(BlockBuffer c) {
		this.blockKillRecorder = c;
	}

	@Override
	public float getAmmoCapacity(WeaponType type) {
		return getManagerContainer().getAmmoCapacity(type);
	}

	@Override
	public float getAmmoCapacityMax(WeaponType type) {
		return getManagerContainer().getAmmoCapacityMax(type);
	}

	@Override
	public AcidFormulaType getAcidType(long weaponId) {
		return getManagerContainer().getAcidType(weaponId);
	}

}
