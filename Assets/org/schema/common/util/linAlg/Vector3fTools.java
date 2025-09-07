/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Vector3fTools</H2>
 * <H3>org.schema.common.util.linAlg</H3>
 * Vector3fTools.java
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

import org.schema.common.FastMath;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * The Class Vector3fTools.
 */
public class Vector3fTools {

	/**
	 * The df2.
	 */
	static DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");
	private static long lastTest = System.currentTimeMillis();

	public static Vector3f add(Vector3f a, Vector3f b) {
		Vector3f result = new Vector3f(a);
		result.add(b);
		return result;
	}

	/**
	 * Linearly interpolates between this and the supplied other vector by the supplied amount,
	 * storing the result in the supplied object.
	 *
	 * @return a reference to the result, for chaining.
	 */
	public static Vector3f lerp(Vector3f from, Vector3f other, float t, Vector3f result) {
		result.set(from.x + t * (other.x - from.x), from.y + t * (other.y - from.y), from.z + t * (other.z - from.z));
		return result;
	}

	/**
	 * Point to segment distance.
	 *
	 * @param point the point
	 * @param ep0   the ep0
	 * @param ep1   the ep1
	 * @return the vector3 d
	 * @throws NaNVectorException the na n vector exception
	 */
	public static Vector3f pointToSegmentDistance(Vector3f point, Vector3f ep0, Vector3f ep1) throws NaNVectorException {

		// convert the test point to be "local" to ep0
		Vector3f pointToEndpointA = Vector3fTools.sub(point, ep0);
		Vector3f normalizedDir = Vector3fTools.sub(ep1, ep0);
		float segmentLength = normalizedDir.length();
		if(normalizedDir.length() > 1) {
			normalizedDir.normalize();
		}
		// find the projection of "local" onto "segmentNormal"
		float segmentProjection = normalizedDir.dot(pointToEndpointA);
		if(segmentProjection == Float.NaN) {
			System.err.println(normalizedDir);
			System.err.println(pointToEndpointA);
			throw new NaNVectorException();
		}
		// handle boundary cases: when projection is not on segment, the
		// nearest point is one of the endpoints of the segment
		if(segmentProjection < 0) {
			return ep0;
		}
		if(segmentProjection > segmentLength) {
			return ep1;

		}
		// distance from A to projected point
		normalizedDir.scale(segmentProjection);

		// Vector to the actual projected Point
		normalizedDir.add(ep0);
		// Vector from our Point to segment point

		return normalizedDir;
	}

	/** Spherically interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is
	 * stored in this vector.
	 *
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @return This vector for chaining. */
	public static Vector3f slerp(Vector3f from, Vector3f target, float alpha, Vector3f res, Vector3f tmp) {
		res.set(from);
		tmp.set(target);
		float dot = from.dot(target);
		if(dot > 0.99995 || dot < 0.9995) {

			tmp.sub(from);
			res.scaleAdd(alpha, tmp);

			res.normalize();
			return res;
		}

		if(dot > 1) dot = 1;
		if(dot < -1) dot = -1;

		float theta0 = FastMath.acos(dot);
		float theta = theta0 * alpha;

		tmp.x -= from.x * dot;
		tmp.y -= from.y * dot;
		tmp.z -= from.z * dot;
		res.set(tmp);

		res.normalize();
		tmp.set(res);
		res.set(from);
		res.scale(FastMath.cos(theta));
		tmp.scale(FastMath.sin(theta));
		res.add(tmp);
		res.normalize();
		return res;
	}

	static Quat4f fromtwovectors(Vector3f uu, Vector3f vv) {
		Vector3f w = crossProduct(uu, vv);
		Quat4f q = new Quat4f(w.x, w.y, w.z, dot(uu, vv));

		float l = w.x * w.x + w.y * w.y + w.z * w.z;//sqlength(w);
		q.w += Math.sqrt(q.w * q.w + l);
		q.scale((float) (1.0 / Math.sqrt(q.w * q.w + l)));
		return q;
	}

