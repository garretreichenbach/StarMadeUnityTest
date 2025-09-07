package org.schema.common.util.linAlg;

public class Vector3b {
	public byte x;
	public byte y;
	public byte z;

	public Vector3b() {
	}

	public Vector3b(byte x, byte y, byte z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3b(float x, float y, float z) {
		this.x = (byte) x;
		this.y = (byte) y;
		this.z = (byte) z;
	}

	public Vector3b(Vector3b dim) {
		this.x = dim.x;
		this.y = dim.y;
		this.z = dim.z;
	}

	public void add(byte x, byte y, byte z) {
		this.x += x;
		this.y += y;
		this.z += z;

	}

	public void add(Vector3b b) {
		x += b.x;
		y += b.y;
		z += b.z;
	}

	public void div(byte s) {
		x /= s;
		y /= s;
		z /= s;

	}

	/**
	 * Returns a hash code value based on the data values in this
	 * object.  Two different Tuple3f objects with identical data values
	 * (i.e., Tuple3f.equals returns true) will return the same hash
	 * code value.  Two objects with different data members may return the
	 * same hash value, although this is not likely.
	 *
	 * @return the byteeger hash code value
	 */
	@Override
	public int hashCode() {
		long bits = 1L;
		bits = 7L * bits + x;
		bits = 7L * bits + y;
		bits = 7L * bits + z;
		return (byte) (bits ^ (bits >> 8));
	}

	/**
	 * Returns true if the Object t1 is of type Tuple3f and all of the
	 * data members of t1 are equal to the corresponding data members in
	 * this Tuple3f.
	 *
	 * @param t1 the Object with which the comparison is made
	 * @return true or false
	 */
	@Override
	public boolean equals(Object t1) {
		try {
			Vector3b t2 = (Vector3b) t1;
			return (this.x == t2.x && this.y == t2.y && this.z == t2.z);
		} catch (NullPointerException e2) {
			return false;
		} catch (ClassCastException e1) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public final float length() {
		return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public void scale(byte s) {
		x *= s;
		y *= s;
		z *= s;
	}

	public void scale(byte x, byte y, byte z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
	}

	public void set(byte x, byte y, byte z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3b p) {
		set(p.x, p.y, p.z);
	}

	public void sub(byte x, byte y, byte z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;

	}

	public void sub(Vector3b b) {
		x -= b.x;
		y -= b.y;
		z -= b.z;

	}
}
