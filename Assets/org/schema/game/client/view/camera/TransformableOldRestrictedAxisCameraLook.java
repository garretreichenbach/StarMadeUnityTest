package org.schema.game.client.view.camera;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.game.common.controller.DockingController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.schine.graphicsengine.camera.look.MouseLookAlgorithm;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;

import com.bulletphysics.linearmath.Transform;

public class TransformableOldRestrictedAxisCameraLook implements MouseLookAlgorithm {

	private final Transform start = new Transform();
	private final Transform following = new Transform();
	private final Transform followingOld = new Transform();
	private final Transform before = new Transform();
	//	private Vector2f mouseSum = new Vector2f();
	public Transformable correcting2Transformable;
	float limit = FastMath.HALF_PI / 2f;
	private Transformable camera;
	private Vector3f vCross = new Vector3f();
	private Vector3f axis = new Vector3f();
	private Vector3f mouse = new Vector3f();
	private boolean correctingForNonCoreEntry;
	private int orientation = Element.FRONT;
	private Transformable transformable;
	//	private void limit(Vector3f forward, Vector3f upVector, Vector3f rightVector){
	//		Vector3f sForw = GlUtil.getForwardVector(new Vector3f(), transformable.getWorldTransform());
	//		Vector3f cforw = new Vector3f(forward);
	//
	//
	//
	//		sForw.y = 0;
	//		sForw.normalize();
	//		cforw.y = 0;
	//		cforw.normalize();
	//
	//		float angleX = sForw.angle(cforw);
	//
	//
	//
	//
	//		sForw = GlUtil.getForwardVector(new Vector3f(), transformable.getWorldTransform());
	//		cforw = new Vector3f(forward);
	//
	//		sForw.x = 0;
	//		sForw.normalize();
	//		cforw.x = 0;
	//		cforw.normalize();
	//
	//		float angleY = sForw.angle(cforw);
	//		if(angleY < limit && angleX < limit){
	//			camera.setForward(forward);
	//			camera.setRight(rightVector);
	//			camera.setUp(upVector);
	//
	//		}else{
	//			System.err.println("LIMIT");
	//			System.err.println("X ----------> "+angleX);
	//			System.err.println("Y ----------> "+angleY);
	//		}
	//	}
	private int iterations = 0;
	private boolean needsInit = true;
	private Quat4f result = new Quat4f();
	private Quat4f rotMulView = new Quat4f();
	private Quat4f rotConj = new Quat4f();
	private Quat4f newRotation = new Quat4f();
	private Quat4f totalRotation = new Quat4f(0, 0, 0, 1);
	private Quat4f tmpQuat = new Quat4f();
	private Vector3f mouseSum = new Vector3f();

	public TransformableOldRestrictedAxisCameraLook(Transformable camera, Transformable t) {
		this.camera = camera;
		this.transformable = t;
		following.setIdentity();
		followingOld.setIdentity();
		start.set(t.getWorldTransform());
	}

	@Override
	public void fix() {

	}

	@Override
	public void force(Transform t) {
		following.set(t);
		followingOld.setIdentity();
		start.set(this.transformable.getWorldTransform());
	}

	@Override
	public void mouseRotate(boolean server, float dx, float dy, float dz, float xSensitivity, float ySensitivity, float zSensitivity) {
		before.set(camera.getWorldTransform());
		iterations = 0;
		mouse.x = -dx * xSensitivity;
		mouse.y = dy * ySensitivity;
		mouse.z = dz * zSensitivity;
		mouseRotate(true);
	}

	@Override
	public void lookTo(Transform n) {
		
	}

	private float getCurentYaw() {

		Quat4f b = new Quat4f();
		Quat4fTools.set(camera.getWorldTransform().basis, b);

		//		Quat4f a = new Quat4f();

		//		if(startTransform != null){
		//			//subtract basic orientation to get raw yaw
		//			a.set(startBasicTransform);
		//			a.inverse();
		//			b.mul(a);
		//		}
		float yaw;

		yaw = Quat4fTools.getYaw(b);
		Vector3f rightVectorR = GlUtil.getRightVector(new Vector3f(), camera.getWorldTransform());
		Vector3f upVectorR = GlUtil.getUpVector(new Vector3f(), camera.getWorldTransform());
		Vector3f forwardVectorR = GlUtil.getForwardVector(new Vector3f(), camera.getWorldTransform());

		if (yaw < 0 && forwardVectorR.dot(new Vector3f(0, 0, 1)) < 0) {
			yaw += Math.PI;
		} else if (yaw > 0 && forwardVectorR.dot(new Vector3f(0, 0, 1)) < 0) {
			yaw -= Math.PI;
		}
		return yaw;

	}

