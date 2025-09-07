package org.schema.common.util.linAlg;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;

import com.bulletphysics.linearmath.Transform;

public class Matrix4fTools {
	public static float[] getCoefficients(Matrix4f mat) {
		float[] coeff = new float[]{
				mat.m00, mat.m01, mat.m02, mat.m03,
				mat.m10, mat.m11, mat.m12, mat.m13,
				mat.m20, mat.m21, mat.m22, mat.m23,
				mat.m30, mat.m31, mat.m32, mat.m33
		};

		//		 float[] coeff = new float[]{
		//				   matTmp.m00, matTmp.m10, matTmp.m20, matTmp.m30,
		//				   matTmp.m01, matTmp.m11, matTmp.m21, matTmp.m31,
		//				   matTmp.m02, matTmp.m12, matTmp.m22, matTmp.m32,
		//				   matTmp.m03, matTmp.m13, matTmp.m23, matTmp.m33
		//		   };
		return coeff;
	}
	/**
	 * Transform a Vector by a matrix and return the result in a destination
	 * vector.
	 * @param left The left matrix
	 * @param right The right vector
	 * @param dest The destination vector, or null if a new one is to be created
	 * @return the destination vector
	 */
	public static Vector4f transform(Matrix4f left, Vector4f right, Vector4f dest) {
		if (dest == null)
			dest = new Vector4f();

		float x = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z + left.m30 * right.w;
		float y = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z + left.m31 * right.w;
		float z = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z + left.m32 * right.w;
		float w = left.m03 * right.x + left.m13 * right.y + left.m23 * right.z + left.m33 * right.w;

		dest.x = x;
		dest.y = y;
		dest.z = z;
		dest.w = w;

		return dest;
	}

	// static
	public static Matrix4f infinitePerspectiveProjection(float fLeft, float fRight,
	                                                     float fBottom, float fTop,
	                                                     float fZNear, boolean directX) {
		Matrix4f projection = new Matrix4f();

		projection.m00 = (2.0f * fZNear) / (fRight - fLeft);
		projection.m11 = (2.0f * fZNear) / (fTop - fBottom);
		projection.m02 = (fRight + fLeft) / (fRight - fLeft);
		projection.m12 = (fTop + fBottom) / (fTop - fBottom);
		projection.m32 = -1;

		// infinite view frustum
		// just take the limit as far --> inf of the regular frustum
		if (directX) {
			projection.m22 = -1.0f;
			projection.m23 = -fZNear;
		} else {
			projection.m22 = -1.0f;
			projection.m23 = -2.0f * fZNear;
		}

		return projection;
	}

	// static
	public static Matrix4f perspectiveProjection(float fovYRadians, float aspect, float zNear, float zFar, boolean directX) {
		Matrix4f m = new Matrix4f(); // zero matrix

		float yScale = 1.f / FastMath.tan(0.5f * fovYRadians);
		float xScale = yScale / aspect;

		m.m00 = xScale;
		m.m11 = yScale;
		m.m32 = 1;

		if (directX) {
			m.m22 = zFar / (zNear - zFar);
			m.m23 = zNear * zFar / (zNear - zFar);
		} else {
			m.m22 = (zFar + zNear) / (zNear - zFar);
			m.m23 = 2.f * zFar * zNear / (zNear - zFar);
		}

		return m;
	}

	// static
	public static Matrix4f perspectiveProjection(float fLeft, float fRight,
	                                             float fBottom, float fTop,
	                                             float fZNear, float fZFar,
	                                             boolean directX) {
		Matrix4f projection = new Matrix4f(); // zero matrix

		projection.m00 = (2.0f * fZNear) / (fRight - fLeft);
		projection.m11 = (2.0f * fZNear) / (fTop - fBottom);
		projection.m02 = (fRight + fLeft) / (fRight - fLeft);
		projection.m12 = (fTop + fBottom) / (fTop - fBottom);
		projection.m32 = -1;

		if (directX) {
			projection.m22 = fZFar / (fZNear - fZFar);
			projection.m23 = (fZNear * fZFar) / (fZNear - fZFar);
		} else {
			projection.m22 = (fZNear + fZFar) / (fZNear - fZFar);
			projection.m23 = (2.0f * fZNear * fZFar) / (fZNear - fZFar);
		}

		return projection;
	}

	public static boolean transformEquals(Transform t, float[] m) {
		return
				m[0] == t.basis.m00 &&
						m[1] == t.basis.m10 &&
						m[2] == t.basis.m20 &&
						m[3] == 0f &&
						m[4] == t.basis.m01 &&
						m[5] == t.basis.m11 &&
						m[6] == t.basis.m21 &&
						m[7] == 0f &&
						m[8] == t.basis.m02 &&
						m[9] == t.basis.m12 &&
						m[10] == t.basis.m22 &&
						m[11] == 0f &&
						m[12] == t.origin.x &&
						m[13] == t.origin.y &&
						m[14] == t.origin.z &&
						m[15] == 1f;
	}

