package org.schema.game.client.view.camera;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.game.common.data.element.Element;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.look.MouseLookAlgorithm;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;

import com.bulletphysics.linearmath.Transform;

public class TransformableRestrictedCameraLook implements MouseLookAlgorithm {

	private final Transform start = new Transform();
	private final Transform following = new Transform();
	//	private Vector2f mouseSum = new Vector2f();
	private final Transform followingOld = new Transform();
	private final Transform before = new Transform();
	float limit = FastMath.HALF_PI / 2f;
	private Camera camera;
	private Vector3f mouse = new Vector3f();
	private Quat4f rotation = new Quat4f(0, 0, 0, 1);
	private boolean correcting;
	private int orientation = Element.FRONT;
	private Transformable transformable;
	private int iterations = 0;
	private boolean init = true;

	public TransformableRestrictedCameraLook(Camera camera, Transformable t) {
		this.camera = camera;
		this.transformable = t;
		following.setIdentity();
		followingOld.setIdentity();
		start.set(t.getWorldTransform());

	}

	@Override
	public void fix() {
		following.setIdentity();
		followingOld.setIdentity();
		start.set(this.transformable.getWorldTransform());
	}

	@Override
	public void force(Transform t) {
		following.set(t);
		followingOld.set(t);
		start.set(this.transformable.getWorldTransform());
	}

	@Override
	public void mouseRotate(boolean server, float dx, float dy, float dz, float xSensitivity, float ySensitivity, float zSensitivity) {
		before.set(camera.getWorldTransform());
		iterations = 0;
		mouse.x = -dx * xSensitivity;
		mouse.y = dy * ySensitivity;
		mouse.z = dz * zSensitivity;
		mouseRotate();
	}

	@Override
	public void lookTo(Transform n) {
		
	}

