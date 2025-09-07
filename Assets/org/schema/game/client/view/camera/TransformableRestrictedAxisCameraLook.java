package org.schema.game.client.view.camera;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rails.RailController;
import org.schema.game.common.data.element.Element;
import org.schema.schine.graphicsengine.camera.look.MouseLookAlgorithm;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.input.KeyboardMappings;

import com.bulletphysics.linearmath.Transform;

public class TransformableRestrictedAxisCameraLook implements MouseLookAlgorithm {

	private final Transform start = new Transform();
	private final Transform following = new Transform();
	public Transformable correcting2Transformable;
	public boolean lastCollision;
	//	private Vector2f mouseSum = new Vector2f();
	private Transformable camera;
	private Vector3f vCross = new Vector3f();
	private Vector3f axis = new Vector3f();
	private Vector3f mouse = new Vector3f();
	private int orientation = Element.FRONT;
	private Transformable transformable;
	private boolean needsInit = true;
	private Quat4f result = new Quat4f();
	private Quat4f rotMulView = new Quat4f();
	private Quat4f rotConj = new Quat4f();
	private Quat4f newRotation = new Quat4f();
	//	private Quat4f totalRotation = new Quat4f(0,0,0,1);
	private Quat4f tmpQuat = new Quat4f();
	private Vector3f mouseSum = new Vector3f();
	private Transform rotatedCamera = new Transform();
	private Transform rotOnlyMouseY = new Transform();
	private Transform rotOnlyMouseX = new Transform();
	private Transform rotatedCameraBefore = new Transform();
	private Transform rotOnlyMouseYBefore = new Transform();
	private Transform rotOnlyMouseXBefore = new Transform();
	
	private int maxIterations = 10;
	private int iterationX;
	private int iterationY;
	private int iterationAlt;
	private boolean didXRotationSuccessfully;
	private float scaleDownPerIteration = 0.5f;

	public TransformableRestrictedAxisCameraLook(Transformable camera, Transformable t) {
		this.camera = camera;
		this.transformable = t;
		following.setIdentity();
		rotOnlyMouseX.setIdentity();
		rotOnlyMouseY.setIdentity();
		rotatedCamera.setIdentity();
//		followingOld.setIdentity();
		start.set(t.getWorldTransform());
	}

	@Override
	public void fix() {

	}

	@Override
	public void force(Transform t) {
		following.set(t);
		start.set(this.transformable.getWorldTransform());
	}

	@Override
	public void mouseRotate(boolean server, float dx, float dy, float dz, float xSensitivity, float ySensitivity, float zSensitivity) {
		lastCollision = false;
		mouse.x = -dx * xSensitivity;
		mouse.y = dy * ySensitivity;
		mouse.z = dz * zSensitivity;
		iterationAlt = 0;
		iterationX = 0;
		iterationY = 0;

		didXRotationSuccessfully = false;
		mouseRotate(server);
	}