	public static Vector3f slerp(Vector3f a, Vector3f b, float t) {
		float o = a.angle(b);//getFullRange2DAngleFast(a, b); //

		float facA = (FastMath.sin(1f - t) * o) / FastMath.sin(o);
		float facB = (FastMath.sin(o * t)) / (float) Math.sinh(o);

		Vector3f res = new Vector3f();

		Vector3f aTmp = new Vector3f(a);
		Vector3f bTmp = new Vector3f(b);
		aTmp.scale(facA);
		bTmp.scale(facB);

		res.add(aTmp, bTmp);
		res.normalize();
//		Vector3f aTmp = new Vector3f(a);
//		Vector3f bTmp = new Vector3f(b);
//		aTmp.scaleAdd(facA, a);
//		bTmp.scaleAdd(facB, b);
//
//		res.add(aTmp, bTmp);

//		System.err.println("RES; "+res+" == "+slerp2(a, b, t));

		return res;

	}

	public static Vector3f slerp2(Vector3f a, Vector3f b, float t) {
//		Quat4f qa = new Quat4f(0.0f,0.0f,0.0f,1.0f);
//		Quat4f qb;
//		 //qa = Quaternionf::Identity();
//		 qb = fromtwovectors(a,b);
//		 Quat4f res = new Quat4f();
//
//		 Quat4Util.slerp(qa, qb, t,res);
//		 System.err.println("SLERP: "+qa+" -> "+qb+" -> "+res+"; form "+a+"; "+b);
//		return new Vector3f(res.x, res.y, res.z);  // * a ?

		Quat4f qa = Quat4Util.fromEulerAngles(a.x, a.y, a.z);
		Quat4f qb = Quat4Util.fromEulerAngles(b.x, b.y, b.z);

		Quat4f res = new Quat4f();
//
		Quat4Util.slerp(qa, qb, t, res);

		return new Vector3f(res.x, res.y, res.z);
	}

	// returns true if line (L1, L2) intersects with the box (B1, B2)
	// returns intersection point in Hit
	public static Vector3f checkLineBox(Vector3f B1, Vector3f B2, Vector3f L1, Vector3f L2) {
		Vector3f hit = null;
		if(L2.x < B1.x && L1.x < B1.x) {
			return null;
		}
		if(L2.x > B2.x && L1.x > B2.x) {
			return null;
		}
		if(L2.y < B1.y && L1.y < B1.y) {
			return null;
		}
		if(L2.y > B2.y && L1.y > B2.y) {
			return null;
		}
		if(L2.z < B1.z && L1.z < B1.z) {
			return null;
		}
		if(L2.z > B2.z && L1.z > B2.z) {
			return null;
		}
		if(L1.x > B1.x && L1.x < B2.x && L1.y > B1.y && L1.y < B2.y && L1.z > B1.z && L1.z < B2.z) {
			hit = L1;
			return hit;
		}
		if((((hit = getIntersection(L1.x - B1.x, L2.x - B1.x, L1, L2)) != null) && inBox(hit, B1, B2, 1)) || (((hit = getIntersection(L1.y - B1.y, L2.y - B1.y, L1, L2)) != null) && inBox(hit, B1, B2, 2)) || (((hit = getIntersection(L1.z - B1.z, L2.z - B1.z, L1, L2)) != null) && inBox(hit, B1, B2, 3)) || (((hit = getIntersection(L1.x - B2.x, L2.x - B2.x, L1, L2)) != null) && inBox(hit, B1, B2, 1)) || (((hit = getIntersection(L1.y - B2.y, L2.y - B2.y, L1, L2)) != null) && inBox(hit, B1, B2, 2)) || (((hit = getIntersection(L1.z - B2.z, L2.z - B2.z, L1, L2)) != null) && inBox(hit, B1, B2, 3))) {
			return hit;
		}

		return null;
	}

	public static void clamp(final Vector3f v, final Vector3f min, final Vector3f max) {
		v.x = FastMath.clamp(v.x, min.x, max.x);
		v.y = FastMath.clamp(v.y, min.y, max.y);
		v.z = FastMath.clamp(v.z, min.z, max.z);
	}

	public static Vector3f[] clone(Vector3f[] from) {
		Vector3f[] copy = Arrays.copyOf(from, from.length);
		return copy;
	}

