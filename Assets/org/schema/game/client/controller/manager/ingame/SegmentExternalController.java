package org.schema.game.client.controller.manager.ingame;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.client.view.camera.ShipOffsetCameraViewable;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.network.objects.container.PhysicsDataContainer;

public class SegmentExternalController extends AbstractControlManager {

	private final TargetAquireHelper aquire = new TargetAquireHelper();
	Vector3f force = new Vector3f();
	Vector3i cockpitTmp = new Vector3i();
	private Camera shipCamera;

	public SegmentExternalController(GameClientState state) {
		super(state);
	}



	public SegmentPiece getEntered() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSegmentControlManager().getEntered();
	}

	public Vector3i getEntered(Vector3i out) {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSegmentControlManager().getEntered().getAbsolutePos(out);
	}

	/**
	 * @return the physicsData
	 */
	public PhysicsDataContainer getPhysicsData() {
		return getEntered().getSegment().getSegmentController().getPhysicsDataContainer();
	}


	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if(e.isSlotKey()) {
			int slot = e.getSlotKey();
			numberKeyPressed(
					(byte) (
							getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getSelectedWeaponBottomBar() * 10 +
									slot));
		}
	}



	@Override
	public void onSwitch(boolean active) {
		aquire.flagSlotChange();
		SegmentControlManager inShipControlManager = getState().getGlobalGameControlManager().
				getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSegmentControlManager();
		if (getState().getPlayer() != null) {
			getState().getPlayer().setAquiredTarget(null);
		}
		if (active) {
			//			if(shipCamera == null || ((FixedViewer)shipCamera.getViewable()).getEntity() != getShip()){
			getState().getGlobalGameControlManager().getIngameControlManager()
					.getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().load(getEntered().getSegment().getSegmentController().getUniqueIdentifier());

			getState().getGlobalGameControlManager().getIngameControlManager()
					.getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().user = (getEntered().getSegment().getSegmentController().getUniqueIdentifier());
			//					System.err.println("USING SHIP CAMERA");
			System.err.println("[CLIENT][SEGMENTEXTERNALEFT_CONTROLLER] ENTERED: " + getEntered());

			shipCamera = new InShipCamera(inShipControlManager, Controller.getCamera(), getEntered());
			shipCamera.setCameraStartOffset(0f);
			Vector3i absolutePos = inShipControlManager.getEntered().getAbsolutePos(new Vector3i());
			absolutePos.sub(Ship.core); //shift by 8 to correct to middle
			((ShipOffsetCameraViewable) shipCamera.getViewable()).getPosMod().set(absolutePos);
			((ShipOffsetCameraViewable) shipCamera.getViewable()).setJumpToBlockSpeed(50);


			Controller.setCamera(shipCamera);
		} else {
			getState().getGlobalGameControlManager().getIngameControlManager()
					.getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().save(getState().getGlobalGameControlManager().getIngameControlManager()
					.getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().user);
			shipCamera = null;

		}
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);


		CameraMouseState.setGrabbed(true);

		aquire.update(getEntered(), timer);
		
	}


	private void numberKeyPressed(byte i) {
		getState().getPlayer().setCurrentShipControllerSlot(i, 0.0F);
		aquire.flagSlotChange();
		

	}

	public NavigationControllerManager getNavigationControllerManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getNavigationControlManager();
	}

	public TargetAquireHelper getAquire() {
		return aquire;
	}
}