	@Override
	public void lookTo(Transform n) {
		
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
			needsInit = true;
		}
		this.orientation = orientation;
	}

	private void freeRotRail() {

		mouseSum.add(mouse);

		Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), rotatedCamera);
		Vector3f upVector = GlUtil.getUpVector(new Vector3f(), rotatedCamera);
		Vector3f rightVector = GlUtil.getRightVector(new Vector3f(), rotatedCamera);

		Vector3f forwardVectorY = GlUtil.getForwardVector(new Vector3f(), rotOnlyMouseY);
		Vector3f upVectorY = GlUtil.getUpVector(new Vector3f(), rotOnlyMouseY);
		Vector3f rightVectorY = GlUtil.getRightVector(new Vector3f(), rotOnlyMouseY);

		Vector3f forwardVectorX = GlUtil.getForwardVector(new Vector3f(), rotOnlyMouseX);
		Vector3f upVectorX = GlUtil.getUpVector(new Vector3f(), rotOnlyMouseX);
		Vector3f rightVectorX = GlUtil.getRightVector(new Vector3f(), rotOnlyMouseX);

		//		System.err.println("CUI:::: "+upVector);

		rotateY(forwardVectorY, upVectorY, rightVectorY, rotOnlyMouseY);
		GlUtil.setForwardVector(forwardVectorY, rotOnlyMouseY);
		GlUtil.setRightVector(rightVectorY, rotOnlyMouseY);
		GlUtil.setUpVector(upVectorY, rotOnlyMouseY);

		rotateX(forwardVectorX, upVectorX, rightVectorX);
		GlUtil.setForwardVector(forwardVectorX, rotOnlyMouseX);
		GlUtil.setRightVector(rightVectorX, rotOnlyMouseX);
		GlUtil.setUpVector(upVectorX, rotOnlyMouseX);

		rotateX(forwardVector, upVector, rightVector);
		rotateY(forwardVector, upVector, rightVector, rotatedCamera);

		GlUtil.setForwardVector(forwardVector, rotatedCamera);
		GlUtil.setRightVector(rightVector, rotatedCamera);
		GlUtil.setUpVector(upVector, rotatedCamera);

	}

	/**
	 * dx, dy are between [-1.0d, 1.0d]
	 * x/ySensitivity are between [0.0d, 1.0d]
	 * <p/>
	 * Recalculates upVector and view Vectors for dx and dy mouse newRotation
	 *
	 * @param restrict
	 */
	public void mouseRotate(boolean server) {

		boolean isInitialDocking = false;
		if (needsInit) {
			needsInit = false;

			rotatedCamera.setIdentity();
			rotOnlyMouseX.setIdentity();
			rotOnlyMouseY.setIdentity();

			if (correcting2Transformable != null) {
				if (((SegmentController) transformable).railController.isDocked()) {

//					System.err.println("RESET TRANSFORM CAMERA:\n"+((SegmentController)transformable).railController.getRailMovingLocalTransform().basis);

					rotatedCamera.set(((SegmentController) transformable).railController.getRailMovingLocalTransformTarget());

					/*
					 * skip collision checking in the initial update for this camera
					 * as that would cause the object to possibly get stuck
					 * when the camera is still initializing
					 */
					isInitialDocking = rotatedCamera.equals(TransformTools.ident);

					if (((SegmentController) transformable).railController.previous.getRailRController().isDockedAndExecuted() &&
							((SegmentController) transformable).railController.previous.getRailRController().previous.isRailTurretYAxis() &&
							((SegmentController) transformable).railController.previous.isRailTurretXAxis()) {

						rotOnlyMouseX.set(((SegmentController) transformable).railController.previous.getRailRController().getRailMovingLocalTransformTarget());
						rotOnlyMouseY.set(((SegmentController) transformable).railController.getRailMovingLocalTransformTarget());

					} else if (((SegmentController) transformable).railController.previous.getRailRController().isDockedAndExecuted() &&
							((SegmentController) transformable).railController.previous.getRailRController().previous.isRailTurretXAxis() &&
							((SegmentController) transformable).railController.previous.isRailTurretYAxis()) {

						rotOnlyMouseY.set(((SegmentController) transformable).railController.previous.getRailRController().getRailMovingLocalTransformTarget());
						rotOnlyMouseX.set(((SegmentController) transformable).railController.getRailMovingLocalTransformTarget());

					} else {
						if (((SegmentController) transformable).railController.previous.isTurretDock() && ((SegmentController) transformable).railController.previous.isRailTurretXAxis()) {
//							rotOnlyMouseY.set(mat);
							rotOnlyMouseY.set(((SegmentController) transformable).railController.getRailMovingLocalTransformTarget());
						} else if (((SegmentController) transformable).railController.previous.isTurretDock() && ((SegmentController) transformable).railController.previous.isRailTurretYAxis()) {
//							rotOnlyMouseX
							rotOnlyMouseX.set(((SegmentController) transformable).railController.getRailMovingLocalTransformTarget());
						}
					}
				}

			}
			//end of init!
		}
		rotatedCameraBefore.set(rotatedCamera);
		rotOnlyMouseXBefore.set(rotOnlyMouseX);
		rotOnlyMouseYBefore.set(rotOnlyMouseY);

		if (correcting2Transformable != null
				&& ((SegmentController) transformable).railController.isDockedAndExecuted() &&
				((SegmentController) transformable).railController.isTurretDocked()) {
			freeRotRail();
		} else {
//			System.err.println("NOT TURRET DOCKED");
			rotatedCamera.setIdentity();
			rotOnlyMouseX.setIdentity();
			rotOnlyMouseY.setIdentity();
		}

		if (correcting2Transformable != null) {

			if (!server && KeyboardMappings.ALIGN_SHIP.isDown()) {

				Transform motherShipTranform = new Transform(((SegmentController) transformable).railController.getRoot().getWorldTransform());
				motherShipTranform.mul(((SegmentController) transformable).railController.getRailUpToThisOriginalLocalTransform());

				camera.getWorldTransform().basis.set(motherShipTranform.basis);

				if (((SegmentController) transformable).railController.previous.getRailRController().isDockedAndExecuted() &&
						((SegmentController) transformable).railController.previous.getRailRController().previous.isTurretDock()) {
					Transform motherShipTranformPrev = new Transform(((SegmentController) transformable).railController.getRoot().getWorldTransform());
					motherShipTranformPrev.mul(((SegmentController) transformable).railController.previous.getRailRController().getRailUpToThisOriginalLocalTransform());
					((SegmentController) transformable).railController.previous.getRailRController().turretRotX = motherShipTranformPrev;
				}
				rotatedCamera.setIdentity();
				rotOnlyMouseX.setIdentity();
				rotOnlyMouseY.setIdentity();
			} else if (((SegmentController) transformable).railController.previous.getRailRController().isDockedAndExecuted() &&
					((SegmentController) transformable).railController.previous.getRailRController().previous.isRailTurretYAxis() &&
					RailController.checkTurretBaseModifiable(((SegmentController) transformable), ((SegmentController) transformable).railController.previous.rail.getSegmentController()) &&
					((SegmentController) transformable).railController.previous.isRailTurretXAxis()) {
				//tip of turret moves up/down
				boolean verticalTip = true;
				rotBothAxis(isInitialDocking, rotOnlyMouseX, rotOnlyMouseY, verticalTip);

			} else if (((SegmentController) transformable).railController.previous.getRailRController().isDockedAndExecuted() &&
					((SegmentController) transformable).railController.previous.getRailRController().previous.isRailTurretXAxis() &&
					RailController.checkTurretBaseModifiable(((SegmentController) transformable), ((SegmentController) transformable).railController.previous.rail.getSegmentController()) &&
					((SegmentController) transformable).railController.previous.isRailTurretYAxis()) {
				
//				if(!isOnServer()) {
//					System.err.println("BOOOO;; "+isInitialDocking+" ;; Y\n"+rotOnlyMouseY.basis+"X\n"+rotOnlyMouseX.basis);
//				}
				//tip of turret moves left/right
				boolean verticalTip = false;
				
				rotBothAxis(isInitialDocking, rotOnlyMouseY, rotOnlyMouseX, verticalTip);

			} else {
				Transform motherShipTranform = new Transform(((SegmentController) transformable).railController.getRoot().getWorldTransform());
				motherShipTranform.mul(((SegmentController) transformable).railController.getRailUpToThisOriginalLocalTransform());

				if (((SegmentController) transformable).railController.previous.isTurretDock() && ((SegmentController) transformable).railController.previous.isRailTurretXAxis()) {
					motherShipTranform.basis.mul(rotOnlyMouseY.basis);
				} else if (((SegmentController) transformable).railController.previous.isTurretDock() && ((SegmentController) transformable).railController.previous.isRailTurretYAxis()) {
					motherShipTranform.basis.mul(rotOnlyMouseX.basis);
				}

				boolean collides = ((SegmentController) transformable).railController.checkChildCollision(motherShipTranform);

				if (!isInitialDocking && collides) {
					lastCollision = true;
					//revert calculated trasformation of this update
					//and try again with half of the distance
					rotatedCamera.set(rotatedCameraBefore);
					rotOnlyMouseX.set(rotOnlyMouseXBefore);
					rotOnlyMouseY.set(rotOnlyMouseYBefore);

					iterationAlt++;

					if (iterationAlt < maxIterations) {
						mouse.scale(scaleDownPerIteration);
//						System.err.println("SCALE::: "+mouse);
						mouseRotate(server);
					}
				} else {

					camera.getWorldTransform().basis.set(motherShipTranform.basis);
					if(((SegmentController) transformable).isOnServer() || ((SegmentController) transformable).getRemoteTransformable().isSendFromClient()){
						((SegmentController) transformable).railController.turretRotYHelp = motherShipTranform.basis;
					}
				}
			}

		} else {
			assert (false);

		}
	}

	public boolean isOnServer() {
		return ((SegmentController) transformable).isOnServer();
	}

	private void rotBothAxis(boolean isInitialDocking, Transform actRotOnlyMouseX, Transform actRotOnlyMouseY, boolean verticalTip) {

		if (!didXRotationSuccessfully) {
			Transform turretBase = new Transform(((SegmentController) transformable).railController.getRoot().getWorldTransform());
			turretBase.mul(((SegmentController) transformable).railController.previous.getRailRController().getRailUpToThisOriginalLocalTransform());
			turretBase.basis.mul(actRotOnlyMouseX.basis);
			//check if a rotation attempt for the Mouse-X has actually been made
			//else a touching collision could prevent the other axis from moving
			//essentially getting the object stuck
			boolean collides;
			
			if(verticalTip) {
				//horizontal base
				collides = !rotOnlyMouseX.equals(rotOnlyMouseXBefore) &&
					((SegmentController) transformable).railController.previous.getRailRController().checkChildCollision(turretBase);
			}else {
				//vertical base
				collides = !rotOnlyMouseY.equals(rotOnlyMouseYBefore) &&
						((SegmentController) transformable).railController.previous.getRailRController().checkChildCollision(turretBase);
			}
			if (!isInitialDocking && collides) {
//				if(!isOnServer()) {
//					System.err.println("Y COL "+rotOnlyMouseX.equals(rotOnlyMouseXBefore)+"; "+rotOnlyMouseY.equals(rotOnlyMouseYBefore) );
//				}
				lastCollision = true;
				//revert calculated trasformation of this update
				//and try again with half of the distance
				if(verticalTip) {
					rotOnlyMouseX.set(rotOnlyMouseXBefore);
				}else {
					rotOnlyMouseY.set(rotOnlyMouseYBefore);
				}

				if (iterationX < maxIterations) {
					iterationX++;
					//revert calculated trasformation of this update
					//and try again with half of the distance
					rotatedCamera.set(rotatedCameraBefore);
					if(verticalTip) {
						rotOnlyMouseY.set(rotOnlyMouseYBefore);
					}else {
						rotOnlyMouseX.set(rotOnlyMouseXBefore);
					}
					if(verticalTip) {
						mouse.x *= scaleDownPerIteration;
					}else {
						mouse.y *= scaleDownPerIteration;
					}
					mouseRotate(((SegmentController) transformable).isOnServer());

					return;
				}

			} else {
				didXRotationSuccessfully = true;
				if(verticalTip) {
					rotOnlyMouseXBefore.set(rotOnlyMouseX);
				}else {
					rotOnlyMouseYBefore.set(rotOnlyMouseY);
				}
				((SegmentController) transformable).railController.previous.getRailRController().turretRotX = turretBase;
			}
		}

		Transform turretTip;

		turretTip = new Transform(((SegmentController) transformable).railController.getRoot().getWorldTransform());
		turretTip.mul(((SegmentController) transformable).railController.getRailUpToThisOriginalLocalTransform());

		turretTip.basis.mul(actRotOnlyMouseY.basis);

		
		boolean collidesY = ((SegmentController) transformable).railController.checkChildCollision(turretTip);

		if (!isInitialDocking && collidesY) {
			
			lastCollision = true;

			//revert calculated trasformation of this update
			//and try again with half of the distance
			rotatedCamera.set(rotatedCameraBefore);

			rotOnlyMouseX.set(rotOnlyMouseXBefore);
			rotOnlyMouseY.set(rotOnlyMouseYBefore);

			if (iterationY < maxIterations && ((verticalTip && mouse.y > 0) || (!verticalTip && mouse.x > 0))) {
				iterationY++;
				if(verticalTip) {
					mouse.y *= scaleDownPerIteration;
				}else {
					mouse.x *= scaleDownPerIteration;
				}
				mouseRotate(isOnServer());
			} else {
				if (didXRotationSuccessfully) {
					
					//set the rotation to what the turret currently is, as it moved its base
					turretTip = new Transform(((SegmentController) transformable).railController.getRoot().getWorldTransform());
					turretTip.mul(((SegmentController) transformable).railController.getRailUpToThisOriginalLocalTransform());
					
					if(verticalTip) {
						turretTip.basis.mul(rotOnlyMouseY.basis);
					}else {
						turretTip.basis.mul(rotOnlyMouseX.basis);
					}
					
					
					camera.getWorldTransform().basis.set(turretTip.basis);

					if(((SegmentController) transformable).isOnServer() || ((SegmentController) transformable).getRemoteTransformable().isSendFromClient()){
						((SegmentController) transformable).railController.turretRotYHelp = turretTip.basis;
					}
				}
			}
			return;
		} else {
			
			camera.getWorldTransform().basis.set(turretTip.basis);
			
			if(((SegmentController) transformable).isOnServer() || ((SegmentController) transformable).getRemoteTransformable().isSendFromClient()){
				((SegmentController) transformable).railController.turretRotYHelp = turretTip.basis;
			}
		}
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

	}

	private void rotateX(Vector3f forwardVector, Vector3f upVector, Vector3f rightVector) {

		if (mouse.x != 0) {
			Vector3f yAxis = new Vector3f(0, 1, 0);
			//			 Rotate around the x axis
			rotateCamera(mouse.x, yAxis, forwardVector, upVector, rightVector);

		}
	}

	private void rotateY(Vector3f forwardVector, Vector3f upVector, Vector3f rightVector, Transform rotatedCamera) {

		if (mouse.y != 0) {
			//Mouse-Y Rotation, newRotation on our view vector x up vector
//			vCross.cross(
//					GlUtil.getForwardVector(new Vector3f(), rotatedCamera),
//					GlUtil.getUpVector(new Vector3f(), rotatedCamera) );
			vCross.cross(
					GlUtil.getForwardVector(new Vector3f(), rotatedCamera),
					GlUtil.getUpVector(new Vector3f(), rotatedCamera));

			axis.set(vCross);
			axis.normalize();
			//			System.err.println("ROTATION: "+Element.getSideString(orientation));
			rotateCamera(mouse.y, axis, forwardVector, upVector, rightVector);

		}
	}

	/**
	 * @param correcting the correcting to set
	 */
	public void setCorrectingForNonCoreEntry(boolean correcting) {
//		this.correctingForNonCoreEntry = correcting;
	}

	/**
	 * @param correcting the correcting to set
	 */
	public void setCorrecting2Transformable(Transformable correcting) {
		if (correcting != null && correcting != this.correcting2Transformable) {
			start.set(correcting.getWorldTransform());
			this.correcting2Transformable = correcting;
			//			followingOld.set(getCorrectingTransform());//set(camera.getWorldTransform());
			needsInit = true;

		} else {
			this.correcting2Transformable = correcting;
		}

	}

	public Vector3f getForward(Vector3f out) {
				return null;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}


	public void setScaleDownPerIteration(float scaleDownPerIteration) {
		this.scaleDownPerIteration = scaleDownPerIteration;
	}

}
