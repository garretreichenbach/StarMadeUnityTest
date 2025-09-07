package org.schema.common.util.linAlg;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

public class Quat4Util {
	/**
	 * <code>dot</code> calculates and returns the dot product of this
	 * quaternion with that of the parameter quaternion.
	 *
	 * @param b the quaternion to calculate the dot product of.
	 * @return the dot product of this and the parameter quaternion.
	 */
	public static float dot(Quat4f a, Quat4f b) {
		return a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;
	}

	/**
	 * <code>fromAngleAxis</code> sets this quaternion to the values specified
	 * by an angle and an axis of rotation. This method creates an object, so
	 * use fromAngleNormalAxis if your axis is already normalized.
	 *
	 * @param angle the angle to rotate (in radians).
	 * @param axis  the axis of rotation.
	 * @return this quaternion
	 */
	public static Quat4f fromAngleAxis(float angle, Vector3f axis, Quat4f ress) {
		Vector3f normAxis = new Vector3f(axis);
		normAxis.normalize();
		fromAngleNormalAxis(angle, normAxis, ress);
		return ress;
	}

	/**
	 * <code>fromAngleNormalAxis</code> sets this quaternion to the values
	 * specified by an angle and a normalized axis of rotation.
	 *
	 * @param angle the angle to rotate (in radians).
	 * @param axis  the axis of rotation (already normalized).
	 */
	public static Quat4f fromAngleNormalAxis(float angle, Vector3f axis, Quat4f res) {
		if (axis.x == 0 && axis.y == 0 && axis.z == 0) {
			res.set(0, 0, 0, 1);
		} else {
			float halfAngle = 0.5f * angle;
			float sin = FastMath.sin(halfAngle);
			res.w = FastMath.cos(halfAngle);
			res.x = sin * axis.x;
			res.y = sin * axis.y;
			res.z = sin * axis.z;
		}
		return res;
	}
	public static Quat4f fromEulerAngles(float roll, float pitch, float yaw) {
		Quat4f qx = new Quat4f(1, 0, 0, roll);
        Quat4f qy = new Quat4f(0, 1, 0, pitch);
        Quat4f qz = new Quat4f(0, 0, 1, yaw);
        qy.mul(qz);
        qy.mul(qx);
        
        return qy;
}
	/**
	 * <code>mult</code> multiplies this quaternion by a parameter quaternion.
	 * The result is returned as a new quaternion. It should be noted that
	 * quaternion multiplication is not commutative so q * p != p * q.
	 * <p/>
	 * It IS safe for q and res to be the same object.
	 * It IS safe for this and res to be the same object.
	 *
	 * @param q   the quaternion to multiply this quaternion by.
	 * @param res the quaternion to store the result in.
	 * @return the new quaternion.
	 */
	public static Quat4f mult(Quat4f g, Quat4f q, Quat4f res) {
		float x = g.x;
		float y = g.y;
		float z = g.z;
		float w = g.w;
		if (res == null) {
			res = new Quat4f();
		}
		float qw = q.w, qx = q.x, qy = q.y, qz = q.z;
		res.x = x * qw + y * qz - z * qy + w * qx;
		res.y = -x * qz + y * qw + z * qx + w * qy;
		res.z = x * qy - y * qx + z * qw + w * qz;
		res.w = -x * qx - y * qy - z * qz + w * qw;
		return res;
	}

	/**
	 * <code>mult</code> multiplies this quaternion by a parameter vector. The
	 * result is returned as a new vector.
	 *
	 * @param v     the vector to multiply this quaternion by.
	 * @param store the vector to store the result in. It IS safe for v and store
	 *              to be the same object.
	 * @return the result vector.
	 */
	public static Vector3f mult(Quat4f q, Vector3f v, Vector3f store) {
		float x = q.x;
		float y = q.y;
		float z = q.z;
		float w = q.w;
		if (store == null) {
			store = new Vector3f();
		}
		if (v.x == 0 && v.y == 0 && v.z == 0) {
			store.set(0, 0, 0);
		} else {
			float vx = v.x, vy = v.y, vz = v.z;
			store.x = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x
					* vx + 2 * y * x * vy + 2 * z * x * vz - z * z * vx - y
					* y * vx;
			store.y = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w
					* z * vx - z * z * vy + w * w * vy - 2 * x * w * vz - x
					* x * vy;
			store.z = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w
					* y * vx - y * y * vz + 2 * w * x * vy - x * x * vz + w
					* w * vz;
		}
		return store;
	}

	/**
	 * Sets the values of this quaternion to the nlerp from itself to q2 by blend.
	 *
	 * @param q2
	 * @param blend
	 */
	public static void nlerp(Quat4f q2, float blend, Quat4f res) {
		float dot = dot(res, q2);
		float blendI = 1.0f - blend;
		if (dot < 0.0f) {
			res.x = blendI * res.x - blend * q2.x;
			res.y = blendI * res.y - blend * q2.y;
			res.z = blendI * res.z - blend * q2.z;
			res.w = blendI * res.w - blend * q2.w;
		} else {
			res.x = blendI * res.x + blend * q2.x;
			res.y = blendI * res.y + blend * q2.y;
			res.z = blendI * res.z + blend * q2.z;
			res.w = blendI * res.w + blend * q2.w;
		}
		res.normalize();
	}

