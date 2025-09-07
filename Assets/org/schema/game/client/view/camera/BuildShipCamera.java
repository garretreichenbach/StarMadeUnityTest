/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ViewerCamera</H2>
 * <H3>org.schema.schine.graphicsengine.camera</H3>
 * ViewerCamera.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.schema.game.client.view.camera;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.EditSegmentInterface;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.common.InputHandler;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.TransitionCamera;
import org.schema.schine.graphicsengine.camera.look.AxialCameraLook;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;

import com.bulletphysics.linearmath.Transform;

public class BuildShipCamera extends Camera implements InputHandler, SegmentControllerCamera {

	private GameClientState state;
	private SegmentController ship;
	private TransitionCamera transitionCamera;
	private Camera toReset;

	public BuildShipCamera(GameClientState state, Camera old, EditSegmentInterface ship, ShipMovableCameraViewable s, Transform startTransform) {
		super(ship.getSegmentController().getState(), s, startTransform);
		this.state = state;
		this.ship = ship.getSegmentController();

		setLookAlgorithm(new AxialCameraLook(this));

		this.toReset = old;
	}
	public BuildShipCamera(GameClientState state, Camera old, EditSegmentInterface ship, Vector3f initialDist, Transform startTransform) {
		this(state, old, ship, new ShipMovableCameraViewable(ship, initialDist), startTransform);
		

	}

	
	public BuildShipCamera(GameClientState state, EditSegmentInterface ship) {
		this(state, null, ship, SegmentBuildController.INITAL_BUILD_CAM_DIST, null);
	}

	@Override
	public SegmentController getSegmentController() {
		return ship;
	}

	public Vector3f getRelativeCubePos() {
		return ((ShipMovableCameraViewable) getViewable()).getRelativeCubePos();
	}

	/**
	 * @return the state
	 */
	public GameClientState getState() {
		return state;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		((ShipMovableCameraViewable) getViewable()).handleKeyEvent(e);
	}



	public void jumpTo(Vector3i absPos) {
		((ShipMovableCameraViewable) getViewable()).jumpTo(absPos);
	}

	public void jumpToInstantly(Vector3i core) {
		((ShipMovableCameraViewable) getViewable()).jumpToInstantly(new Vector3i(core));

	}

	public void resetTransition(Camera camera) {
		setStable(false);
		this.transitionCamera = new TransitionCamera(camera, this, 0.2f);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Camera#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer, boolean server) {
		if (!PlayerInteractionControlManager.isAdvancedBuildMode(state)) {
			((AxialCameraLook) getLookAlgorithm()).getFollowing().set(ship.getWorldTransform());

			super.update(timer, server);
		} else {
			//			updateViewer(timer);
		}
		if (transitionCamera != null && transitionCamera.isActive()) {
			setStable(false);
			transitionCamera.update(timer, server);
			this.getWorldTransform().set(transitionCamera.getWorldTransform());
		} else {
			setStable(true);
		}

		if (toReset != null) {
			resetTransition(toReset);
			toReset = null;
		}
	}

}