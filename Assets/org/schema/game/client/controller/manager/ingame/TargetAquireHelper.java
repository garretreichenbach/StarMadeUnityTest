package org.schema.game.client.controller.manager.ingame;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.MissileModuleInterface;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

public class TargetAquireHelper {
	private float targetTime = 0;
	private float targetHoldTime = 0;
	private SimpleTransformableSendableObject target;
	private boolean targetMode = false;
	private boolean flagSlotChange = true;
	private long currentSlotUsableIndex;
	
	
	
	public void checkSlot(SegmentPiece entered) {
		long oldSlotIndex = currentSlotUsableIndex;
		if (flagSlotChange && entered != null) {
			GameClientState state = (GameClientState) entered.getSegmentController().getState();
			SlotAssignment shipConfiguration = entered.getSegmentController().getSlotAssignment();

			if (shipConfiguration.hasConfigForSlot(state.getPlayer()
					.getCurrentShipControllerSlot())) {
				
				currentSlotUsableIndex = shipConfiguration.getAsIndex(state.getPlayer()
						.getCurrentShipControllerSlot());
				if (currentSlotUsableIndex != Long.MIN_VALUE) {
					SegmentPiece pointUnsave = entered.getSegmentController().getSegmentBuffer()
							.getPointUnsave(currentSlotUsableIndex);//autorequest true previously
					if(pointUnsave == null){
						return;
					}
					targetMode = false;

					if (entered.getSegment().getSegmentController() instanceof ManagedSegmentController<?>) {
						targetMode = ((ManagedSegmentController<?>) entered.getSegment().getSegmentController()).getManagerContainer().isTargetLocking(pointUnsave);
					}
					if(!targetMode) {
						if(state.getPlayer().getAquiredTarget() != null) {
							System.err.println("[CLIENT][SLOT] no longer in target mode");
							state.getPlayer().setAquiredTarget(null);
						}
					}
					
				}
			} else {
				targetMode = false;
			}
		}
		if(oldSlotIndex != currentSlotUsableIndex) {
			targetTime = 0;
			targetHoldTime = 0;
			flagSlotChange = false;
			if(entered != null) {
				GameClientState state = (GameClientState) entered.getSegmentController().getState();
				if(state.getPlayer().getAquiredTarget() != null) {
					System.err.println("[CLIENT] Aquired target reset (slot changed) from "+state.getPlayer().getAquiredTarget());
				}
				state.getPlayer().setAquiredTarget(null);
			}
		}
	}
	private void checkTargetLockOnMode(SegmentPiece entered, Timer timer) {
		GameClientState state = (GameClientState) entered.getSegmentController().getState();
		boolean usingFreelock = KeyboardMappings.FREE_CAM.isDownOrSticky(state);

		if (targetMode && !usingFreelock) {

			if (state.getPlayer().getAquiredTarget() != null && state.getPlayer().getAquiredTarget() instanceof Ship && 
					((Ship) state.getPlayer().getAquiredTarget()).isJammingFor(entered.getSegmentController())) {
				if(state.getPlayer().getAquiredTarget() != null) {
					System.err.println("[CLIENT] Aquired target reset (jamming) from "+state.getPlayer().getAquiredTarget());
				}
				state.getPlayer().setAquiredTarget(null);
				targetTime = 0;
				targetHoldTime = 0;
			}

			if (state.getPlayer().getAquiredTarget() != null) {
				targetHoldTime += timer.getDelta();
			}
			float dotDist = 0.9f;
			boolean useDot = false;
			float distFromMid = 90;
			boolean closest = true;
			if (targetHoldTime > 0 && targetHoldTime < 3f) {
				
				/*
				 * keep selection until target has been out of sight for more
				 * than 3 seconds
				 */
				if (PlayerInteractionControlManager.getLookingAt(state, true, true, distFromMid, useDot, dotDist, closest) == state.getPlayer()
						.getAquiredTarget()) {
					targetHoldTime = 0;
				}

			} else {
				/*
				 * make a new try to acquire a target a target has to be near
				 * the corsair for 5 seconds
				 */
				targetHoldTime = 0;

				SimpleTransformableSendableObject<?> lookingAt = PlayerInteractionControlManager.getLookingAt(state, true, true, distFromMid, useDot, dotDist, closest);
				if (lookingAt != null && lookingAt.isInAdminInvisibility()) {
					lookingAt = null;
				}
				if (!state.isInWarp() && (target != lookingAt || (lookingAt instanceof Ship && ((Ship) lookingAt).isJammingFor(entered.getSegmentController())))) {
					if(state.getPlayer().getAquiredTarget() != null) {
						System.err.println("[CLIENT] Aquired target reset from "+state.getPlayer().getAquiredTarget());
					}
					target = lookingAt;
					targetTime = 0;
					state.getPlayer().setAquiredTarget(null);
				} else {
					if (target != null) {
						targetTime += timer.getDelta();
						if (targetTime > getAcquireTime(entered.getSegmentController(), target)) {
							state.getPlayer().setAquiredTarget(target);
						}
					}
				}

			}
		} else {
			if(state.getPlayer().getAquiredTarget() != null) {
				System.err.println("[CLIENT] Aquired target reset (no longer in target mode) from "+state.getPlayer().getAquiredTarget());
			}
			state.getPlayer().setAquiredTarget(null);
			targetTime = 0;
			targetHoldTime = 0;
		}
	}


