/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the description of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.schema.common;

import java.util.Random;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;

/**
 * <code>FastMath</code> provides 'fast' math approximations and float equivalents of Math
 * functions.  These are all used as static values and functions.
 *
 * @author Various
 * @version $Id: FastMath.java 4131 2009-03-19 20:15:28Z blaine.dev $
 */

final public class FastMath {
	/**
	 * A "close to zero" double epsilon value for use
	 */
	public static final double DBL_EPSILON = 2.220446049250313E-16d;
	/**
	 * A "close to zero" float epsilon value for use
	 */
	public static final float FLT_EPSILON = 1.1920928955078125E-7f;
	/**
	 * A "close to zero" float epsilon value for use
	 */
	public static final float ZERO_TOLERANCE = 0.0001f;
	public static final float ONE_THIRD = 1f / 3f;
	/**
	 * The value PI as a float. (180 degrees)
	 */
	public static final float PI = (float) Math.PI;
	public static final float LOG2 = FastMath.log(2);
	public static final double LOG2d = Math.log(2);
	/**
	 * The value 2PI as a float. (360 degrees)
	 */
	public static final float TWO_PI = 2.0f * PI;
	/**
	 * The value PI/2 as a float. (90 degrees)
	 */
	public static final float HALF_PI = 0.5f * PI;
	/**
	 * The value PI/4 as a float. (45 degrees)
	 */
	public static final float QUARTER_PI = 0.25f * PI;
	/**
	 * The value 1/PI as a float.
	 */
	public static final float INV_PI = 1.0f / PI;
	/**
	 * The value 1/(2PI) as a float.
	 */
	public static final float INV_TWO_PI = 1.0f / TWO_PI;
	/**
	 * A value to multiply a degree value by, to convert it to radians.
	 */
	public static final float DEG_TO_RAD = PI / 180.0f;
	/**
	 * A value to multiply a radian value by, to convert it to degrees.
	 */
	public static final float RAD_TO_DEG = 180.0f / PI;
	/**
	 * A precreated random object for random numbers.
	 */
	public static final Random rand = new Random(System.currentTimeMillis());
	@SuppressWarnings("unused")
	private static final float RAD;
	private static final int SIN_BITS, SIN_MASK, SIN_COUNT;
	private static final float radFull, radToIndex;
	private static final float[] sin, cos;
	private static final int ATAN2_BITS = 7;
	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
	private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	private static final int ATAN2_COUNT = ATAN2_MASK + 1;
	private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
	private static final float ATAN2_DIM_MINUS_1 = (ATAN2_DIM - 1);
	private static final float[] atan2 = new float[ATAN2_COUNT];
	private static final int BIG_ENOUGH_INT = 16 * 1024;
	private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
	private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;
	private static float WAVE_BUFFER[];

	static {
		WAVE_BUFFER = new float[0x10000];

		for (int i = 0; i < 0x10000; i++) {
			WAVE_BUFFER[i] = (float) Math
					.sin((i * Math.PI * 2D) / 65536D);
		}
	}
	public static final float sinDegStrict(float deg) {
		return (float) Math.sin(deg * RAD);
	}
	static {
		RAD = (float) Math.PI / 180.0f;
		SIN_BITS = 12;
		SIN_MASK = ~(-1 << SIN_BITS);
		SIN_COUNT = SIN_MASK + 1;

		radFull = (float) (Math.PI * 2.0);
		radToIndex = SIN_COUNT / radFull;
		sin = new float[SIN_COUNT];
		cos = new float[SIN_COUNT];

		for (int i = 0; i < SIN_COUNT; i++) {
			sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
			cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
		}
	}

	static {
		for (int i = 0; i < ATAN2_DIM; i++) {
			for (int j = 0; j < ATAN2_DIM; j++) {
				float x0 = (float) i / ATAN2_DIM;
				float y0 = (float) j / ATAN2_DIM;

				atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
			}
		}
	}

	private FastMath() {
	}

	public static float min(float a, float b, float c) {
		if (a < b) {
			//a smaller
			if (a < c) {
				return a;
			} else {
				return c;
			}
		} else {
			//b smaller
			if (b < c) {
				return b;
			} else {
				return c;
			}
		}
	}

