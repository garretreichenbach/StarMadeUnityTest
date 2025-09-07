package org.schema.common.util.linAlg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;

public class TransformTools {

	public static final float eps = BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON;
	public static final Transform ident;

	static {
		ident = new Transform();
		ident.setIdentity();
	}

	public static float calculateDiffAxisAngle(Transform transform0, Transform transform1, Vector3f axis, Matrix3f tmp, Matrix3f dmat, Quat4f dorn) {
		// #ifdef USE_QUATERNION_DIFF
		//		btQuaternion orn0 = transform0.getRotation();
		//		btQuaternion orn1a = transform1.getRotation();
		//		btQuaternion orn1 = orn0.farthest(orn1a);
		//		btQuaternion dorn = orn1 * orn0.inverse();
		// #else
		//		Matrix3f tmp = new @Stack Matrix3f();
		tmp.set(transform0.basis);
		invert(tmp);

		//		Matrix3f dmat = new @Stack Matrix3f();
		dmat.mul(transform1.basis, tmp);

		//		Quat4f dorn = new @Stack Quat4f();
		MatrixUtil.getRotation(dmat, dorn);
		// #endif

		// floating point inaccuracy can lead to w component > 1..., which breaks

		dorn.normalize();

		float angle = QuaternionTools.getAngle(dorn);
		axis.set(dorn.x, dorn.y, dorn.z);
		// TODO: probably not needed
		//axis[3] = btScalar(0.);

		// check for axis length
		float len = axis.lengthSquared();
		if (len < eps) {
			axis.set(1f, 0f, 0f);
		} else {
			axis.scale(1f / (float) Math.sqrt(len));
		}
		return angle;
	}

	public static void calculateVelocity(Transform transform0, Transform transform1, float timeStep, Vector3f linVel, Vector3f angVel, Vector3f axis, Matrix3f tmp, Matrix3f dmat, Quat4f dorn) {
		linVel.sub(transform1.origin, transform0.origin);
		linVel.scale(1f / timeStep);

		//		Vector3f axis = new @Stack Vector3f();
		float angle = calculateDiffAxisAngle(transform0, transform1, axis, tmp, dmat, dorn);
		angVel.scale(angle / timeStep, axis);
	}

	private static float cofac(Matrix3f mat, int r1, int c1, int r2, int c2) {
		return mat.getElement(r1, c1) * mat.getElement(r2, c2) - mat.getElement(r1, c2) * mat.getElement(r2, c1);
	}

	public static void getRotation(Matrix3f mat, Quat4f dest, float[] float4Tmp) {
		//		ArrayPool<float[]> floatArrays = ArrayPool.get(float.class);

		float trace = mat.m00 + mat.m11 + mat.m22;
		float[] temp = float4Tmp;//floatArrays.getFixed(4);

		if (trace > 0f) {
			float s = (float) Math.sqrt(trace + 1f);
			temp[3] = (s * 0.5f);
			s = 0.5f / s;

			temp[0] = ((mat.m21 - mat.m12) * s);
			temp[1] = ((mat.m02 - mat.m20) * s);
			temp[2] = ((mat.m10 - mat.m01) * s);
		} else {
			int i = mat.m00 < mat.m11 ? (mat.m11 < mat.m22 ? 2 : 1) : (mat.m00 < mat.m22 ? 2 : 0);
			int j = (i + 1) % 3;
			int k = (i + 2) % 3;

			float s = (float) Math.sqrt(mat.getElement(i, i) - mat.getElement(j, j) - mat.getElement(k, k) + 1f);
			temp[i] = s * 0.5f;
			s = 0.5f / s;

			temp[3] = (mat.getElement(k, j) - mat.getElement(j, k)) * s;
			temp[j] = (mat.getElement(j, i) + mat.getElement(i, j)) * s;
			temp[k] = (mat.getElement(k, i) + mat.getElement(i, k)) * s;
		}
		dest.set(temp[0], temp[1], temp[2], temp[3]);

		//		floatArrays.release(temp);
	}

	public static void getRotation(Transform mat, Quat4f dest, float[] float4Tmp) {
		getRotation(mat.basis, dest, float4Tmp);
	}

