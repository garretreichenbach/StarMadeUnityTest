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

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.schine.common.InputHandler;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.look.AxialCameraLook;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.linearmath.Transform;

public class PlayerCamera extends Camera implements InputHandler {

	public static boolean alignedContinously = true;
	boolean first = true;
	private GameClientState state;
	private PlayerCharacter character;
	private Transform stdTransform;
	private ClosestRayResultCallback callBack;
	private Transform align;

	public PlayerCamera(GameClientState state, PlayerCharacter e) {
		super(state, new UpperFixedViewer(e));
		this.setCharacter(e);
		this.state = state;
		this.stdTransform = new Transform();
		stdTransform.setIdentity();
		//		setLookAlgorithm(new TransformableRestrictedCameraLook(this, e));
		setLookAlgorithm(new AxialCameraLook(this));
	}

	public Vector3i getCameraCubeOffset() {
		return ((ShipOffsetCameraViewable) getViewable()).getPosMod();
	}

	/**
	 * @return the character
	 */
	public PlayerCharacter getCharacter() {
		return character;
	}

	/**
	 * @param character the character to set
	 */
	public void setCharacter(PlayerCharacter character) {
		this.character = character;
		((FixedViewer) getViewable()).setEntity(character);
	}

	public Vector3i getCurrentBlock() {
		return ((ShipOffsetCameraViewable) getViewable()).getCurrentBlock();

	}

	public ClosestRayResultCallback getNearestIntersection(PlayerCharacter playerCharacter) {
		if (getCameraOffset() > 0) {

			Vector3f camPos = new Vector3f(playerCharacter.getHeadWorldTransform().origin);

			Vector3f camTo = new Vector3f(getOffsetPos(new Vector3f()));

			CubeRayCastResult rayCallback = new CubeRayCastResult(
					camPos, camTo, false);
			rayCallback.setOnlyCubeMeshes(true);
			ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) state.getPhysics()).testRayCollisionPoint(
					camPos, camTo, rayCallback, false);

			return testRayCollisionPoint;
		}
		return null;

		//((PhysicsExt)getState().getPhysics()).testRayCollisionPoint(camPos, camTo, false, null, null, false, null, false);
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		((ShipOffsetScrollableCameraViewable) getViewable()).handleKeyEvent(e);
	}


	public void jumpTo(Vector3i absPos) {
		((ShipOffsetCameraViewable) getViewable()).jumpTo(absPos);
	}

	@Override
	public Matrix3f getExtraOrientationRotation() {
		return character.getCharacterController().getAddRoation();
	}

	@Override
	protected int limitWheel(int in) {
		return Math.max(0, Math.min(in, 2500));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.Camera#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer, boolean server) {

		if (character.getGravity().isAligedOnly() || character.getGravity().isGravityOn()) {
			if (character.getGravity().isGravityOn() || (character.getGravity().isAligedOnly() && alignedContinously)) {
				((AxialCameraLook) getLookAlgorithm()).getFollowing().set(character.getGravity().source.getWorldTransform());
				align = null;
			} else {
				//aligned only
				//only use one instance
				if (align == null) {
					align = new Transform(character.getGravity().source.getWorldTransform());
				}
				((AxialCameraLook) getLookAlgorithm()).getFollowing().set(align);
			}
		} else {
			align = null;
			((AxialCameraLook) getLookAlgorithm()).getFollowing().set(stdTransform);
		}
		{
			super.update(timer, server);

		}

		callBack = getNearestIntersection(character);

		if (callBack != null && callBack.hasHit()) {
			Vector3f diff = new Vector3f();
			diff.sub(getOffsetPos(new Vector3f()), callBack.hitPointWorld);
			diff.scale(1.01f);
			getWorldTransform().origin.sub(diff);
		}

	}

}