	public static float max(float a, float b, float c) {
		if (a > b) {
			//a smaller
			if (a > c) {
				return a;
			} else {
				return c;
			}
		} else {
			//b smaller
			if (b > c) {
				return b;
			} else {
				return c;
			}
		}
	}

	/**
	 * Returns Absolute value of a float.
	 *
	 * @param fValue The value to abs.
	 * @return The abs of the value.
	 * @see java.lang.Math#abs(float)
	 */
	public static float abs(float fValue) {
		if (fValue < 0) {
			return -fValue;
		}
		return fValue;
	}

	public static short abs(short fValue) {
		return fValue < 0 ? (short) -fValue : fValue;
	}

	/**
	 * Returns the arc cosine of an angle given in radians.<br>
	 * Special cases:
	 * <ul><li>If fValue is smaller than -1, then the result is PI.
	 * <li>If the argument is greater than 1, then the result is 0.</ul>
	 *
	 * @param fValue The angle, in radians.
	 * @return fValue's acos
	 * @see java.lang.Math#acos(double)
	 */
	public static float acos(float fValue) {
		if (-1.0f < fValue) {
			if (fValue < 1.0f) {
				return (float) Math.acos(fValue);
			}

			return 0.0f;
		}

		return PI;
	}

	/**
	 * Returns a cubic approximation (the Lagrange polynomial) of
	 * the arc cosine of an angle given in radians.<br>
	 * Special cases:
	 * <ul><li>If fValue is smaller than -1, then the result is PI.
	 * <li>If the argument is greater than 1, then the result is 0.</ul>
	 *
	 * @param fValue The angle, in radians.
	 * @return fValue's acos
	 * @see java.lang.Math#acos(double)
	 */
	public static double acosFast(double x) {
		return (-0.69813170079773212 * x * x - 0.87266462599716477) * x + 1.5707963267948966;
	}

	/**
	 * Returns the arc sine of an angle given in radians.<br>
	 * Special cases:
	 * <ul><li>If fValue is smaller than -1, then the result is -HALF_PI.
	 * <li>If the argument is greater than 1, then the result is HALF_PI.</ul>
	 *
	 * @param fValue The angle, in radians.
	 * @return fValue's asin
	 * @see java.lang.Math#asin(double)
	 */
	public static float asin(float fValue) {
		if (-1.0f < fValue) {
			if (fValue < 1.0f) {
				return (float) Math.asin(fValue);
			}

			return HALF_PI;
		}

		return -HALF_PI;
	}

	/**
	 * Returns the arc tangent of an angle given in radians.<br>
	 *
	 * @param fValue The angle, in radians.
	 * @return fValue's asin
	 * @see java.lang.Math#atan(double)
	 */
	public static float atan(float fValue) {
		return (float) Math.atan(fValue);
	}

	/**
	 * A direct call to Math.atan2.
	 *
	 * @param fY
	 * @param fX
	 * @return Math.atan2(fY, fX)
	 * @see java.lang.Math#atan2(double, double)
	 */
	public static float atan2(float fY, float fX) {
		return (float) Math.atan2(fY, fX);
	}

	public static final float atan2Fast(float y, float x) {
		float add, mul;

		if (x < 0.0f) {
			if (y < 0.0f) {
				x = -x;
				y = -y;

				mul = 1.0f;
			} else {
				x = -x;
				mul = -1.0f;
			}

			add = -3.141592653f;
		} else {
			if (y < 0.0f) {
				y = -y;
				mul = -1.0f;
			} else {
				mul = 1.0f;
			}

			add = 0.0f;
		}

		float invDiv = ATAN2_DIM_MINUS_1 / ((x < y) ? y : x);

		int xi = (int) (x * invDiv);
		int yi = (int) (y * invDiv);

		return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
	}

	public static float carmackInvSqrt(float x) {
		float xhalf = 0.5f * x;
		int i = Float.floatToIntBits(x);
		i = 0x5f3759df - (i >> 1);
		x = Float.intBitsToFloat(i);
		x = x * (1.5f - xhalf * x * x);
		return x;
	}