	public final static void transformMul(Transform mInAndResult, Transform b) {
		float a00 = mInAndResult.basis.m00;
		float a01 = mInAndResult.basis.m01;
		float a02 = mInAndResult.basis.m02;
		float a03 = mInAndResult.origin.x;
		float a10 = mInAndResult.basis.m10;
		float a11 = mInAndResult.basis.m11;
		float a12 = mInAndResult.basis.m12;
		float a13 = mInAndResult.origin.y;
		float a20 = mInAndResult.basis.m20;
		float a21 = mInAndResult.basis.m21;
		float a22 = mInAndResult.basis.m22;
		float a23 = mInAndResult.origin.z;
		float a30 = 0.0f;
		float a31 = 0.0f;
		float a32 = 0.0f;
		float a33 = 1.0f;

		float b00 = b.basis.m00;
		float b01 = b.basis.m01;
		float b02 = b.basis.m02;
		float b03 = b.origin.x;
		float b10 = b.basis.m10;
		float b11 = b.basis.m11;
		float b12 = b.basis.m12;
		float b13 = b.origin.y;
		float b20 = b.basis.m20;
		float b21 = b.basis.m21;
		float b22 = b.basis.m22;
		float b23 = b.origin.z;
		float b30 = 0.0f;
		float b31 = 0.0f;
		float b32 = 0.0f;
		float b33 = 1.0f;

		float m00, m01, m02, m03,
				m10, m11, m12, m13,
				m20, m21, m22, m23,
				m30, m31, m32, m33;  // vars for temp result matrix

		m00 = a00 * b00 + a01 * b10 +
				a02 * b20 + a03 * b30;
		m01 = a00 * b01 + a01 * b11 +
				a02 * b21 + a03 * b31;
		m02 = a00 * b02 + a01 * b12 +
				a02 * b22 + a03 * b32;
		m03 = a00 * b03 + a01 * b13 +
				a02 * b23 + a03 * b33;

		m10 = a10 * b00 + a11 * b10 +
				a12 * b20 + a13 * b30;
		m11 = a10 * b01 + a11 * b11 +
				a12 * b21 + a13 * b31;
		m12 = a10 * b02 + a11 * b12 +
				a12 * b22 + a13 * b32;
		m13 = a10 * b03 + a11 * b13 +
				a12 * b23 + a13 * b33;

		m20 = a20 * b00 + a21 * b10 +
				a22 * b20 + a23 * b30;
		m21 = a20 * b01 + a21 * b11 +
				a22 * b21 + a23 * b31;
		m22 = a20 * b02 + a21 * b12 +
				a22 * b22 + a23 * b32;
		m23 = a20 * b03 + a21 * b13 +
				a22 * b23 + a23 * b33;

		m30 = a30 * b00 + a31 * b10 +
				a32 * b20 + a33 * b30;
		m31 = a30 * b01 + a31 * b11 +
				a32 * b21 + a33 * b31;
		m32 = a30 * b02 + a31 * b12 +
				a32 * b22 + a33 * b32;
		m33 = a30 * b03 + a31 * b13 +
				a32 * b23 + a33 * b33;

		//          this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
		//          this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
		//          this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
		//          this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33;

		mInAndResult.basis.m00 = m00;
		mInAndResult.basis.m01 = m01;
		mInAndResult.basis.m02 = m02;
		mInAndResult.origin.x = m03;
		mInAndResult.basis.m10 = m10;
		mInAndResult.basis.m11 = m11;
		mInAndResult.basis.m12 = m12;
		mInAndResult.origin.y = m13;
		mInAndResult.basis.m20 = m20;
		mInAndResult.basis.m21 = m21;
		mInAndResult.basis.m22 = m22;
		mInAndResult.origin.z = m23;

	}
	
