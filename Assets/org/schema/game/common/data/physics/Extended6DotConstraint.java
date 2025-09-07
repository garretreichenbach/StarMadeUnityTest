package org.schema.game.common.data.physics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.schine.network.StateInterface;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.dynamics.constraintsolver.RotationalLimitMotor;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

public class Extended6DotConstraint extends Generic6DofConstraint {

	public final Vector3f linearJointAxis = new Vector3f();
	public final Vector3f aJ = new Vector3f();
	public final Vector3f bJ = new Vector3f();
	public final Vector3f m_0MinvJt = new Vector3f();
	public final Vector3f m_1MinvJt = new Vector3f();
	protected final RotationalLimitMotorAbsVelocity[] angularLimits/*[3]*/ = new RotationalLimitMotorAbsVelocity[]{new RotationalLimitMotorAbsVelocity(), new RotationalLimitMotorAbsVelocity(), new RotationalLimitMotorAbsVelocity()};
	public Extended6DotConstraint(StateInterface state, RigidBody bodyA, RigidBody bodyB,
	                              Transform rbAFrame, Transform rbBFrame, boolean b) {
		super(bodyA, bodyB, rbAFrame, rbBFrame, b);
		for (int i = 0; i < 3; i++) {
			//			angularLimits[i].maxLimitForce = 300000.0f; //should be faster than orientation speed
			//			angularLimits[i].ERP = 0.7f;
			//			angularLimits[i].limitSoftness = 200.0f;
		}
	}

	private static float getMatrixElem(Matrix3f mat, int index) {
		int i = index % 3;
		int j = index / 3;
		return mat.getElement(i, j);
	}

	/**
	 * MatrixToEulerXYZ from http://www.geometrictools.com/LibFoundation/Mathematics/Wm4Matrix3.inl.html
	 */
	private static boolean matrixToEulerXYZ(Matrix3f mat, Vector3f xyz) {
		//	// rot =  cy*cz          -cy*sz           sy
		//	//        cz*sx*sy+cx*sz  cx*cz-sx*sy*sz -cy*sx
		//	//       -cx*cz*sy+sx*sz  cz*sx+cx*sy*sz  cx*cy
		//

		if (getMatrixElem(mat, 2) < 1.0f) {
			if (getMatrixElem(mat, 2) > -1.0f) {
				xyz.x = (float) Math.atan2(-getMatrixElem(mat, 5), getMatrixElem(mat, 8));
				xyz.y = (float) Math.asin(getMatrixElem(mat, 2));
				xyz.z = (float) Math.atan2(-getMatrixElem(mat, 1), getMatrixElem(mat, 0));
				return true;
			} else {
				System.err.println("WARNING.  Not unique.  XA - ZA = -atan2(r10,r11)");
				// WARNING.  Not unique.  XA - ZA = -atan2(r10,r11)
				xyz.x = -(float) Math.atan2(getMatrixElem(mat, 3), getMatrixElem(mat, 4));
				xyz.y = -BulletGlobals.SIMD_HALF_PI;
				xyz.z = 0.0f;
				return false;
			}
		} else {
			System.err.println("WARNING.  Not unique.  XAngle + ZAngle = atan2(r10,r11)");
			// WARNING.  Not unique.  XAngle + ZAngle = atan2(r10,r11)
			xyz.x = (float) Math.atan2(getMatrixElem(mat, 3), getMatrixElem(mat, 4));
			xyz.y = BulletGlobals.SIMD_HALF_PI;
			xyz.z = 0.0f;
		}

		return false;
	}

	/**
	 * Calcs the euler angles between the two bodies.
	 */
	@Override
	protected void calculateAngleInfo() {
		Matrix3f mat = new Matrix3f();

		Matrix3f relative_frame = new Matrix3f();
		mat.set(calculatedTransformA.basis);
		MatrixUtil.invert(mat);
		relative_frame.mul(mat, calculatedTransformB.basis);

		matrixToEulerXYZ(relative_frame, calculatedAxisAngleDiff);

		//		System.err.println(state+" Euler: "+calculatedAxisAngleDiff);

		// in euler angle mode we do not actually constrain the angular velocity
		// along the axes axis[0] and axis[2] (although we do use axis[1]) :
		//
		//    to get			constrain w2-w1 along		...not
		//    ------			---------------------		------
		//    d(angle[0])/dt = 0	ax[1] x ax[2]			ax[0]
		//    d(angle[1])/dt = 0	ax[1]
		//    d(angle[2])/dt = 0	ax[0] x ax[1]			ax[2]
		//
		// constraining w2-w1 along an axis 'a' means that a'*(w2-w1)=0.
		// to prove the result for angle[0], write the expression for angle[0] from
		// GetInfo1 then take the derivative. to prove this for angle[2] it is
		// easier to take the euler rate expression for d(angle[2])/dt with respect
		// to the components of w and set that to 0.

		Vector3f axis0 = new Vector3f();
		calculatedTransformB.basis.getColumn(0, axis0);

		Vector3f axis2 = new Vector3f();
		calculatedTransformA.basis.getColumn(2, axis2);

		calculatedAxis[1].cross(axis2, axis0);
		calculatedAxis[0].cross(calculatedAxis[1], axis2);
		calculatedAxis[2].cross(axis0, calculatedAxis[1]);

		//    if(m_debugDrawer)
		//    {
		//
		//    	char buff[300];
		//		sprintf(buff,"\n X: %.2f ; Y: %.2f ; Z: %.2f ",
		//		m_calculatedAxisAngleDiff[0],
		//		m_calculatedAxisAngleDiff[1],
		//		m_calculatedAxisAngleDiff[2]);
		//    	m_debugDrawer->reportErrorWarning(buff);
		//    }
	}