	//	public static float carmackSqrt(float x) {
	//		float xhalf = 0.5f * x;
	//		int i = Float.floatToIntBits(x);
	//		i = 0x5f3759df - (i >> 1);
	//		x = Float.intBitsToFloat(i);
	//		x = x * (1.5f - xhalf * x * x);
	//		x = x * (1.5f - xhalf * x * x);
	//		x = x * (1.5f - xhalf * x * x);
	//		x = x * (1.5f - xhalf * x * x);
	//		return 1 / x;
	//	}
	public static float carmackSqrt(float n) {
		float x = n;
		float xhalf = 0.5f * x;
		int i = Float.floatToIntBits(x);
		i = 0x5f3759df - (i >> 1);
		x = Float.intBitsToFloat(i);
		x = x * (1.5f - xhalf * x * x);
		return x * n;
	}

	/**
	 * Converts a point from Cartesian coordinates (using positive Y as up) to
	 * Spherical and stores the results in the store var. (Radius, Azimuth,
	 * Polar)
	 */
	public static Vector3f cartesianToSpherical(Vector3f cartCoords,
	                                            Vector3f store) {
		if (cartCoords.x == 0) {
			cartCoords.x = FastMath.FLT_EPSILON;
		}
		store.x = FastMath
				.sqrt((cartCoords.x * cartCoords.x)
						+ (cartCoords.y * cartCoords.y)
						+ (cartCoords.z * cartCoords.z));
		store.y = FastMath.atan(cartCoords.z / cartCoords.x);
		if (cartCoords.x < 0) {
			store.y += FastMath.PI;
		}
		store.z = FastMath.asin(cartCoords.y / store.x);
		return store;
	}

	/**
	 * Converts a point from Cartesian coordinates (using positive Z as up) to
	 * Spherical and stores the results in the store var. (Radius, Azimuth,
	 * Polar)
	 */
	public static Vector3f cartesianZToSpherical(Vector3f cartCoords,
	                                             Vector3f store) {
		if (cartCoords.x == 0) {
			cartCoords.x = FastMath.FLT_EPSILON;
		}
		store.x = FastMath
				.sqrt((cartCoords.x * cartCoords.x)
						+ (cartCoords.y * cartCoords.y)
						+ (cartCoords.z * cartCoords.z));
		store.z = FastMath.atan(cartCoords.z / cartCoords.x);
		if (cartCoords.x < 0) {
			store.z += FastMath.PI;
		}
		store.y = FastMath.asin(cartCoords.y / store.x);
		return store;
	}

	/**
	 * Rounds a fValue up.  A call to Math.ceil
	 *
	 * @param fValue The value.
	 * @return The fValue rounded up
	 * @see java.lang.Math#ceil(double)
	 */
	public static float ceil(float fValue) {
		return (float) Math.ceil(fValue);
	}

	/**
	 * Take a float input and clamp it between min and max.
	 *
	 * @param input
	 * @param min
	 * @param max
	 * @return clamped input
	 */
	public static byte clamp(byte input, byte min, byte max) {
		return (input < min) ? min : (input > max) ? max : input;
	}

	/**
	 * Take a float input and clamp it between min and max.
	 *
	 * @param input
	 * @param min
	 * @param max
	 * @return clamped input
	 */
	public static float clamp(float input, float min, float max) {
		return Math.min(max, Math.max(min, input));
	}

	/**
	 * @param x the value whose sign is to be adjusted.
	 * @param y the value whose sign is to be used.
	 * @return x with its sign changed to match the sign of y.
	 */
	public static float copysign(float x, float y) {
		if (y >= 0 && x <= -0) {
			return -x;
		} else if (y < 0 && x >= 0) {
			return -x;
		} else {
			return x;
		}
	}

	/**
	 * Returns cos of a value.
	 *
	 * @param fValue The value to cosine, in radians.
	 * @return The cosine of fValue.
	 * @see java.lang.Math#cos(double)
	 */
	public static float cos(float fValue) {
		return sin(fValue + HALF_PI);
	}

	public static final float cosFast(float rad) {
		return cos[(int) (rad * radToIndex) & SIN_MASK];
	}