	public static void integrateTransform(Transform curTrans, Vector3f linvel, Vector3f angvel, float timeStep, Transform predictedTransform,
	                                      Vector3f axis, Quat4f dorn, Quat4f orn0, Quat4f predictedOrn, float[] float4Tmp) {

		if (timeStep == 0 || (angvel.lengthSquared() < 0.001 && linvel.lengthSquared() < 0.001)) {
//			System.err.println("NOTE: zero timestep on integrate transform");
			predictedTransform.set(curTrans);
			return;
		}
		predictedTransform.origin.scaleAdd(timeStep, linvel, curTrans.origin);

		if(angvel.lengthSquared() < 0.001) {
			predictedTransform.basis.set(curTrans.basis);
			return;
		}
		//	//#define QUATERNION_DERIVATIVE
		//	#ifdef QUATERNION_DERIVATIVE
		//		btQuaternion predictedOrn = curTrans.getRotation();
		//		predictedOrn += (angvel * predictedOrn) * (timeStep * btScalar(0.5));
		//		predictedOrn.normalize();
		//	#else
		// Exponential map
		// google for "Practical Parameterization of Rotations Using the Exponential Map", F. Sebastian Grassia

		//		Vector3f axis = new @Stack Vector3f();
		float fAngle = angvel.length();

		// limit the angular motion
		if (fAngle * timeStep > TransformUtil.ANGULAR_MOTION_THRESHOLD) {
			fAngle = TransformUtil.ANGULAR_MOTION_THRESHOLD / timeStep;
		}

		if (fAngle < 0.001f) {
			// use Taylor's expansions of sync function
			axis.scale(0.5f * timeStep - (timeStep * timeStep * timeStep) * (0.020833333333f) * fAngle * fAngle, angvel);
		} else {
			// sync(fAngle) = sin(c*fAngle)/t
			axis.scale((float) Math.sin(0.5f * fAngle * timeStep) / fAngle, angvel);
		}
		//		Quat4f dorn = new @Stack Quat4f();
		dorn.set(axis.x, axis.y, axis.z, (float) Math.cos(fAngle * timeStep * 0.5f));
		getRotation(curTrans, orn0, float4Tmp);//new @Stack Quat4f());

		//		Quat4f predictedOrn = new @Stack Quat4f();
		predictedOrn.mul(dorn, orn0);
		predictedOrn.normalize();
		//  #endif
		float d = predictedOrn.x * predictedOrn.x + predictedOrn.y * predictedOrn.y + predictedOrn.z * predictedOrn.z + predictedOrn.w * predictedOrn.w;
		
		assert(d != 0f):"timestep: "+timeStep+"; ang "+angvel+"; linVel: "+linvel+"; Axis: "+axis;
		predictedTransform.setRotation(predictedOrn);
	}

	public static void invert(Matrix3f mat) {
		float co_x = cofac(mat, 1, 1, 2, 2);
		float co_y = cofac(mat, 1, 2, 2, 0);
		float co_z = cofac(mat, 1, 0, 2, 1);

		float det = mat.m00 * co_x + mat.m01 * co_y + mat.m02 * co_z;
		assert (det != 0f) : "\n" + mat;

		float s = 1f / det;
		float m00 = co_x * s;
		float m01 = cofac(mat, 0, 2, 2, 1) * s;
		float m02 = cofac(mat, 0, 1, 1, 2) * s;
		float m10 = co_y * s;
		float m11 = cofac(mat, 0, 0, 2, 2) * s;
		float m12 = cofac(mat, 0, 2, 1, 0) * s;
		float m20 = co_z * s;
		float m21 = cofac(mat, 0, 1, 2, 0) * s;
		float m22 = cofac(mat, 0, 0, 1, 1) * s;

		mat.m00 = m00;
		mat.m01 = m01;
		mat.m02 = m02;
		mat.m10 = m10;
		mat.m11 = m11;
		mat.m12 = m12;
		mat.m20 = m20;
		mat.m21 = m21;
		mat.m22 = m22;
	}

	public static void main(String[] asd) {
		Matrix3f s = new Matrix3f();

		s.setIdentity();
		System.err.println("MMMMMM\n" + s);
		MatrixUtil.invert(s);
	}

	public static void rotateAroundPoint(Vector3f center, Matrix3f rot,
	                                     Transform inOut, Transform tmp) {

		tmp.setIdentity();
		tmp.origin.set(center);
		tmp.origin.negate();

		Matrix4fTools.transformMul(inOut, tmp);

		tmp.setIdentity();
		tmp.basis.set(rot);
		Matrix4fTools.transformMul(inOut, tmp);

		tmp.setIdentity();
		tmp.origin.set(center);
		Matrix4fTools.transformMul(inOut, tmp);

	}

