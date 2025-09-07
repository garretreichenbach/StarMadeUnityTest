package org.schema.common.util.linAlg;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

public class Quat4fTools {

	public static float angularDifference(Quat4f a, Quat4f b) {
		double dotProduct = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
		return (float) (2.0f * FastMath.acosFast(dotProduct));
	}

	/**
	 * between 0 and 2PI
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static float angularDifference2PI(Quat4f a, Quat4f b) {
		double dotProduct = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
		return (float) (FastMath.acosFast(2.0f * (dotProduct * dotProduct) - 1f));
	}

	/**
	 * <code>getRotationColumn</code> returns one of three columns specified
	 * by the parameter. This column is returned as a <code>Vector3f</code>
	 * object.
	 *
	 * @param i the column to retrieve. Must be between 0 and 2.
	 * @return the column specified by the index.
	 */
	public static Vector3f getRotationColumn(Quat4f from, int i) {
		return getRotationColumn(from, i, null);
	}

	/**
	 * <code>getRotationColumn</code> returns one of three columns specified
	 * by the parameter. This column is returned as a <code>Vector3f</code>
	 * object. The value is retrieved as if this quaternion was first normalized.
	 *
	 * @param i     the column to retrieve. Must be between 0 and 2.
	 * @param store the vector object to store the result in. if null, a new one
	 *              is created.
	 * @return the column specified by the index.
	 */
	public static Vector3f getRotationColumn(Quat4f from, int i, Vector3f store) {
		if (store == null) {
			store = new Vector3f();
		}

		float norm = from.w * from.w + from.x * from.x + from.y * from.y + from.z * from.z;//norm();
		if (norm != 1.0f) {
			norm = FastMath.invSqrt(norm);
		}

		float xx = from.x * from.x * norm;
		float xy = from.x * from.y * norm;
		float xz = from.x * from.z * norm;
		float xw = from.x * from.w * norm;
		float yy = from.y * from.y * norm;
		float yz = from.y * from.z * norm;
		float yw = from.y * from.w * norm;
		float zz = from.z * from.z * norm;
		float zw = from.z * from.w * norm;
		switch(i) {
			case 0 -> {
				store.x = 1 - 2 * (yy + zz);
				store.y = 2 * (xy + zw);
				store.z = 2 * (xz - yw);
			}
			case 1 -> {
				store.x = 2 * (xy - zw);
				store.y = 1 - 2 * (xx + zz);
				store.z = 2 * (yz + xw);
			}
			case 2 -> {
				store.x = 2 * (xz + yw);
				store.y = 2 * (yz - xw);
				store.z = 1 - 2 * (xx + yy);
			}
			default -> throw new IllegalArgumentException("Invalid column index. " + i);
		}

		return store;
	}

