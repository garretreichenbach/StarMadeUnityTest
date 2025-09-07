package org.schema.schine.graphicsengine.camera.look;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import com.bulletphysics.linearmath.Transform;

public class FirstPersonCameraLook implements MouseLookAlgorithm {

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

	public FirstPersonCameraLook(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void fix() {
		
	}

	@Override
	public void force(Transform t) {
	}

	/**
	 * dx, dy are between [-1.0d, 1.0d]
	 * x/ySensitivity are between [0.0d, 1.0d]
	 * <p/>
	 * Recalculates upVector and view Vectors for dx and dy mouse newRotation
	 */
	@Override
	public void mouseRotate(boolean server, float dx, float dy, float dz, float xSensitivity, float ySensitivity, float zSensibilits) {

		mouse.x = -dx * xSensitivity;
		mouse.y = (EngineSettings.S_MOUSE_ALL_INVERT.isOn() ? dy : -dy) * ySensitivity;

		mouseSum.add(mouse);
		//		System.err.println("MMMMM "+mouseSum+";;; "+FastMath.HALF_PI+": "+totalRotation);
		if (mouseSum.y > FastMath.HALF_PI) {
			mouseSum.y -= mouse.y;

			mouse.y = FastMath.HALF_PI - mouseSum.y;
			mouseSum.y = FastMath.HALF_PI;

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

		camera.setForward(view);
		camera.setUp(upVector);
		camera.setRight(rightVector);

	}

	@Override
	public void lookTo(Transform n) {
		
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