	private void freeRot() {

		Vector3f tForwardVector = new Vector3f();

		Vector3f tUpVector = new Vector3f();

		Vector3f tRightVector = new Vector3f();

		getVectors(tForwardVector, tUpVector, tRightVector, transformable.getWorldTransform());

		if (init) {
			camera.setForward(tForwardVector);
			camera.setUp(tUpVector);
			camera.setRight(tRightVector);
			init = false;
			followingOld.set(camera.getWorldTransform());
		}

		camera.getWorldTransform().set(followingOld);

		//Mouse-Y Rotation, rotation on our view vector x up vector
		Vector3f vCross = new Vector3f();
		vCross.cross(tForwardVector, tUpVector);
		Vector3f vRot = new Vector3f(vCross);
		vRot.normalize();

		//dy must be negated for some reason
		float yRotAngle = mouse.y; //angle
		if (yRotAngle > 1) yRotAngle = 1;
		if (yRotAngle < -1) yRotAngle = -1;

		//don't rotate if no delta
		Quat4f tempA;
		if (mouse.y == 0.0f) {
			tempA = new Quat4f(0, 0, 0, 1);
		} else {
			tempA = new Quat4f(
					vRot.x * FastMath.sin(yRotAngle),
					vRot.y * FastMath.sin(yRotAngle),
					vRot.z * FastMath.sin(yRotAngle),
					FastMath.cos(yRotAngle));
		}

		//Mouse-X Rotation, rotation on our up vector
		vRot = new Vector3f(tUpVector);
		vRot.normalize();

		//dx must be negated for some reason
		float xRotAngle = mouse.x; //angle
		if (xRotAngle > 1) xRotAngle = 1;
		if (xRotAngle < -1) xRotAngle = -1;

		Quat4f tempB;
		if (mouse.x == 0.0f) {
			tempB = new Quat4f(0, 0, 0, 1);
		} else {
			tempB = new Quat4f(
					vRot.x * FastMath.sin(xRotAngle),
					vRot.y * FastMath.sin(xRotAngle),
					vRot.z * FastMath.sin(xRotAngle),
					FastMath.cos(xRotAngle));
		}

		vRot = new Vector3f(tForwardVector);
		vRot.normalize();

		//dx must be negated for some reason
		float zRotAngle = mouse.z; //angle
		if (zRotAngle > 1) zRotAngle = 1;
		if (zRotAngle < -1) zRotAngle = -1;
		Quat4f tempC;
		
		if (mouse.z == 0.0f) {
			tempC = new Quat4f(0, 0, 0, 1);
		} else {
			
			GlUtil.getForwardVector(vRot, camera.getWorldTransform());
			vRot.normalize();
			
			Matrix3f m = new Matrix3f(transformable.getWorldTransform().basis);
			Quat4f mm = new Quat4f();
			Quat4fTools.set(m, mm);

			Matrix3f mC = new Matrix3f(camera.getWorldTransform().basis);
			Quat4f mmC = new Quat4f();
			Quat4fTools.set(mC, mmC);

			float rollA = FastMath.atan2Fast(
					GlUtil.getUpVector(new Vector3f(), transformable.getWorldTransform()).y,
					GlUtil.getRightVector(new Vector3f(), transformable.getWorldTransform()).y) - FastMath.HALF_PI;

			float rollB = FastMath.atan2Fast(
					camera.getUp().y,
					camera.getRight().y) - FastMath.HALF_PI;
			float diff = FastMath.atan2(FastMath.sin(rollA - rollB), FastMath.cos(rollA - rollB));

			if (Math.abs(diff) < FastMath.HALF_PI - 0.00003f) {
				tempC = new Quat4f(
						vRot.x * FastMath.sin(zRotAngle),
						vRot.y * FastMath.sin(zRotAngle),
						vRot.z * FastMath.sin(zRotAngle),
						FastMath.cos(zRotAngle));
			} else {
				tempC = new Quat4f(0, 0, 0, 1);
			}
//			Matrix3f rC = new Matrix3f();
//			rC.rotZ(mouse.z);
//			Matrix3f r = new Matrix3f(camera.getWorldTransform().basis);
//			r.setIdentity();
//			r.mul(rC);
//			Quat4fTools.set(r, tempC);
		}

		//We mutiply both rotations to get THE rotation, am I wrong here?
		//tempA rotationQuat generated by moving the mouse up/down
		//tempB rotationQuat generated by moving the mouse left/right
		//THE rotation is assigned to tempB...
		//		tempB = tempA.getMultiplicationBy(tempB).getNormalized();
		rotation.set(tempA);
		rotation.mul(tempB);
		rotation.mul(tempC);
//		rotation.mul(tempA, tempB);
//		rotation.mul(tempC);
		rotation.normalize();

		
		
		Quat4f mTmpConjugated = new Quat4f();

		mTmpConjugated.conjugate(rotation);

		//we apply the rotation to the view vector, that way we can get the new targetVector
		Quat4f viewQuat = new Quat4f(camera.getForward().x, camera.getForward().y, camera.getForward().z, 0);
		
		
		
		Quat4f result = new Quat4f();
		result.mul(rotation, viewQuat);
		
		
		
		result.mul(mTmpConjugated);
		
		
		
		//			result = rotation.getMultiplicationBy(viewQuat).getMultiplicationBy(mTmpConjugated);
		tForwardVector.x = result.x;
		tForwardVector.y = result.y;
		tForwardVector.z = result.z;

		tForwardVector.normalize();

		//find "rotated" target vector
		//			targetVector = Vector3f.add(position, view);

		//we apply the rotation to the up vector
		Quat4f upQuat = new Quat4f(camera.getUp().x, camera.getUp().y, camera.getUp().z, 0);
		result.mul(rotation, upQuat);
		result.mul(mTmpConjugated);
		//			result = rotation.getMultiplicationBy(upQuat).getMultiplicationBy(mTmpConjugated);
		tUpVector.x = result.x;
		tUpVector.y = result.y;
		tUpVector.z = result.z;

		tUpVector.normalize();

		//we apply the rotation to the up vector
		Quat4f rightQuat = new Quat4f(camera.getRight().x, camera.getRight().y, camera.getRight().z, 0);
		result.mul(rotation, rightQuat);
		result.mul(mTmpConjugated);
		//					result = rotation.getMultiplicationBy(upQuat).getMultiplicationBy(mTmpConjugated);
		tRightVector.x = result.x;
		tRightVector.y = result.y;
		tRightVector.z = result.z;

		tRightVector.normalize();

		//finished
		if (limitS(tForwardVector, tUpVector, tRightVector, transformable.getWorldTransform())) {
			camera.setForward(tForwardVector);
			camera.setUp(tUpVector);
			camera.setRight(tRightVector);
		}
	}