	/**
	 * cos looked up in the sin table with the appropriate offset
	 */
	public static final float cosTable(float par0) {
		return WAVE_BUFFER[(int) (par0 * 10430.38F + 16384F) & 0xffff];
	}

	/**
	 * Given 3 points in a 2d plane, this function computes if the points going from A-B-C
	 * are moving counter clock wise.
	 *
	 * @param p0 Point 0.
	 * @param p1 Point 1.
	 * @param p2 Point 2.
	 * @return 1 If they are CCW, -1 if they are not CCW, 0 if p2 is between p0 and p1.
	 */
	public static int counterClockwise(Vector2f p0, Vector2f p1, Vector2f p2) {
		float dx1, dx2, dy1, dy2;
		dx1 = p1.x - p0.x;
		dy1 = p1.y - p0.y;
		dx2 = p2.x - p0.x;
		dy2 = p2.y - p0.y;
		if (dx1 * dy2 > dy1 * dx2) {
			return 1;
		}
		if (dx1 * dy2 < dy1 * dx2) {
			return -1;
		}
		if ((dx1 * dx2 < 0) || (dy1 * dy2 < 0)) {
			return -1;
		}
		if ((dx1 * dx1 + dy1 * dy1) < (dx2 * dx2 + dy2 * dy2)) {
			return 1;
		}
		return 0;
	}

	public static byte cyclicBWModulo(byte i, byte max) {
		if (max == 0) {
			return 0;
		}
		if (i < 0) {
			return (byte) (Math.abs(i + 1) % (max));
		} else {
			return (byte) (i % max);
		}
	}

	public static int cyclicBWModulo(int i, int max) {
		if (max == 0) {
			return 0;
		}
		if (i < 0) {
			return (Math.abs(i + 1) % (max));
		} else {
			return i % max;
		}
	}

	public static byte cyclicModulo(byte i, byte max) {
		if (max == 0) {
			return 0;
		}
		if (i < 0) {
			return (byte) ((max - 1) - (Math.abs(i + 1) % (max)));
		} else {
			return (byte) (i % max);
		}
	}

	public static float cyclicModulo(float i, float max) {
		if (max == 0) {
			return 0;
		}
		if (i < 0) {
			return (max - 1) - (Math.abs(i + 1) % (max));
		} else {
			return i % max;
		}
	}