	/**
	 * between 0 and 1 (180 degrees)
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static float angularDifferenceNorm(Quat4f a, Quat4f b) {
		float dotProduct = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
		return 1f - (dotProduct * dotProduct);
	}

	public static float getPitch(Quat4f mm) {
		return FastMath.asin(2f * (mm.w * mm.y - mm.z * mm.x));
	}

	public static float getRoll(Quat4f mm) {
		return FastMath.atan2Fast(2f * (mm.w * mm.z + mm.x * mm.y), 1f - 2f * (mm.y * mm.y + mm.z * mm.z));
	}

	public static float getYaw(Quat4f mm) {
		return FastMath.atan2Fast(2f * (mm.w * mm.x + mm.y * mm.z), 1f - 2f * (mm.x * mm.x + mm.y * mm.y));
//		
	}

//	 public static quatRotate(const btQuaternion& rotation, const btVector3& v)
//	 {
//	  btQuaternion q = rotation * v;
//	  q *= rotation.inverse();
//	 #if defined (BT_USE_SSE_IN_API) && defined (BT_USE_SSE)
//	  return btVector3(_mm_and_ps(q.get128(), btvFFF0fMask));
//	 #elif defined(BT_USE_NEON)
//	  return btVector3((float32x4_t)vandq_s32((int32x4_t)q.get128(), btvFFF0Mask));
//	 #else
//	  return btVector3(q.getX(),q.getY(),q.getZ());
//	 #endif
//	 }


	public static Vector3f toEuler(Quat4f q1) {
		float sqw = q1.w * q1.w;
		float sqx = q1.x * q1.x;
		float sqy = q1.y * q1.y;
		float sqz = q1.z * q1.z;
		float heading;
		float attitude;
		float bank;
		float unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
		float test = q1.x * q1.y + q1.z * q1.w;
		if (test > 0.499 * unit) { // singularity at north pole
			heading = 2 * FastMath.atan2(q1.x, q1.w);
			attitude = FastMath.PI / 2;
			bank = 0;
			return new Vector3f(heading, attitude, bank);
		}
		if (test < -0.499 * unit) { // singularity at south pole
			heading = -2 * FastMath.atan2(q1.x, q1.w);
			attitude = -FastMath.PI / 2;
			bank = 0;
			return new Vector3f(heading, attitude, bank);
		}
		heading = FastMath.atan2(2 * q1.y * q1.w - 2 * q1.x * q1.z, sqx - sqy - sqz + sqw);
		attitude = FastMath.asin(2 * test / unit);
		bank = FastMath.atan2(2 * q1.x * q1.w - 2 * q1.y * q1.z, -sqx + sqy - sqz + sqw);
		return new Vector3f(heading, attitude, bank);
	}


	/**
	 * USE THIS METHOD TO SET FROM ROTATION MATRIXES!
	 * The method in VectorMath Quat4f has a BUG!
	 *
	 * @param m1
	 * @param q
	 */
	public static final Quat4f set(Matrix3f m1, Quat4f q) {
		float tr = m1.m00 + m1.m11 + m1.m22;
		if (tr > 0) {
			float S = FastMath.sqrt(tr + 1.0f) * 2f; // S=4*qw
			q.w = 0.25f * S;
			q.x = (m1.m21 - m1.m12) / S;
			q.y = (m1.m02 - m1.m20) / S;
			q.z = (m1.m10 - m1.m01) / S;
		} else if ((m1.m00 > m1.m11) && (m1.m00 > m1.m22)) {
			float S = FastMath.sqrt(1.0f + m1.m00 - m1.m11 - m1.m22) * 2f; // S=4*qx
			q.w = (m1.m21 - m1.m12) / S;
			q.x = 0.25f * S;
			q.y = (m1.m01 + m1.m10) / S;
			q.z = (m1.m02 + m1.m20) / S;
		} else if (m1.m11 > m1.m22) {
			float S = FastMath.sqrt(1.0f + m1.m11 - m1.m00 - m1.m22) * 2f; // S=4*qy
			q.w = (m1.m02 - m1.m20) / S;
			q.x = (m1.m01 + m1.m10) / S;
			q.y = 0.25f * S;
			q.z = (m1.m12 + m1.m21) / S;
		} else {
			float S = FastMath.sqrt(1.0f + m1.m22 - m1.m00 - m1.m11) * 2f; // S=4*qz
			q.w = (m1.m10 - m1.m01) / S;
			q.x = (m1.m02 + m1.m20) / S;
			q.y = (m1.m12 + m1.m21) / S;
			q.z = 0.25f * S;
		}
		return q;
	}

	public static boolean isZero(Quat4f q) {
		return q.x == 0 && q.y == 0 && q.z == 0 && q.w == 0;
	}

	public static Quat4f getNewQuat(float x, float y, float z, float w) {
		Quat4f quat4f = new Quat4f();
		quat4f.x = x;
		quat4f.y = y;
		quat4f.z = z;
		quat4f.w = w;
		return quat4f;
	}

	public static float toAngleAxis(Quat4f q1, Vector3f outAxis) {
		if (q1.w > 1)
			q1.normalize(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised

		float angle = (2 * FastMath.acos(q1.w));
		float s = FastMath.sqrt(1 - q1.w * q1.w); // assuming quaternion normalised then w is less than 1, so term always positive.
		if (s < 0.001) { // test to avoid divide by zero, s is always positive due to sqrt
			// if s close to zero then direction of axis not important
			outAxis.x = q1.x; // if it is important that axis is normalised then replace with x=1; y=z=0;
			outAxis.y = q1.y;
			outAxis.z = q1.z;
		} else {
			outAxis.x = q1.x / s; // normalise axis
			outAxis.y = q1.y / s;
			outAxis.z = q1.z / s;
		}

		return angle;
	}

	public static void rotateVector3f(Quat4f rotation, Vector3f point) {
		float num = rotation.x * 2f;
		float num2 = rotation.y * 2f;
		float num3 = rotation.z * 2f;
		float num4 = rotation.x * num;
		float num5 = rotation.y * num2;
		float num6 = rotation.z * num3;
		float num7 = rotation.x * num2;
		float num8 = rotation.x * num3;
		float num9 = rotation.y * num3;
		float num10 = rotation.w * num;
		float num11 = rotation.w * num2;
		float num12 = rotation.w * num3;

		point.set((1f - (num5 + num6)) * point.x + (num7 - num12) * point.y + (num8 + num11) * point.z,
				  (num7 + num12) * point.x + (1f - (num4 + num6)) * point.y + (num9 - num10) * point.z,
				  (num8 - num11) * point.x + (num9 + num10) * point.y + (1f - (num4 + num5)) * point.z);
	}
}
