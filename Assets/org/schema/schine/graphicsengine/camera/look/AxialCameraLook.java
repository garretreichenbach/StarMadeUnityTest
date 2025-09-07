package org.schema.schine.graphicsengine.camera.look;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class AxialCameraLook implements MouseLookAlgorithm {

	private final Transform following = new Transform();
	private final Transform followingOld = new Transform();
	private Camera camera;
	private Vector3f vCross = new Vector3f();
	private Vector3f axis = new Vector3f();
	private Vector2f mouse = new Vector2f();
	private Vector2f mouseSum = new Vector2f();

	private Quat4f result = new Quat4f();

	private Quat4f rotMulView = new Quat4f();

	private Quat4f rotConj = new Quat4f();
	private Quat4f newRotation = new Quat4f();
	private Quat4f totalRotation = new Quat4f(0, 0, 0, 1);
	private Quat4f tmpQuat = new Quat4f();

	private Transform lookTo;
	private String lastLook = "";

	public AxialCameraLook(Camera camera) {
		this.camera = camera;
		following.setIdentity();
		followingOld.setIdentity();
	}

	@Override
	public void fix() {
		
	}

	@Override
	public void force(Transform t) {
		following.set(t);
		followingOld.set(t);
	}

	/**
	 * dx, dy are between [-1.0d, 1.0d]
	 * x/ySensitivity are between [0.0d, 1.0d]
	 * <p/>
	 * Recalculates upVector and view Vectors for dx and dy mouse newRotation
	 */
	@Override
	public void mouseRotate(boolean server, float dx, float dy, float dz, float xSensitivity, float ySensitivity, float zSensibilits) {

		camera.getWorldTransform().set(followingOld);

		mouse.x = -dx * xSensitivity;
		mouse.y = (EngineSettings.S_MOUSE_ALL_INVERT.isOn() ? dy : -dy) * ySensitivity;

		mouseSum.add(mouse);
		if (mouseSum.y > FastMath.HALF_PI) {
			mouseSum.y -= mouse.y;

			mouse.y = FastMath.HALF_PI - mouseSum.y;
			mouseSum.y = FastMath.HALF_PI;

		}

		String newLook = "x: " + mouse.x + " y: " + mouse.y;
		if(lastLook == null || !lastLook.equals(newLook)) {
			lastLook = newLook;
		}


		// We don't want to rotate down more than one radian, so we cap it.
		if (mouseSum.y < -FastMath.HALF_PI) {
			mouseSum.y -= mouse.y;

			mouse.y = -(FastMath.HALF_PI - Math.abs(mouseSum.y));
			mouseSum.y = -FastMath.HALF_PI;
		}

		Vector3f view = new Vector3f(camera.getForward()); //Vector3fTools.subtract(targetVector, position);
		Vector3f upVector = new Vector3f(camera.getUp()); //Vector3fTools.subtract(targetVector, position);
		Vector3f rightVector = new Vector3f(camera.getRight()); //Vector3fTools.subtract(targetVector, position);

		if (mouse.y != 0) {
			//Mouse-Y Rotation, newRotation on our view vector x up vector
			vCross.cross(camera.getForward(), camera.getUp());
			axis.set(vCross);
			axis.normalize();
			rotateCamera(mouse.y, axis, view, upVector, rightVector);

		}
		// Rotate around the x axis
		rotateCamera(mouse.x, new Vector3f(0, 1, 0), view, upVector, rightVector);

		if (lookTo != null) {
			Vector2f planeXZ = new Vector2f(view.x, view.z);
			Vector3f toForward = GlUtil.getForwardVector(new Vector3f(), lookTo);
			Vector2f toPlaneXZ = new Vector2f(toForward.x, toForward.z);
			while (planeXZ.angle(toPlaneXZ) > 0.01) {
				rotateCamera(0.005f, new Vector3f(0, 1, 0), view, upVector, rightVector);

				planeXZ = new Vector2f(view.x, view.z);
				toPlaneXZ = new Vector2f(toForward.x, toForward.z);
			}
			;

			lookTo = null;
		}

		camera.setForward(view);
		camera.setUp(upVector);
		camera.setRight(rightVector);

		followingOld.set(camera.getWorldTransform());

		if (camera.getExtraOrientationRotation() != null) {
//			System.err.println("ADDING EXTRA \n"+camera.getExtraOrientationRotation());
			following.basis.mul(camera.getExtraOrientationRotation());
		}

		following.basis.mul(camera.getWorldTransform().basis);

		camera.getWorldTransform().set(following);

	}

	@Override
	public void lookTo(Transform n) {
		this.lookTo = new Transform(n);
	}

	/**
	 * @return the following
	 */
	public Transform getFollowing() {
		return following;
	}

	private void rotate(Vector3f v) {
		tmpQuat.set(v.x, v.y, v.z, 0);
		rotMulView.mul(newRotation, tmpQuat);
		result.mul(rotMulView, rotConj);
		v.set(result.x, result.y, result.z);
	}

	private void rotateCamera(float angle, Vector3f axis, Vector3f forward, Vector3f up, Vector3f right) {

		newRotation.x = axis.x * FastMath.sin(angle / 2);
		newRotation.y = axis.y * FastMath.sin(angle / 2);
		newRotation.z = axis.z * FastMath.sin(angle / 2);
		newRotation.w = FastMath.cos(angle / 2);

		rotConj.conjugate(newRotation);

		rotate(forward);
		rotate(up);
		rotate(right);

		totalRotation.mul(newRotation);
	}

}