	/**
	 * 
	 * @param m
	 * @param result
	 * @return
	 */
	public static boolean ToEulerAnglesXYZ (Matrix3f m, Vector3f result) {
		//Radian& rfYAngle, Radian& rfPAngle,  Radian& rfRAngle
		float rfYAngle;
		float rfPAngle;
		float rfRAngle;
	        // rot =  cy*cz          -cy*sz           sy
	        //        cz*sx*sy+cx*sz  cx*cz-sx*sy*sz -cy*sx
	        //       -cx*cz*sy+sx*sz  cz*sx+cx*sy*sz  cx*cy

	        rfPAngle = FastMath.asin(m.m02); //[0][2]
	        if ( rfPAngle < FastMath.HALF_PI )
	        {
	            if ( rfPAngle > -FastMath.HALF_PI )
	            {
	                rfYAngle = FastMath.atan2(-m.m12,m.m22); //-m[1][2],m[2][2]
	                rfRAngle = FastMath.atan2(-m.m01,m.m00); //-m[0][1],m[0][0]
	                result.set(rfYAngle, rfPAngle, rfRAngle);
	                return true;
	            }
	            else
	            {
	                // WARNING.  Not a unique solution.
	                float fRmY = FastMath.atan2(m.m10,m.m11); //m[1][0],m[1][1]
	                rfRAngle = 0.0f;  // any angle works
	                rfYAngle = rfRAngle - fRmY;
	                result.set(rfYAngle, rfPAngle, rfRAngle);
	                return false;
	            }
	        }
	        else
	        {
	            // WARNING.  Not a unique solution.
	            float fRpY = FastMath.atan2(m.m10,m.m11); //m[1][0],m[1][1]
	            rfRAngle = 0.0f;  // any angle works
	            rfYAngle = fRpY - rfRAngle;
	            
	            result.set(rfYAngle, rfPAngle, rfRAngle);
	            return false;
	        }
	    }
    /**
	 * Translate the source matrix and stash the result in the destination matrix
	 * @param vec The vector to translate by
	 * @param src The source matrix
	 * @param dest The destination matrix or null if a new matrix is to be created
	 * @return The translated matrix
	 */
	public static Matrix4f translate(Vector3f vec, Matrix4f src, Matrix4f dest) {
		if (dest == null)
			dest = new Matrix4f();

		dest.m30 += src.m00 * vec.x + src.m10 * vec.y + src.m20 * vec.z;
		dest.m31 += src.m01 * vec.x + src.m11 * vec.y + src.m21 * vec.z;
		dest.m32 += src.m02 * vec.x + src.m12 * vec.y + src.m22 * vec.z;
		dest.m33 += src.m03 * vec.x + src.m13 * vec.y + src.m23 * vec.z;

		return dest;
	}
    public static void translate(Matrix4f translation, Vector3f v) {
		translate(v, translation, translation);
	}
    /**
     * Multiplies this matrix to another matrix.
     *
     * @param other The other matrix
     *
     * Result is put into m
     */
    public static void mul(Matrix4f m, Matrix4f other) {

        float m00 = m.m00 * other.m00 + m.m01 * other.m10 + m.m02 * other.m20 + m.m03 * other.m30;
        float m10 = m.m10 * other.m00 + m.m11 * other.m10 + m.m12 * other.m20 + m.m13 * other.m30;
        float m20 = m.m20 * other.m00 + m.m21 * other.m10 + m.m22 * other.m20 + m.m23 * other.m30;
        float m30 = m.m30 * other.m00 + m.m31 * other.m10 + m.m32 * other.m20 + m.m33 * other.m30;

        float m01 = m.m00 * other.m01 + m.m01 * other.m11 + m.m02 * other.m21 + m.m03 * other.m31;
        float m11 = m.m10 * other.m01 + m.m11 * other.m11 + m.m12 * other.m21 + m.m13 * other.m31;
        float m21 = m.m20 * other.m01 + m.m21 * other.m11 + m.m22 * other.m21 + m.m23 * other.m31;
        float m31 = m.m30 * other.m01 + m.m31 * other.m11 + m.m32 * other.m21 + m.m33 * other.m31;

        float m02 = m.m00 * other.m02 + m.m01 * other.m12 + m.m02 * other.m22 + m.m03 * other.m32;
        float m12 = m.m10 * other.m02 + m.m11 * other.m12 + m.m12 * other.m22 + m.m13 * other.m32;
        float m22 = m.m20 * other.m02 + m.m21 * other.m12 + m.m22 * other.m22 + m.m23 * other.m32;
        float m32 = m.m30 * other.m02 + m.m31 * other.m12 + m.m32 * other.m22 + m.m33 * other.m32;

        float m03 = m.m00 * other.m03 + m.m01 * other.m13 + m.m02 * other.m23 + m.m03 * other.m33;
        float m13 = m.m10 * other.m03 + m.m11 * other.m13 + m.m12 * other.m23 + m.m13 * other.m33;
        float m23 = m.m20 * other.m03 + m.m21 * other.m13 + m.m22 * other.m23 + m.m23 * other.m33;
        float m33 = m.m30 * other.m03 + m.m31 * other.m13 + m.m32 * other.m23 + m.m33 * other.m33;

        
        m.m00 = m00;
		m.m10 = m10;
		m.m20 = m20;
		m.m30 = m30;
		
		m.m01 = m01;
		m.m11 = m11;
		m.m21 = m21;
		m.m31 = m31;
		
		m.m02 = m02;
		m.m12 = m12;
		m.m22 = m22;
		m.m32 = m32;
		
		m.m03 = m03;
		m.m13 = m13;
		m.m23 = m23;
		m.m33 = m33;
        
    }
    /**
	 * Multiply the right matrix by the left and place the result in a third matrix.
	 * @param left The left source matrix
	 * @param right The right source matrix
	 * @param dest The destination matrix, or null if a new one is to be created
	 * @return the destination matrix
	 */
	public static Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f dest) {
		if (dest == null)
			dest = new Matrix4f();

		float m00 = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03;
		float m01 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03;
		float m02 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03;
		float m03 = left.m03 * right.m00 + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03;
		float m10 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12 + left.m30 * right.m13;
		float m11 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12 + left.m31 * right.m13;
		float m12 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12 + left.m32 * right.m13;
		float m13 = left.m03 * right.m10 + left.m13 * right.m11 + left.m23 * right.m12 + left.m33 * right.m13;
		float m20 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22 + left.m30 * right.m23;
		float m21 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22 + left.m31 * right.m23;
		float m22 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22 + left.m32 * right.m23;
		float m23 = left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * right.m22 + left.m33 * right.m23;
		float m30 = left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * right.m33;
		float m31 = left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * right.m33;
		float m32 = left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * right.m33;
		float m33 = left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * right.m33;

		dest.m00 = m00;
		dest.m01 = m01;
		dest.m02 = m02;
		dest.m03 = m03;
		dest.m10 = m10;
		dest.m11 = m11;
		dest.m12 = m12;
		dest.m13 = m13;
		dest.m20 = m20;
		dest.m21 = m21;
		dest.m22 = m22;
		dest.m23 = m23;
		dest.m30 = m30;
		dest.m31 = m31;
		dest.m32 = m32;
		dest.m33 = m33;

		return dest;
	}
   
	/**
	 * Scales the source matrix and put the result in the destination matrix
	 * @param vec The vector to scale by
	 * @param src The source matrix
	 * @param dest The destination matrix, or null if a new matrix is to be created
	 * @return The scaled matrix
	 */
	public static Matrix4f scale(Vector3f vec, Matrix4f src, Matrix4f dest) {
		if (dest == null)
			dest = new Matrix4f();
		dest.m00 = src.m00 * vec.x;
		dest.m01 = src.m01 * vec.x;
		dest.m02 = src.m02 * vec.x;
		dest.m03 = src.m03 * vec.x;
		dest.m10 = src.m10 * vec.y;
		dest.m11 = src.m11 * vec.y;
		dest.m12 = src.m12 * vec.y;
		dest.m13 = src.m13 * vec.y;
		dest.m20 = src.m20 * vec.z;
		dest.m21 = src.m21 * vec.z;
		dest.m22 = src.m22 * vec.z;
		dest.m23 = src.m23 * vec.z;
		return dest;
	}
	public static void scale(Matrix4f scaling, Vector3f v) {
		scale(v, scaling, scaling);
	}
	/**
	 * Rotates the source matrix around the given axis the specified angle and
	 * put the result in the destination matrix.
	 * @param angle the angle, in radians.
	 * @param axis The vector representing the rotation axis. Must be normalized.
	 * @param src The matrix to rotate
	 * @param dest The matrix to put the result, or null if a new matrix is to be created
	 * @return The rotated matrix
	 */
	public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest) {
		if (dest == null)
			dest = new Matrix4f();
		float c = (float) Math.cos(angle);
		float s = (float) Math.sin(angle);
		float oneminusc = 1.0f - c;
		float xy = axis.x*axis.y;
		float yz = axis.y*axis.z;
		float xz = axis.x*axis.z;
		float xs = axis.x*s;
		float ys = axis.y*s;
		float zs = axis.z*s;

		float f00 = axis.x*axis.x*oneminusc+c;
		float f01 = xy*oneminusc+zs;
		float f02 = xz*oneminusc-ys;
		// n[3] not used
		float f10 = xy*oneminusc-zs;
		float f11 = axis.y*axis.y*oneminusc+c;
		float f12 = yz*oneminusc+xs;
		// n[7] not used
		float f20 = xz*oneminusc+ys;
		float f21 = yz*oneminusc-xs;
		float f22 = axis.z*axis.z*oneminusc+c;

		float t00 = src.m00 * f00 + src.m10 * f01 + src.m20 * f02;
		float t01 = src.m01 * f00 + src.m11 * f01 + src.m21 * f02;
		float t02 = src.m02 * f00 + src.m12 * f01 + src.m22 * f02;
		float t03 = src.m03 * f00 + src.m13 * f01 + src.m23 * f02;
		float t10 = src.m00 * f10 + src.m10 * f11 + src.m20 * f12;
		float t11 = src.m01 * f10 + src.m11 * f11 + src.m21 * f12;
		float t12 = src.m02 * f10 + src.m12 * f11 + src.m22 * f12;
		float t13 = src.m03 * f10 + src.m13 * f11 + src.m23 * f12;
		dest.m20 = src.m00 * f20 + src.m10 * f21 + src.m20 * f22;
		dest.m21 = src.m01 * f20 + src.m11 * f21 + src.m21 * f22;
		dest.m22 = src.m02 * f20 + src.m12 * f21 + src.m22 * f22;
		dest.m23 = src.m03 * f20 + src.m13 * f21 + src.m23 * f22;
		dest.m00 = t00;
		dest.m01 = t01;
		dest.m02 = t02;
		dest.m03 = t03;
		dest.m10 = t10;
		dest.m11 = t11;
		dest.m12 = t12;
		dest.m13 = t13;
		return dest;
	}
    public static Matrix4f rotate( Matrix4f rotation, float angle, Vector3f v) {
    	return rotate(angle, v, rotation, rotation);
    }
	public static void store(Matrix4f m, float[] b) {
	//    	b[0] = m.m00;
	//		b[1] = m.m10;
	//		b[2] = m.m20;
	//		b[3] = m.m30;
	//		
	//		b[4] = m.m01;
	//		b[5] = m.m11;
	//		b[6] = m.m21;
	//		b[7] = m.m31;
	//		
	//		b[8] = m.m02;
	//		b[9] = m.m12;
	//		b[10] = m.m22;
	//		b[11] = m.m32;
	//		
	//		b[12] = m.m03;
	//		b[13] = m.m13;
	//		b[14] = m.m23;
	//		b[15] = m.m33;
	    	
	    	b[0] = (m.m00);
			b[1] = (m.m01);
			b[2] = (m.m02);
			b[3] = (m.m03);
			b[4] = (m.m10);
			b[5] = (m.m11);
			b[6] = (m.m12);
			b[7] = (m.m13);
			b[8] = (m.m20);
			b[9] = (m.m21);
			b[10] = (m.m22);
			b[11] = (m.m23);
			b[12] = (m.m30);
			b[13] = (m.m31);
			b[14] = (m.m32);
			b[15] = (m.m33);
	    	
	    }
	/**
	     * Stores the matrix in a given Buffer.
	     *
	     * @param buffer The buffer to store the matrix data
	     */
	    public static void store(Matrix4f m, FloatBuffer buffer) {
	//        buffer.put(m.m00).put(m.m10).put(m.m20).put(m.m30);
	//        buffer.put(m.m01).put(m.m11).put(m.m21).put(m.m31);
	//        buffer.put(m.m02).put(m.m12).put(m.m22).put(m.m32);
	//        buffer.put(m.m03).put(m.m13).put(m.m23).put(m.m33);
	        
	        buffer.put(m.m00);
			buffer.put(m.m01);
			buffer.put(m.m02);
			buffer.put(m.m03);
			buffer.put(m.m10);
			buffer.put(m.m11);
			buffer.put(m.m12);
			buffer.put(m.m13);
			buffer.put(m.m20);
			buffer.put(m.m21);
			buffer.put(m.m22);
			buffer.put(m.m23);
			buffer.put(m.m30);
			buffer.put(m.m31);
			buffer.put(m.m32);
			buffer.put(m.m33);
	    }
	public static void store(Matrix3f m, FloatBuffer buffer) {
		buffer.put(m.m00);
		buffer.put(m.m01);
		buffer.put(m.m02);
		buffer.put(m.m10);
		buffer.put(m.m11);
		buffer.put(m.m12);
		buffer.put(m.m20);
		buffer.put(m.m21);
		buffer.put(m.m22);
	}
	public static void load(FloatBuffer from, Matrix3f out) {
		out.m00 = from.get();
		out.m01 = from.get();
		out.m02 = from.get();
		out.m10 = from.get();
		out.m11 = from.get();
		out.m12 = from.get();
		out.m20 = from.get();
		out.m21 = from.get();
		out.m22 = from.get();
	}
	public static void load(FloatBuffer from, Matrix4f out) {
//		out.m00 = from.get();
//		out.m10 = from.get();
//		out.m20 = from.get();
//		out.m30 = from.get();
//		
//		out.m01 = from.get();
//		out.m11 = from.get();
//		out.m21 = from.get();
//		out.m31 = from.get();
//		
//		out.m02 = from.get();
//		out.m12 = from.get();
//		out.m22 = from.get();
//		out.m32 = from.get();
//		
//		out.m03 = from.get();
//		out.m13 = from.get();
//		out.m23 = from.get();
//		out.m33 = from.get();
		
		
		out.m00 = from.get();
		out.m01 = from.get();
		out.m02 = from.get();
		out.m03 = from.get();
		out.m10 = from.get();
		out.m11 = from.get();
		out.m12 = from.get();
		out.m13 = from.get();
		out.m20 = from.get();
		out.m21 = from.get();
		out.m22 = from.get();
		out.m23 = from.get();
		out.m30 = from.get();
		out.m31 = from.get();
		out.m32 = from.get();
		out.m33 = from.get();
	}
	public static void load(float[] from, Matrix4f out) {
//		out.m00 = from[0];
//		out.m10 = from[1];
//		out.m20 = from[2];
//		out.m30 = from[3];
//		
//		out.m01 = from[4];
//		out.m11 = from[5];
//		out.m21 = from[6];
//		out.m31 = from[7];
//		
//		out.m02 = from[8];
//		out.m12 = from[9];
//		out.m22 = from[10];
//		out.m32 = from[11];
//		
//		out.m03 = from[12];
//		out.m13 = from[13];
//		out.m23 = from[14];
//		out.m33 = from[15];
		
		
		out.m00 = from[0];
		out.m01 = from[1];
		out.m02 = from[2];
		out.m03 = from[3];
		out.m10 = from[4];
		out.m11 = from[5];
		out.m12 = from[6];
		out.m13 = from[7];
		out.m20 = from[8];
		out.m21 = from[9];
		out.m22 = from[10];
		out.m23 = from[11];
		out.m30 = from[12];
		out.m31 = from[13];
		out.m32 = from[14];
		out.m33 = from[15];
	}

	/**
	 * all done in local variables for performance
	 * @param projection
	 * @param modelview
	 * @param winX
	 * @param winY
	 * @param winZ
	 * @param viewport
	 * @param dest
	 * @return
	 */
	public static Vector4f unproject(Matrix4f projection, Matrix4f modelview, float winX, float winY, float winZ,
			int[] viewport, Vector4f dest) {

		// multiply matrices
		float m00 = projection.m00 * modelview.m00 + projection.m01 * modelview.m10 + projection.m02 * modelview.m20
				+ projection.m03 * modelview.m30;
		float m10 = projection.m10 * modelview.m00 + projection.m11 * modelview.m10 + projection.m12 * modelview.m20
				+ projection.m13 * modelview.m30;
		float m20 = projection.m20 * modelview.m00 + projection.m21 * modelview.m10 + projection.m22 * modelview.m20
				+ projection.m23 * modelview.m30;
		float m30 = projection.m30 * modelview.m00 + projection.m31 * modelview.m10 + projection.m32 * modelview.m20
				+ projection.m33 * modelview.m30;

		float m01 = projection.m00 * modelview.m01 + projection.m01 * modelview.m11 + projection.m02 * modelview.m21
				+ projection.m03 * modelview.m31;
		float m11 = projection.m10 * modelview.m01 + projection.m11 * modelview.m11 + projection.m12 * modelview.m21
				+ projection.m13 * modelview.m31;
		float m21 = projection.m20 * modelview.m01 + projection.m21 * modelview.m11 + projection.m22 * modelview.m21
				+ projection.m23 * modelview.m31;
		float m31 = projection.m30 * modelview.m01 + projection.m31 * modelview.m11 + projection.m32 * modelview.m21
				+ projection.m33 * modelview.m31;

		float m02 = projection.m00 * modelview.m02 + projection.m01 * modelview.m12 + projection.m02 * modelview.m22
				+ projection.m03 * modelview.m32;
		float m12 = projection.m10 * modelview.m02 + projection.m11 * modelview.m12 + projection.m12 * modelview.m22
				+ projection.m13 * modelview.m32;
		float m22 = projection.m20 * modelview.m02 + projection.m21 * modelview.m12 + projection.m22 * modelview.m22
				+ projection.m23 * modelview.m32;
		float m32 = projection.m30 * modelview.m02 + projection.m31 * modelview.m12 + projection.m32 * modelview.m22
				+ projection.m33 * modelview.m32;

		float m03 = projection.m00 * modelview.m03 + projection.m01 * modelview.m13 + projection.m02 * modelview.m23
				+ projection.m03 * modelview.m33;
		float m13 = projection.m10 * modelview.m03 + projection.m11 * modelview.m13 + projection.m12 * modelview.m23
				+ projection.m13 * modelview.m33;
		float m23 = projection.m20 * modelview.m03 + projection.m21 * modelview.m13 + projection.m22 * modelview.m23
				+ projection.m23 * modelview.m33;
		float m33 = projection.m30 * modelview.m03 + projection.m31 * modelview.m13 + projection.m32 * modelview.m23
				+ projection.m33 * modelview.m33;

		// invert matrix
		float a = m00 * m11 - m01 * m10;
		float b = m00 * m12 - m02 * m10;
		float c = m00 * m13 - m03 * m10;
		float d = m01 * m12 - m02 * m11;
		float e = m01 * m13 - m03 * m11;
		float f = m02 * m13 - m03 * m12;
		float g = m20 * m31 - m21 * m30;
		float h = m20 * m32 - m22 * m30;
		float i = m20 * m33 - m23 * m30;
		float j = m21 * m32 - m22 * m31;
		float k = m21 * m33 - m23 * m31;
		float l = m22 * m33 - m23 * m32;
		float det = a * l - b * k + c * j + d * i - e * h + f * g;
		det = 1.0f / det;
		float ik00 = (m11 * l - m12 * k + m13 * j) * det;
		float ik01 = (-m01 * l + m02 * k - m03 * j) * det;
		float ik02 = (m31 * f - m32 * e + m33 * d) * det;
		float ik03 = (-m21 * f + m22 * e - m23 * d) * det;
		float ik10 = (-m10 * l + m12 * i - m13 * h) * det;
		float ik11 = (m00 * l - m02 * i + m03 * h) * det;
		float ik12 = (-m30 * f + m32 * c - m33 * b) * det;
		float ik13 = (m20 * f - m22 * c + m23 * b) * det;
		float ik20 = (m10 * k - m11 * i + m13 * g) * det;
		float ik21 = (-m00 * k + m01 * i - m03 * g) * det;
		float ik22 = (m30 * e - m31 * c + m33 * a) * det;
		float ik23 = (-m20 * e + m21 * c - m23 * a) * det;
		float ik30 = (-m10 * j + m11 * h - m12 * g) * det;
		float ik31 = (m00 * j - m01 * h + m02 * g) * det;
		float ik32 = (-m30 * d + m31 * b - m32 * a) * det;
		float ik33 = (m20 * d - m21 * b + m22 * a) * det;

		// Transformation of normalized coordinates between -1 and 1
		float ndcX = (winX - viewport[0]) / viewport[2] * 2.0f - 1.0f;
		float ndcY = (winY - viewport[1]) / viewport[3] * 2.0f - 1.0f;
		float ndcZ = winZ + winZ - 1.0f;
		float invW = 1.0f / (ik03 * ndcX + ik13 * ndcY + ik23 * ndcZ + ik33);
		// Objects coordinates
		dest.x = (ik00 * ndcX + ik10 * ndcY + ik20 * ndcZ + ik30) * invW;
		dest.y = (ik01 * ndcX + ik11 * ndcY + ik21 * ndcZ + ik31) * invW;
		dest.z = (ik02 * ndcX + ik12 * ndcY + ik22 * ndcZ + ik32) * invW;
		dest.w = 1.0f;
		return dest;

	}
	  public static Vector4f unproject(Matrix4f m, float winX, float winY, float winZ, int[] viewport, Vector4f dest) {
	        float a = m.m00 * m.m11 - m.m01 * m.m10;
	        float b = m.m00 * m.m12 - m.m02 * m.m10;
	        float c = m.m00 * m.m13 - m.m03 * m.m10;
	        float d = m.m01 * m.m12 - m.m02 * m.m11;
	        float e = m.m01 * m.m13 - m.m03 * m.m11;
	        float f = m.m02 * m.m13 - m.m03 * m.m12;
	        float g = m.m20 * m.m31 - m.m21 * m.m30;
	        float h = m.m20 * m.m32 - m.m22 * m.m30;
	        float i = m.m20 * m.m33 - m.m23 * m.m30;
	        float j = m.m21 * m.m32 - m.m22 * m.m31;
	        float k = m.m21 * m.m33 - m.m23 * m.m31;
	        float l = m.m22 * m.m33 - m.m23 * m.m32;
	        float det = a * l - b * k + c * j + d * i - e * h + f * g;
	        det = 1.0f / det;
	        float ik00 = ( m.m11 * l - m.m12 * k + m.m13 * j) * det;
	        float ik01 = (-m.m01 * l + m.m02 * k - m.m03 * j) * det;
	        float ik02 = ( m.m31 * f - m.m32 * e + m.m33 * d) * det;
	        float ik03 = (-m.m21 * f + m.m22 * e - m.m23 * d) * det;
	        float ik10 = (-m.m10 * l + m.m12 * i - m.m13 * h) * det;
	        float ik11 = ( m.m00 * l - m.m02 * i + m.m03 * h) * det;
	        float ik12 = (-m.m30 * f + m.m32 * c - m.m33 * b) * det;
	        float ik13 = ( m.m20 * f - m.m22 * c + m.m23 * b) * det;
	        float ik20 = ( m.m10 * k - m.m11 * i + m.m13 * g) * det;
	        float ik21 = (-m.m00 * k + m.m01 * i - m.m03 * g) * det;
	        float ik22 = ( m.m30 * e - m.m31 * c + m.m33 * a) * det;
	        float ik23 = (-m.m20 * e + m.m21 * c - m.m23 * a) * det;
	        float ik30 = (-m.m10 * j + m.m11 * h - m.m12 * g) * det;
	        float ik31 = ( m.m00 * j - m.m01 * h + m.m02 * g) * det;
	        float ik32 = (-m.m30 * d + m.m31 * b - m.m32 * a) * det;
	        float ik33 = ( m.m20 * d - m.m21 * b + m.m22 * a) * det;
	        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
	        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
	        float ndcZ = winZ+winZ-1.0f;
	        float invW = 1.0f / (ik03 * ndcX + ik13 * ndcY + ik23 * ndcZ + ik33);
	        dest.x = (ik00 * ndcX + ik10 * ndcY + ik20 * ndcZ + ik30) * invW;
	        dest.y = (ik01 * ndcX + ik11 * ndcY + ik21 * ndcZ + ik31) * invW;
	        dest.z = (ik02 * ndcX + ik12 * ndcY + ik22 * ndcZ + ik32) * invW;
	        dest.w = 1.0f;
	        return dest;
	    }

	    /* (non-Javadoc)
	     * @see org.jom.ml.Matrix4fc#unproject(float, float, float, int[], org.jom.ml.Vector3f)
	     */
	    public static Vector3f unproject(Matrix4f m, float winX, float winY, float winZ, int[] viewport, Vector3f dest) {
	        float a = m.m00 * m.m11 - m.m01 * m.m10;
	        float b = m.m00 * m.m12 - m.m02 * m.m10;
	        float c = m.m00 * m.m13 - m.m03 * m.m10;
	        float d = m.m01 * m.m12 - m.m02 * m.m11;
	        float e = m.m01 * m.m13 - m.m03 * m.m11;
	        float f = m.m02 * m.m13 - m.m03 * m.m12;
	        float g = m.m20 * m.m31 - m.m21 * m.m30;
	        float h = m.m20 * m.m32 - m.m22 * m.m30;
	        float i = m.m20 * m.m33 - m.m23 * m.m30;
	        float j = m.m21 * m.m32 - m.m22 * m.m31;
	        float k = m.m21 * m.m33 - m.m23 * m.m31;
	        float l = m.m22 * m.m33 - m.m23 * m.m32;
	        float det = a * l - b * k + c * j + d * i - e * h + f * g;
	        det = 1.0f / det;
	        float ik00 = ( m.m11 * l - m.m12 * k + m.m13 * j) * det;
	        float ik01 = (-m.m01 * l + m.m02 * k - m.m03 * j) * det;
	        float ik02 = ( m.m31 * f - m.m32 * e + m.m33 * d) * det;
	        float ik03 = (-m.m21 * f + m.m22 * e - m.m23 * d) * det;
	        float ik10 = (-m.m10 * l + m.m12 * i - m.m13 * h) * det;
	        float ik11 = ( m.m00 * l - m.m02 * i + m.m03 * h) * det;
	        float ik12 = (-m.m30 * f + m.m32 * c - m.m33 * b) * det;
	        float ik13 = ( m.m20 * f - m.m22 * c + m.m23 * b) * det;
	        float ik20 = ( m.m10 * k - m.m11 * i + m.m13 * g) * det;
	        float ik21 = (-m.m00 * k + m.m01 * i - m.m03 * g) * det;
	        float ik22 = ( m.m30 * e - m.m31 * c + m.m33 * a) * det;
	        float ik23 = (-m.m20 * e + m.m21 * c - m.m23 * a) * det;
	        float ik30 = (-m.m10 * j + m.m11 * h - m.m12 * g) * det;
	        float ik31 = ( m.m00 * j - m.m01 * h + m.m02 * g) * det;
	        float ik32 = (-m.m30 * d + m.m31 * b - m.m32 * a) * det;
	        float ik33 = ( m.m20 * d - m.m21 * b + m.m22 * a) * det;
	        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
	        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
	        float ndcZ = winZ+winZ-1.0f;
	        float invW = 1.0f / (ik03 * ndcX + ik13 * ndcY + ik23 * ndcZ + ik33);
	        dest.x = (ik00 * ndcX + ik10 * ndcY + ik20 * ndcZ + ik30) * invW;
	        dest.y = (ik01 * ndcX + ik11 * ndcY + ik21 * ndcZ + ik31) * invW;
	        dest.z = (ik02 * ndcX + ik12 * ndcY + ik22 * ndcZ + ik32) * invW;
	        return dest;
	    }

