package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.RotationalLimitMotor;

public class RotationalLimitMotorAbsVelocity extends RotationalLimitMotor {

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.constraintsolver.RotationalLimitMotor#solveAngularLimits(float, javax.vecmath.Vector3f, float, com.bulletphysics.dynamics.RigidBody, com.bulletphysics.dynamics.RigidBody)
	 */
	@Override
	public float solveAngularLimits(float timeStep, Vector3f axis,
	                                float jacDiagABInv, RigidBody body0, RigidBody body1) {
		if (needApplyTorques() == false) {
			return 0.0f;
		}

		float target_velocity = this.targetVelocity;
		float maxMotorForce = this.maxMotorForce;

		// current error correction
		if (currentLimit != 0) {
			target_velocity = -ERP * currentLimitError / (timeStep);
			maxMotorForce = maxLimitForce;
		}

		maxMotorForce *= timeStep;

		// current velocity difference
		Vector3f vel_diff = body0.getAngularVelocity(new Vector3f());
		if (body1 != null) {
			vel_diff.sub(body1.getAngularVelocity(new Vector3f()));
		}

		float rel_vel = axis.dot(vel_diff);

		// correction velocity
		float motor_relvel = limitSoftness * (target_velocity - damping * rel_vel);

		if (motor_relvel < BulletGlobals.FLT_EPSILON && motor_relvel > -BulletGlobals.FLT_EPSILON) {
			return 0.0f; // no need for applying force
		}

		// correction impulse
		float unclippedMotorImpulse = (1 + bounce) * motor_relvel * jacDiagABInv;

		// clip correction impulse
		float clippedMotorImpulse;

		// todo: should clip against accumulated impulse
		if (unclippedMotorImpulse > 0.0f) {
			clippedMotorImpulse = unclippedMotorImpulse > maxMotorForce ? maxMotorForce : unclippedMotorImpulse;
		} else {
			clippedMotorImpulse = unclippedMotorImpulse < -maxMotorForce ? -maxMotorForce : unclippedMotorImpulse;
		}

		// sort with accumulated impulses
		float lo = -1e30f;
		float hi = 1e30f;

		float oldaccumImpulse = accumulatedImpulse;
		float sum = oldaccumImpulse + clippedMotorImpulse;
		accumulatedImpulse = sum > hi ? 0f : sum < lo ? 0f : sum;

		clippedMotorImpulse = accumulatedImpulse - oldaccumImpulse;

		Vector3f motorImp = new Vector3f();
		motorImp.scale(clippedMotorImpulse * 10.0f, axis);

		Vector3f angularVelocity = body0.getAngularVelocity(new Vector3f());
		angularVelocity.x = (Math.signum(motorImp.x) == Math.signum(angularVelocity.x)) ? angularVelocity.x : motorImp.x;
		angularVelocity.y = (Math.signum(motorImp.y) == Math.signum(angularVelocity.y)) ? angularVelocity.y : motorImp.y;
		angularVelocity.z = (Math.signum(motorImp.z) == Math.signum(angularVelocity.z)) ? angularVelocity.z : motorImp.z;

		//		body0.applyTorqueImpulse(motorImp);
		body0.setAngularVelocity(angularVelocity);
		if (body1 != null) {
			motorImp.negate();
			//			body1.applyTorqueImpulse(motorImp);
			if (!body1.isStaticObject()) {

				body1.setAngularVelocity(angularVelocity);
			}
		}

		return clippedMotorImpulse;
	}

}
