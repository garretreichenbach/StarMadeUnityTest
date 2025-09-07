package org.schema.common.util.linAlg;

import javax.vecmath.Vector4f;

import org.schema.common.FastMath;

/**
 * A 4-element vector represented by single-precision inting point x,y,z,w
 * coordinates.
 */
public class Vector4i implements java.io.Serializable {

	// Compatible with 1.1
	static final long serialVersionUID = 8749319902347760659L;

	public int x, y, z, w;

	/**
	 * Constructs and initializes a Vector4i to (0,0,0,0).
	 */
	public Vector4i() {
		super();
	}

	/**
	 * Constructs and initializes a Vector4i from the specified xyzw
	 * coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param w the w coordinate
	 */
	public Vector4i(int x, int y, int z, int w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Constructs and initializes a Vector4i from the array of length 4.
	 *
	 * @param v the array of length 4 containing xyzw in order
	 */
	public Vector4i(int[] v) {
		this(v[0], v[1], v[2], v[3]);
	}

	/**
	 * Constructs and initializes a Vector4i from the specified Tuple3f. The
	 * x,y,z components of this vector are set to the corresponding components
	 * of tuple t1. The w component of this vector is set to 0.
	 *
	 * @param t1 the tuple to be copied
	 * @since vecmath 1.2
	 */
	public Vector4i(Vector3i t1) {
		this(t1.x, t1.y, t1.z, 0);
	}

	public Vector4i(Vector3i v1, int i) {
		this(v1.x, v1.y, v1.z, i);
	}

	/**
	 * Constructs and initializes a Vector4i from the specified Vector4d.
	 *
	 * @param v1 the Vector4d containing the initialization x y z w data
	 */
	public Vector4i(Vector4f v1) {
		this((int) v1.x, (int) v1.y, (int) v1.z, (int) v1.w);
	}

	/**
	 * Constructs and initializes a Vector4i from the specified Vector4i.
	 *
	 * @param v1 the Vector4i containing the initialization x y z w data
	 */
	public Vector4i(Vector4i v1) {
		this(v1.x, v1.y, v1.z, v1.w);
	}

	/**
	 * returns the dot product of this vector and v1
	 *
	 * @param v1 the other vector
	 * @return the dot product of this vector and v1
	 */
	public final int dot(Vector4i v1) {
		return (this.x * v1.x + this.y * v1.y + this.z * v1.z + this.w * v1.w);
	}

	/**
	 * Returns a hash code value based on the data values in this
	 * object.  Two different Tuple4f objects with identical data values
	 * (i.e., Tuple4f.equals returns true) will return the same hash
	 * code value.  Two objects with different data members may return the
	 * same hash value, although this is not likely.
	 *
	 * @return the integer hash code value
	 */
	@Override
	public int hashCode() {

		long bits = 1L;
		bits = 31L * bits + x;
		bits = 31L * bits + y;
		bits = 31L * bits + z;
		bits = 31L * bits + w;
		return (int) (bits ^ (bits >> 32));
	}

	/**
	 * Returns true if the Object t1 is of type Tuple4f and all of the
	 * data members of t1 are equal to the corresponding data members in
	 * this Tuple4f.
	 *
	 * @param t1 the object with which the comparison is made
	 * @return true or false
	 */
	@Override
	public boolean equals(Object t1) {
		try {
			Vector4i t2 = (Vector4i) t1;
			return (this.x == t2.x && this.y == t2.y &&
					this.z == t2.z && this.w == t2.w);
		} catch (NullPointerException e2) {
			return false;
		} catch (ClassCastException e1) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}

	/**
	 * Returns the length of this vector.
	 *
	 * @return the length of this vector as a int
	 */
	public final float length() {
		return FastMath.sqrt(this.x * this.x + this.y * this.y + this.z
				* this.z + this.w * this.w);
	}

	/**
	 * Returns the squared length of this vector
	 *
	 * @return the squared length of this vector as a int
	 */
	public final int lengthSquared() {
		return (this.x * this.x + this.y * this.y + this.z * this.z + this.w
				* this.w);
	}

	public void set(int x, int y, int z,
	                int w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;

	}

	/**
	 * Sets the x,y,z components of this vector to the corresponding components
	 * of tuple t1. The w component of this vector is set to 0.
	 *
	 * @param t1 the tuple to be copied
	 * @since vecmath 1.2
	 */
	public final void set(Vector3i t1) {
		this.x = t1.x;
		this.y = t1.y;
		this.z = t1.z;
		this.w = 0;
	}

	/**
	 * Sets the x,y,z components of this vector to the corresponding components
	 * of tuple t1. The w component of this vector is set to 0.
	 *
	 * @param t1 the tuple to be copied
	 * @since vecmath 1.2
	 */
	public final void set(Vector4i t1) {
		this.x = t1.x;
		this.y = t1.y;
		this.z = t1.z;
		this.w = t1.w;
	}

}