	public static void serializeFully(DataOutput stream, Transform t) throws IOException {
		stream.writeFloat(t.origin.x);
		stream.writeFloat(t.origin.y);
		stream.writeFloat(t.origin.z);

		stream.writeFloat(t.basis.m00);
		stream.writeFloat(t.basis.m01);
		stream.writeFloat(t.basis.m02);

		stream.writeFloat(t.basis.m10);
		stream.writeFloat(t.basis.m11);
		stream.writeFloat(t.basis.m12);

		stream.writeFloat(t.basis.m20);
		stream.writeFloat(t.basis.m21);
		stream.writeFloat(t.basis.m22);
	}

	public static Transform deserializeFully(DataInput stream, Transform out) throws IOException {
		out.origin.x = stream.readFloat();
		out.origin.y = stream.readFloat();
		out.origin.z = stream.readFloat();

		out.basis.m00 = stream.readFloat();
		out.basis.m01 = stream.readFloat();
		out.basis.m02 = stream.readFloat();

		out.basis.m10 = stream.readFloat();
		out.basis.m11 = stream.readFloat();
		out.basis.m12 = stream.readFloat();

		out.basis.m20 = stream.readFloat();
		out.basis.m21 = stream.readFloat();
		out.basis.m22 = stream.readFloat();

		return out;
	}

	public static boolean isNan(Transform out) {
//		System.err.println("MMMM "+out.getMatrix(new Matrix4f()));
		if(0 == (out.basis.m00) &&
				0 == (out.basis.m01) &&
				0 == (out.basis.m02) &&

				0 == (out.basis.m10) &&
				0 == (out.basis.m11) &&
				0 == (out.basis.m12) &&

				0 == (out.basis.m20) &&
				0 == (out.basis.m21) &&
				0 == (out.basis.m22)){
//			System.err.println("ROTATION ZERO!!!!!!!!!");
			return false;
		}
		return Float.isNaN(out.origin.x) ||
				Float.isNaN(out.origin.y) ||
				Float.isNaN(out.origin.z) ||

				Float.isNaN(out.basis.m00) ||
				Float.isNaN(out.basis.m01) ||
				Float.isNaN(out.basis.m02) ||

				Float.isNaN(out.basis.m10) ||
				Float.isNaN(out.basis.m11) ||
				Float.isNaN(out.basis.m12) ||

				Float.isNaN(out.basis.m20) ||
				Float.isNaN(out.basis.m21) ||
				Float.isNaN(out.basis.m22);
		
		
		
	}

//	public static Transform getFromMatrix4f(Matrix4f m, Transform transform) {
//		transform.setIdentity();
//		transform.basis.m00 = m.m00;
//		transform.basis.m01 = m.m01;
//		transform.basis.m02 = m.m02;
//		transform.basis.m10 = m.m10;
//		transform.basis.m11 = m.m11;
//		transform.basis.m12 = m.m12;
//		transform.basis.m20 = m.m20;
//		transform.basis.m21 = m.m21;
//		transform.basis.m22 = m.m22;
//
//		transform.origin.x = m.m03;
//		transform.origin.y = m.m13;
//		transform.origin.z = m.m23;
////		transform.origin.x = m.m30;
////		transform.origin.y = m.m31;
////		transform.origin.z = m.m32;
//		return transform;
//	}

//	public static void readTransform(
//			DataInputStream b, Transform out) throws IOException {
//		out.basis.m00 = b.readFloat();
//		out.basis.m01 = b.readFloat();
//		out.basis.m02 = b.readFloat();
//		out.basis.m10 = b.readFloat();
//		out.basis.m11 = b.readFloat();
//		out.basis.m12 = b.readFloat();
//		out.basis.m20 = b.readFloat();
//		out.basis.m21 = b.readFloat();
//		out.basis.m22 = b.readFloat();
//		out.origin.x = b.readFloat();
//		out.origin.y = b.readFloat();
//		out.origin.z = b.readFloat();
//	}
//	public static void writeTransform(
//			DataOutputStream b, Transform t) throws IOException {
//		b.writeFloat(t.basis.m00);
//		b.writeFloat(t.basis.m01);
//		b.writeFloat(t.basis.m02);
//		b.writeFloat(t.basis.m10);
//		b.writeFloat(t.basis.m11);
//		b.writeFloat(t.basis.m12);
//		b.writeFloat(t.basis.m20);
//		b.writeFloat(t.basis.m21);
//		b.writeFloat(t.basis.m22);
//		b.writeFloat(t.origin.x);
//		b.writeFloat(t.origin.y);
//		b.writeFloat(t.origin.z);
//	}

}