	public void update(SegmentPiece entered, Timer timer) {
		if(entered != null) {
			checkSlot(entered);
			checkTargetLockOnMode(entered, timer);
		}
		
			
			
	}
	/**
	 * @return the acquireTime
	 */
	public float getAcquireTime(SimpleTransformableSendableObject<?> shooter, SimpleTransformableSendableObject<?> target) {
		float acquireTime = DumbMissileElementManager.LOCK_ON_TIME_SEC;
		
		if(shooter instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)shooter).getManagerContainer() instanceof MissileModuleInterface) {
			ManagerModuleCollection<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> m = 
					((MissileModuleInterface)((ManagedSegmentController<?>)shooter).getManagerContainer()).getMissile();
			
			SegmentController s = m.getElementManager().getSegmentController();
			SlotAssignment shipConfiguration = s.getSlotAssignment();

			GameClientState state = (GameClientState)s.getState();
//			System.err.println("SJJSJSJ "+s+"; "+shipConfiguration.hasConfigForSlot(state.getPlayer()
//					.getCurrentShipControllerSlot())+"; "+state.getPlayer()
//					.getCurrentShipControllerSlot()+"; "+shipConfiguration.getUniqueIdentifier());
			if (shipConfiguration.hasConfigForSlot(state.getPlayer()
					.getCurrentShipControllerSlot())) {
				long index = shipConfiguration.getAsIndex(state.getPlayer()
						.getCurrentShipControllerSlot());
				
				
				DumbMissileCollectionManager cm = m.getCollectionManagersMap().get(index);
				if(cm != null) {
					acquireTime = cm.getLockOnTime();
				}else {
//					System.err.println("[CLIENT][ERROR] MISSILE MODULE NOT FOUND: "+index);
				}
				
			}
			
			
		}
		if(target instanceof ConfigManagerInterface){
			return ((ConfigManagerInterface)target).getConfigManager().apply(StatusEffectType.STEALTH_MISSILE_LOCK_ON_TIME, acquireTime);
		}
		
		return acquireTime;
	}
	/**
	 * @return the target
	 */
	public SimpleTransformableSendableObject getTarget() {
		return target;
	}

	/**
	 * @return the targetTime
	 */
	public float getTargetTime() {
		return targetTime;
	}
	/**
	 * @return the targetMode
	 */
	public boolean isTargetMode() {
		return targetMode;
	}

	/**
	 * @param targetMode the targetMode to set
	 */
	public void setTargetMode(boolean targetMode) {
		this.targetMode = targetMode;
	}
		

	public void flagSlotChange() {
		flagSlotChange = true;		
	}

}
