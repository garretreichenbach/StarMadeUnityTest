package org.schema.game.client.controller.manager.ingame.ship;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.TargetAquireHelper;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.CockpitManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.container.PhysicsDataContainer;

import javax.vecmath.Vector3f;

public class ShipExternalFlightController extends AbstractControlManager {
	private final TargetAquireHelper aquire = new TargetAquireHelper();
	ControllerStateUnit unit = new ControllerStateUnit();
	Vector3f force = new Vector3f();
	
	boolean cancelledMov = false;
	public Camera shipCamera;

	private boolean lastDocked;

	public ShipExternalFlightController(ShipControllerManager controlManager) {
		super(controlManager.getState());
	}

//	private void checkSlot() {
//		if (flagSlotChange) {
//			SlotAssignment shipConfiguration = getShip().getSlotAssignment();
//
//			Vector3i absPos = shipConfiguration.get(getState().getPlayer()
//					.getCurrentShipControllerSlot());
//			if (absPos != null) {
//				SegmentPiece pointUnsave = getShip().getSegmentBuffer()
//						.getPointUnsave(absPos);
//
//				if (pointUnsave != null && getEntered().getSegment().getSegmentController() instanceof ManagedSegmentController<?>) {
//					targetMode = ((ManagedSegmentController<?>) getEntered().getSegment().getSegmentController()).getManagerContainer().isTargetLocking(pointUnsave);
//				}
//			} else {
//				targetMode = false;
//			}
//			flagSlotChange = false;
//		}
//	}

	

	public SegmentPiece getEntered() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getEntered();
	}

	public Vector3i getEntered(Vector3i out) {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getEntered().getAbsolutePos(out);
	}

	/**
	 * @return the physicsData
	 */
	public PhysicsDataContainer getPhysicsData() {
		return getShip().getPhysicsDataContainer();
	}

	/**
	 * @return the ship
	 */
	public Ship getShip() {
		return getState().getShip();
	}