	public static int cyclicModulo(int i, int max) {
		if (max == 0) {
			return 0;
		}
		if (i < 0) {
			return (max - 1) - (Math.abs(i + 1) % (max));
		} else {
			return i % max;
		}
	}
	
	
	public static boolean calculateThreeCircleIntersection(double x0, double y0,
			double r0, double x1, double y1, double r1, double x2, double y2,
			double r2, Vector2d result) {
		double a, dx, dy, d, h, rx, ry;
		double point2_x, point2_y;

		/*
		 * dx and dy are the vertical and horizontal distances between the
		 * circle centers.
		 */
		dx = x1 - x0;
		dy = y1 - y0;

		/* Determine the straight-line distance between the centers. */
		d = Math.sqrt((dy * dy) + (dx * dx));

		/* Check for solvability. */
		if (d > (r0 + r1)) {
			/* no solution. circles do not intersect. */
			return false;
		}
		if (d < Math.abs(r0 - r1)) {
			/* no solution. one circle is contained in the other */
			return false;
		}

		/*
		 * 'point 2' is the point where the line through the circle intersection
		 * points crosses the line between the circle centers.
		 */

		/* Determine the distance from point 0 to point 2. */
		a = ((r0 * r0) - (r1 * r1) + (d * d)) / (2.0 * d);

		/* Determine the coordinates of point 2. */
		point2_x = x0 + (dx * a / d);
		point2_y = y0 + (dy * a / d);

		/*
		 * Determine the distance from point 2 to either of the intersection
		 * points.
		 */
		h = Math.sqrt((r0 * r0) - (a * a));

		/*
		 * Now determine the offsets of the intersection points from point 2.
		 */
		rx = -dy * (h / d);
		ry = dx * (h / d);

		/* Determine the absolute intersection points. */
		double intersectionPoint1_x = point2_x + rx;
		double intersectionPoint2_x = point2_x - rx;
		double intersectionPoint1_y = point2_y + ry;
		double intersectionPoint2_y = point2_y - ry;

		System.err.println("INTERSECTION Circle1 AND Circle2: (" + intersectionPoint1_x
				+ "," + intersectionPoint1_y + ")" + " AND ("
				+ intersectionPoint2_x + "," + intersectionPoint2_y + ")");

		/*
		 * Lets determine if circle 3 intersects at either of the above
		 * intersection points.
		 */
		dx = intersectionPoint1_x - x2;
		dy = intersectionPoint1_y - y2;
		double d1 = Math.sqrt((dy * dy) + (dx * dx));

		dx = intersectionPoint2_x - x2;
		dy = intersectionPoint2_y - y2;
		double d2 = Math.sqrt((dy * dy) + (dx * dx));

		if (Math.abs(d1 - r2) < DBL_EPSILON+2d) {
//			Log.d("INTERSECTION Circle1 AND Circle2 AND Circle3:", "("
//					+ intersectionPoint1_x + "," + intersectionPoint1_y + ")");
			result.x = intersectionPoint1_x;
			result.y = intersectionPoint1_y;
		} else if (Math.abs(d2 - r2) < DBL_EPSILON+2d) {
//			Log.d("INTERSECTION Circle1 AND Circle2 AND Circle3:", "("
//					+ intersectionPoint2_x + "," + intersectionPoint2_y + ")"); // here
//																				// was
//																				// an
//																				// error
			result.x = intersectionPoint2_x;
			result.y = intersectionPoint2_y;
		} else {
			
			result.x = intersectionPoint2_x;
			result.y = intersectionPoint2_y;
			if(Math.abs(d1 - r2) < Math.abs(d2 - r2)){
				dx = intersectionPoint1_x - x2;
				dy = intersectionPoint1_y - y2;
				
				 double norm;

			        norm =  (1.0/Math.sqrt(dx*dx + dy*dy));
			        dx *= norm;
			        dy *= norm;
				
				result.x += dx * Math.abs(d1 - r2)*0.5;
				result.y += dy * Math.abs(d1 - r2)*0.5;
			}else{
				dx = intersectionPoint2_x - x2;
				dy = intersectionPoint2_y - y2;
				
				 double norm;

			        norm =  (1.0/Math.sqrt(dx*dx + dy*dy));
			        dx *= norm;
			        dy *= norm;
				
				result.x += dx * Math.abs(d2 - r2)*0.5;
				result.y += dy * Math.abs(d2 - r2)*0.5;
			}
			
			
			
			System.err.println("INTERSECTION Circle1 AND Circle2 AND Circle3: NONE");
			return true;
			
		}
		return true;
	}
	/**
	 * Returns the determinant of a 4x4 matrix.
	 */
	public static float determinant(double m00, double m01, double m02,
	                                double m03, double m10, double m11, double m12, double m13,
	                                double m20, double m21, double m22, double m23, double m30,
	                                double m31, double m32, double m33) {

		double det01 = m20 * m31 - m21 * m30;
		double det02 = m20 * m32 - m22 * m30;
		double det03 = m20 * m33 - m23 * m30;
		double det12 = m21 * m32 - m22 * m31;
		double det13 = m21 * m33 - m23 * m31;
		double det23 = m22 * m33 - m23 * m32;
		return (float) (m00 * (m11 * det23 - m12 * det13 + m13 * det12) - m01
				* (m10 * det23 - m12 * det03 + m13 * det02) + m02
				* (m10 * det13 - m11 * det03 + m13 * det01) - m03
				* (m10 * det12 - m11 * det02 + m12 * det01));
	}

	/**
	 * Returns E^fValue
	 *
	 * @param fValue Value to raise to a power.
	 * @return The value E^fValue
	 * @see java.lang.Math#exp(double)
	 */
	public static float exp(float fValue) {
		return (float) Math.exp(fValue);
	}

