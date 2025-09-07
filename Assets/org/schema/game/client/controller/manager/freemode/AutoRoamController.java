package org.schema.game.client.controller.manager.freemode;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.JoinMenu;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.camera.AutoViewerCamera;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.network.objects.container.TransformTimed;

public class AutoRoamController extends AbstractControlManager {

	final static float minSpeed = 5;
	final static float fastSpeed = 50;
	private AutoViewerCamera camera;

	public AutoRoamController(GameClientState state) {
		super(state);
		initialize();

	}

	private void initialize() {

	}

	@Override
	public void onSwitch(boolean active) {

		CameraMouseState.setGrabbed(!active);

		if (active) {
			//			 setCamera();
			//			if(Controller.getCamera() != null){
			//				camera.getPos().set(Controller.getCamera().getPos());
			//			}
			//			Controller.setCamera(camera);

			System.err.println("[CLIENT][CONTROLLER] Switched to auto roam (spawn screen)");
			getState().setPlayerSpawned(false);
			boolean contains = false;
			for (DialogInterface p : getState().getController().getPlayerInputs()) {
				if (p instanceof JoinMenu) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				JoinMenu gameMenu = new JoinMenu(getState());
				getState().getController().getPlayerInputs().add(gameMenu);
			}

		}
		super.onSwitch(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);
		CameraMouseState.setGrabbed(false);
		if (camera == null) {
			setCamera();
		}
		if (Controller.getCamera() != camera) {
			Controller.setCamera(camera);
			camera.update(timer, false);
		}

		//		System.err.println("CAMERA POS: "+camera.getPos());
	}

	private void setCamera() {
		final TransformTimed f = new TransformTimed();
		f.setIdentity();
		FixedViewer viewable = new FixedViewer(() -> f);
		Vector3f pos = new Vector3f(getState().getScene().getLight().getPos());
		if (pos.lengthSquared() == 0) {
			pos.set(0, 0, 1);
		}
		pos.negate();
		pos.normalize();
		camera = new AutoViewerCamera(getState(), viewable, new Vector3f(125, 70, 223), pos);
	}

}