	public static float getAngleSigned(Vector3f Va, Vector3f Vb, Vector3f plane, Vector3f helper) {
		Va.normalize();
		Vb.normalize();
		float dot = Vector3fTools.dot(Va, Vb);
		Vector3f cross = crossProduct(Va, Vb, helper);
		//float angle = FastMath.acos(Vector3fTools.dot(Va, Vb));
		float angle = FastMath.atan2(cross.length(), dot);
		if(dot(plane, cross) < 0) { // Or > 0
			angle = -angle;
		}
		return angle;
	}

	public static float getAngleSigned(Vector3f Va, Vector3f Vb, Vector3f plane) {
		return getAngleSigned(Va, Vb, plane, new Vector3f());
	}

	/**
	 * Cross.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the float
	 */
	public static float cross(Vector3f a, Vector3f b) {
		float u = (b.z * a.y) - (b.y * a.z);
		float v = (b.x * a.z) - (b.z * a.x);
		float w = (b.x * a.y) - (b.y * a.x);
		return u + v + w;
	}

	/**
	 * Cross product.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the vector3 d
	 * @throws NaNVectorException the na n vector exception
	 */
	public static Vector3f crossProduct(Vector3f a, Vector3f b) {
		Vector3f r = new Vector3f(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
		return r;
	}

	public static void cross(float[] a, float[] b, float[] r) {
		r[0] = a[1] * b[2] - a[2] * b[1];
		r[1] = a[2] * b[0] - a[0] * b[2];
		r[2] = a[0] * b[1] - a[1] * b[0];
	}

	public static void normalize(float[] v) {
		float norm;

		norm = (float) (1.0 / Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
		v[0] *= norm;
		v[1] *= norm;
		v[2] *= norm;
	}

	/**
	 * Cross product.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the vector3 d
	 * @throws NaNVectorException the na n vector exception
	 */
	public static Vector3f crossProduct(Vector3f a, Vector3f b, Vector3f out) {
		float x = a.y * b.z - a.z * b.y;
		float y = a.z * b.x - a.x * b.z;
		float z = a.x * b.y - a.y * b.x;
		out.set(x, y, z);
		return out;
	}

	/**
	 * Returning the x angle between coordinate system axis' and a vector
	 */
	public static float getAngleX(Vector3f v) {
		return (float) Math.acos(v.x / Math.sqrt(Math.pow(v.x - 1, 2) + Math.pow(v.y, 2) + Math.pow(v.z, 2)));
	}

	/**
	 * Returning the y angle between coordinate system axis' and a vector
	 */
	public static float getAngleY(Vector3f v) {
		return (float) Math.acos(v.y / Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y - 1, 2) + Math.pow(v.z, 2)));
	}