	private void freeRot(boolean restrict) {

		//		System.err.println("ORIENTATION: "+Element.getSideString(orientation));
		//		Vector3f tForwardVector = new Vector3f();
		//
		//		Vector3f tUpVector = new Vector3f();
		//
		//		Vector3f tRightVector = new Vector3f();
		//		if(correcting2Transformable == null){
		//			getVectors(tForwardVector, tUpVector, tRightVector, transformable.getWorldTransform());
		////			tForwardVector.set(0,0,1);
		////			tUpVector.set(0,1,0);
		////			tRightVector.set(1,0,0);
		//		}else{
		//			getVectors(tForwardVector, tUpVector, tRightVector, ((SegmentController)transformable).getPhysicsDataContainer().getShapeChild().transform);
		////			tForwardVector.set(0,0,1);
		////			tUpVector.set(0,1,0);
		////			tRightVector.set(1,0,0);
		//		}

		float oldYaw = getCurentYaw();

		float posLimit = -FastMath.HALF_PI / 2; //FastMath.HALF_PI
		float negLimit = FastMath.HALF_PI;

		//		if(restrict){
		//
		//
		//
		//
		//
		switch (orientation) {
			case Element.TOP:
				break;
			case Element.BOTTOM:
				break;
			case Element.FRONT:
				mouse.x = -mouse.x;
				break;
			case Element.BACK:
				break;

			case Element.RIGHT:
				mouse.x = -mouse.x;
				break;
			case Element.LEFT:
				mouse.x = -mouse.x;
				break;
		}
		mouseSum.add(mouse);

		//		if(restrict){
		//			if (mouseSum.y > posLimit) {
		//				mouseSum.y -= mouse.y;
		//
		//				mouse.y = posLimit - mouseSum.y;
		//				mouseSum.y = posLimit;
		//			}
		//
		//
		//			// We don't want to rotate down more than one radian, so we cap it.
		//			if (mouseSum.y < -negLimit) {
		//				mouseSum.y -= mouse.y;
		//
		//				mouse.y =  -(negLimit - Math.abs(mouseSum.y));
		//				mouseSum.y = -negLimit;
		//			}
		//		}
		Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), camera.getWorldTransform()); //Vector3fTools.subtract(targetVector, position);
		Vector3f upVector = GlUtil.getUpVector(new Vector3f(), camera.getWorldTransform());  //Vector3fTools.subtract(targetVector, position);
		Vector3f rightVector = GlUtil.getRightVector(new Vector3f(), camera.getWorldTransform());  //Vector3fTools.subtract(targetVector, position);

		rotateY(forwardVector, upVector, rightVector);
		rotateX(forwardVector, upVector, rightVector);

		//		System.err.println("YAW: "+yaw);

		//finished

		if (correcting2Transformable == null) {
			limitS(forwardVector, upVector, rightVector, transformable.getWorldTransform());
		} else {
			GlUtil.setForwardVector(forwardVector, camera.getWorldTransform());
			GlUtil.setRightVector(rightVector, camera.getWorldTransform());
			GlUtil.setUpVector(upVector, camera.getWorldTransform());
		}

		Quat4f cur = new Quat4f();
		Quat4fTools.set(camera.getWorldTransform().basis, cur);
		float yaw = getCurentYaw();

		//		System.err.println("YAW: "+yaw+";     "+cur);
		float minYaw = -FastMath.HALF_PI / 2;
		float maxYaw = FastMath.HALF_PI / 4;

		if ((yaw < oldYaw && yaw < minYaw) || (yaw > oldYaw && yaw > maxYaw)) {
			mouse.y = -mouse.y;

			forwardVector = GlUtil.getForwardVector(new Vector3f(), camera.getWorldTransform()); //Vector3fTools.subtract(targetVector, position);
			upVector = GlUtil.getUpVector(new Vector3f(), camera.getWorldTransform());  //Vector3fTools.subtract(targetVector, position);
			rightVector = GlUtil.getRightVector(new Vector3f(), camera.getWorldTransform());  //Vector3fTools.subtract(targetVector, position);

			rotateY(forwardVector, upVector, rightVector);

			GlUtil.setForwardVector(forwardVector, camera.getWorldTransform());
			GlUtil.setRightVector(rightVector, camera.getWorldTransform());
			GlUtil.setUpVector(upVector, camera.getWorldTransform());

			yaw = getCurentYaw();

			//			System.err.println("->YAW: "+yaw);

		} else if (yaw != oldYaw && (yaw < minYaw || yaw > maxYaw)) {
			//			System.err.println("NNNN: "+oldYaw+" -> "+yaw);
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
		if (orientation != this.orientation) {
			start.set(correcting2Transformable.getWorldTransform());
			//			followingOld.set(getCorrectingTransform());//set(camera.getWorldTransform());
			followingOld.setIdentity();
			needsInit = true;
		}
		this.orientation = orientation;
	}

	private void getVectors(Vector3f tForwardVector, Vector3f tUpVector, Vector3f tRightVector, Transform from) {
		//		System.err.println("ORIENTATION: "+Element.getSideString(orientation));
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
			case Element.RIGHT -> {
				GlUtil.getRightVector(tForwardVector, from);
				GlUtil.getBottomVector(tUpVector, from);
				GlUtil.getForwardVector(tRightVector, from);
			}
			case Element.LEFT -> {
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
	public boolean isCorrectingForNonCoreEntry() {
		return correctingForNonCoreEntry;
	}

	/**
	 * @param correcting the correcting to set
	 */
	public void setCorrectingForNonCoreEntry(boolean correcting) {
		this.correctingForNonCoreEntry = correcting;
	}

	private void limitS(Vector3f forward, Vector3f upVector, Vector3f rightVector, Transform to) {
		Vector3f tForwardVector = new Vector3f();
		Vector3f tUpVector = new Vector3f();
		Vector3f tRightVector = new Vector3f();
		getVectors(tForwardVector, tUpVector, tRightVector, to);

		Vector3f sForw = new Vector3f(tForwardVector);
		forward.normalize();
		rightVector.normalize();

		float angle = sForw.angle(forward);

		//		float angleZ = sRight.angle(rightVector);
		if (angle < limit) {
			GlUtil.setForwardVector(forward, camera.getWorldTransform());
			GlUtil.setRightVector(rightVector, camera.getWorldTransform());
			GlUtil.setUpVector(upVector, camera.getWorldTransform());

		} else {
			mouse.scale(0.5f);
			//			System.err.println("LIMIT");
			//
			//			System.err.println("A ----------> "+angle+" "+forward+" -- "+sForw+" --- "+f.length()+ "  / "+limit);
			//			System.err.println("B ----------> "+angleZ+" "+rightVector+" -- "+sRight+" --- "+r.length()+ "  / "+limit);
			if (iterations < 10) {
				camera.getWorldTransform().set(followingOld);
				freeRot(true);
			}
		}
	}

	/**
	 * dx, dy are between [-1.0d, 1.0d]
	 * x/ySensitivity are between [0.0d, 1.0d]
	 * <p/>
	 * Recalculates upVector and view Vectors for dx and dy mouse newRotation
	 *
	 * @param restrict
	 */
	public void mouseRotate(boolean restrict) {
		if (needsInit) {
			needsInit = false;
			followingOld.setIdentity();

			if (correcting2Transformable != null) {
				if (DockingController.isTurretDocking(((SegmentController) transformable).getDockingController().getDockedOn().to)) {
					Transform startTransform = new Transform(((SegmentController) transformable).getPhysicsDataContainer().getShapeChild().transform);

					//					Matrix3f r = new Matrix3f();
					//					r.set(startBasicTransform);
					//					r.invert();
					//					startTransform.basis.mul(r);

					if (((SegmentController) correcting2Transformable).getDockingController().isDocked()) {
						Transform d1 = new Transform(((SegmentController) correcting2Transformable).getPhysicsDataContainer().getShapeChild().transform);
						Transform d2 = new Transform(((SegmentController) correcting2Transformable).getDockingController()
								.getDockedOn().to.getSegment().getSegmentController().getPhysicsDataContainer().getShapeChild().transform);
						d1.inverse();
						d1.basis.mul(startTransform.basis);
						startTransform.basis.set(d1.basis);

						//						startTransform.basis.mul(d1.basis);
					}

					Transform d = new Transform();
					d.setIdentity();
					DockingController.getDockingTransformation((byte) orientation, d);
					//					d.basis.invert();

					d.basis.mul(startTransform.basis);
					startTransform.basis.set(d.basis);

					followingOld.set(startTransform);
					//
					//
					//					Vector3f rightVector = GlUtil.getRightVector(new Vector3f(), camera.getWorldTransform());
					//					Vector3f upVector = GlUtil.getUpVector(new Vector3f(), camera.getWorldTransform());
					//					Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), camera.getWorldTransform());
					//
					//
					//					Vector3f rightVectorTo = GlUtil.getRightVector(new Vector3f(), startTransform);
					//					Vector3f upVectorTo = GlUtil.getUpVector(new Vector3f(), startTransform);
					//					Vector3f forwardVectorTo = GlUtil.getForwardVector(new Vector3f(), startTransform);
					//
					//					Quat4f a = new Quat4f();
					//					Quat4f b = new Quat4f();
					//
					//					Quat4fTools.set(camera.getWorldTransform().basis, a);
					//					Quat4fTools.set(startTransform.basis, b);
					//					Quat4f relative = new Quat4f();
					//
					////					a.inverse();
					////					a.mul(b);
					//
					//
					//
					//					float rollA = Quat4fTools.getRoll(a);//(float) Math.atan2(2*a.y*a.w - 2*a.x*a.z, 1 - 2*a.y*a.y - 2*a.z*a.z);
					//					float pitchA = Quat4fTools.getPitch(a);//(float) Math.atan2(2*a.x*a.w - 2*a.y*a.z, 1 - 2*a.x*a.x - 2*a.z*a.z);
					//					float yawA = Quat4fTools.getYaw(a);//(float) Math.asin(2*a.x*a.y + 2*a.z*a.w);
					//
					//				   float roll = Quat4fTools.getRoll(b);//(float) Math.atan2(2*a.y*a.w - 2*a.x*a.z, 1 - 2*a.y*a.y - 2*a.z*a.z);
					//				   float pitch = Quat4fTools.getPitch(b);//(float) Math.atan2(2*a.x*a.w - 2*a.y*a.z, 1 - 2*a.x*a.x - 2*a.z*a.z);
					//				   float yaw = Quat4fTools.getYaw(b);//(float) Math.asin(2*a.x*a.y + 2*a.z*a.w);
					//
					//				   System.err.println("!!!!!!!!PITCH "+pitchA+"; "+yawA+"; "+rollA+" ------> "+mouseSum.x%FastMath.TWO_PI+"; "+mouseSum.y%FastMath.TWO_PI+"         "+a);
					//
					//
					//				   Vector3f bef = new Vector3f(mouse);
					//				   mouseSum.x = pitch;
					//				   mouseSum.y = yaw;
					//				   mouseSum.z = roll;//roll;
					//
					//				   mouseRotate(false);
					//				   mouse.set(bef);
				}

			}

		}

		//this is the camera without the added transform of the mothership
		camera.getWorldTransform().set(followingOld);

		freeRot(restrict);

		if (correctingForNonCoreEntry) {
			System.err.println("[CLIENT][CAMERAALGO] correcting for non core entry (not entered at core)");
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

		//this is the camera without the added transform of the mothership
		followingOld.set(camera.getWorldTransform());

		if (correcting2Transformable == null) {

		} else {
			//			if(orientation == Element.RIGHT || orientation == Element.LEFT){
			//				Vector3f tForwardVector = new Vector3f();
			//				Vector3f tUpVector = new Vector3f();
			//				Vector3f tRightVector = new Vector3f();
			//				getVectors(tForwardVector, tUpVector, tRightVector, camera.getWorldTransform());
			//
			//				GlUtil.setForwardVector(tForwardVector, camera.getWorldTransform());
			//				GlUtil.setUpVector(tUpVector, camera.getWorldTransform());
			//				GlUtil.setRightVector(tRightVector, camera.getWorldTransform());
			//			}

			orientate(camera.getWorldTransform().basis);

			Transform s = new Transform(((SegmentController) correcting2Transformable).getWorldTransform());

			if (((SegmentController) correcting2Transformable).getDockingController().isDocked()) {
				Transform d1 = new Transform(((SegmentController) correcting2Transformable).getPhysicsDataContainer().getShapeChild().transform);

				Transform d2 = new Transform(((SegmentController) correcting2Transformable).getDockingController()
						.getDockedOn().to.getSegment().getSegmentController().getPhysicsDataContainer().getShapeChild().transform);

				//				s.basis.mul(((SegmentController)correcting2Transformable).getDockingController()
				//						.getDockedOn().to.getSegment().getSegmentController().getWorldTransform().basis);
				//
				//				d1.inverse();
				s.basis.mul(d1.basis);

			} else {
				//no chain Docking
			}
			//add trans of the mother and set cam for current frame
			s.basis.mul(camera.getWorldTransform().basis);

			//			if(startBasicTransform != null){
			//				Matrix3f r = new Matrix3f();
			//				r.set(startBasicTransform);
			//				s.basis.mul(r);
			//			}
			//
			camera.getWorldTransform().basis.set(s.basis);

			start.set(correcting2Transformable.getWorldTransform());

		}
	}

	private void orientate(Matrix3f basis) {

		Transform from = new Transform();
		from.setIdentity();
		from.basis.set(basis);
		Vector3f tForwardVector = new Vector3f();
		Vector3f tUpVector = new Vector3f();
		Vector3f tRightVector = new Vector3f();

		GlUtil.getForwardVector(tForwardVector, from);
		GlUtil.getUpVector(tUpVector, from);
		GlUtil.getRightVector(tRightVector, from);

		Transform d = new Transform();
		d.setIdentity();
		DockingController.getDockingTransformation((byte) orientation, d);
		//		if(orientation == Element.FRONT){
		d.basis.invert();
		//		}
		d.basis.mul(basis);
		basis.set(d.basis);
		//		d.basis.invert();
		//		basis.mul(d.basis);

		//		switch(orientation){
		//		case Element.FRONT:
		//			GlUtil.setForwardVector(tUpVector, from);
		//			GlUtil.setUpVector(tRightVector, from);
		//			GlUtil.setRightVector(tForwardVector, from);
		//			break;
		//		case Element.BACK:
		//			GlUtil.setForwardVector(tForwardVector, from);
		//			GlUtil.setUpVector(tUpVector, from);
		//			GlUtil.setRightVector(tRightVector, from);
		//			tForwardVector.negate();
		//			tRightVector.negate();
		//			break;
		//		case Element.RIGHT:
		//			GlUtil.setRightVector(tForwardVector, from);
		//			GlUtil.setBottomVector(tUpVector, from);
		//			GlUtil.setForwardVector(tRightVector, from);
		//			break;
		//
		//		case Element.LEFT:
		//			GlUtil.setLeftVector(tForwardVector, from);
		//			GlUtil.setUpVector(tUpVector, from);
		//			GlUtil.setForwardVector(tRightVector, from);
		//
		//			break;
		//
		//		case Element.TOP:
		////			GlUtil.setUpVector(tForwardVector, from);
		////			GlUtil.setRightVector(tUpVector, from);
		////			GlUtil.setForwardVector(tRightVector, from);
		//			break;
		//		case Element.BOTTOM:
		//			GlUtil.setBottomVector(tForwardVector, from);
		//			GlUtil.setLeftVector(tUpVector, from);
		//			GlUtil.setForwardVector(tRightVector, from);
		//			break;
		//		}
		//
		//		basis.set(from.basis);

	}

	private void rotate(Vector3f v) {

		tmpQuat.set(v.x, v.y, v.z, 0);

		rotMulView.mul(newRotation, tmpQuat);

		result.mul(rotMulView, rotConj);

		v.set(result.x, result.y, result.z);
	}

	private void rotateCamera(float angle, Vector3f axis, Vector3f forward, Vector3f up, Vector3f right) {
		//		System.err.println("ANGLES: "+right+", "+up+", "+forward+"; Angle: "+angle);

		newRotation.x = axis.x * FastMath.sin(angle / 2);
		newRotation.y = axis.y * FastMath.sin(angle / 2);
		newRotation.z = axis.z * FastMath.sin(angle / 2);
		newRotation.w = FastMath.cos(angle / 2);

		newRotation.normalize();

		rotConj.conjugate(newRotation);

		rotate(forward);
		rotate(up);
		rotate(right);

		totalRotation.mul(newRotation);
	}

	private void rotateX(Vector3f forwardVector, Vector3f upVector, Vector3f rightVector) {
		Vector3f yAxis = new Vector3f(0, 1, 0);

		//		switch(orientation){
		//		case Element.TOP: 		yAxis.set(0, 1,0);break;
		//		case Element.BOTTOM: 	yAxis.set(0, -1,0);break;
		//
		//		case Element.FRONT: 	yAxis.set(0,0, 1);break;
		//		case Element.BACK: 		yAxis.set(0,0, -1);break;
		//
		//		case Element.RIGHT: 	yAxis.set(-1, 0,0);break;
		//		case Element.LEFT: 		yAxis.set(1,0,0);break;
		//		}
		//		Vector3f yAxis = GlUtil.getBackVector(new Vector3f(), correcting2Transformable.getWorldTransform());
		if (mouse.x != 0) {
			//			 Rotate around the x axis
			rotateCamera(mouse.x, yAxis, forwardVector, upVector, rightVector);

		}
	}

	private void rotateY(Vector3f forwardVector, Vector3f upVector, Vector3f rightVector) {

		if (mouse.y != 0) {
			//Mouse-Y Rotation, newRotation on our view vector x up vector
			vCross.cross(
					GlUtil.getForwardVector(new Vector3f(), camera.getWorldTransform()),
					GlUtil.getUpVector(new Vector3f(), camera.getWorldTransform()));
			//			if(orientation == Element.LEFT || orientation == Element.RIGHT){
			//				vCross.cross(camera.getRight(), camera.getUp());
			//			}

			axis.set(vCross);
			axis.normalize();
			//			System.err.println("ROTATION: "+Element.getSideString(orientation));
			rotateCamera(mouse.y, axis, forwardVector, upVector, rightVector);
			//			switch(orientation){
			//
			//			case Element.TOP:
			//				rotateCamera(mouse.y, axis, forwardVector, upVector, rightVector);
			//				break;
			//
			//			case Element.BOTTOM:
			//				rotateCamera(mouse.y, axis, forwardVector, upVector, rightVector);
			//				break;
			//
			//			case Element.FRONT:
			//				rotateCamera(mouse.y, axis, forwardVector, rightVector, upVector);
			//				break;
			//
			//			case Element.BACK:
			//				axis.negate();
			//				rotateCamera(mouse.y, axis, forwardVector, rightVector, upVector);
			//				break;
			//
			//
			//			case Element.RIGHT:
			//				axis.set(GlUtil.getUpVector(new Vector3f(), camera.getWorldTransform()));
			//								axis.negate();
			//				rotateCamera(mouse.y, axis, forwardVector, upVector, rightVector);
			//				break;
			//
			//			case Element.LEFT:
			//				axis.set(GlUtil.getUpVector(new Vector3f(), camera.getWorldTransform()));
			////								axis.negate();
			//				rotateCamera(mouse.y, axis, forwardVector, upVector, rightVector);
			//				break;
			//			}
		}
	}

	/**
	 * @param correcting the correcting to set
	 */
	public void setCorrecting2Transformable(Transformable correcting) {
		if (correcting != null && correcting != this.correcting2Transformable) {
			start.set(correcting.getWorldTransform());
			this.correcting2Transformable = correcting;
			//			followingOld.set(getCorrectingTransform());//set(camera.getWorldTransform());
			followingOld.setIdentity();
			needsInit = true;

		} else {
			this.correcting2Transformable = correcting;
		}

	}

}