	/**
	 * Sets the values of this quaternion to the slerp from itself to q2 by
	 * changeAmnt
	 *
	 * @param q2         Final interpolation value
	 * @param changeAmnt The amount diffrence
	 */
	public static void slerp(Quat4f q2, float changeAmnt, Quat4f res) {
		if (res.x == q2.x && res.y == q2.y && res.z == q2.z
				&& res.w == q2.w) {
			return;
		}

		float result = (res.x * q2.x) + (res.y * q2.y) + (res.z * q2.z)
				+ (res.w * q2.w);

		if (result < 0.0f) {
			// Negate the second quaternion and the result of the dot product
			q2.x = -q2.x;
			q2.y = -q2.y;
			q2.z = -q2.z;
			q2.w = -q2.w;
			result = -result;
		}

		// Set the first and second scale for the interpolation
		float scale0 = 1 - changeAmnt;
		float scale1 = changeAmnt;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - result) > 0.1f) {
			// Get the angle between the 2 quaternions, and then store the sin()
			// of that angle
			float theta = FastMath.acos(result);
			float invSinTheta = 1f / FastMath.sin(theta);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = FastMath.sin((1 - changeAmnt) * theta) * invSinTheta;
			scale1 = FastMath.sin((changeAmnt * theta)) * invSinTheta;
		}

		// Calculate the x, y, z and w values for the quaternion by using a
		// special
		// form of linear interpolation for quaternions.
		res.x = (scale0 * res.x) + (scale1 * q2.x);
		res.y = (scale0 * res.y) + (scale1 * q2.y);
		res.z = (scale0 * res.z) + (scale1 * q2.z);
		res.w = (scale0 * res.w) + (scale1 * q2.w);
	}

	/**
	 * Returns the slerp interpolation of quaternions {@code a} and {@code b}, at
	 * time {@code t}.
	 * <p/>
	 * {@code t} should range in {@code [0,1]}. Result is a when {@code t=0 } and
	 * {@code b} when {@code t=1}.
	 * <p/>
	 * When {@code allowFlip} is true (default) the slerp interpolation will
	 * always use the "shortest path" between the quaternions' orientations, by
	 * "flipping" the source Quaternion if needed (see {@link #negate()}).
	 *
	 * @param a         the first Quaternion
	 * @param b         the second Quaternion
	 * @param t         the t interpolation parameter
	 * @param allowFlip tells whether or not the interpolation allows axis flip
	 */
	public static final Quat4f slerp(Quat4f a, Quat4f b, float t,
	                                 boolean allowFlip, Quat4f res) {
		// Warning: this method should not normalize the Quaternion
		float cosAngle = dot(a, b);

		float c1, c2;
		// Linear interpolation for close orientations
		if ((1.0f - Math.abs(cosAngle)) < 0.01f) {
			c1 = 1.0f - t;
			c2 = t;
		} else {
			// Spherical interpolation
			float angle = FastMath.acos(FastMath.abs(cosAngle));
			float sinAngle = FastMath.sin(angle);
			c1 = FastMath.sin(angle * (1.0f - t)) / sinAngle;
			c2 = FastMath.sin(angle * t) / sinAngle;
		}

		// Use the shortest path
		if (allowFlip && (cosAngle < 0.0))
			c1 = -c1;

		res.set(c1 * a.x + c2 * b.x, c1 * a.y + c2 * b.y, c1 * a.z
				+ c2 * b.z, c1 * a.w + c2 * b.w);
		return res;
	}

	/**
	 * <code>slerp</code> sets this quaternion's value as an interpolation
	 * between two other quaternions.
	 *
	 * @param q1 the first quaternion.
	 * @param q2 the second quaternion.
	 * @param t  the amount to interpolate between the two quaternions.
	 */
	public static Quat4f slerp(Quat4f q1, Quat4f q2, float t, Quat4f res) {
		// Create a local quaternion to store the interpolated quaternion
		if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) {
			res.set(q1);
			return res;
		}

		float result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z)
				+ (q1.w * q2.w);

		if (result < 0.0f) {
			// Negate the second quaternion and the result of the dot product
			q2.x = -q2.x;
			q2.y = -q2.y;
			q2.z = -q2.z;
			q2.w = -q2.w;
			result = -result;
		}

		// Set the first and second scale for the interpolation
		float scale0 = 1 - t;
		float scale1 = t;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - result) > 0.1f) {// Get the angle between the 2 quaternions,
			// and then store the sin() of that angle
			float theta = FastMath.acos(result);
			float invSinTheta = 1f / FastMath.sin(theta);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = FastMath.sin((1 - t) * theta) * invSinTheta;
			scale1 = FastMath.sin((t * theta)) * invSinTheta;
		}

		// Calculate the x, y, z and w values for the quaternion by using a
		// special
		// form of linear interpolation for quaternions.
		res.x = (scale0 * q1.x) + (scale1 * q2.x);
		res.y = (scale0 * q1.y) + (scale1 * q2.y);
		res.z = (scale0 * q1.z) + (scale1 * q2.z);
		res.w = (scale0 * q1.w) + (scale1 * q2.w);

		// Return the interpolated quaternion
		return res;
	}

}