	/**
	 * Returning the z angle between coordinate system axis' and a vector
	 */
	public static float getAngleZ(Vector3f v) {
		return (float) Math.acos(v.z / Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2) + Math.pow(v.z - 1, 2)));
	}

	public static float getFullRange2DAngle(Vector3f v1, Vector3f v2) {
		float dot = v1.x * v2.x + v1.y * v2.y;
		float cross = v1.x * v2.y - v1.y * v2.x;
		float angle = FastMath.atan2(cross, dot);
		return angle;
	}

	public static float getFullRange2DAngleFast(Vector3f v1, Vector3f v2) {
		float dot = v1.x * v2.x + v1.y * v2.y;
		float cross = v1.x * v2.y - v1.y * v2.x;
		float angle = FastMath.atan2Fast(cross, dot);
		return angle;
	}

	public static Vector3f getIntersection(float fDst1, float fDst2, Vector3f P1, Vector3f P2) {
		if((fDst1 * fDst2) >= 0.0f) {
			return null; //0
		}
		if(fDst1 == fDst2) {
			return null;  //0
		}
		Vector3f hit = new Vector3f(P1);
		hit.add(sub(P2, P1));
		hit.scale(-fDst1 / (fDst2 - fDst1));
		//		System.err.println("hit is now "+hit);
		//		Hit = P1 + (P2-P1) * ( -fDst1/(fDst2-fDst1) );
		return hit; //1
	}

	/**
	 * p0, p1: define the line
	 * p_co, p_no: define the plane:
	 * p_co is a point on the plane (plane coordinate).
	 * p_no is a normal vector defining the plane direction; does not need to be normalized.
	 *
	 * @param p0
	 * @param p1
	 * @param p_co
	 * @param p_no
	 * @return a Vector or None (when the intersection can't be found).
	 */
	public static Vector3f intersectLinePLane(Vector3f p0, Vector3f p1, Vector3f p_co, Vector3f p_no) {

		Vector3f u = new Vector3f();
		Vector3f w = new Vector3f();

		u.sub(p1, p0);
		w.sub(p0, p_co);

		float dot = p_no.dot(u);

		if(Math.abs(dot) > FastMath.FLT_EPSILON) {
			/*
			 * the factor of the point between p0 -> p1 (0 - 1)
			 *
			 * if 'fac' is between (0 - 1) the point intersects with the
			 * segment. otherwise:
			 * < 0.0: behind p0.
			 * > 1.0: infront of p1.
			 */

			float fac = -p_no.dot(w) / dot;

			if(fac >= 0f && fac <= 1f) {

				u.scale(fac);
				//	        mul_v3_fl(u, fac)
				u.add(p0);
				return u;
			} else {
				return null;
			}
		} else {
			// The segment is parallel to plane
			return null;
		}

	}

	/**
	 * Gets the rounded string.
	 *
	 * @param clazz the clazz
	 * @return the rounded string
	 */
	public static String getRoundedString(Vector3f v) {
		return "(" + df2.format(v.x) + ", " + df2.format(v.y) + ", " + df2.format(v.z) + ")";
	}

	public static boolean inBox(Vector3f hit, Vector3f B1, Vector3f B2, int Axis) {

		if(Axis == 1 && hit.z > B1.z && hit.z < B2.z && hit.y > B1.y && hit.y < B2.y) {
			//System.err.println("1hit: "+hit+", B1: "+B1+", B2: "+B2);
			return true;
		}
		if(Axis == 2 && hit.z > B1.z && hit.z < B2.z && hit.x > B1.x && hit.x < B2.x) {
			//			System.err.println("2hit: "+hit+", B1: "+B1+", B2: "+B2);
			return true;
		}
		if(Axis == 3 && hit.x > B1.x && hit.x < B2.x && hit.y > B1.y && hit.y < B2.y) {
			//			System.err.println("3hit: "+hit+", B1: "+B1+", B2: "+B2);
			return true;
		}
		return false; //0
	}

	/**
	 * Intersect_ ray triangle.
	 *
	 * @param P0 the p0
	 * @param P1 the p1
	 * @param V0 the v0
	 * @param V1 the v1
	 * @param V2 the v2
	 * @return the vector3 d
	 * @throws NaNVectorException the na n vector exception
	 */
	public static Vector3f intersect_RayTriangle(Vector3f P0, Vector3f P1, Vector3f V0, Vector3f V1, Vector3f V2) {
		Vector3f u, v, n; // triangle vectors
		Vector3f dir, w0, w; // ray vectors
		float r, a, b; // params to calc ray-plane intersect
		// get triangle edge vectors and plane normal
		u = Vector3fTools.sub(V1, V0);
		v = Vector3fTools.sub(V2, V0);
		n = Vector3fTools.crossProduct(u, v);// cross product
		if(n.length() == 0) { // triangle is degenerate
			// System.err.println("degenerate");
			return null; // return -1; // do not deal with this case
		}
		dir = Vector3fTools.sub(P1, P0); // ray direction vector
		w0 = Vector3fTools.sub(P0, V0);
		Vector3f nMinus = new Vector3f(n);
		nMinus.scale(-1);
		a = nMinus.dot(w0);
		b = n.dot(dir);
		if(FastMath.abs(b) < 0.0000f) { // ray is parallel to triangle plane
			if(a == 0) { // ray lies in triangle plane
				System.err.println("lies in");
				return P1;
			} else {
				return null; // return 0; // ray disjoint from plane
			}
		}

		// get intersect point of ray with triangle plane
		r = a / b;
		if(b == 0) {
			return null;
		}
		if(r < 0.0) {
			return null; // return 0; // => no intersect
		}

		// for a segment, also test if (r > 1.0) => no intersect
		dir.scale(r);
		Vector3f intersection = Vector3fTools.add(P0, dir); // intersect
		// point
		// of
		// ray
		// and
		// plane

		// is I inside T?
		float uu, uv, vv, wu, wv, D;
		uu = u.dot(u);
		uv = u.dot(v);
		vv = v.dot(v);
		w = Vector3fTools.sub(intersection, V0);
		wu = w.dot(u);
		wv = w.dot(v);
		D = uv * uv - uu * vv;

		// get and test parametric coords
		float s, t;
		s = (uv * wv - vv * wu) / D;
		if(s < 0.0 || s > 1.0) {
			return null; // return 0;
		}
		t = (uv * wu - uu * wv) / D;
		if(t < 0.0 || (s + t) > 1.0) {
			return null; // return 0;
		}
		return intersection; // I is in T
	}

	/**
	 * Calculates intersection with the given ray between a certain distance
	 * interval.
	 * <p/>
	 * Ray-box intersection is using IEEE numerical properties to ensure the
	 * test is both robust and efficient, as described in:
	 * <p/>
	 * Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An
	 * Efficient and Robust Ray-Box Intersection Algorithm" Journal of graphics
	 * tools, 10(1):49-54, 2005
	 *
	 * @param ray          incident ray
	 * @param minDirLength
	 * @param maxDirLength
	 * @return intersection point on the bounding box (only the first is
	 * returned) or null if no intersection
	 */
	public static Vector3f intersectsRayAABB(Vector3f pos, Vector3f dir, Vector3f min, Vector3f max, float minDirLength, float maxDirLength) {
		Vector3f invDir = new Vector3f(1f / dir.x, 1f / dir.y, 1f / dir.z);
		boolean signDirX = invDir.x < 0;
		boolean signDirY = invDir.y < 0;
		boolean signDirZ = invDir.z < 0;
		Vector3f bbox = signDirX ? max : min;
		float tmin = (bbox.x - pos.x) * invDir.x;
		bbox = signDirX ? min : max;
		float tmax = (bbox.x - pos.x) * invDir.x;
		bbox = signDirY ? max : min;
		float tymin = (bbox.y - pos.y) * invDir.y;
		bbox = signDirY ? min : max;
		float tymax = (bbox.y - pos.y) * invDir.y;

		if((tmin > tymax) || (tymin > tmax)) {
			return null;
		}
		if(tymin > tmin) {
			tmin = tymin;
		}
		if(tymax < tmax) {
			tmax = tymax;
		}

		bbox = signDirZ ? max : min;
		float tzmin = (bbox.z - pos.z) * invDir.z;
		bbox = signDirZ ? min : max;
		float tzmax = (bbox.z - pos.z) * invDir.z;

		if((tmin > tzmax) || (tzmin > tmax)) {
			return null;
		}
		if(tzmin > tmin) {
			tmin = tzmin;
		}
		if(tzmax < tmax) {
			tmax = tzmax;
		}
		if((tmin < maxDirLength) && (tmax > minDirLength)) {
			Vector3f intersect = new Vector3f(dir);
			intersect.scale(tmin);
			intersect.add(pos);
			return intersect;
		}
		return null;
	}

	public static boolean isNan(Vector3f a) {
		return Float.isNaN(a.x) || Float.isNaN(a.y) || Float.isNaN(a.z);
	}

	public static float length(float x, float y, float z) {
		return FastMath.carmackSqrt(x * x + y * y + z * z);
	}

	public static float lengthSquared(float x, float y, float z) {
		return x * x + y * y + z * z;
	}

	public static float diffLengthSquared(Vector3f a, Vector3f b) {

		float x = a.x - b.x;
		float y = a.y - b.y;
		float z = a.z - b.z;

		return lengthSquared(x, y, z);
	}

	public static float diffLength(Vector3f a, Vector3f b) {

		float x = a.x - b.x;
		float y = a.y - b.y;
		float z = a.z - b.z;

		return length(x, y, z);
	}

	/**
	 * Sets this vector's components to the maximum of its own and the
	 * ones of the passed in vector.
	 *
	 * @remarks 'Maximum' in this case means the combination of the highest
	 * value of x, y and z from both vectors. Highest is taken just
	 * numerically, not magnitude, so 1 > -3.
	 */
	public static void makeCeil(final Vector3f inOut, final Vector3f cmp) {
		if(cmp.x > inOut.x) inOut.x = cmp.x;
		if(cmp.y > inOut.y) inOut.y = cmp.y;
		if(cmp.z > inOut.z) inOut.z = cmp.z;
	}

	/**
	 * Sets this vector's components to the minimum of its own and the
	 * ones of the passed in vector.
	 *
	 * @remarks 'Minimum' in this case means the combination of the lowest
	 * value of x, y and z from both vectors. Lowest is taken just
	 * numerically, not magnitude, so -1 < 0.
	 */
	public static void makeFloor(final Vector3f inOut, final Vector3f cmp) {
		if(cmp.x < inOut.x) inOut.x = cmp.x;
		if(cmp.y < inOut.y) inOut.y = cmp.y;
		if(cmp.z < inOut.z) inOut.z = cmp.z;
	}

	public static void max(final Vector3f a, final Vector3f cmp, Vector3f out) {
		out.x = Math.max(a.x, cmp.x);
		out.y = Math.max(a.y, cmp.y);
		out.z = Math.max(a.z, cmp.z);
	}

	public static void min(final Vector3f a, final Vector3f cmp, Vector3f out) {
		out.x = Math.min(a.x, cmp.x);
		out.y = Math.min(a.y, cmp.y);
		out.z = Math.min(a.z, cmp.z);
	}

	/**
	 * Point mult.
	 *
	 * @param a the a
	 * @param b the b
	 */
	public static void pointMult(Vector3f a, Vector3f b) {
		a.set(a.x * b.x, a.y * b.y, a.z * b.z);
	}

	/**
	 * Point mult instance.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the vector3f
	 */
	public static Vector3f pointMultInstance(Vector3f a, Vector3f b) {
		return new Vector3f(a.x * b.x, a.y * b.y, a.z * b.z);
	}

	public static Vector3f predictPoint(Vector3f targetPosition, Vector3f targetVelocity, Vector3f targetAngularVelocity, float bulletVelocity, Vector3f shooterPosition) {
		Vector3f totarget = new Vector3f();
		totarget.sub(targetPosition, shooterPosition);
		if(targetVelocity.lengthSquared() == 0) return totarget;
		//		targetVelocity.negate();

//		targetVelocity.dot(targetVelocity);
		//		float a = Vector.Dot(target.velocity, target.velocity) - (bullet.velocity * bullet.velocity);

		double timeToTarget = totarget.length() / bulletVelocity;
		Vector3f targetHeading = new Vector3f();
		targetHeading.set(targetVelocity);
		targetHeading.normalize();
		targetHeading.scale((float) (targetAngularVelocity.length() * timeToTarget));

		double a = targetVelocity.dot(targetVelocity) - (bulletVelocity * bulletVelocity);
		double b = 2 * totarget.dot(targetVelocity);
		double c = totarget.dot(totarget);
		//double p = -b / (2d * a);
		double d = Math.abs((b * b) - 4d * a * c);
		d = (float) Math.sqrt(d);

		double t1 = (-b - d) / (2.0d * a);
		double t2 = (-b + d) / (2.0d * a);
		double t;

		if(t1 > t2 && t2 > 0) t = t2;
		else t = t1;
		//		System.err.println("TTTT "+t+"; A "+a+"; B "+b+"; C "+c+"; P "+p+"; Q "+d+"; M: "+((b * b) - 4 * a * c)+"; "+(2*a) );
		Vector3f aimSpot = new Vector3f();
		Vector3f targetVeloScaled = new Vector3f(targetVelocity);
		targetVeloScaled.add(targetHeading);
		targetVeloScaled.scale((float) t);
		aimSpot.add(targetPosition, targetVeloScaled);
		Vector3f bulletPath = new Vector3f();
		bulletPath.sub(aimSpot, shooterPosition);
		return bulletPath;
	}

	/**
	 * Quaternion to degree.
	 */
	public static void quaternionToDegree(Vector4f v) {
		if(v.x == 0 && v.y == 0 && v.z == 0) {
			v.w = 0;
			return;
		}

		float s = FastMath.carmackSqrt(v.x * v.x + v.y * v.y + v.z * v.z);
		// System.err.println("scale "+s);
		{
			v.x = (v.x / s); // normalise axis
			v.y = (v.y / s);
			v.z = (v.z / s);
		}
		v.w = (float) (2 * FastMath.acosFast(v.w));
		// System.err.println("a "+a);
	}

	public static Vector3f sub(Vector3f a, Vector3f b) {
		Vector3f result = new Vector3f(a);
		result.sub(b);
		return result;
	}

	public static Vector3f projectOnPlaneUnnormalized(Vector3f v, Vector3f normal) {
		Vector3f m = new Vector3f(v);
		Vector3f n = new Vector3f(normal);
		n.scale(v.dot(n));
		m.sub(n);
		return m;
	}

	public static Vector3f projectOnPlane(Vector3f v, Vector3f normal) {
		Vector3f m = new Vector3f(v);
		Vector3f n = new Vector3f(normal);
		m.normalize();
		n.normalize();
		n.scale(v.dot(n));
		m.sub(n);
		return m;
	}

	public static float dot(Vector3f v1, Vector3f v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	public static float length(Vector3i a, Vector3i b) {
		return length(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static float lengthSquared(Vector3i a, Vector3i b) {
		return lengthSquared(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static float lengthSquared(Vector3f a, Vector3f b) {
		return lengthSquared(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static float length(Vector3f a, Vector3f b) {
		return length(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static float projectScalar(Vector3f v, Vector3f onto) {
		float len = FastMath.carmackLength(v);
		float x = v.x * len;
		float y = v.y * len;
		float z = v.z * len;

		float dotAB = (onto.x * x + onto.y * y + onto.z * z); //onto.dot(v.scale(v.len))

		return dotAB;
	}

	public static void serialize(Vector3f v, DataOutput b) throws IOException {
		b.writeFloat(v.x);
		b.writeFloat(v.y);
		b.writeFloat(v.z);
	}

	public static Vector3f deserialize(Vector3f v, DataInput b) throws IOException {
		v.x = b.readFloat();
		v.y = b.readFloat();
		v.z = b.readFloat();
		return v;
	}

	public static Vector3f deserialize(DataInput b) throws IOException {
		return deserialize(new Vector3f(), b);
	}

	public static float dot2(Vector3f v) {
		return dot(v, v);
	}

	public static Vector3f mult(Vector3f v, float f) {
		Vector3f r = new Vector3f(v);
		r.scale(f);
		return r;
	}

	public static Vector3f read(String s) {
		String[] c = s.split(",");
		return new Vector3f(Float.parseFloat(c[0].trim()), Float.parseFloat(c[1].trim()), Float.parseFloat(c[2].trim()));
	}

	public static String toStringRaw(Vector3f v) {
		return v.x + ", " + v.y + ", " + v.z;
	}

	public static Vector3f getCenterOfTriangle(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f out) {
		out.x = (v1.x + v2.x + v3.x) / 3f;
		out.y = (v1.y + v2.y + v3.y) / 3f;
		out.z = (v1.z + v2.z + v3.z) / 3f;
		return out;
	}

	public static float distance(float x, float y, float z, float ox, float oy, float oz) {
		return length(x - ox, y - oy, z - oz);
	}

	public static float distance(Vector3f a, Vector3f b) {
		return length(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static float distance(Vector3i a, Vector3i b) {
		return length(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static void addScaled(Vector3f inOut, float scaled, Vector3f toAdd) {
		inOut.x += scaled * toAdd.x;
		inOut.y += scaled * toAdd.y;
		inOut.z += scaled * toAdd.z;
	}

}