	@Override
	protected void buildAngularJacobian(/*JacobianEntry jacAngular*/int jacAngular_index, Vector3f jointAxisW) {
		Matrix3f mat1 = rbA.getCenterOfMassTransform(new Transform()).basis;
		mat1.transpose();

		Matrix3f mat2 = rbB.getCenterOfMassTransform(new Transform()).basis;
		mat2.transpose();
		if (!checkJacobian(jointAxisW,
				mat1,
				mat2,
				rbA.getInvInertiaDiagLocal(new Vector3f()),
				rbB.getInvInertiaDiagLocal(new Vector3f()))) {
			System.err.println("Exception: Jacobian Entry init failed!");
			return;
		}

		super.buildAngularJacobian(jacAngular_index, jointAxisW);
	}

	/**
	 * Test angular limit.<p>
	 * Calculates angular correction and returns true if limit needs to be corrected.
	 * Generic6DofConstraint.buildJacobian must be called previously.
	 */
	@Override
	public boolean testAngularLimitMotor(int axis_index) {
		float angle = VectorUtil.getCoord(calculatedAxisAngleDiff, axis_index);

		// test limits
		angularLimits[axis_index].testLimitValue(angle);
		return angularLimits[axis_index].needApplyTorques();
	}

	@Override
	public void buildJacobian() {
		// Clear accumulated impulses for the next simulation step
		linearLimits.accumulatedImpulse.set(0f, 0f, 0f);
		for (int i = 0; i < 3; i++) {
			angularLimits[i].accumulatedImpulse = 0f;
		}

		// calculates transform
		calculateTransforms();

		Vector3f tmpVec = new Vector3f();
		//  const btVector3& pivotAInW = m_calculatedTransformA.getOrigin();
		//  const btVector3& pivotBInW = m_calculatedTransformB.getOrigin();
		calcAnchorPos();
		Vector3f pivotAInW = new Vector3f(anchorPos);
		Vector3f pivotBInW = new Vector3f(anchorPos);

		// not used here
		//    btVector3 rel_pos1 = pivotAInW - m_rbA.getCenterOfMassPosition();
		//    btVector3 rel_pos2 = pivotBInW - m_rbB.getCenterOfMassPosition();

		Vector3f normalWorld = new Vector3f();
		// linear part
		for (int i = 0; i < 3; i++) {
			if (linearLimits.isLimited(i)) {
				if (useLinearReferenceFrameA) {
					calculatedTransformA.basis.getColumn(i, normalWorld);
				} else {
					calculatedTransformB.basis.getColumn(i, normalWorld);
				}

				buildLinearJacobian(
						/*jacLinear[i]*/i, normalWorld,
						pivotAInW, pivotBInW);

			}
		}

		// angular part
		for (int i = 0; i < 3; i++) {
			// calculates error angle
			if (testAngularLimitMotor(i)) {
				this.getAxis(i, normalWorld);
				// Create angular atom
				buildAngularJacobian(/*jacAng[i]*/i, normalWorld);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint#solveConstraint(float)
	 */
	@Override
	public void solveConstraint(float timeStep) {
		this.timeStep = timeStep;

		//calculateTransforms();

		int i;

		// linear

		Vector3f pointInA = new Vector3f(calculatedTransformA.origin);
		Vector3f pointInB = new Vector3f(calculatedTransformB.origin);

		float jacDiagABInv;
		Vector3f linear_axis = new Vector3f();
		for (i = 0; i < 3; i++) {
			if (linearLimits.isLimited(i)) {
				jacDiagABInv = 1f / jacLinear[i].getDiagonal();

				if (useLinearReferenceFrameA) {
					calculatedTransformA.basis.getColumn(i, linear_axis);
				} else {
					calculatedTransformB.basis.getColumn(i, linear_axis);
				}

				linearLimits.solveLinearAxis(
						this.timeStep,
						jacDiagABInv,
						rbA, pointInA,
						rbB, pointInB,
						i, linear_axis, anchorPos);

			}
		}

		// angular
		Vector3f angular_axis = new Vector3f();
		float angularJacDiagABInv;
		for (i = 0; i < 3; i++) {
			if (angularLimits[i].needApplyTorques()) {
				// get axis
				getAxis(i, angular_axis);

				if (jacAng[i].getDiagonal() == 0) {
					angularJacDiagABInv = 0.1f;
				} else {
					angularJacDiagABInv = 1f / jacAng[i].getDiagonal();
				}
				float solveAngularLimits = angularLimits[i].solveAngularLimits(this.timeStep, angular_axis, angularJacDiagABInv, rbA, rbB);
				//				System.err.println(state+" ["+i+"]: " +
				//						"tV "+solveAngularLimits+"; " +
				//						"aI "+angularLimits[i].accumulatedImpulse+"; " +
				//						"ER "+angularLimits[i].currentLimitError+"; " +
				//						"JA "+angularJacDiagABInv+"; ");
			}
		}
	}

	@Override
	public void setAngularLowerLimit(Vector3f angularLower) {
		angularLimits[0].loLimit = angularLower.x;
		angularLimits[1].loLimit = angularLower.y;
		angularLimits[2].loLimit = angularLower.z;
	}

	@Override
	public void setAngularUpperLimit(Vector3f angularUpper) {
		angularLimits[0].hiLimit = angularUpper.x;
		angularLimits[1].hiLimit = angularUpper.y;
		angularLimits[2].hiLimit = angularUpper.z;
	}

	/**
	 * Retrieves the angular limit informacion.
	 */
	@Override
	public RotationalLimitMotor getRotationalLimitMotor(int index) {
		return angularLimits[index];
	}

	/**
	 * first 3 are linear, next 3 are angular
	 */
	@Override
	public void setLimit(int axis, float lo, float hi) {
		if (axis < 3) {
			VectorUtil.setCoord(linearLimits.lowerLimit, axis, lo);
			VectorUtil.setCoord(linearLimits.upperLimit, axis, hi);
		} else {
			angularLimits[axis - 3].loLimit = lo;
			angularLimits[axis - 3].hiLimit = hi;
		}
	}

	/**
	 * Test limit.<p>
	 * - free means upper &lt; lower,<br>
	 * - locked means upper == lower<br>
	 * - limited means upper &gt; lower<br>
	 * - limitIndex: first 3 are linear, next 3 are angular
	 */
	@Override
	public boolean isLimited(int limitIndex) {
		if (limitIndex < 3) {
			return linearLimits.isLimited(limitIndex);

		}
		return angularLimits[limitIndex - 3].isLimited();
	}

	public boolean checkJacobian(Vector3f jointAxis,
	                             Matrix3f world2A,
	                             Matrix3f world2B,
	                             Vector3f inertiaInvA,
	                             Vector3f inertiaInvB) {
		linearJointAxis.set(0f, 0f, 0f);

		aJ.set(jointAxis);
		world2A.transform(aJ);

		bJ.set(jointAxis);
		bJ.negate();
		world2B.transform(bJ);

		VectorUtil.mul(m_0MinvJt, inertiaInvA, aJ);
		VectorUtil.mul(m_1MinvJt, inertiaInvB, bJ);
		float Adiag = m_0MinvJt.dot(aJ) + m_1MinvJt.dot(bJ);

		if (Adiag <= 0) {
			//				System.err.println("WARNING: "+linearJointAxis+"; "+aJ+"; "+bJ+"; "+m_0MinvJt+"; "+m_1MinvJt);
		}
		return (Adiag > 0f);

	}

	public void matrixToEuler(Matrix3f mat, Vector3f xyz) {

		//				|  0  1  2  3 |
		//	    M =  	|  4  5  6  7 |
		//	         	|  8  9 10 11 |
		//	         	| 12 13 14 15 |
		float D = 0;
		float angle_y = D = (float) -Math.asin(mat.m02/*mat[2]*/);        /* Calculate Y-axis angle */

		float C = (float) Math.cos(angle_y);

		float trX = 0;
		float trY = 0;
		float angle_x = 0;
		float angle_z = 0;
		if (FastMath.abs(C) > 0.005)             /* Gimball lock? */ {
			trX = mat.m22/*mat[10]*/ / C;           /* No, so get X-axis angle */
			trY = -mat.m12/*-mat[6]*/ / C;

			angle_x = (float) (Math.atan2(trY, trX));

			trX = mat.m00/*mat[0]*/ / C;            /* Get Z-axis angle */
			trY = -mat.m01 /*-mat[1]*/ / C;

			angle_z = (float) (Math.atan2(trY, trX));
		} else                                 /* Gimball lock has occurred */ {
			angle_x = 0;                      /* Set X-axis angle to zero */

			trX = mat.m11 /*mat[5]*/;                 /* And calculate Z-axis angle */
			trY = mat.m10 /*mat[4]*/;

			angle_z = (float) (Math.atan2(trY, trX));
		}

		angle_x = FastMath.clamp(angle_x, 0, (float) Math.PI * 2);  /* Clamp all angles to range */
		angle_y = FastMath.clamp(angle_y, 0, (float) Math.PI * 2);
		angle_z = FastMath.clamp(angle_z, 0, (float) Math.PI * 2);

		xyz.x = angle_x;
		xyz.y = angle_y;
		xyz.z = angle_z;

		System.err.println("Euler: " + xyz);
	}
}