//	/**
//	 * @return the target
//	 */
//	public SimpleTransformableSendableObject getTarget() {
//		return target;
//	}
//
//	/**
//	 * @return the targetTime
//	 */
//	public float getTargetTime() {
//		return targetTime;
//	}
	public CockpitManager getCockpitManager() {
		return getShip().getManagerContainer().getCockpitManager();
	}
	
	@Override
	public void handleKeyEvent(KeyEventInterface e) {
			
			if(shipCamera instanceof InShipCamera && e.isTriggered(KeyboardMappings.ADJUST_COCKPIT)) {
				Controller.FREE_CAM_STICKY = false; //TODO Check if sticky cam still disables controls for all plaeyrs
				((InShipCamera)shipCamera).switchAdjustMode();
			}
			if(shipCamera instanceof InShipCamera && e.isTriggered(KeyboardMappings.ADJUST_COCKPIT_RESET)) {
				((InShipCamera)shipCamera).resetAdjustMode();
			}
			
			if (e.isSlotKey()) {
				numberKeyPressed(
						(byte) (
								getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getSelectedWeaponBottomBar() * 10 +
								e.getSlotKey()));
			}

			getCockpitManager().handleKeyEvent(e, this);


			if (e.isTriggered(KeyboardMappings.SHIP_PRIMARY_FIRE)) {
				aquire.flagSlotChange();
			}
				int selectedSlot = getState().getPlayer().getCurrentShipControllerSlot();

			if(!e.isTriggered(KeyboardMappings.SCROLL_MOUSE_ZOOM_IN) && !e.isTriggered(KeyboardMappings.SCROLL_MOUSE_ZOOM_OUT)) {
				if (e.isTriggered(KeyboardMappings.SCROLL_BOTTOM_BAR_NEXT) || e.isTriggered(KeyboardMappings.SCROLL_BOTTOM_BAR_PREVIOUS)) {
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().modSelectedWeaponBottomBar(e.isTriggered(KeyboardMappings.SCROLL_BOTTOM_BAR_NEXT) ? 1 : -1);
					getState().getPlayer().setCurrentShipControllerSlot((byte) (FastMath.cyclicModulo(selectedSlot, 10) + getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getSelectedWeaponBottomBar() * 10), 0.1F);
					aquire.flagSlotChange();
				} else if (e.isTriggered(KeyboardMappings.PREVIOUS_SLOT) || e.isTriggered(KeyboardMappings.NEXT_SLOT)) {
					int nVal;
					int normVal;
					int newSelectedSlot;
					if (e.isTriggered(KeyboardMappings.PREVIOUS_SLOT)) {
						nVal = selectedSlot - 1;
						normVal = FastMath.cyclicModulo(selectedSlot, 10) - 1;
					} else {
						nVal = selectedSlot + 1;
						normVal = FastMath.cyclicModulo(selectedSlot, 10) + 1;
					}
					newSelectedSlot = FastMath.cyclicModulo(nVal, 10);


					boolean barChanged = false;
					if (e.isTriggered(KeyboardMappings.NEXT_SLOT) && normVal > 9) {
						//interchange bottom bar to the left
						getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().modSelectedWeaponBottomBar(1);
						barChanged = true;

					} else if (e.isTriggered(KeyboardMappings.PREVIOUS_SLOT) && normVal < 0) {
						//interchange bottom bar to the right
						getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().modSelectedWeaponBottomBar(-1);
						barChanged = true;
					}
					if (barChanged || selectedSlot != newSelectedSlot) {
						getState().getPlayer().setCurrentShipControllerSlot((byte) (newSelectedSlot + getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getSelectedWeaponBottomBar() * 10), 0.1F);
						aquire.flagSlotChange();
					}

				}
			}
			aquire.checkSlot(getEntered());


	}

	@Override
	public void onSwitch(boolean active) {

		System.out.println("SHIPCAMERA: onSwitch called");
		InShipControlManager inShipControlManager = getState().getGlobalGameControlManager().
				getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
		if (getState().getPlayer() != null) {
			getState().getPlayer().setAquiredTarget(null);
		}
		aquire.flagSlotChange();
		
		if(!active && shipCamera instanceof InShipCamera) {
			((InShipCamera)shipCamera).setAdjustMode(false);
		}
		
		if (active) {
			//			try{
			//				throw new  NullPointerException();
			//			}catch (Exception e) {
			//				e.printStackTrace();
			//			}

			if (shipCamera == null || ((FixedViewer) shipCamera.getViewable()).getEntity() != getShip()) {

				Camera prefCamera = Controller.getCamera();

				assert (getShip() != null) : "SHIP NOT FOUND ";
				System.err.println("[CLIENT] SWITCHING TO USING SHIP CAMERA (last cam: " + prefCamera + "); DOCKED: " + getEntered().getSegmentController().getDockingController().isDocked() + "; RAIL: " + getEntered().getSegmentController().railController.isDockedOrDirty());

				shipCamera = new InShipCamera(inShipControlManager.getShipControlManager(), Controller.getCamera(), getEntered());
				shipCamera.setCameraStartOffset(0f);
				((InShipCamera) shipCamera).resetTransition(prefCamera);

				getState().getPlayer().setLastOrientation(getEntered().getSegment().getSegmentController().getWorldTransform());

				((InShipCamera) shipCamera).docked =
						getEntered().getSegmentController().getDockingController().isDocked() ||
								getEntered().getSegmentController().railController.isDockedOrDirty();

			} else if (shipCamera != null) {
				((InShipCamera) shipCamera).resetTransition(Controller.getCamera());
				getState().getPlayer().setLastOrientation(getEntered().getSegment().getSegmentController().getWorldTransform());
				((InShipCamera) shipCamera).docked =
						getEntered().getSegmentController().getDockingController().isDocked() ||
								getEntered().getSegmentController().railController.isDockedOrDirty();
			}
			getCockpitManager().onSwitch(this);
			

			getState().getController().timeOutBigMessage(Lng.str("Build Mode"));
			String fltMode = Lng.str("Flight Mode");
			getState().getController().showBigMessage("Flight Mode", fltMode, Lng.str("(press %s to switch to BUILD MODE; press %s to exit structure)", KeyboardMappings.CHANGE_SHIP_MODE.getKeyChar(), KeyboardMappings.ENTER_SHIP.getKeyChar()), 0);
			Controller.setCamera(shipCamera);
		} else {
			shipCamera = null;

		}
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);

		if (KeyboardMappings.ALIGN_SHIP.isDown()) {
			if (Controller.getCamera() instanceof InShipCamera) {
				((InShipCamera) Controller.getCamera()).align();

			}
		}
		if (!cancelledMov && KeyboardMappings.CANCEL_SHIP.isDown()) {
			if (Controller.getCamera() instanceof InShipCamera) {
				((InShipCamera) Controller.getCamera()).cancel();

			}
			cancelledMov = true;
		} else {
			cancelledMov = KeyboardMappings.CANCEL_SHIP.isDown();
		}

		if (lastDocked && (getEntered() != null &&
				!getEntered().getSegmentController().getDockingController().isDocked() &&
				!getEntered().getSegmentController().railController.isDockedOrDirty())) {
			System.err.println("[CLIENT] undocked: restting camera");
			//undocked
			shipCamera = null;

			/*
			 * the vital part for orientation on undock is
			 * the orientate() function in InShipCamera
			 */
			onSwitch(true);
		}

		lastDocked = (getEntered() != null &&
				(getEntered().getSegmentController().getDockingController().isDocked() || getEntered().getSegmentController().railController.isDockedOrDirty()));

		CameraMouseState.setGrabbed(true);
		getCockpitManager().updateLocal(this, shipCamera, timer);
		
		aquire.update(getEntered(), timer);

	}






	private void numberKeyPressed(byte i) {
		getState().getPlayer().setCurrentShipControllerSlot(i, 0.0F);
		aquire.flagSlotChange();

	}

	public void resetShipCamera() {
		this.shipCamera = null;
	}

	public NavigationControllerManager getNavigationControllerManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getNavigationControlManager();
	}

	public TargetAquireHelper getAquire() {
		return aquire;
	}

	public boolean isTreeActiveInFlight() {
		return isTreeActive() && (!(shipCamera instanceof InShipCamera) || !((InShipCamera)shipCamera).isInAdjustMode());
	}

	

	

}
