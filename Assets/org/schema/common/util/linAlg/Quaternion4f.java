/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Quaternion4f</H2>
 * <H3>org.schema.common.util.linAlg</H3>
 * Quaternion4f.java
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

import static java.lang.Math.max;
import static org.schema.common.FastMath.abs;
import static org.schema.common.FastMath.acos;
import static org.schema.common.FastMath.cos;
import static org.schema.common.FastMath.sin;
import static org.schema.common.FastMath.sqrt;

import java.io.Serializable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;

/**
 * What is a quaternion ?<BR>
 * ----------------------<BR>
 * A quaternion is the generalization of complex number.<BR>
 * complex number c = a + i.b<BR>
 * quaternon q = a + i.b + j.c + k.d<BR>
 * <BR>
 * A quaternion can be considered as a scalar number (a) and a vector (b,c,d).<BR>
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
public strictfp class Quaternion4f implements Cloneable, Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 0x010101;
	/**
	 * The w.
	 */
	private float x, y, z, w;

	/**
	 * Instantiates a new quaternion4f.
	 */
	public Quaternion4f() {
		this(0, 0, 0, 1);
	}

	/**
	 * Instantiates a new quaternion4f.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param w the w
	 */
	public Quaternion4f(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * From axis angle.
	 *
	 * @param e1    , also called cosinus director in x direction
	 * @param e2    , also called cosinus director in y direction
	 * @param e3    , also called cosinus director in z direction
	 * @param alpha rotation angle in radians
	 * @return the quaternion4f
	 */
	public static Quaternion4f fromAxisAngle(float e1, float e2, float e3,
	                                         float alpha) {
		// Normalize axis vector
		float f = sqrt(e1 * e1 + e2 * e2 + e3 * e3);
		if (f == 0) {
			return new Quaternion4f(0, 0, 0, 1);
		}

		// Correct angle if needed (make sure it is in a valid range)
		float alphaOverTwo = alpha / 2;

		// Calculate cos/sin one time
		float w = cos(alphaOverTwo);
		float scale = sin(alphaOverTwo);
		scale /= f;

		return new Quaternion4f(e1 * scale, e2 * scale, e3 * scale, w);
	}

	/**
	 * From euler angles.
	 *
	 * @param pitch rotation around x, in radians
	 * @param yaw   rotation around y, in radians
	 * @param roll  rotation around z, in radians
	 * @return the quaternion4f
	 */
	public static Quaternion4f fromEulerAngles(float pitch, float yaw,
	                                           float roll) {
		// May be easier, but slower
		// return fromAxisAngle(1, 0, 0, pitch).
		// multiply(fromAxisAngle(0, 1, 0, yaw)).
		// multiply(fromAxisAngle(0, 0, 1, roll));

		pitch *= 0.5f;
		yaw *= 0.5f;
		roll *= 0.5f;

		float cPitch = cos(pitch);
		float cYaw = cos(yaw);
		float cRoll = cos(roll);

		float sPitch = sin(pitch);
		float sYaw = sin(yaw);
		float sRoll = sin(roll);

		float cYawRoll = cYaw * cRoll;
		float sYawRoll = sYaw * sRoll;

		return new Quaternion4f(sPitch * cYawRoll - cPitch * sYawRoll, cPitch
				* sYaw * cRoll + sPitch * cYaw * sRoll, cPitch * cYaw * sRoll
				- sPitch * sYaw * cRoll, cPitch * cYawRoll + sPitch * sYawRoll)/*
	             * .normalize
				 * (
				 * )
				 */;
	}

	/**
	 * From matrix.
	 *
	 * @param m the m
	 * @return the quaternion4f
	 */
	public static Quaternion4f fromMatrix(Matrix16f m) {
		float x, y, z, w;

		float trace = m.m[0] + m.m[5] + m.m[10] + 1;
		if (trace > 0) {
			float s = sqrt(trace);
			w = 0.5f * s;
			s = 0.5f / s;
			x = (m.m[6] - m.m[9]) * s;
			y = (m.m[8] - m.m[2]) * s;
			z = (m.m[1] - m.m[4]) * s;
		} else {
			float max = max(max(m.m[0], m.m[5]), m.m[10]);
			if (max == m.m[0]) {
				float s = sqrt(1 + m.m[0] - m.m[5] - m.m[10]);
				x = 0.5f * s;
				s = 0.5f / s;
				y = (m.m[1] + m.m[4]) * s;
				z = (m.m[2] + m.m[8]) * s;
				w = (m.m[6] - m.m[9]) * s;
			} else if (max == m.m[5]) {
				float s = sqrt(1 + m.m[5] - m.m[0] - m.m[10]);
				y = 0.5f * s;
				s = 0.5f / s;
				x = (m.m[1] + m.m[4]) * s;
				z = (m.m[6] + m.m[9]) * s;
				w = (m.m[8] - m.m[2]) * s;
			} else // if(max == m.m[10])
			{
				float s = sqrt(1 + m.m[10] - m.m[0] - m.m[5]);
				z = 0.5f * s;
				s = 0.5f / s;
				x = (m.m[2] + m.m[8]) * s;
				y = (m.m[6] + m.m[9]) * s;
				w = (m.m[1] - m.m[4]) * s;
			}
		}
		return new Quaternion4f(x, y, z, w);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Quaternion4f clone() {
		return new Quaternion4f(x, y, z, w);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Quaternion] " + x + ", " + y + ", " + z + ", " + w;
	}

	/**
	 * Conjugate the quaternion.<BR>
	 * This is equivalent to transpose the corresponding matrix.
	 *
	 * @return this
	 */
	public Quaternion4f conjugate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/**
	 * Copy from.
	 *
	 * @param from the from
	 */
	public void copyFrom(Quaternion4f from) {
		this.x = from.x;
		this.y = from.y;
		this.z = from.z;
		this.w = from.w;
	}

	/**
	 * Creates the axis angles.
	 *
	 * @return Axis (x,y,z) & Angle(w) (angle in radians)
	 */
	public Vector4f createAxisAngles() {
		float l = sqrt(x * x + y * y + z * z);

		float e1 = 0, e2 = 0, e3 = 0;
		if (l != 0) {
			e1 = x / l;
			e2 = y / l;
			e3 = z / l;
		}

		float alpha = (2 * acos(w));
		return new Vector4f(e1, e2, e3, alpha);
	}

	/**
	 * Creates the euler angles.
	 *
	 * @return Euler angles in radians
	 */
	public Vector3f createEulerAngles() {
		float cosY = cos(y);
		if (abs(cosY) > 0.001f) {
			float oneOverCosY = 1.0f / cosY;

			// No gimball lock
			float xx = FastMath.atan2(2 * (w * x - y * z) * oneOverCosY,
					(1.0f - 2.0f * (x * x + y * y)) * oneOverCosY);
			float yy = FastMath.asin(2 * (x * z + w * y));
			float zz = FastMath.atan2(2 * (w * z - x * y) * oneOverCosY,
					(1 - 2 * (y * y + z * z)) * oneOverCosY);
			return new Vector3f(xx, yy, zz);
		} else {
			// Gimball lock
			float xx = 0.0f;
			float yy = FastMath.asin(2.0f * (x * z + w * y));
			float zz = FastMath.atan2(2.0f * (x * y + w * z),
					1 - 2 * (x * x + z * z));
			return new Vector3f(xx, yy, zz);
		}

		// float phi = (float)FastMath.atan2(2 * (w*x+y*z), (1-2*(x*x+y*y)) );
		// float teta = (float)FastMath.asin (2 * (w*y-z*x));
		// float psi = (float)FastMath.atan2(2 * (w*z+x*y), (1-2*(y*y+z*z)));
		// float phi = (float)FastMath.atan2(2 * (w*x-y*z), (1-2*(x*x+y*y)) );
		// float teta = (float)FastMath.asin (2 * (w*y+z*x));
		// float psi = (float)FastMath.atan2(2 * (w*z-x*y), (1-2*(y*y+z*z)));
		// return new Vector3f(phi, teta, psi);
	}

	/**
	 * Creates the matrix.
	 *
	 * @return the matrix16f
	 */
	public Matrix16f createMatrix() {
		float[] m = new float[16];

		m[0] = 1 - 2 * y * y - 2 * z * z;
		m[1] = 2 * x * y + 2 * z * w;
		m[2] = 2 * x * z - 2 * y * w;
		m[3] = 0;

		m[4] = 2 * x * y - 2 * z * w;
		m[5] = 1 - 2 * x * x - 2 * z * z;
		m[6] = 2 * y * z + 2 * x * w;
		m[7] = 0;

		m[8] = 2 * x * z + 2 * y * w;
		m[9] = 2 * y * z - 2 * x * w;
		m[10] = 1 - 2 * x * x - 2 * y * y;
		m[11] = 0;

		m[12] = 0;
		m[13] = 0;
		m[14] = 0;
		m[15] = 1;

		return new Matrix16f(m);
	}

	// Getters

	/**
	 * Gets the w.
	 *
	 * @return the w
	 */
	public float getW() {
		return w;
	}

	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * Gets the z.
	 *
	 * @return the z
	 */
	public float getZ() {
		return z;
	}

	/**
	 * Invert this quaternion (q^-1, with qq^-1=1).
	 *
	 * @return this
	 */
	public Quaternion4f invert() {
		// Conjugate / (norme * norme)
		float f = (x * x + y * y + z * z + w * w);
		x /= -f;
		y /= -f;
		z /= -f;
		w /= f;
		return this;
	}

	/**
	 * Length.
	 *
	 * @return the float
	 */
	public float length() {
		return sqrt(x * x + y * y + z * z + w * w);
	}

	/**
	 * Multiply this quaternion by q (this*q).
	 *
	 * @param q the q
	 * @return this
	 */
	public Quaternion4f multiply(Quaternion4f q) {
		float x = this.x * q.w + this.y * q.z - this.z * q.y + this.w * q.x;
		float y = -this.x * q.z + this.y * q.w + this.z * q.x + this.w * q.y;
		float z = this.x * q.y - this.y * q.x + this.z * q.w + this.w * q.z;
		float w = -this.x * q.x - this.y * q.y - this.z * q.z + this.w * q.w;

		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;

		return this;
	}

	/**
	 * Calculate the product q*clazz<BR>
	 * This is a quaternion*quaternion product, vector clazz is considered as a the
	 * quaternion (clazz.x, clazz.y, clazz.z, 0)
	 *
	 * @param clazz the clazz
	 * @return the quaternion4f
	 */
	public Quaternion4f multiply(Vector3f v) {
		float x = this.w * v.x - this.z * v.y + this.y * v.z;
		float y = this.z * v.x + this.w * v.y - this.x * v.z;
		float z = -this.y * v.x + this.x * v.y + this.w * v.z;
		float w = -this.x * v.x - this.y * v.y - this.z * v.z;

		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;

		return this;
	}

	/**
	 * Multiply invert.
	 *
	 * @param q the q
	 * @return this
	 */
	public Quaternion4f multiplyInvert(Quaternion4f q) {
		return invert().multiply(q);
	}

	/**
	 * Normalize.
	 *
	 * @return this
	 */
	public Quaternion4f normalize() {
		float length = length();
		if (length > 0) {
			x /= length;
			y /= length;
			z /= length;
			w /= length;
		}
		return this;
	}

	/**
	 * Rotation q*clazz*q^-1 and return the x, y, z components<BR>
	 * Same result than <code>this.createMatrix().multiply(clazz)</code>.
	 *
	 * @param clazz the clazz
	 * @return the vector3 d
	 */
	public Vector3f rotate(Vector3f v) {
		Quaternion4f inv = this.clone().conjugate().normalize();
		// Quaternion4d inv = this.clone().invert();
		inv = this.clone().multiply(v).multiply(inv);
		return new Vector3f(inv.x, inv.y, inv.z);
	}

	/**
	 * Scale.
	 *
	 * @param scale the scale
	 * @return this
	 */
	public Quaternion4f scale(float scale) {
		x *= scale;
		y *= scale;
		z *= scale;
		w *= scale;
		return this;
	}

	/**
	 * Slerp.
	 *
	 * @param q                   the q
	 * @param transformationArray the transformationArray
	 * @return the interpolated quaternion
	 */
	public Quaternion4f slerp(Quaternion4f q, float t) {
		if (q == null) {
			return this.clone();
		}

		// Do a linear interpolation between two quaternions
		float l = this.x * q.x + this.y * q.y + this.z * q.z + this.w * q.w;
		Quaternion4f slerp = null;
		if (l < 0) {
			l = -l;
			slerp = new Quaternion4f(-q.x, -q.y, -q.z, -q.w);
		} else {
			slerp = new Quaternion4f(q.x, q.y, q.z, q.w);
		}

		// If the quaternions are really close, do a simple linear interpolation
		float scale0 = 1 - t;
		float scale1 = t;
		// Otherwise SLERP
		// if((1 - l) > 0.0001f)
		// {
		// float acosL = acos(l);
		// float sinAcosL = sin(acosL);
		// scale0 = sin(scale0 * acosL) / sinAcosL;
		// scale1 = sin(scale1 * acosL) / sinAcosL;
		// }
		if ((1 - l) > 0.0001f) {
			if (l > 1.0f) {
				l /= l;
			}
			float acosL = acos(l);
			float sinAcosL = sin(acosL);
			if (FastMath.abs(sinAcosL) > 0.0001f) {
				scale0 = sin(scale0 * acosL) / sinAcosL;
				scale1 = sin(scale1 * acosL) / sinAcosL;
			}
		}

		// Calculate interpolated quaternion
		slerp.x = scale0 * this.x + scale1 * slerp.x;
		slerp.y = scale0 * this.y + scale1 * slerp.y;
		slerp.z = scale0 * this.z + scale1 * slerp.z;
		slerp.w = scale0 * this.w + scale1 * slerp.w;
		return slerp;
	}

	/**
	 * Slerp2.
	 *
	 * @param q                   the q
	 * @param transformationArray the transformationArray
	 * @return the quaternion4f
	 */
	public Quaternion4f slerp2(Quaternion4f q, float t) {
		if (q == null) {
			return this.clone();
		}

		float l = this.x * q.x + this.y * q.y + this.z * q.z + this.w * q.w;

		Quaternion4f slerp = null;
		float scale0 = 1 - t;
		float scale1 = t;
		if (1 + l > 0.001f) {
			slerp = new Quaternion4f(q.x, q.y, q.z, q.w);

			if (abs(l) > 1.0f) {
				l /= abs(l);
			}
			float acosL = acos(l);
			float sinAcosL = sin(acosL);
			if (abs(sinAcosL) > 0.001f) {
				scale0 = sin(scale0 * acosL) / sinAcosL;
				scale1 = sin(scale1 * acosL) / sinAcosL;
			}
		} else {
			slerp = new Quaternion4f(-this.y, this.x, -this.w, this.z);

			scale0 = sin(scale0 * FastMath.PI / 2.0f);
			scale1 = sin(scale1 * FastMath.PI / 2.0f);
		}

		// Calculate interpolated quaternion
		slerp.x = scale0 * this.x + scale1 * slerp.x;
		slerp.y = scale0 * this.y + scale1 * slerp.y;
		slerp.z = scale0 * this.z + scale1 * slerp.z;
		slerp.w = scale0 * this.w + scale1 * slerp.w;
		return slerp;
	}
}