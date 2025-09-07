package org.schema.game.client.view.camera;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.EditSegmentInterface;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.InputHandler;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

import com.bulletphysics.linearmath.Transform;

public class ShipMovableCameraViewable extends FixedViewer implements InputHandler {

	protected SegmentController controller;
	protected EditSegmentInterface edit;
	Transform tinv = new Transform();
	private Vector3f posCur = new Vector3f();
	private float minSpeed = 5;
	public final Vector3f initialDist;
	public Vector3f blockPos = new Vector3f();
	public ShipMovableCameraViewable(EditSegmentInterface edit, Vector3f initalDist) {
		super(edit.getSegmentController());
		this.controller = edit.getSegmentController();
		this.edit = edit;
		this.initialDist = initalDist;
	}

	@Override
	public synchronized Vector3f getPos() {
		Vector3f pos = super.getPos();
		Vector3f mod = getRelativeCubePos();

		tinv.set(getEntity().getWorldTransform());
		tinv.basis.transform(mod);
		pos.add(mod);
		return pos;
	}
	public boolean canMove(PlayerInteractionControlManager playerIntercationManager) {

		if (!playerIntercationManager.getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive()
				&& !playerIntercationManager.getSegmentControlManager().isTreeActive()) {
			return false;
		}
		if (!playerIntercationManager.isTreeActive() || playerIntercationManager.isSuspended()) {
			return false;
		}
		if (((GameClientState) controller.getState()).getGlobalGameControlManager().getIngameControlManager().getChatControlManager().isActive()) {
			return false;
		}
		if (((GameClientState) controller.getState()).getPlayer() != null && !((GameClientState) controller.getState()).getPlayer().getControllerState().canClientPressKey()) {
			return false;
		}
		return true;
	}
	@Override
	public void update(Timer timer) {
		
		if(!canMove(((GameClientState) controller.getState()).getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager())) {
			return;
		}

		Vector3f forw = new Vector3f(controller.getCamForwLocal());
		Vector3f up = new Vector3f(controller.getCamUpLocal());
		Vector3f left = new Vector3f(controller.getCamLeftLocal());

		float speed = KeyboardMappings.BUILD_MODE_FAST_MOVEMENT.isDown() ? EngineSettings.BUILD_MODE_SHIFT_SPEED.getFloat() : minSpeed;
		forw.scale(speed * timer.getDelta());
		up.scale(speed * timer.getDelta());
		left.scale(speed * timer.getDelta());

		if (!(KeyboardMappings.FORWARD.isDown() && KeyboardMappings.BACKWARDS.isDown())) {
			if (KeyboardMappings.FORWARD.isDown()) {
				posCur.add(forw);
			}
			if (KeyboardMappings.BACKWARDS.isDown()) {
				forw.scale(-1);
				posCur.add(forw);
			}
		}
		if (!(KeyboardMappings.STRAFE_LEFT.isDown() && KeyboardMappings.STRAFE_RIGHT.isDown())) {
			if (KeyboardMappings.STRAFE_LEFT.isDown()) {
				left.scale(-1);
				posCur.add(left);
			}
			if (KeyboardMappings.STRAFE_RIGHT.isDown()) {
				posCur.add(left);
			}
		}
		if (!(KeyboardMappings.DOWN.isDown() && KeyboardMappings.UP.isDown())) {
			if (KeyboardMappings.DOWN.isDown()) {
				up.scale(-1);
				posCur.add(up);
			}
			if (KeyboardMappings.UP.isDown()) {
				posCur.add(up);
			}
		}
	}
	

	public Vector3f getRelativeCubePos() {
		return new Vector3f(posCur.x + edit.getCore().x - SegmentData.SEG_HALF+initialDist.x, posCur.y + edit.getCore().y - SegmentData.SEG_HALF+initialDist.y, posCur.z + edit.getCore().z - SegmentData.SEG_HALF +initialDist.z);
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {

	}


	public void jumpTo(Vector3i absPos) {
		absPos.sub(edit.getCore());
	}

	public void jumpToInstantly(Vector3i absPos) {
		absPos.sub(edit.getCore());
		this.blockPos.set(absPos.x, absPos.y, absPos.z);		
		posCur.set(absPos.x, absPos.y, absPos.z);
	}

	public Vector3f getPosRaw() {
		return posCur;
	}

	public void jumpToInstantlyWithoutOffset(Vector3i absPos) {
		this.blockPos.set(absPos.x, absPos.y, absPos.z);		
		posCur.set(absPos.x, absPos.y, absPos.z);		
	}

}
