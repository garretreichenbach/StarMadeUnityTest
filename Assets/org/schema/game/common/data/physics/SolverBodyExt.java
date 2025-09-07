package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;

/**
 * SolverBody is an internal data structure for the constraint solver. Only necessary
 * data is packed to increase cache coherence/performance.
 *
 * @author jezek2
 */
public class SolverBodyExt {

	//protected final BulletStack stack = BulletStack.get();

	public final Vector3f angularVelocity = new Vector3f();
	public final Vector3f linearVelocity = new Vector3f();
	public final Vector3f centerOfMassPosition = new Vector3f();
	public final Vector3f pushVelocity = new Vector3f();
	public final Vector3f turnVelocity = new Vector3f();
	public float angularFactor;
	public float invMass;
	public float friction;
	public RigidBody originalBody;

	public void getVelocityInLocalPoint(Vector3f rel_pos, Vector3f velocity, SequentialImpulseContraintSolverExtVariableSet v) {
		Vector3f tmp = v.tttt;
		tmp.cross(angularVelocity, rel_pos);
		velocity.add(linearVelocity, tmp);
	}

	/**
	 * Optimization for the iterative solver: avoid calculating constant terms involving inertia, normal, relative position.
	 */
	public void internalApplyImpulse(Vector3f linearComponent, Vector3f angularComponent, float impulseMagnitude) {
		if (invMass != 0f) {
			//			if(impulseMagnitude != 0)
			//			System.err.println("NORMAL IMPULSE: "+linearComponent+"; MAG: "+impulseMagnitude);
			linearVelocity.scaleAdd(impulseMagnitude, linearComponent, linearVelocity);
			angularVelocity.scaleAdd(impulseMagnitude * angularFactor, angularComponent, angularVelocity);
		}
	}

	public void internalApplyPushImpulse(Vector3f linearComponent, Vector3f angularComponent, float impulseMagnitude) {
		if (invMass != 0f) {
			System.err.println("PUSH IMPULSE: " + linearComponent + "; MAG: " + impulseMagnitude);
			pushVelocity.scaleAdd(impulseMagnitude, linearComponent, pushVelocity);
			turnVelocity.scaleAdd(impulseMagnitude * angularFactor, angularComponent, turnVelocity);
		}
	}

	public void writebackVelocity() {
		if (invMass != 0f) {
			originalBody.setLinearVelocity(linearVelocity);
			originalBody.setAngularVelocity(angularVelocity);
			//m_originalBody->setCompanionId(-1);

			//			System.err.println("PUUUSH BBB "+linearVelocity+"; "+angularVelocity);
		}
	}

	public void writebackVelocity(float timeStep, SequentialImpulseContraintSolverExtVariableSet v) {
		if (invMass != 0f) {
			originalBody.setLinearVelocity(linearVelocity);
			originalBody.setAngularVelocity(angularVelocity);

			// correct the position/orientation based on push/turn recovery
			Transform newTransform = v.transTmp1;//new @Stack Transform();
			Transform curTrans = originalBody.getWorldTransform(v.transTmp2);
			TransformUtil.integrateTransform(curTrans, pushVelocity, turnVelocity, timeStep, newTransform);
			originalBody.setWorldTransform(newTransform);

			System.err.println("PUUUSH " + pushVelocity + "; " + turnVelocity);
			//m_originalBody->setCompanionId(-1);
		}
	}

	public void readVelocity() {
		if (invMass != 0f) {
			originalBody.getLinearVelocity(linearVelocity);
			originalBody.getAngularVelocity(angularVelocity);
		}
	}

}