	public static int fastCeil(float x) {
		//return (int)(x + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
		//		    return BIG_ENOUGH_INT - (int)(BIG_ENOUGH_FLOOR-x);
		return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - x); // credit: roquen
	}

	public static int fastFloor(float x) {
		return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
	}

	public static int fastRound(float x) {
		return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
	}

	/**
	 * Returns a number rounded down.
	 *
	 * @param fValue The value to round
	 * @return The given number rounded down
	 * @see java.lang.Math#floor(double)
	 */
	public static float floor(float fValue) {
		return (float) Math.floor(fValue);
	}

	/**
	 * Returns 1/sqrt(fValue)
	 *
	 * @param fValue The value to process.
	 * @return 1/sqrt(fValue)
	 * @see java.lang.Math#sqrt(double)
	 */
	public static float invSqrt(float fValue) {
		return (float) (1.0f / Math.sqrt(fValue));
	}

	/**
	 * Returns true if the number is a power of 2 (2,4,8,16...)
	 * <p/>
	 * A good implementation found on the Java boards. note: a number is a power
	 * of two if and only if it is the smallest number with that number of
	 * significant bits. Therefore, if you subtract 1, you know that the new
	 * number will have fewer bits, so ANDing the original number with anything
	 * less than it will give 0.
	 *
	 * @param number The number to test.
	 * @return True if it is a power of two.
	 */
	public static boolean isPowerOfTwo(int number) {
		return (number > 0) && (number & (number - 1)) == 0;
	}

	/**
	 * Linear interpolation from startValue to endValue by the given percent.
	 * Basically: ((1 - percent) * startValue) + (percent * endValue)
	 *
	 * @param percent    Percent value to use.
	 * @param startValue Begining value. 0% of f
	 * @param endValue   ending value. 100% of f
	 * @return The interpolated value between startValue and endValue.
	 */
	public static float LERP(float percent, float startValue, float endValue) {
		if (startValue == endValue) {
			return startValue;
		}
		return ((1 - percent) * startValue) + (percent * endValue);
	}

	/**
	 * Returns the log base E of a value.
	 *
	 * @param fValue The value to log.
	 * @return The log of fValue base E
	 * @see java.lang.Math#log(double)
	 */
	public static float log(float fValue) {
		return (float) Math.log(fValue);
	}

	/**
	 * Returns the logarithm of value with given base, calculated as log(value)/log(base),
	 * so that pow(base, return)==value (contributed by vear)
	 *
	 * @param value The value to log.
	 * @param base  Base of logarithm.
	 * @return The logarithm of value with given base
	 */
	public static float log(float value, float base) {
		return (float) (Math.log(value) / Math.log(base));
	}

	public static float log2(float fValue) {
		return (float) ((Math.log(fValue)) / LOG2d);
	}

	/**
	 * (((1 / (1 + 1.5^-x))-0.5)*2.0)*p
	 *
	 * @param x
	 * @param p         maximum
	 * @param steepNess > 1 (1.5 is good default)
	 * @return
	 */
	public static double logGrowth(double x, double steepNess, double p) {
		return (((1.0 / (1.0 + Math.pow(steepNess, -x))) - 0.5) * 2.0) * p;
	}

	public static void main(String[] asd) {
		for (int i = 32; i >= -32; i--) {
			System.err.println((float) Math.sqrt(i) + " -- " + FastMath.carmackSqrt(i));
		}
	}

	public static int nearestPowerOfTwo(int number) {
		return (int) Math.pow(2, Math.ceil(Math.log(number) / Math.log(2)));
	}

	/**
	 * Returns a random float between 0 and 1.
	 *
	 * @return A random float between <tt>0.0f</tt> (inclusive) to
	 * <tt>1.0f</tt> (exclusive).
	 */
	public static float nextRandomFloat() {
		return rand.nextFloat();
	}

	public static int nextRandomInt() {
		return rand.nextInt();
	}

	/**
	 * Returns a random float between min and max.
	 *
	 * @return A random int between <tt>min</tt> (inclusive) to
	 * <tt>max</tt> (inclusive).
	 */
	public static int nextRandomInt(int min, int max) {
		return (int) (nextRandomFloat() * (max - min + 1)) + min;
	}

	/**
	 * Takes an value and expresses it in terms of min to max.
	 *
	 * @param val -
	 *            the angle to normalize (in radians)
	 * @return the normalized angle (also in radians)
	 */
	public static float normalize(float val, float min, float max) {
		if (Float.isInfinite(val) || Float.isNaN(val)) {
			return 0f;
		}
		float range = max - min;
		while (val > max) {
			val -= range;
		}
		while (val < min) {
			val += range;
		}
		return val;
	}

	/**
	 * Test if a point is inside a triangle.  1 if the point is on the ccw side,
	 * -1 if the point is on the cw side, and 0 if it is on neither.
	 *
	 * @param t0                      First point of the triangle.
	 * @param t1                      Second point of the triangle.
	 * @param t2                      Third point of the triangle.
	 * @param multiTexturePathPattern The point to test.
	 * @return Value 1 or -1 if inside triangle, 0 otherwise.
	 */
	public static int pointInsideTriangle(Vector2f t0, Vector2f t1, Vector2f t2, Vector2f p) {
		int val1 = counterClockwise(t0, t1, p);
		if (val1 == 0) {
			return 1;
		}
		int val2 = counterClockwise(t1, t2, p);
		if (val2 == 0) {
			return 1;
		}
		if (val2 != val1) {
			return 0;
		}
		int val3 = counterClockwise(t2, t0, p);
		if (val3 == 0) {
			return 1;
		}
		if (val3 != val1) {
			return 0;
		}
		return val3;
	}

	/**
	 * Returns a number raised to an exponent power.  fBase^fExponent
	 *
	 * @param fBase     The base value (IE 2)
	 * @param fExponent The exponent value (IE 3)
	 * @return base raised to exponent (IE 8)
	 * @see java.lang.Math#pow(double, double)
	 */
	public static float pow(float fBase, float fExponent) {
		return (float) Math.pow(fBase, fExponent);
	}

	/**
	 * Fast Trig functions for x86. This forces the trig functiosn to stay
	 * within the safe area on the x86 processor (-45 degrees to +45 degrees)
	 * The results may be very slightly off from what the Math and StrictMath
	 * trig functions give due to rounding in the angle reduction but it will be
	 * very very close.
	 * <p/>
	 * note: code from wiki posting on java.net by jeffpk
	 */
	public static float reduceSinAngle(float radians) {
		radians %= TWO_PI; // put us in -2PI to +2PI space
		if (Math.abs(radians) > PI) { // put us in -PI to +PI space
			radians = radians - (TWO_PI);
		}
		if (Math.abs(radians) > HALF_PI) {// put us in -PI/2 to +PI/2 space
			radians = PI - radians;
		}

		return radians;
	}

	/**
	 * this function resambles an S-curve
	 * <p/>
	 * f(0,p) = 0
	 * f(1,p) = 1
	 * <p/>
	 * p should be >= 2
	 * <p/>
	 * Slope at x == 0 and x == 1 is 0.
	 *
	 * @param x
	 * @param p Where 'p' is the power coefficient and denotes how steep the curve is.
	 * @return
	 */
	public static double sigmoid(double x, double p) {
		return x < .5 ? .5 * Math.pow(2.0 * x, p) : 1 - .5 * Math.pow(
				2.0 * (1.0 - x), p);
	}

	/**
	 * this function resambles an S-curve
	 * <p/>
	 * f(0,p) = 0
	 * f(1,p) = 1
	 * <p/>
	 * p should be >= 2
	 * <p/>
	 * Slope at x == 0 and x == 1 is 0.
	 *
	 * @param x
	 * @param p Where 'p' is the power coefficient and denotes how steep the curve is.
	 * @return
	 */
	public static float sigmoid(float x, float p) {
		return x < .5f ? .5f * pow(2.0f * x, p) : 1f - .5f * pow(
				2.0f * (1.0f - x), p);
	}

	/**
	 * Returns 1 if the number is positive, -1 if the number is negative, and 0 otherwise
	 *
	 * @param fValue The float to examine.
	 * @return The float's sign.
	 */
	public static float sign(float fValue) {
		return Math.signum(fValue);
	}

	/**
	 * Returns 1 if the number is positive, -1 if the number is negative, and 0 otherwise
	 *
	 * @param iValue The integer to examine.
	 * @return The integer's sign.
	 */
	public static int sign(int iValue) {
		if (iValue > 0) {
			return 1;
		}

		if (iValue < 0) {
			return -1;
		}

		return 0;
	}

	/**
	 * Returns sine of a value.
	 * <p/>
	 * note: code from wiki posting on java.net by jeffpk
	 *
	 * @param fValue The value to sine, in radians.
	 * @return The sine of fValue.
	 * @see java.lang.Math#sin(double)
	 */
	public static float sin(float fValue) {
		fValue = reduceSinAngle(fValue); // limits angle to between -PI/2 and +PI/2
		if (Math.abs(fValue) <= Math.PI / 4) {
			return (float) Math.sin(fValue);
		}

		return (float) Math.cos(Math.PI / 2 - fValue);
	}

	public static final float sinFast(float rad) {
		return sin[(int) (rad * radToIndex) & SIN_MASK];
	}

	/**
	 * sin looked up in a table
	 */
	public static final float sinTable(float par0) {
		return WAVE_BUFFER[(int) (par0 * 10430.38F) & 0xffff];
	}

	/**
	 * Converts a point from Spherical coordinates to Cartesian (using positive
	 * Y as up) and stores the results in the store var.
	 */
	public static Vector3f sphericalToCartesian(Vector3f sphereCoords,
	                                            Vector3f store) {
		store.y = sphereCoords.x * FastMath.sin(sphereCoords.z);
		float a = sphereCoords.x * FastMath.cos(sphereCoords.z);
		store.x = a * FastMath.cos(sphereCoords.y);
		store.z = a * FastMath.sin(sphereCoords.y);

		return store;
	}

	/**
	 * Converts a point from Spherical coordinates to Cartesian (using positive
	 * Z as up) and stores the results in the store var.
	 */
	public static Vector3f sphericalToCartesianZ(Vector3f sphereCoords,
	                                             Vector3f store) {
		store.z = sphereCoords.x * FastMath.sin(sphereCoords.z);
		float a = sphereCoords.x * FastMath.cos(sphereCoords.z);
		store.x = a * FastMath.cos(sphereCoords.y);
		store.y = a * FastMath.sin(sphereCoords.y);

		return store;
	}

	/**
	 * Returns the value squared.  fValue ^ 2
	 *
	 * @param fValue The vaule to square.
	 * @return The square of the given value.
	 */
	public static float sqr(float fValue) {
		return fValue * fValue;
	}

	/**
	 * Returns the square root of a given value.
	 *
	 * @param fValue The value to sqrt.
	 * @return The square root of the given value.
	 * @see java.lang.Math#sqrt(double)
	 */
	public static float sqrt(float fValue) {
		return (float) Math.sqrt(fValue);
	}

	/**
	 * Returns the tangent of a value.  If USE_FAST_TRIG is enabled, an approximate value
	 * is returned.  Otherwise, a direct value is used.
	 *
	 * @param fValue The value to tangent, in radians.
	 * @return The tangent of fValue.
	 * @see java.lang.Math#tan(double)
	 */
	public static float tan(float fValue) {
		return (float) Math.tan(fValue);
	}

	public static float carmackLength(Vector3f v) {
		return carmackSqrt(v.x * v.x + v.y * v.y + v.z * v.z);
	}
	public static float carmackLength(Vector3i v) {
		return carmackSqrt(v.x * v.x + v.y * v.y + v.z * v.z);
	}

	public static float carmackLength(float x, float y, float z) {
		return carmackSqrt(x * x + y * y + z * z);
	}

	public static float carmackLength(int x, int y, int z) {
		return carmackSqrt(x * x + y * y + z * z);
	}

	public static void normalizeCarmack(Vector3f v) {
		float norm;

		norm = (carmackInvSqrt(v.x * v.x + v.y * v.y + v.z * v.z));
		v.x *= norm;
		v.y *= norm;
		v.z *= norm;
	}

	public static int round(float x) {
		return FastMath.fastFloor(x + 0.5f);
	}

	public static int round(double x) {
		return (int) Math.floor(x + 0.5d);
	}

	

}