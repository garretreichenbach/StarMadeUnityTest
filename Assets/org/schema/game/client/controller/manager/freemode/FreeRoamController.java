package org.schema.game.client.controller.manager.freemode;

import javax.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.camera.viewer.AbstractViewer;
import org.schema.schine.graphicsengine.camera.viewer.PositionableViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.Keyboard;

public class FreeRoamController extends AbstractControlManager {

	final static float minSpeed = 5;
	final static float fastSpeed = 50;
	private Camera camera;

	public FreeRoamController(GameClientState state) {
		super(state);
		initialize();
	}


	@Override
	public void onSwitch(boolean active) {

		CameraMouseState.setGrabbed(active);
		if (active) {
			System.err.println("[CLIENT][CONTROLLER] Switched to free roam");
			PositionableViewer viewable = new PositionableViewer();
			camera = new Camera(getState(), viewable);
			if (Controller.getCamera() != null) {
				camera.getPos().set(Controller.getCamera().getPos());
			}
			Controller.setCamera(camera);
		}
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		//		if(getShip().isGhost()){
		//			return;
		//		}
		CameraMouseState.setGrabbed(true);

		AbstractViewer viewable = Controller.getCamera().getViewable();

		Vector3f forw = new Vector3f(viewable.getForward());
		Vector3f up = new Vector3f(viewable.getUp());
		Vector3f right = new Vector3f(viewable.getRight());

		float speed = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? fastSpeed : minSpeed;
		forw.scale(speed * timer.getDelta());
		up.scale(speed * timer.getDelta());
		right.scale(speed * timer.getDelta());

		if (!(Keyboard.isKeyDown(GLFW.GLFW_KEY_W) && Keyboard.isKeyDown(GLFW.GLFW_KEY_S))) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_W)) {
				viewable.getPos().add(forw);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_S)) {
				forw.scale(-1);
				viewable.getPos().add(forw);
			}
		}
		if (!(Keyboard.isKeyDown(GLFW.GLFW_KEY_A) && Keyboard.isKeyDown(GLFW.GLFW_KEY_D))) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_A)) {
				right.scale(-1);
				viewable.getPos().add(right);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_D)) {

				viewable.getPos().add(right);
			}
		}
		if (!(Keyboard.isKeyDown(GLFW.GLFW_KEY_Q) && Keyboard.isKeyDown(GLFW.GLFW_KEY_E))) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_Q)) {
				up.scale(-1);
				viewable.getPos().add(up);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_E)) {
				viewable.getPos().add(up);
			}
		}
	}

	private void initialize() {
		if (camera == null) {
			PositionableViewer viewable = new PositionableViewer();
			camera = new Camera(getState(), viewable);
		}
		if (Controller.getCamera() == null) {
			Controller.setCamera(camera);
		}
	}

}
