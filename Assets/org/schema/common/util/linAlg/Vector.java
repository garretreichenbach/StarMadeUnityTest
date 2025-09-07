/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Vector</H2>
 * <H3>org.schema.common.util.linAlg</H3>
 * Vector.java
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
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
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

import org.schema.common.FastMath;

/**
 * The Class Vector.
 */
public class Vector implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The x.
	 */
	public float x;

	/**
	 * The y.
	 */
	public float y;

	/**
	 * The basis.
	 */
	private Vector basis;

	/**
	 * Instantiates a new vector.
	 *
	 * @param x the x
	 * @param y the y
	 */
	public Vector(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Adds the.
	 *
	 * @param b the b
	 * @return the vector
	 */
	public Vector add(Vector b) {
		return new Vector(x + b.x, y + b.y);
	}

	/**
	 * Cross mult.
	 *
	 * @param b the b
	 * @return the double
	 */
	public float crossMult(Vector b) {
		return x * b.y - y * b.x;
	}

	/**
	 * Dot mult.
	 *
	 * @param b the b
	 * @return the double
	 */
	public float dotMult(Vector b) {
		return x * b.x + y * b.y;
	}

	/**
	 * Gets the angle.
	 *
	 * @param b the b
	 * @return the angle
	 */
	public float getAngle(Vector b) {
		return FastMath.acos(dotMult(b) / (getLength() * b.getLength()));
	}

	/**
	 * Gets the basis.
	 *
	 * @return the basis
	 */
	public Vector getBasis() {
		return basis;
	}

	/**
	 * Sets the basis.
	 *
	 * @param basis the new basis
	 */
	public void setBasis(Vector basis) {
		this.basis = basis;
	}

	/**
	 * Use to get a Directional Vector between to Vectors. the Length of the
	 * Vector is the distance between the to Vectors
	 *
	 * @param tarVec the tar vec
	 * @return Directional Vector (not normalized)
	 */
	public Vector getDirVec(Vector tarVec) {
		return (tarVec.sub(this));
	}

	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public float getLength() {
		return FastMath.sqrt((x * x) + (y * y));
	}

	/**
	 * Gets the normalized.
	 *
	 * @return the normalized
	 */
	public Vector getNormalized() {
		return mult(1f / getLength());
	}

	/**
	 * Gets the normal vector.
	 *
	 * @return the normal vector
	 */
	public Vector getNormalVector() {
		return new Vector(-y, x);
	}

	/**
	 * Gets the total angle.
	 *
	 * @param b the b
	 * @return the total angle
	 */
	public float getTotalAngle(Vector b) {
		float angle = getAngle(b);
		// System.err.print(angle);

		if (x > b.x) {
			angle = ((2 * FastMath.PI) - angle);
		}
		// System.err.println(" ---> "+angle);

		return angle;
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
	 * Sets the x.
	 *
	 * @param x the new x
	 */
	public void setX(float x) {
		this.x = x;
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
	 * Sets the y.
	 *
	 * @param y the new y
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * Mult.
	 *
	 * @param scalar the scalar
	 * @return the vector
	 */
	public Vector mult(float scalar) {
		return new Vector(x * scalar, y * scalar);
	}

	/**
	 * Rotate.
	 *
	 * @param angle the angle
	 * @return the vector
	 */
	public Vector rotate(float angle) {
		float sin = FastMath.sin(angle);
		float cos = FastMath.cos(angle);
		return new Vector(x * cos + y * sin, x * -sin + y * cos);
	}

	/**
	 * Sub.
	 *
	 * @param b the b
	 * @return the vector
	 */
	public Vector sub(Vector b) {
		return new Vector(x - b.x, y - b.y);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return x + ", " + y;
	}
}
