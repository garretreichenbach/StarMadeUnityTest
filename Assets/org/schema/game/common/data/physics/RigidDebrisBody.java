package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import org.schema.game.client.view.shards.Shard;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

public class RigidDebrisBody extends RigidBodyExt {

	public Shard shard;
	Vector3f linTmp = new Vector3f();
	Vector3f angTmp = new Vector3f();
	public boolean noCollision;

	public RigidDebrisBody(float mass, MotionState motionState,
	                       CollisionShape collisionShape) {
		super(CollisionType.DEBRIS, mass, motionState, collisionShape);
	}

	public RigidDebrisBody(float mass, MotionState motionState,
	                       CollisionShape collisionShape, Vector3f localInertia) {
		super(CollisionType.DEBRIS, mass, motionState, collisionShape, localInertia);
	}
	public RigidDebrisBody(RigidBodyConstructionInfo constructionInfo) {
		super(CollisionType.DEBRIS, constructionInfo);
	}

	@Override
	public void activate(boolean forceActivation) {
		if (forceActivation /*|| (collisionFlags & (CollisionFlags.STATIC_OBJECT | CollisionFlags.KINEMATIC_OBJECT)) == 0*/) {
			//			System.err.println("ACTIVATE: forced: "+forceActivation);
			setActivationState(ACTIVE_TAG);
			deactivationTime = 0f;
		}
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.RigidBody#updateDeactivation(float)
	 */
	@Override
	public void updateDeactivation(float timeStep) {
		if ((getActivationState() == ISLAND_SLEEPING) || (getActivationState() == DISABLE_DEACTIVATION)) {
			return;
		}
		float linMax = getLinearSleepingThreshold() * getLinearSleepingThreshold();
		float angMax = getAngularSleepingThreshold() * getAngularSleepingThreshold();
		if ((getLinearVelocity(linTmp).lengthSquared() < linMax) &&
				(getAngularVelocity(angTmp).lengthSquared() < angMax)) {
			//			System.err.println("DEACTIVATION TIME: "+deactivationTime);
			deactivationTime += timeStep;
		} else {
			//			System.err.println("RESET DEACTIVATION TIME: "+deactivationTime+"; "+getLinearVelocity(linTmp).lengthSquared()+"/"+linMax+" ::: "+getAngularVelocity(angTmp).lengthSquared()+"/"+angMax);
			deactivationTime = 0f;
			setActivationState(0);
		}
	}

	//	/* (non-Javadoc)
	//	 * @see com.bulletphysics.collision.dispatch.CollisionObject#setActivationState(int)
	//	 */
	//	@Override
	//	public void setActivationState(int newState) {
	//		super.setActivationState(newState);
	//		if(newState == ACTIVE_TAG){
	//			try{
	//				throw new Exception("SET ACTIVATION STATE: "+newState);
	//			}catch(Exception e){
	//				e.printStackTrace();
	//			}
	//
	//		}
	//	}

}
