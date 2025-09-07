package org.schema.game.common.data.physics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class RigidBodySegmentController extends RigidBodyExt implements RelativeBody, GamePhysicsObject {

	private final SegmentController segmentController;
	private final Vector3f angularVeloTmp = new Vector3f();
	private final Vector3f linearVeloTmp = new Vector3f();
	private final Vector3f axis = new Vector3f();
	private final Matrix3f tmp = new Matrix3f();
	private final Matrix3f dmat = new Matrix3f();
	private final Quat4f dorn = new Quat4f();
	public String virtualString;
	public Vector3i virtualSec;
	public SegmentController undockingProtection;
	private boolean collisionException;
	private boolean changedShape;

	private int oldSize;
	private Vector3f tmpLin = new Vector3f();
	public boolean hadRecoil;
	private long lastRecoil;
	private long lastIntegrate;

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.RigidBody#saveKinematicState(float)
	 */
	public RigidBodySegmentController(SegmentController controller, float mass, MotionState motionState,
	                    CollisionShape collisionShape) {
		super(CollisionType.CUBE_STRUCTURE, mass, motionState, collisionShape);
		this.segmentController = controller;
		interpolationWorldTransform.setIdentity();
	}

	public RigidBodySegmentController(SegmentController controller, float mass, MotionState motionState,
	                    CollisionShape collisionShape, Vector3f localInertia) {
		super(CollisionType.CUBE_STRUCTURE, mass, motionState, collisionShape, localInertia);
		this.segmentController = controller;
		interpolationWorldTransform.setIdentity();
	}

	public RigidBodySegmentController(SegmentController controller, RigidBodyConstructionInfo constructionInfo) {
		super(CollisionType.CUBE_STRUCTURE, constructionInfo);
		this.segmentController = controller;
		interpolationWorldTransform.setIdentity();
	}

	@Override
	public void predictIntegratedTransform(float timeStep, Transform predictedTransform) {
//		System.err.println("0BBB "+this+"; "+predictedTransform.getMatrix(new Matrix4f()));
//		System.err.println("1BBB "+this+"; "+worldTransform.getMatrix(new Matrix4f()));
//		System.err.println("2BBB "+this+"; "+getLinearVelocity(new Vector3f()));
//		System.err.println("3BBB "+this+"; "+getAngularVelocity(new Vector3f()));
		super.predictIntegratedTransform(timeStep, predictedTransform);
	}

	/**
	 * @return the segmentController
	 */
	public SegmentController getSegmentController() {
		return segmentController;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String exp = "";
		if (collisionException) {
			exp = "[EXCEPT]";
		}
		if (getCollisionShape() instanceof CubeShape) {
			String sec = segmentController.getSectorId() + "";
			if (segmentController.isOnServer()) {
				Sector sector = ((GameServerState) segmentController.getState()).getUniverse().getSector(segmentController.getSectorId());
				if (sector != null) {
					sec = sector.toString();
				} else {
					sec = "null";
				}
			}
			return "{RigBEx" + (virtualString != null ? virtualString : "Orig") + "@" + hashCode() + segmentController + ";COLSHAPE:" + getCollisionShape() + ";AT" + getWorldTransform(new Transform()).origin + "(SID: " + sec + ")}" + exp;
		}
		return "{RigBEx" + (virtualString != null ? virtualString : "Orig") + "@" + hashCode() + "(" + getCollisionShape() + ")}" + exp;

	}



	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#setWorldTransform(com.bulletphysics.linearmath.Transform)
	 */

	public Sector getSectorOnServer() {
		assert (segmentController.isOnServer());
		Sector sector = ((GameServerState) segmentController.getState()).getUniverse().getSector(segmentController.getSectorId());
		return sector;
	}

	//	SegmentController c = getSegmentController();
	//	if(c instanceof ManagedSegmentController<?>){
	//		ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>)c).getManagerContainer();
	//		if(managerContainer instanceof ManagerThrustInterface){
	//			ManagerThrustInterface m = (ManagerThrustInterface)managerContainer;
	//			Transform playerTrans = m.getThrusterElementManager().getPlayerTrans(new Transform());
	//
	//			float angleP = TransformTools.calculateDiffAxisAngle(interpolationWorldTransform, playerTrans, axis, tmp, dmat, dorn);
	//			float angleO = TransformTools.calculateDiffAxisAngle(interpolationWorldTransform, worldTransform, axis, tmp, dmat, dorn);
	//
	//			System.err.println("ANGLES = PL "+angleP+"; ORI "+angleO);
	//		}
	//	}

	@Override
	public SimpleTransformableSendableObject getSimpleTransformableSendableObject() {
		return segmentController;
	}

	/**
	 * @return the collisionException
	 */
	public boolean isCollisionException() {
		return collisionException;
	}

	//
	//	/* (non-Javadoc)
	//	 * @see com.bulletphysics.collision.dispatch.CollisionObject#hasContactResponse()
	//	 */
	//	@Override
	//	public boolean hasContactResponse() {
	//		boolean a = virtualSec != null;
	//		assert(a == super.hasContactResponse()):this;
	//		return a;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.bulletphysics.collision.dispatch.CollisionObject#isStaticOrKinematicObject()
	//	 */
	//	@Override
	//	public boolean isStaticOrKinematicObject() {
	//		boolean a = segmentController.getMass() <= 0 || virtualSec != null;
	//		assert(a == super.isStaticOrKinematicObject()):segmentController+"; "+this;
	//		return a;
	//	}

	/**
	 * @param collisionException the collisionException to set
	 */
	public void setCollisionException(boolean collisionException) {
		this.collisionException = collisionException;
	}

	public boolean isChangedShape() {
		return changedShape;
	}

	/**
	 * @param changedShape the changedShape to set
	 */
	public void setChangedShape(boolean changedShape) {
		this.changedShape = changedShape;
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#setActivationState(int)
	 */
	@Override
	public void setActivationState(int newState) {
		if (segmentController.getTotalElements() != oldSize) {
			changedShape = true;
			oldSize = segmentController.getTotalElements();
		}
		super.setActivationState(newState);
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#activate(boolean)
	 */
	@Override
	public void activate(boolean forceActivation) {
		//		if(segmentController instanceof Ship){
		//		try{
		//			throw new NullPointerException(segmentController.toNiceString());
		//		}catch(Exception e){
		//			e.printStackTrace();
		//		}
		//		}
		super.activate(forceActivation);
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.RigidBody#getMotionState()
	 */
	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#setWorldTransform(com.bulletphysics.linearmath.Transform)
	 */
	@Override
	public void setWorldTransform(Transform worldTransform) {
		//		if(virtualString == null &&  segmentController.toString().contains("schema") && !segmentController.isOnServer()){
		////			System.err.println("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
		//			try{
		//				throw new NullPointerException("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), worldTransform));
		//			}catch(Exception r){
		//				r.printStackTrace();
		//			}
		//		}
		super.setWorldTransform(worldTransform);
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#setInterpolationWorldTransform(com.bulletphysics.linearmath.Transform)
	 */
	@Override
	public void setInterpolationWorldTransform(
			Transform interpolationWorldTransform) {
				//		if(virtualString == null &&  segmentController.toString().contains("schema") && !segmentController.isOnServer()){
		////			System.err.println("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
		//			try{
		//				throw new NullPointerException("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
		//			}catch(Exception r){
		//				r.printStackTrace();
		//			}
		//		}
		//		if(GlUtil.getUpVector(new Vector3f(), getInterpolationWorldTransform(new Transform())).equals(new Vector3f(0,1,0)) && !GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform).equals(new Vector3f(0,1,0))){
		////			assert(false):interpolationWorldTransform.getMatrix(new Matrix4f());
		//			try{
		//			throw new NullPointerException("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
		//			}catch(Exception r){
		//				r.printStackTrace();
		//			}
		//		}

		super.setInterpolationWorldTransform(interpolationWorldTransform);
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#checkCollideWith(com.bulletphysics.collision.dispatch.CollisionObject)
	 */
	@Override
	public boolean checkCollideWith(CollisionObject co) {
		if (!collisionException) {
			return super.checkCollideWith(co);
		} else {
			return false;
		}
	}

	public boolean isVirtual() {
		return virtualSec != null;
	}

	//	/* (non-Javadoc)
	//	 * @see com.bulletphysics.dynamics.RigidBody#getMotionState()
	//	 */
	//	@Override
	//	public MotionState getMotionState() {
	//		if(virtualString == null &&  segmentController.toString().contains("schema") && !segmentController.isOnServer()){
	////			System.err.println("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
	//			try{
	//				throw new NullPointerException("---------> GGGGGA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), super.getMotionState().getWorldTransform(new Transform()))+"     "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
	//			}catch(Exception r){
	//				r.printStackTrace();
	//			}
	//		}
	//		return super.getMotionState();
	//	}
	@Override
	public void saveKinematicState(float timeStep) {
		//todo: clamp to some (user definable) safe minimum timestep, to limit maximum angular/linear velocities
		if (timeStep != 0f) {

			//			if(virtualString == null &&  segmentController.toString().contains("schema") && !segmentController.isOnServer()){
			////				System.err.println("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
			//				try{
			//					throw new NullPointerException("--------->0 GGGGGA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), super.getMotionState().getWorldTransform(new Transform()))+"     "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
			//				}catch(Exception r){
			//					r.printStackTrace();
			//				}
			//			}

			//if we use motionstate to synchronize world transforms, get the new kinematic/animated world transform
			if (getMotionState() != null) {
				getMotionState().getWorldTransform(worldTransform);
			}
			getLinearVelocity(linearVeloTmp);
			getAngularVelocity(angularVeloTmp);

			//FIXME: check if this length check is actually valid...
			if (linearVeloTmp.lengthSquared() > 0 || angularVeloTmp.lengthSquared() > 0) {
				//Vector3f linVel = new Vector3f(), angVel = new Vector3f();

				TransformTools.calculateVelocity(
						interpolationWorldTransform, worldTransform, timeStep,
						linearVeloTmp, angularVeloTmp, axis, tmp, dmat, dorn);

				setLinearVelocity(linearVeloTmp);
				setAngularVelocity(angularVeloTmp);
			}

			interpolationLinearVelocity.set(linearVeloTmp);
			interpolationAngularVelocity.set(angularVeloTmp);
			interpolationWorldTransform.set(worldTransform);
			
			
			//printf("angular = %f %f %f\n",m_angularVelocity.getX(),m_angularVelocity.getY(),m_angularVelocity.getZ());

			//			if(virtualString == null &&  segmentController.toString().contains("schema") && !segmentController.isOnServer()){
			////				System.err.println("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
			//				try{
			//					throw new NullPointerException("--------->1 GGGGGA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), super.getMotionState().getWorldTransform(new Transform()))+"     "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
			//				}catch(Exception r){
			//					r.printStackTrace();
			//				}
			//			}
		}
	}

	//		if(virtualString == null &&  segmentController.toString().contains("schema") && !segmentController.isOnServer()){
	//			//	System.err.println("---------> MMMMA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), interpolationWorldTransform));
	//				try{
	//					throw new NullPointerException("---------> GGGGGA CHANGED FROM UP "+GlUtil.getUpVector(new Vector3f(), worldTransform)+"     "+GlUtil.getUpVector(new Vector3f(), predictedTransform)+"; "+getLinearVelocity(new Vector3f())+"; "+getAngularVelocity(new Vector3f()));
	//				}catch(Exception r){
	//					r.printStackTrace();
	//				}
	//			}
	//		super.predictIntegratedTransform(timeStep, predictedTransform);
	//	}
	@Override
	public void setCenterOfMassTransform(Transform xform) {

		if (isStaticOrKinematicObject()) {
			interpolationWorldTransform.set(worldTransform);
		} else {
			interpolationWorldTransform.set(xform);
		}
		getLinearVelocity(interpolationLinearVelocity);
		getAngularVelocity(interpolationAngularVelocity);
		worldTransform.set(xform);
		updateInertiaTensor();
	}


	@Override
	public void applyGravity() {
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin))):getLinearVelocity(tmpLin);
		super.applyGravity();
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin))):getLinearVelocity(tmpLin);
	}
	
	@Override
	public void integrateVelocities(float step) {
		
		float speedBef = getLinearVelocity(tmpLin).length();
		
		assert(!Float.isNaN(getInvMass())):getInvMass();
		assert(!Vector3fTools.isNan(totalForce)):totalForce;
		
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin))):getLinearVelocity(tmpLin);
		super.integrateVelocities(step);
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin))):getLinearVelocity(tmpLin);
		if(hadRecoil || lastRecoil == segmentController.getState().getUpdateTime()) {
			
			Vector3f lin = getLinearVelocity(tmpLin);
			float speedAft = lin.length();
			if(speedAft > segmentController.getMaxServerSpeed()) {
				lin.normalize();
				lin.scale(segmentController.getMaxServerSpeed());
				setLinearVelocity(lin);
			}
			//make sure the limiting will call for all substepts of this physics update
			lastRecoil = segmentController.getState().getUpdateTime();
			hadRecoil = false;
		}
		lastIntegrate = segmentController.getState().getUpdateTime();
	}
	public void clearForces() {
		if(segmentController.getState().getUpdateTime() == lastIntegrate) {
			//only clear if we actually handled it
			super.clearForces();
		}
	}
	@Override
	public void applyDamping(float timeStep) {
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin))):getLinearVelocity(tmpLin);
		super.applyDamping(timeStep);
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin))):getLinearVelocity(tmpLin);
	}

	@Override
	public void applyCentralForce(Vector3f force) {
		assert(!Vector3fTools.isNan(force)):force;
		super.applyCentralForce(force);
		assert(!Vector3fTools.isNan(totalForce)):totalForce;
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin)));
	}

	@Override
	public void applyTorque(Vector3f torque) {
		super.applyTorque(torque);
		assert(!Vector3fTools.isNan(getAngularVelocity(tmpLin)));
	}

	@Override
	public void applyForce(Vector3f force, Vector3f rel_pos) {
		super.applyForce(force, rel_pos);
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin)));
	}

	@Override
	public void applyCentralImpulse(Vector3f impulse) {
		super.applyCentralImpulse(impulse);
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin)));
	}

	@Override
	public void applyTorqueImpulse(Vector3f torque) {
		super.applyTorqueImpulse(torque);
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin)));
	}

	@Override
	public void applyImpulse(Vector3f impulse, Vector3f rel_pos) {
		super.applyImpulse(impulse, rel_pos);
		assert(!Vector3fTools.isNan(getLinearVelocity(tmpLin)));
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.RigidBody#setLinearVelocity(javax.vecmath.Vector3f)
	 */
	@Override
	public void setLinearVelocity(Vector3f lin_vel) {
		if (virtualSec == null) {

			//			if(!getLinearVelocity(new Vector3f()).equals(lin_vel) && segmentController.toString().contains("339")){
			//				System.err.println(segmentController.getState()+"; "+segmentController+" LIN VELO "+lin_vel);
			//				try{
			//					throw new NullPointerException();
			//				}catch(Exception e){
			//					e.printStackTrace();
			//				}
			//			}
			assert(!Vector3fTools.isNan(lin_vel));
			super.setLinearVelocity(lin_vel);

		} else {
			/*
			 * Don't set velocity on
			 * kinematic virtual objects. The collision
			 * will be huge on sector change, and the inverse
			 * will be applied to the real object
			 * and send it flying with huge speed
			 */
		}
	}
	//	public void predictIntegratedTransform(float timeStep, Transform predictedTransform) {

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.RigidBody#setAngularVelocity(javax.vecmath.Vector3f)
	 */
	@Override
	public void setAngularVelocity(Vector3f ang_vel) {
		if (virtualSec == null) {
			//			if(ang_vel.length() > 0){
			//				try{
			//					throw new NullPointerException(segmentController.getState()+" ---------> GGGGGA CHANGED FROM UP "+ang_vel);
			//				}catch(Exception r){
			//					r.printStackTrace();
			//				}
			//			}
			super.setAngularVelocity(ang_vel);
		} else {
			/*
			 * Don't set velocity on
			 * kinematic virtual objects. The collision
			 * will be huge on sector change, and the inverse
			 * will be applied to the real object
			 * and send it flying with huge speed
			 */
		}
	}
//
//	@Override
//	public void setDamping(float lin_damping, float ang_damping) {
//		try{
//			if(lin_damping != getLinearDamping()){
//				throw new NullPointerException(lin_damping+", "+ang_damping);
//			}
//		}catch(NullPointerException e){
//			e.printStackTrace();
//		}
//		super.setDamping(lin_damping, ang_damping);
//		
//	}

	public boolean isRelatedTo(CollisionObject collisionObject) {
		if (collisionObject != null && collisionObject instanceof RigidBodySegmentController && ((RigidBodySegmentController) collisionObject).segmentController == segmentController) {
			//true for virtual objects of themselves
			return true;
		}
		return false;
	}

	public void setTorque(Vector3f torque) {
		totalTorque.set(torque);
	}


}
