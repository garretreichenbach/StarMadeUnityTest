/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Matrix16f</H2>
 * <H3>org.schema.common.util.linAlg</H3>
 * Matrix16f.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright © 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.schema.common.util.linAlg;

import java.io.Serializable;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

/**
 * <H2></H2>
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Jerome JOUVIE (Jouvieje)
 * @mail <A HREF="mailto:jerome.jouvie@gmail.com">jerome.jouvie@gmail.com</A>
 * @site <A
 * HREF="http://jerome.jouvie.free.fr/">http://jerome.jouvie.free.fr/</A>
 * @project Java OpenGl BaseCode
 * @homepage <A
 * HREF="http://jerome.jouvie.free.fr/OpenGl/BaseCode/BaseCode.php">
 * http://jerome.jouvie.free.fr/OpenGl/BaseCode/BaseCode.php</A>
 * @copyright Copyright © 2004-2008 Jerome JOUVIE (Jouvieje)
 */
public strictfp class Matrix16f implements Cloneable, Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 0x010101;
	/**
	 * The m.
	 */
	public final float[] m; // TODO Hide ?

	/**
	 * Instantiates a new matrix16f.
	 */
	public Matrix16f() {
		m = new float[16];
	}

	/**
	 * Instantiates a new matrix16f.
	 *
	 * @param m a float[16]
	 * @throws RuntimeException , when array is null or length is not 16
	 */
	public Matrix16f(float[] m) throws RuntimeException {
		if (m == null || m.length != 16) {
			throw new RuntimeException();
		}
		this.m = m;
	}

	/**
	 * Determinant.
	 *
	 * @param f the f
	 * @return the double
	 */
	public static double determinant(float[] f) {
		double value;
		value = f[3] * f[6] * f[9] * f[12] - f[2] * f[7] * f[9] * f[12] - f[3]
				* f[5] * f[10] * f[12] + f[1] * f[7] * f[10] * f[12] + f[2]
				* f[5] * f[11] * f[12] - f[1] * f[6] * f[11] * f[12] - f[3]
				* f[6] * f[8] * f[13] + f[2] * f[7] * f[8] * f[13] + f[3]
				* f[4] * f[10] * f[13] - f[0] * f[7] * f[10] * f[13] - f[2]
				* f[4] * f[11] * f[13] + f[0] * f[6] * f[11] * f[13] + f[3]
				* f[5] * f[8] * f[14] - f[1] * f[7] * f[8] * f[14] - f[3]
				* f[4] * f[9] * f[14] + f[0] * f[7] * f[9] * f[14] + f[1]
				* f[4] * f[11] * f[14] - f[0] * f[5] * f[11] * f[14] - f[2]
				* f[5] * f[8] * f[15] + f[1] * f[6] * f[8] * f[15] + f[2]
				* f[4] * f[9] * f[15] - f[0] * f[6] * f[9] * f[15] - f[1]
				* f[4] * f[10] * f[15] + f[0] * f[5] * f[10] * f[15];
		return value;
	}

	/**
	 * Create the identity matrix.
	 *
	 * @return identity matrix
	 */
	public static Matrix16f identity() {
		Matrix16f m = new Matrix16f();
		m.m[0] = 1;
		m.m[5] = 1;
		m.m[10] = 1;
		m.m[15] = 1;
		return m;
	}

	/**
	 * Invert.
	 *
	 * @param f the f
	 * @return the float[]
	 */
	public static float[] invert(float[] f) {
		float[] inv = new float[16];
		inv[0] = f[6] * f[11] * f[13] - f[7] * f[10] * f[13] + f[7] * f[9]
				* f[14] - f[5] * f[11] * f[14] - f[6] * f[9] * f[15] + f[5]
				* f[10] * f[15];
		inv[1] = f[3] * f[10] * f[13] - f[2] * f[11] * f[13] - f[3] * f[9]
				* f[14] + f[1] * f[11] * f[14] + f[2] * f[9] * f[15] - f[1]
				* f[10] * f[15];
		inv[2] = f[2] * f[7] * f[13] - f[3] * f[6] * f[13] + f[3] * f[5]
				* f[14] - f[1] * f[7] * f[14] - f[2] * f[5] * f[15] + f[1]
				* f[6] * f[15];
		inv[3] = f[3] * f[6] * f[9] - f[2] * f[7] * f[9] - f[3] * f[5] * f[10]
				+ f[1] * f[7] * f[10] + f[2] * f[5] * f[11] - f[1] * f[6]
				* f[11];
		inv[4] = f[7] * f[10] * f[12] - f[6] * f[11] * f[12] - f[7] * f[8]
				* f[14] + f[4] * f[11] * f[14] + f[6] * f[8] * f[15] - f[4]
				* f[10] * f[15];
		inv[5] = f[2] * f[11] * f[12] - f[3] * f[10] * f[12] + f[3] * f[8]
				* f[14] - f[0] * f[11] * f[14] - f[2] * f[8] * f[15] + f[0]
				* f[10] * f[15];
		inv[6] = f[3] * f[6] * f[12] - f[2] * f[7] * f[12] - f[3] * f[4]
				* f[14] + f[0] * f[7] * f[14] + f[2] * f[4] * f[15] - f[0]
				* f[6] * f[15];
		inv[7] = f[2] * f[7] * f[8] - f[3] * f[6] * f[8] + f[3] * f[4] * f[10]
				- f[0] * f[7] * f[10] - f[2] * f[4] * f[11] + f[0] * f[6]
				* f[11];
		inv[8] = f[5] * f[11] * f[12] - f[7] * f[9] * f[12] + f[7] * f[8]
				* f[13] - f[4] * f[11] * f[13] - f[5] * f[8] * f[15] + f[4]
				* f[9] * f[15];
		inv[9] = f[3] * f[9] * f[12] - f[1] * f[11] * f[12] - f[3] * f[8]
				* f[13] + f[0] * f[11] * f[13] + f[1] * f[8] * f[15] - f[0]
				* f[9] * f[15];
		inv[10] = f[1] * f[7] * f[12] - f[3] * f[5] * f[12] + f[3] * f[4]
				* f[13] - f[0] * f[7] * f[13] - f[1] * f[4] * f[15] + f[0]
				* f[5] * f[15];
		inv[11] = f[3] * f[5] * f[8] - f[1] * f[7] * f[8] - f[3] * f[4] * f[9]
				+ f[0] * f[7] * f[9] + f[1] * f[4] * f[11] - f[0] * f[5]
				* f[11];
		inv[12] = f[6] * f[9] * f[12] - f[5] * f[10] * f[12] - f[6] * f[8]
				* f[13] + f[4] * f[10] * f[13] + f[5] * f[8] * f[14] - f[4]
				* f[9] * f[14];
		inv[13] = f[1] * f[10] * f[12] - f[2] * f[9] * f[12] + f[2] * f[8]
				* f[13] - f[0] * f[10] * f[13] - f[1] * f[8] * f[14] + f[0]
				* f[9] * f[14];
		inv[14] = f[2] * f[5] * f[12] - f[1] * f[6] * f[12] - f[2] * f[4]
				* f[13] + f[0] * f[6] * f[13] + f[1] * f[4] * f[14] - f[0]
				* f[5] * f[14];
		inv[15] = f[1] * f[6] * f[8] - f[2] * f[5] * f[8] + f[2] * f[4] * f[9]
				- f[0] * f[6] * f[9] - f[1] * f[4] * f[10] + f[0] * f[5]
				* f[10];
		double det = determinant(f);
		for (int i = 0; i < f.length; i++) {
			inv[i] /= det;
		}
		return inv;
	}

	/**
	 * Look at.
	 *
	 * @param eye the eye
	 * @param ref the ref
	 * @param up  the up
	 * @return the matrix16f
	 */
	public static Matrix16f lookAt(Vector3f eye, Vector3f ref, Vector3f up) {
		Vector3f forward = Vector3fTools.sub(ref, eye);
		Vector3f left = Vector3fTools.crossProduct(up, forward);
		return lookAt(left, up, forward, eye);
	}

	/**
	 * Look at.
	 *
	 * @param left     the left
	 * @param up       the up
	 * @param forward  the forward
	 * @param position the position
	 * @return the matrix16f
	 */
	public static Matrix16f lookAt(Vector3f left, Vector3f up,
	                               Vector3f forward, Vector3f position) {
		Matrix16f m = new Matrix16f();
		m.m[0] = -left.x;
		m.m[4] = -left.y;
		m.m[8] = -left.z;
		m.m[12] = 0;
		m.m[1] = up.x;
		m.m[5] = up.y;
		m.m[9] = up.z;
		m.m[13] = 0;
		m.m[2] = -forward.x;
		m.m[6] = -forward.y;
		m.m[10] = -forward.z;
		m.m[14] = 0;
		m.m[3] = 0;
		m.m[7] = 0;
		m.m[11] = 0;
		m.m[15] = 1;
		m.translate(new Vector3f(-position.x, -position.y, -position.z));
		return m;
	}

	/**
	 * Prints the matrix.
	 *
	 * @param f the f
	 */
	public static void printMatrix(float[] f) {
		System.err.println(f[0] + "   " + f[4] + "   " + f[8] + "   " + f[12]);
		System.err.println(f[1] + "   " + f[5] + "   " + f[9] + "   " + f[13]);
		System.err.println(f[2] + "   " + f[6] + "   " + f[10] + "   " + f[14]);
		System.err.println(f[3] + "   " + f[7] + "   " + f[11] + "   " + f[15]);
	}

	/**
	 * Rotation around x.
	 *
	 * @param angle in radians
	 * @return the matrix16f
	 */
	public static Matrix16f rotationAroundX(float angle) {
		float cos = FastMath.cos(angle);
		float sin = FastMath.sin(angle);

		Matrix16f m = new Matrix16f();
		m.m[0] = 1;
		m.m[4] = 0;
		m.m[8] = 0;
		m.m[12] = 0;
		m.m[1] = 0;
		m.m[5] = cos;
		m.m[9] = sin;
		m.m[13] = 0;
		m.m[2] = 0;
		m.m[6] = -sin;
		m.m[10] = cos;
		m.m[14] = 0;
		m.m[3] = 0;
		m.m[7] = 0;
		m.m[11] = 0;
		m.m[15] = 1;
		return m;
	}

	/**
	 * Rotation around y.
	 *
	 * @param angle in radians
	 * @return the matrix16f
	 */
	public static Matrix16f rotationAroundY(float angle) {
		float cos = FastMath.cos(angle);
		float sin = FastMath.sin(angle);

		Matrix16f m = new Matrix16f();
		m.m[0] = cos;
		m.m[4] = 0;
		m.m[8] = sin;
		m.m[12] = 0;
		m.m[1] = 0;
		m.m[5] = 1;
		m.m[9] = 0;
		m.m[13] = 0;
		m.m[2] = -sin;
		m.m[6] = 0;
		m.m[10] = cos;
		m.m[14] = 0;
		m.m[3] = 0;
		m.m[7] = 0;
		m.m[11] = 0;
		m.m[15] = 1;
		return m;
	}

	/**
	 * Rotation around z.
	 *
	 * @param angle in radians
	 * @return the matrix16f
	 */
	public static Matrix16f rotationAroundZ(float angle) {
		float cos = FastMath.cos(angle);
		float sin = FastMath.sin(angle);

		Matrix16f m = new Matrix16f();
		m.m[0] = cos;
		m.m[4] = sin;
		m.m[8] = 0;
		m.m[12] = 0;
		m.m[1] = -sin;
		m.m[5] = cos;
		m.m[9] = 0;
		m.m[13] = 0;
		m.m[2] = 0;
		m.m[6] = 0;
		m.m[10] = 1;
		m.m[14] = 0;
		m.m[3] = 0;
		m.m[7] = 0;
		m.m[11] = 0;
		m.m[15] = 1;
		return m;
	}

	/**
	 * Scale matrix.
	 *
	 * @param scale the scale
	 * @return the matrix16f
	 */
	public static Matrix16f scaleMatrix(Vector3f scale) {
		float x = scale.x;
		float y = scale.y;
		float z = scale.z;

		Matrix16f m = new Matrix16f();
		m.m[0] = x;
		m.m[4] = 0;
		m.m[8] = 0;
		m.m[12] = 0;
		m.m[1] = 0;
		m.m[5] = y;
		m.m[9] = 0;
		m.m[13] = 0;
		m.m[2] = 0;
		m.m[6] = 0;
		m.m[10] = z;
		m.m[14] = 0;
		m.m[3] = 0;
		m.m[7] = 0;
		m.m[11] = 0;
		m.m[15] = 1;
		return m;
	}

	/**
	 * Translation.
	 *
	 * @param pos the pos
	 * @return the matrix16f
	 */
	public static Matrix16f translation(Vector3f pos) {
		Matrix16f m = identity();
		m.m[12] = pos.x;
		m.m[13] = pos.y;
		m.m[14] = pos.z;
		return m;
	}

	/**
	 * Clone this matrix.
	 *
	 * @return a clone of this matrix
	 */
	@Override
	public Matrix16f clone() {
		Matrix16f clone = new Matrix16f();
		System.arraycopy(m, 0, clone.m, 0, m.length);
		// for(int i = 0; i < m.length; i++) clone.m[i] = m[i];
		return clone;
	}

	/**
	 * Copy from.
	 *
	 * @param from the from
	 * @return the matrix16f
	 */
	public Matrix16f copyFrom(Matrix16f from) {
		System.arraycopy(from.m, 0, this.m, 0, this.m.length);
		return this;
	}

	/**
	 * Copy to.
	 *
	 * @param to the to
	 * @return the matrix16f
	 */
	public Matrix16f copyTo(Matrix16f to) {
		System.arraycopy(this.m, 0, to.m, 0, m.length);
		return this;
	}

	/**
	 * Matrix determinant.
	 *
	 * @return matrix determinant
	 * @author Gabor Simko (tsg@coder.hu)
	 * @date 08.10.2001
	 * @copyright (c) Gabor Simko, All Rights Reserved.
	 */
	public float determinant() {
		return (m[0] * m[5] - m[1] * m[4]) * (m[10] * m[15] - m[11] * m[14])
				- (m[0] * m[6] - m[2] * m[4]) * (m[9] * m[15] - m[11] * m[13])
				+ (m[0] * m[7] - m[3] * m[4]) * (m[9] * m[14] - m[10] * m[13])
				+ (m[1] * m[6] - m[2] * m[5]) * (m[8] * m[15] - m[11] * m[12])
				- (m[1] * m[7] - m[3] * m[5]) * (m[8] * m[14] - m[10] * m[12])
				+ (m[2] * m[7] - m[3] * m[6]) * (m[8] * m[13] - m[9] * m[12]);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.CameraPositionable#getForward()
	 */
	public Vector3f getForward() {
		return transformTransposed(new Vector3f(0, 0, -1));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.CameraPositionable#getLeft()
	 */
	public Vector3f getLeft() {
		return transformTransposed(new Vector3f(-1, 0, 0));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.PositionableEx#getMatrix()
	 */
	public org.schema.common.util.linAlg.Matrix16f getMatrix() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.CameraPositionable#getPosition()
	 */
	public Vector3f getPosition() {
		return transformTransposed(new Vector3f(-m[12], -m[13], -m[14]));
	}

	/**
	 * Gets the quaternion.
	 *
	 * @return the quaternion
	 */
	public Quaternion4f getQuaternion() {
		Matrix16f m = clone();
		// Don'transformationArray need translation information
		m.m[12] = 0;
		m.m[13] = 0;
		m.m[14] = 0;
		// Vector3f x = normalize(new Vector3f(m.m[0], m.m[4], m.m[ 8]));
		// Vector3f y = normalize(new Vector3f(m.m[1], m.m[5], m.m[ 9]));
		// Vector3f z = normalize(new Vector3f(m.m[2], m.m[6], m.m[10]));
		// m.m[0] = x.x; m.m[4] = y.x; m.m[ 8] = z.x;
		// m.m[1] = x.y; m.m[5] = y.y; m.m[ 9] = z.y;
		// m.m[2] = x.z; m.m[6] = y.z; m.m[10] = z.z;
		return Quaternion4f.fromMatrix(m);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.camera.CameraPositionable#getUp()
	 */
	public Vector3f getUp() {
		return transformTransposed(new Vector3f(0, 1, 0));
	}

	/**
	 * Matrix inverse.
	 *
	 * @return inverse of this matrix
	 * @author Gabor Simko (tsg@coder.hu)
	 * @date 08.10.2001
	 * @copyright (c) Gabor Simko, All Rights Reserved.
	 */
	public Matrix16f inverse() {
		float det = determinant();
		if (det == 0) {
			throw new RuntimeException(
					"Can'transformationArray calculate the inverse matrix, determinant is null.");
		}

		Matrix16f inv = new Matrix16f();
		inv.m[0] = (m[5] * (m[10] * m[15] - m[11] * m[14]) + m[6]
				* (m[11] * m[13] - m[9] * m[15]) + m[7]
				* (m[9] * m[14] - m[10] * m[13]))
				/ det;
		inv.m[1] = (m[9] * (m[2] * m[15] - m[3] * m[14]) + m[10]
				* (m[3] * m[13] - m[1] * m[15]) + m[11]
				* (m[1] * m[14] - m[2] * m[13]))
				/ det;
		inv.m[2] = (m[13] * (m[2] * m[7] - m[3] * m[6]) + m[14]
				* (m[3] * m[5] - m[1] * m[7]) + m[15]
				* (m[1] * m[6] - m[2] * m[5]))
				/ det;
		inv.m[3] = (m[1] * (m[7] * m[10] - m[6] * m[11]) + m[2]
				* (m[5] * m[11] - m[7] * m[9]) + m[3]
				* (m[6] * m[9] - m[5] * m[10]))
				/ det;

		inv.m[4] = (m[6] * (m[8] * m[15] - m[11] * m[12]) + m[7]
				* (m[10] * m[12] - m[8] * m[14]) + m[4]
				* (m[11] * m[14] - m[10] * m[15]))
				/ det;
		inv.m[5] = (m[10] * (m[0] * m[15] - m[3] * m[12]) + m[11]
				* (m[2] * m[12] - m[0] * m[14]) + m[8]
				* (m[3] * m[14] - m[2] * m[15]))
				/ det;
		inv.m[6] = (m[14] * (m[0] * m[7] - m[3] * m[4]) + m[15]
				* (m[2] * m[4] - m[0] * m[6]) + m[12]
				* (m[3] * m[6] - m[2] * m[7]))
				/ det;
		inv.m[7] = (m[2] * (m[7] * m[8] - m[4] * m[11]) + m[3]
				* (m[4] * m[10] - m[6] * m[8]) + m[0]
				* (m[6] * m[11] - m[7] * m[10]))
				/ det;

		inv.m[8] = (m[7] * (m[8] * m[13] - m[9] * m[12]) + m[4]
				* (m[9] * m[15] - m[11] * m[13]) + m[5]
				* (m[11] * m[12] - m[8] * m[15]))
				/ det;
		inv.m[9] = (m[11] * (m[0] * m[13] - m[1] * m[12]) + m[8]
				* (m[1] * m[15] - m[3] * m[13]) + m[9]
				* (m[3] * m[12] - m[0] * m[15]))
				/ det;
		inv.m[10] = (m[15] * (m[0] * m[5] - m[1] * m[4]) + m[12]
				* (m[1] * m[7] - m[3] * m[5]) + m[13]
				* (m[3] * m[4] - m[0] * m[7]))
				/ det;
		inv.m[11] = (m[3] * (m[5] * m[8] - m[4] * m[9]) + m[0]
				* (m[7] * m[9] - m[5] * m[11]) + m[1]
				* (m[4] * m[11] - m[7] * m[8]))
				/ det;

		inv.m[12] = (m[4] * (m[10] * m[13] - m[9] * m[14]) + m[5]
				* (m[8] * m[14] - m[10] * m[12]) + m[6]
				* (m[9] * m[12] - m[8] * m[13]))
				/ det;
		inv.m[13] = (m[8] * (m[2] * m[13] - m[1] * m[14]) + m[9]
				* (m[0] * m[14] - m[2] * m[12]) + m[10]
				* (m[1] * m[12] - m[0] * m[13]))
				/ det;
		inv.m[14] = (m[12] * (m[2] * m[5] - m[1] * m[6]) + m[13]
				* (m[0] * m[6] - m[2] * m[4]) + m[14]
				* (m[1] * m[4] - m[0] * m[5]))
				/ det;
		inv.m[15] = (m[0] * (m[5] * m[10] - m[6] * m[9]) + m[1]
				* (m[6] * m[8] - m[4] * m[10]) + m[2]
				* (m[4] * m[9] - m[5] * m[8]))
				/ det;
		return inv;
	}

	/**
	 * Invert this matrix.
	 *
	 * @return this inverted
	 */
	public Matrix16f inverseRotation() {
	    /*
         * In case of rotation matrix or identity, transposed matrix is the
		 * inverse ie MM-1 = MMT
		 *
		 * A rotation matrix have always a determinant of 1
		 */
		transpose();
		Vector3f tr = transform(new Vector3f(m[12], m[13], m[14]));
		m[12] = -tr.x;
		m[13] = -tr.y;
		m[14] = -tr.z;
		return this;
	}

	/**
	 * Multiply this matrix.<BR>
	 * Formula : <code>this</code>[i][j] = row i of <code>this</code> column j
	 * of <code>matrix</code>.<BR>
	 * Where <code>this</code>[i][j] is the element at row i and column j.
	 *
	 * @param matrix the matrix
	 * @return this
	 */
	public Matrix16f multiply(Matrix16f matrix) {
		// FIXME Can probably be optimized

		Matrix16f clone = this.clone();
		for (int j = 0; j < 4; j++) // Loop over matrix columns
		{
			for (int i = 0; i < 4; i++) // Loop over matrix rows
			{
				// This could be done with a for loop, but it is slower i think
				// ...
				// int jj = 4*j;
				// m[jj+i] = 0.0f;
				// for(int k = 0; k < 4; k++)
				// m[i+jj] += clone.m[i+4*k]*matrix.m[k+jj];

				int jj = 4 * j;
				m[i + jj] = clone.m[i] * matrix.m[jj] + clone.m[i + 4]
						* matrix.m[1 + jj] + clone.m[i + 8] * matrix.m[2 + jj]
						+ clone.m[i + 12] * matrix.m[3 + jj];
			}
		}
		clone = null;
		return this;
	}

	/**
	 * Rotate and translate vector.
	 *
	 * @param clazz Vector to rotate and translate
	 * @return the vector3 d
	 */
	public Vector3f multiply(Vector3f v) {
		Vector3f result = transform(v);
		result.x = (result.x + (m[12]));
		result.y = (result.y + (m[13]));
		result.z = (result.z + (m[14]));
		return result;
	}

	/**
	 * Multiply3x3.
	 *
	 * @param matrix the matrix
	 * @return the matrix16f
	 */
	public Matrix16f multiply3x3(Matrix16f matrix) {
		// FIXME Can probably be optimized

		Matrix16f clone = this.clone();
		for (int j = 0; j < 3; j++) // Loop over matrix columns
		{
			for (int i = 0; i < 3; i++) // Loop over matrix rows
			{
				// This could be done with a for loop, but it is slower i think
				// ...
				// int jj = 4*j;
				// m[jj+i] = 0.0f;
				// for(int k = 0; k < 3; k++)
				// m[i+jj] += clone.m[i+4*k]*matrix.m[k+jj];

				int jj = 4 * j;
				m[i + jj] = clone.m[i] * matrix.m[jj] + clone.m[i + 4]
						* matrix.m[1 + jj] + clone.m[i + 8] * matrix.m[2 + jj];
			}
		}
		clone = null;
		return this;
	}

	/* CameraPositionable interface */

	/**
	 * Prints the.
	 */
	public void print() {
		System.err
				.printf(
						"%.4f\transformationArray%.4f\transformationArray%.4f\transformationArray%.4f\n%.4f\transformationArray%.4f\transformationArray%.4f\transformationArray%.4f\n%.4f\transformationArray%.4f\transformationArray%.4f\transformationArray%.4f\n%.4f\transformationArray%.4f\transformationArray%.4f\transformationArray%.4f\n",
						m[0], m[4], m[8], m[12], m[1], m[5], m[9], m[13], m[2],
						m[6], m[10], m[14], m[3], m[7], m[11], m[15]);
	}

	/**
	 * Rotate this matrix.
	 *
	 * @param q the q
	 * @return this
	 */
	public Matrix16f rotate(Quaternion4f q) {
		return multiply3x3(q.createMatrix());
	}

	/**
	 * Scale this matrix.<BR>
	 * Scale column i of <code>this</code> by <code>scale</code>.
	 *
	 * @param scale scale scalar
	 * @return this
	 */
	public Matrix16f scale(float scale) {
		m[0] *= scale;
		m[4] *= scale;
		m[8] *= scale;
		m[1] *= scale;
		m[5] *= scale;
		m[9] *= scale;
		m[2] *= scale;
		m[6] *= scale;
		m[10] *= scale;
		m[3] *= scale;
		m[7] *= scale;
		m[11] *= scale;
		return this;
	}

	/**
	 * Scale this matrix.<BR>
	 * Scale column i of <code>this</code> by the i<sub>th</sub> component of
	 * <code>scale</code>.
	 *
	 * @param scale scale vector
	 * @return this
	 */
	public Matrix16f scale(Vector3f scale) {
		m[0] *= scale.x;
		m[4] *= scale.y;
		m[8] *= scale.z;
		m[1] *= scale.x;
		m[5] *= scale.y;
		m[9] *= scale.z;
		m[2] *= scale.x;
		m[6] *= scale.y;
		m[10] *= scale.z;
		m[3] *= scale.x;
		m[7] *= scale.y;
		m[11] *= scale.z;
		return this;
	}

	/**
	 * Rotate vector.
	 *
	 * @param clazz Vector to rotate
	 * @return the vector3 d
	 */
	public Vector3f transform(Vector3f v) {
		float x = m[0] * v.x + m[4] * v.y + m[8] * v.z;
		float y = m[1] * v.x + m[5] * v.y + m[9] * v.z;
		float z = m[2] * v.x + m[6] * v.y + m[10] * v.z;
		return new Vector3f(x, y, z);
	}

	/**/

	/**
	 * Rotate a vector by the transformed matrix.
	 *
	 * @param clazz Vector to rotate
	 * @return the vector3 d
	 */
	public Vector3f transformTransposed(Vector3f v) {
		float x = m[0] * v.x + m[1] * v.y + m[2] * v.z;
		float y = m[4] * v.x + m[5] * v.y + m[6] * v.z;
		float z = m[8] * v.x + m[9] * v.y + m[10] * v.z;
		return new Vector3f(x, y, z);
	}

	/**
	 * Translate this matrix.
	 *
	 * @param translate translation vector.
	 * @return this
	 */
	public Matrix16f translate(Vector3f translate) {
		Vector3f v = transform(translate);
		m[12] += v.x;
		m[13] += v.y;
		m[14] += v.z;
		return this;
	}

	/**
	 * Transpose this matrix.<BR>
	 * Formula : <code>this</code>[i][j] = <code>this</code>[j][i]<BR>
	 * .
	 *
	 * @return this transposed
	 */
	public Matrix16f transpose() {
		for (int j = 0; j < 4; j++) // Loop over matrix columns
		{
			for (int i = j + 1; i < 4; i++) // Loop over matrix rows
			{
				final int jj = 4 * j, ii = 4 * i;
				final float swap = m[i + jj];
				m[i + jj] = m[ii + j];
				m[ii + j] = swap;
			}
		}
		return this;
	}

	/**
	 * 3x3 transpose this matrix.<BR>
	 * Formula : <code>this</code>[i][j] = <code>this</code>[j][i]<BR>
	 * .
	 *
	 * @return this transposed
	 */
	public Matrix16f transpose3x3() {
		for (int j = 0; j < 3; j++) // Loop over matrix columns
		{
			for (int i = j + 1; i < 3; i++) // Loop over matrix rows
			{
				final int jj = 4 * j, ii = 4 * i;
				final float swap = m[i + jj];
				m[i + jj] = m[ii + j];
				m[ii + j] = swap;
			}
		}
		return this;
	}
}