	/**
	 * @return the following
	 */
	public Transform getFollowing() {
		return following;
	}

	/**
	 * @return the orientation
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	private void getVectors(Vector3f tForwardVector, Vector3f tUpVector, Vector3f tRightVector, Transform from) {
		switch(orientation) {
			case Element.FRONT -> {
				GlUtil.getForwardVector(tForwardVector, from);
				GlUtil.getUpVector(tUpVector, from);
				GlUtil.getRightVector(tRightVector, from);
			}
			case Element.BACK -> {
				GlUtil.getForwardVector(tForwardVector, from);
				GlUtil.getUpVector(tUpVector, from);
				GlUtil.getRightVector(tRightVector, from);
				tForwardVector.negate();
				tRightVector.negate();
			}
			case Element.LEFT -> {
				GlUtil.getRightVector(tForwardVector, from);
				GlUtil.getUpVector(tUpVector, from);
				GlUtil.getForwardVector(tRightVector, from);
			}
			case Element.RIGHT -> {
				GlUtil.getLeftVector(tForwardVector, from);
				GlUtil.getUpVector(tUpVector, from);
				GlUtil.getForwardVector(tRightVector, from);
			}
			case Element.TOP -> {
				GlUtil.getUpVector(tForwardVector, from);
				GlUtil.getRightVector(tUpVector, from);
				GlUtil.getForwardVector(tRightVector, from);
			}
			case Element.BOTTOM -> {
				GlUtil.getBottomVector(tForwardVector, from);
				GlUtil.getLeftVector(tUpVector, from);
				GlUtil.getForwardVector(tRightVector, from);
			}
		}

	}

	/**
	 * @return the correcting
	 */
	public boolean isCorrecting() {
		return correcting;
	}

	/**
	 * @param correcting the correcting to set
	 */
	public void setCorrecting(boolean correcting) {
		this.correcting = correcting;
	}

	private boolean limitS(Vector3f forward, Vector3f upVector, Vector3f rightVector, Transform to) {
		Vector3f tForwardVector = new Vector3f();
		Vector3f tUpVector = new Vector3f();
		Vector3f tRightVector = new Vector3f();
		getVectors(tForwardVector, tUpVector, tRightVector, to);

		Vector3f sForw = new Vector3f(tForwardVector);
		forward.normalize();
		rightVector.normalize();

		float angle = sForw.angle(forward);

		if (angle < limit) {
			return true;
		} else {
			mouse.scale(0.5f);
			if (iterations < 10) {
				iterations++;
				freeRot();
			}
		}
		return false;

	}

	/**
	 * dx, dy are between [-1.0d, 1.0d]
	 * x/ySensitivity are between [0.0d, 1.0d]
	 * <p/>
	 * Recalculates upVector and view Vectors for dx and dy mouse newRotation
	 */
	public void mouseRotate() {

		freeRot();

		if (correcting) {
			Transform s = new Transform(transformable.getWorldTransform());
			s.basis.sub(start.basis);
			Vector3f tForwardVector = new Vector3f();
			Vector3f tUpVector = new Vector3f();
			Vector3f tRightVector = new Vector3f();
			getVectors(tForwardVector, tUpVector, tRightVector, s);

			GlUtil.setForwardVector(tForwardVector, s);
			GlUtil.setUpVector(tUpVector, s);
			GlUtil.setRightVector(tRightVector, s);

			camera.getWorldTransform().basis.add(s.basis);
			start.set(transformable.getWorldTransform());

		}
		followingOld.set(camera.getWorldTransform());
	}

	public void reset() {
		following.setIdentity();
		followingOld.setIdentity();
		start.set(transformable.getWorldTransform());
	}

}
