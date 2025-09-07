package org.schema.common.util.linAlg;

import org.schema.common.FastMath;
import theleo.jstruct.Struct;

import javax.vecmath.Vector3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Struct
public class Vector3i implements Comparable<Vector3i>, Serializable {
	public static final long SHORT_MAX = Short.MAX_VALUE;
	public static final long SHORT_MAX2 = SHORT_MAX * 2;
	public static final long SHORT_MAX2x2 = SHORT_MAX2 * SHORT_MAX2;
	private static final Pattern parameterRegex = Pattern.compile("(\\-?\\s*[0-9]+)[\\s\\,\\.]+(\\-?\\s*[0-9]+)[\\s\\,\\.]+(\\-?\\s*[0-9]+)");
	public int x,y,z;

	public Vector3i() {
	}

	public Vector3i(float x, float y, float z) {
		this.x = (int) x;
		this.y = (int) y;
		this.z = (int) z;
	}

	public Vector3i(int x, int y, int z) {

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3i(Vector3f v) {
		this.x = (int) v.x;
		this.y = (int) v.y;
		this.z = (int) v.z;
	}

	public Vector3i(Vector3i dim) {
		this.x = dim.x;
		this.y = dim.y;
		this.z = dim.z;
	}

	public static void main(String[] sdf) {

		try {
			parseVector3iFree("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("asdf asdf asfd");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("1 2 3");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("-12 2 3");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("-12 2 -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("-012 2 -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("012 2 -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("012, 2, -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("012,   2,   -0343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("012,  . - 2,  . -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3iFree("1.-2.33");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Vector3i parseVector3i(String in) {
		String[] split = in.split(",");
		if (split.length != 3) {
			throw new NumberFormatException("Wrong number of arguments");
		}
		return new Vector3i(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
	}

	public static Vector3i parseVector3iFree(String in) {
		String temp = in;
		temp = temp.replaceAll("~", "0");
		Vector3i p = new Vector3i();
		Matcher regexMatcher = parameterRegex.matcher(temp.trim());
		int i = 0;
		while (regexMatcher.find()) {
			try {
				p.x = Integer.parseInt(regexMatcher.group(1).replaceAll("\\s", ""));
				p.y = Integer.parseInt(regexMatcher.group(2).replaceAll("\\s", ""));
				p.z = Integer.parseInt(regexMatcher.group(3).replaceAll("\\s", ""));
			} catch (Exception e) {
				e.printStackTrace();
				throw new NumberFormatException("Exception in string: " + in);
			}
			i++;
		}
		if (i == 0) {
			throw new NumberFormatException("No pattern found in " + in);
		}
		System.err.println("[VECTOR3i] PARSED: FROM \"" + in + "\" -> " + p);
		return p;
	}

	public static float getDisatance(Vector3i p1, Vector3i p2) {
		int x = p2.x - p1.x;
		int y = p2.y - p1.y;
		int z = p2.z - p1.z;
		return FastMath.sqrt((x * x + y * y + z * z));
	}
	public static long getDisatanceSquaredD(Vector3i p1, Vector3i p2) {
		long x = p2.x - p1.x;
		long y = p2.y - p1.y;
		long z = p2.z - p1.z;
		return (x * x + y * y + z * z);
	}

	public void absolute() {
		this.x = Math.abs(this.x);
		this.y = Math.abs(this.y);
		this.z = Math.abs(this.z);
	}

	public void add(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;

	}

	public void add(Vector3i b) {
		x += b.x;
		y += b.y;
		z += b.z;
	}

	public void add(Vector3i a, Vector3i b) {
		this.x = a.x + b.x;
		this.y = a.y + b.y;
		this.z = a.z + b.z;
	}

	@Override
	public int compareTo(Vector3i arg0) {
		//manhattan distance
		return (Math.abs(x) + Math.abs(y) + Math.abs(z)) - (Math.abs(arg0.x) + Math.abs(arg0.y) + Math.abs(arg0.z));

	}

	public void coordAdd(int coord, int val) {
		switch (coord) {
			case (0):
				x += val;
			case (1):
				y += val;
			case (2):
				z += val;
			default:
				assert (false) : coord;
		}
		throw new NullPointerException(coord + " coord");
	}

	public void div(int s) {
		x /= s;
		y /= s;
		z /= s;

	}

	public boolean equals(int x, int y, int z) {
		return this.x == x && this.y == y && this.z == z;
	}

	public int getCoord(int coord) {
		switch (coord) {
			case (0):
				return x;
			case (1):
				return y;
			case (2):
				return z;
			default:
				assert (false) : coord;
		}
		throw new NullPointerException(coord + " coord");
	}

	/**
	 * Returns a hash code value based on the data values in this
	 * object.  Two different Tuple3f objects with identical data values
	 * (i.e., Tuple3f.equals returns true) will return the same hash
	 * code value.  Two objects with different data members may return the
	 * same hash value, although this is not likely.
	 *
	 * @return the integer hash code value
	 */
	@Override
	public int hashCode() {
		int result = (x ^ (x >>> 16));
		result = 15 * result + (y ^ (y >>> 16));
		result = 15 * result + (z ^ (z >>> 16));
		return result;
	}

	/**
	 * Returns true if the Object t1 is of type Tuple3f and all of the
	 * data members of t1 are equal to the corresponding data members in
	 * this Tuple3f.
	 *
	 * @param o the Object with which the comparison is made
	 * @return true or false
	 */
	@Override
	public boolean equals(Object o) {
		try {
			Vector3i other = (Vector3i) o;
			return (this.x == other.x && this.y == other.y && this.z == other.z);
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

	public long code() {
		long xL = x + SHORT_MAX;
		long yL = y + SHORT_MAX;
		long zL = z + SHORT_MAX;

		//		System.err.println("INNER: "+v+" -> "+x+"; "+y+"; "+z);

		long index = zL * SHORT_MAX2x2 + yL * SHORT_MAX2 + x;
		return index * 232323;
	}

	public final float length() {
		return FastMath.sqrt((this.x * this.x + this.y * this.y + this.z * this.z));
	}

	public void negate() {
		this.x = -this.x;
		this.y = -this.y;
		this.z = -this.z;
	}

	public void scale(int s) {
		x *= s;
		y *= s;
		z *= s;
	}

	public void scale(int x, int y, int z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
	}

	public void scaleFloat(float s) {
		x *= s;
		y *= s;
		z *= s;
	}

	public void set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3i pos) {
		set(pos.x, pos.y, pos.z);
	}

	public void sub(int x, int y, int z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;

	}

	public void sub(Vector3i b) {
		x -= b.x;
		y -= b.y;
		z -= b.z;

	}

	public void sub(Vector3i a, Vector3i b) {
		this.x = a.x - b.x;
		this.y = a.y - b.y;
		this.z = a.z - b.z;
	}

	public Vector3f toVector3f() {
		return new Vector3f(x, y, z);
	}

	public void set(Vector3b a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;

	}

	public float lengthSquared() {
		return (this.x * this.x + this.y * this.y + this.z * this.z);
	}
	public double lengthSquaredDouble() {
		return ((double)this.x * (double)this.x + (double)this.y * (double)this.y + (double)this.z * (double)this.z);
	}

	public boolean betweenIncl(Vector3i from, Vector3i to) {
		return x >= from.x && y >= from.y && z >= from.z && x <= to.x && y <= to.y && z <= to.z;
	}

	public boolean betweenExcl(Vector3i from, Vector3i to) {
		return x > from.x && y > from.y && z > from.z && x < to.x && y < to.y && z < to.z;
	}

	public boolean betweenIncExcl(Vector3i from, Vector3i to) {
		return x >= from.x && y >= from.y && z >= from.z && x < to.x && y < to.y && z < to.z;
	}

	public String toStringPure() {
		return x + ", " + y + ", " + z;
	}
	
	public static Vector3i deserializeStatic(DataInput b) throws IOException {
		Vector3i r = new Vector3i();
		r.deserialize(b);
		return r;
	}
	public void deserialize(DataInput b) throws IOException {
		x = b.readInt();
		y = b.readInt();
		z = b.readInt();
	}

	public void serialize(DataOutput b) throws IOException {
		b.writeInt(x);
		b.writeInt(y);
		b.writeInt(z);
	}

	public void min(int x, int y, int z) {
		this.x = Math.min(this.x, x);
		this.y = Math.min(this.y, y);
		this.z = Math.min(this.z, z);
	}

	public void max(int x, int y, int z) {
		this.x = Math.max(this.x, x);
		this.y = Math.max(this.y, y);
		this.z = Math.max(this.z, z);
	}

	public int getFirstLargest() {
		return Math.max(Math.max(x,y),z);
	}

	public int getFirstSmallest() {
		return Math.min(Math.min(x,y),z);
	}

	public float getDistance(Vector3i other) {
		return Vector3fTools.distance(this, other);
	}
}
