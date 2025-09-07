package org.schema.common.util.linAlg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

public class Vector3s implements Comparable<Vector3s> {
	public static final long SHORT_MAX = Short.MAX_VALUE;
	public static final long SHORT_MAX2 = SHORT_MAX * 2;
	public static final long SHORT_MAX2x2 = SHORT_MAX2 * SHORT_MAX2;
	private static final Pattern parameterRegex = Pattern.compile("(\\-?\\s*[0-9]+)[\\s\\,\\.]+(\\-?\\s*[0-9]+)[\\s\\,\\.]+(\\-?\\s*[0-9]+)");
	public short x;
	public short y;
	public short z;

	public Vector3s() {
	}

	public Vector3s(float x, float y, float z) {
		this.x = (short) x;
		this.y = (short) y;
		this.z = (short) z;
	}

	public Vector3s(short x, short y, short z) {

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3s(Vector3f v) {
		this.x = (short) v.x;
		this.y = (short) v.y;
		this.z = (short) v.z;
	}

	public Vector3s(Vector3s dim) {
		this.x = dim.x;
		this.y = dim.y;
		this.z = dim.z;
	}

	public static void main(String[] sdf) {

		try {
			parseVector3sFree("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("asdf asdf asfd");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("1 2 3");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("-12 2 3");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("-12 2 -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("-012 2 -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("012 2 -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("012, 2, -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("012,   2,   -0343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("012,  . - 2,  . -343");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			parseVector3sFree("1.-2.33");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Vector3s parseVector3i(String in) {
		String[] split = in.split(",");
		if (split.length != 3) {
			throw new NumberFormatException("Wrong number of arguments");
		}
		return new Vector3s(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
	}

	public static Vector3s parseVector3sFree(String in) {
		Vector3s p = new Vector3s();
		Matcher regexMatcher = parameterRegex.matcher(in.trim());
		short i = 0;
		while (regexMatcher.find()) {
			try {
				p.x = Short.parseShort(regexMatcher.group(1).replaceAll("\\s", ""));
				p.y = Short.parseShort(regexMatcher.group(2).replaceAll("\\s", ""));
				p.z = Short.parseShort(regexMatcher.group(3).replaceAll("\\s", ""));
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

	public static float getDisatance(Vector3s p1, Vector3s p2) {
		int x = (p2.x - p1.x);
		int y = (p2.y - p1.y);
		int z = (p2.z - p1.z);
		return FastMath.sqrt((x * x + y * y + z * z));
	}

	public void absolute() {
		this.x = (short) Math.abs(this.x);
		this.y = (short) Math.abs(this.y);
		this.z = (short) Math.abs(this.z);
	}

	public void add(short x, short y, short z) {
		this.x += x;
		this.y += y;
		this.z += z;

	}

	public void add(Vector3s b) {
		x += b.x;
		y += b.y;
		z += b.z;
	}

	public void add(Vector3s a, Vector3s b) {
		this.x = (short) (a.x + b.x);
		this.y = (short) (a.y + b.y);
		this.z = (short) (a.z + b.z);
	}

	@Override
	public int compareTo(Vector3s arg0) {
		//manhattan distance
		return (Math.abs(x) + Math.abs(y) + Math.abs(z)) - (Math.abs(arg0.x) + Math.abs(arg0.y) + Math.abs(arg0.z));

	}

	public void coordAdd(short coord, short val) {
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

	public void div(short s) {
		x /= s;
		y /= s;
		z /= s;

	}

	public boolean equals(short x, short y, short z) {
		return this.x == x && this.y == y && this.z == z;
	}

	public short getCoord(short coord) {
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
	 * @return the shorteger hash code value
	 */
	@Override
	public int hashCode() {
		int result = (x ^ (x >>> 8));
		result = 15 * result + (y ^ (y >>> 8));
		result = 15 * result + (z ^ (z >>> 8));
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
			Vector3s other = (Vector3s) o;
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

		//		System.err.prshortln("INNER: "+v+" -> "+x+"; "+y+"; "+z);

		long index = zL * SHORT_MAX2x2 + yL * SHORT_MAX2 + x;
		return index * 232323;
	}

	public final float length() {
		return FastMath.sqrt((this.x * this.x + this.y * this.y + this.z * this.z));
	}

	public void negate() {
		this.x = (short) -this.x;
		this.y = (short) -this.y;
		this.z = (short) -this.z;
	}

	public void scale(short s) {
		x *= s;
		y *= s;
		z *= s;
	}

	public void scale(short x, short y, short z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
	}

	public void scaleFloat(float s) {
		x *= s;
		y *= s;
		z *= s;
	}

	public void set(short x, short y, short z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3s pos) {
		set(pos.x, pos.y, pos.z);
	}

	public void sub(short x, short y, short z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;

	}

	public void sub(Vector3s b) {
		x -= b.x;
		y -= b.y;
		z -= b.z;

	}

	public void sub(Vector3s a, Vector3s b) {
		this.x = (short) (a.x - b.x);
		this.y = (short) (a.y - b.y);
		this.z = (short) (a.z - b.z);
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

	public boolean betweenIncl(Vector3s from, Vector3s to) {
		return x >= from.x && y >= from.y && z >= from.z && x <= to.x && y <= to.y && z <= to.z;
	}

	public boolean betweenExcl(Vector3s from, Vector3s to) {
		return x > from.x && y > from.y && z > from.z && x < to.x && y < to.y && z < to.z;
	}

	public boolean betweenIncExcl(Vector3s from, Vector3s to) {
		return x >= from.x && y >= from.y && z >= from.z && x < to.x && y < to.y && z < to.z;
	}

	public String toStringPure() {
		return x + ", " + y + ", " + z;
	}
}