//	    /* (non-Javadoc)
//	     * @see org.jom.ml.Matrix4fc#unproject(org.jom.ml.Vector3fc, int[], org.jom.ml.Vector4f)
//	     */
//	    public Vector4f unproject(Vector3f winCoords, int[] viewport, Vector4f dest) {
//	        return unproject(winCoords.x, winCoords.y, winCoords.z, viewport, dest);
//	    }
//
//	    /* (non-Javadoc)
//	     * @see org.jom.ml.Matrix4fc#unproject(org.jom.ml.Vector3fc, int[], org.jom.ml.Vector3f)
//	     */
//	    public Vector3f unproject(Vector3f winCoords, int[] viewport, Vector3f dest) {
//	        return unproject(winCoords.x, winCoords.y, winCoords.z, viewport, dest);
//	    }

	    /* (non-Javadoc)
	     * @see org.jom.ml.Matrix4fc#unprojectRay(float, float, int[], org.jom.ml.Vector3f, org.jom.ml.Vector3f)
	     */
	    public static Matrix4f unprojectRay(Matrix4f m, float winX, float winY, int[] viewport, Vector3f originDest, Vector3f dirDest) {
	        float a = m.m00 * m.m11 - m.m01 * m.m10;
	        float b = m.m00 * m.m12 - m.m02 * m.m10;
	        float c = m.m00 * m.m13 - m.m03 * m.m10;
	        float d = m.m01 * m.m12 - m.m02 * m.m11;
	        float e = m.m01 * m.m13 - m.m03 * m.m11;
	        float f = m.m02 * m.m13 - m.m03 * m.m12;
	        float g = m.m20 * m.m31 - m.m21 * m.m30;
	        float h = m.m20 * m.m32 - m.m22 * m.m30;
	        float i = m.m20 * m.m33 - m.m23 * m.m30;
	        float j = m.m21 * m.m32 - m.m22 * m.m31;
	        float k = m.m21 * m.m33 - m.m23 * m.m31;
	        float l = m.m22 * m.m33 - m.m23 * m.m32;
	        float det = a * l - b * k + c * j + d * i - e * h + f * g;
	        det = 1.0f / det;
	        float ik00 = ( m.m11 * l - m.m12 * k + m.m13 * j) * det;
	        float ik01 = (-m.m01 * l + m.m02 * k - m.m03 * j) * det;
	        float ik02 = ( m.m31 * f - m.m32 * e + m.m33 * d) * det;
	        float ik03 = (-m.m21 * f + m.m22 * e - m.m23 * d) * det;
	        float ik10 = (-m.m10 * l + m.m12 * i - m.m13 * h) * det;
	        float ik11 = ( m.m00 * l - m.m02 * i + m.m03 * h) * det;
	        float ik12 = (-m.m30 * f + m.m32 * c - m.m33 * b) * det;
	        float ik13 = ( m.m20 * f - m.m22 * c + m.m23 * b) * det;
	        float ik20 = ( m.m10 * k - m.m11 * i + m.m13 * g) * det;
	        float ik21 = (-m.m00 * k + m.m01 * i - m.m03 * g) * det;
	        float ik22 = ( m.m30 * e - m.m31 * c + m.m33 * a) * det;
	        float ik23 = (-m.m20 * e + m.m21 * c - m.m23 * a) * det;
	        float ik30 = (-m.m10 * j + m.m11 * h - m.m12 * g) * det;
	        float ik31 = ( m.m00 * j - m.m01 * h + m.m02 * g) * det;
	        float ik32 = (-m.m30 * d + m.m31 * b - m.m32 * a) * det;
	        float ik33 = ( m.m20 * d - m.m21 * b + m.m22 * a) * det;
	        float ndcX = (winX-viewport[0])/viewport[2]*2.0f-1.0f;
	        float ndcY = (winY-viewport[1])/viewport[3]*2.0f-1.0f;
	        float px = ik00 * ndcX + ik10 * ndcY + ik30;
	        float py = ik01 * ndcX + ik11 * ndcY + ik31;
	        float pz = ik02 * ndcX + ik12 * ndcY + ik32;
	        float invNearW = 1.0f / (ik03 * ndcX + ik13 * ndcY - ik23 + ik33);
	        float nearX = (px - ik20) * invNearW;
	        float nearY = (py - ik21) * invNearW;
	        float nearZ = (pz - ik22) * invNearW;
	        float invFarW = 1.0f / (ik03 * ndcX + ik13 * ndcY + ik23 + ik33);
	        float farX = (px + ik20) * invFarW;
	        float farY = (py + ik21) * invFarW;
	        float farZ = (pz + ik22) * invFarW;
	        originDest.x = nearX; originDest.y = nearY; originDest.z = nearZ;
	        dirDest.x = farX - nearX; dirDest.y = farY - nearY; dirDest.z = farZ - nearZ;
	        return m;
	    }

//	    /* (non-Javadoc)
//	     * @see org.joml.Matrix4fc#unprojectRay(org.joml.Vector2fc, int[], org.joml.Vector3f, org.joml.Vector3f)
//	     */
//	    public Matrix4f unprojectRay(Vector2f winCoords, int[] viewport, Vector3f originDest, Vector3f dirDest) {
//	        return unprojectRay(winCoords.x, winCoords.y, viewport, originDest, dirDest);
//	    }
//
//	    /* (non-Javadoc)
//	     * @see org.joml.Matrix4fc#unprojectInv(org.joml.Vector3fc, int[], org.joml.Vector4f)
//	     */
//	    public Vector4f unprojectInv(Vector3f winCoords, int[] viewport, Vector4f dest) {
//	        return unprojectInv(winCoords.x, winCoords.y, winCoords.z, viewport, dest);
//	    }
	
}
