/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>BoundingBox</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * BoundingBox.java
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
package org.schema.schine.graphicsengine.forms;

import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.VectorUtil;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;

import javax.vecmath.Vector3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

/**
 * The Class BoundingBox.
 */
public class BoundingBox {

	/**
	 * The max.
	 */
	public final Vector3f min, max;
	private float t0;
	private float t1;

	/**
	 * sets max to Float.POSITIVE_INFINITY
	 * and min to Float.NEGATIVE_INFINITY
	 */
	public BoundingBox() {
		min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	}

	public BoundingBox(BoundingBox c) {
		this();
		this.min.set(c.min);
		this.max.set(c.max);
	}

	public BoundingBox(Vector3f min, Vector3f max) {
		assert (max != null && min != null);
		this.max = max;
		this.min = min;
	}

	public BoundingBox(Vector3i min, Vector3i max) {
		this.max = new Vector3f();
		this.min = new Vector3f();
		
		this.min.x =min.x;
		this.min.y =min.y;
		this.min.z =min.z;
		
		this.max.x =max.x;
		this.max.y =max.y;
		this.max.z =max.z;
	}

	public static boolean rayIntersects(Vector3f org, Vector3f dir, Vector3f min, Vector3f max) {
		// r.dir is unit direction vector of ray
		float dirfracx = 1.0f / dir.x;
		float dirfracy = 1.0f / dir.y;
		float dirfracz = 1.0f / dir.z;
		// lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
		// r.org is origin of ray
		float t1 = (min.x - org.x) * dirfracx;
		float t2 = (max.x - org.x) * dirfracx;
		float t3 = (min.y - org.y) * dirfracy;
		float t4 = (max.y - org.y) * dirfracy;
		float t5 = (min.z - org.z) * dirfracz;
		float t6 = (max.z - org.z) * dirfracz;

		float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0) {
			//				    t = tmax;
			return false;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax) {
			//				    t = tmax;
			return false;
		}

		//				t = tmin;
		return true;
	}

	public static boolean doesCubeIntersectSphere(BoundingBox aabb, Vector3f S, float R) {
		return doesCubeIntersectSphere(aabb.min, aabb.max, S, R);
	}

	public static boolean doesCubeIntersectSphere(Vector3f C1, Vector3f C2, Vector3f S, float R) {
		float dist_squared = R * R;
	    /* assume C1 and C2 are element-wise sorted, if not, do that now */
		if (S.x < C1.x) dist_squared -= FastMath.sqr(S.x - C1.x);
		else if (S.x > C2.x) dist_squared -= FastMath.sqr(S.x - C2.x);
		if (S.y < C1.y) dist_squared -= FastMath.sqr(S.y - C1.y);
		else if (S.y > C2.y) dist_squared -= FastMath.sqr(S.y - C2.y);
		if (S.z < C1.z) dist_squared -= FastMath.sqr(S.z - C1.z);
		else if (S.z > C2.z) dist_squared -= FastMath.sqr(S.z - C2.z);
		return dist_squared > 0;
	}

	public static boolean testCircleAABB(final Vector3f circleCenter, final float circleRadius, final BoundingBox box) {
		// get the squared distance between circle center and the AABB
		float sqDist = sqDistPointAABB(circleCenter, box);
		float r = circleRadius;

		return sqDist <= r * r;

	}

	public static float sqDistPointAABB(final Vector3f p, final BoundingBox aabb) {
		float sqDist = 0.0f;
		float v;
		float minX, minY, minZ, maxX, maxY, maxZ;

		// get the minX, maxX, minY, maxY and minZ, maxZ points of the AABB
		minX = aabb.min.x;
		maxX = aabb.max.x;

		minY = aabb.min.y;
		maxY = aabb.max.y;

		minZ = aabb.min.z;
		maxZ = aabb.max.z;

		// test the bounds against the points X axis
		v = p.x;

		if (v < minX) sqDist += (minX - v) * (minX - v);
		if (v > maxX) sqDist += (v - maxX) * (v - maxX);

		// test the bounds against the points Y axis
		v = p.y;

		if (v < minY) sqDist += (minY - v) * (minY - v);
		if (v > maxY) sqDist += (v - maxY) * (v - maxY);

		// test the bounds against the points Z axis
		v = p.z;

		if (v < minZ) sqDist += (minZ - v) * (minZ - v);
		if (v > maxZ) sqDist += (v - maxZ) * (v - maxZ);

		return sqDist;
	}

	public static boolean testPointAABB(Vector3f point,
	                                    Vector3f min, Vector3f max) {
		return (point.x >= min.x && point.x <= max.x &&
				point.y >= min.y && point.y <= max.y &&
				point.z >= min.z && point.z <= max.z);

	}

	public static boolean intersectsTriangle(Vector3f[] tri, Vector3f min, Vector3f max, TriBoundingBoxVariables v) {
		v.center.sub(max, min);
		v.extent.set(v.center);
		v.extent.scale(0.5f);
		v.center.scaleAdd(0.5f, min);
		return intersectsTriangleWithExtents(tri, v.center, v.extent, v);
	}

	private static boolean intersectsTriangleWithExtents(Vector3f[] tri, Vector3f center, Vector3f extent, TriBoundingBoxVariables v) {
		// use separating axis theorem to test overlap between triangle and box
		// need to test for overlap in these directions:
		//
		// 1) the {x,y,z}-directions (actually, since we use the AABB of the
		// triangle
		// we do not even need to test these)
		// 2) normal of the triangle
		// 3) crossproduct(edge from tri, {x,y,z}-directin)
		// this gives 3x3=9 more tests
		Vector3f v0, v1, v2;
		Vector3f normal, e0, e1, e2, f;

		v0 = v.v0;
		v1 = v.v1;
		v2 = v.v2;
		normal = v.normal;
		e0 = v.e0;
		e1 = v.e1;
		e2 = v.e2;
		f = v.f;

		// move everything so that the boxcenter is in (0,0,0)
//        v0 = tri.a.sub(this);
//        v1 = tri.b.sub(this);
//        v2 = tri.c.sub(this);
		v0.sub(tri[0], center);
		v1.sub(tri[1], center);
		v2.sub(tri[2], center);
		// compute triangle edges
		e0.sub(v1, v0);
		e1.sub(v2, v1);
		e2.sub(v0, v2);
//    	e0 = v1.sub(v0);
//    	e1 = v2.sub(v1);
//    	e2 = v0.sub(v2);

		// test the 9 tests first (this was faster)
		f.set(e0);
		f.absolute();
//        f = e0.getAbs();
		if (testAxis(e0.z, -e0.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, extent.y,
				extent.z)) {
			return false;
		}
		if (testAxis(-e0.z, e0.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, extent.x,
				extent.z)) {
			return false;
		}
		if (testAxis(e0.y, -e0.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, extent.x,
				extent.y)) {
			return false;
		}
		f.set(e1);
		f.absolute();
//        f = e1.getAbs();
		if (testAxis(e1.z, -e1.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, extent.y,
				extent.z)) {
			return false;
		}
		if (testAxis(-e1.z, e1.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, extent.x,
				extent.z)) {
			return false;
		}
		if (testAxis(e1.y, -e1.x, f.y, f.x, v0.x, v0.y, v1.x, v1.y, extent.x,
				extent.y)) {
			return false;
		}

		f.set(e2);
		f.absolute();
//        f = e2.getAbs();
		if (testAxis(e2.z, -e2.y, f.z, f.y, v0.y, v0.z, v1.y, v1.z, extent.y,
				extent.z)) {
			return false;
		}
		if (testAxis(-e2.z, e2.x, f.z, f.x, v0.x, v0.z, v1.x, v1.z, extent.x,
				extent.z)) {
			return false;
		}
		if (testAxis(e2.y, -e2.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, extent.x,
				extent.y)) {
			return false;
		}

		// first test overlap in the {x,y,z}-directions
		// find min, max of the triangle each direction, and test for overlap in
		// that direction -- this is equivalent to testing a minimal AABB around
		// the triangle against the AABB

		// test in X-direction
		if (FastMath.min(v0.x, v1.x, v2.x) > extent.x
				|| FastMath.max(v0.x, v1.x, v2.x) < -extent.x) {
			return false;
		}
		// test in Y-direction
		if (FastMath.min(v0.y, v1.y, v2.y) > extent.y
				|| FastMath.max(v0.y, v1.y, v2.y) < -extent.y) {
			return false;
		}

		// test in Z-direction
		if (FastMath.min(v0.z, v1.z, v2.z) > extent.z
				|| FastMath.max(v0.z, v1.z, v2.z) < -extent.z) {
			return false;
		}

		// test if the box intersects the plane of the triangle
		// compute plane equation of triangle: normal*x+d=0
		normal.cross(e0, e1);
//        normal = e0.cross(e1);
		float d = -normal.dot(v0);
		if (!planeBoxOverlap(normal, d, extent, v)) {
			return false;
		}
		return true;
	}

	private static boolean testAxis(float a, float b, float fa, float fb, float va,
	                                float vb, float wa, float wb, float ea, float eb) {
		float p0 = a * va + b * vb;
		float p2 = a * wa + b * wb;
		float min, max;
		if (p0 < p2) {
			min = p0;
			max = p2;
		} else {
			min = p2;
			max = p0;
		}
		float rad = fa * ea + fb * eb;
		return (min > rad || max < -rad);
	}

	public static boolean intersectsPlane(Vector3f center, Vector3f normal, BoundingBox box) {
		//d is distance to origin
		float d = -normal.dot(center);
		return intersectsPlane(box, d, normal);
	}

	private static boolean intersectsPlane(BoundingBox box, float D, Vector3f normal) {
		Vector3f vector = new Vector3f();
		Vector3f vector2 = new Vector3f();
		vector2.x = (normal.x >= 0f) ? box.min.x : box.max.x;
		vector2.y = (normal.y >= 0f) ? box.min.y : box.max.y;
		vector2.z = (normal.z >= 0f) ? box.min.z : box.max.z;
		vector.x = (normal.x >= 0f) ? box.max.x : box.min.x;
		vector.y = (normal.y >= 0f) ? box.max.y : box.min.y;
		vector.z = (normal.z >= 0f) ? box.max.z : box.min.z;
		float num = ((normal.x * vector2.x) + (normal.y * vector2.y))
				+ (normal.z * vector2.z);
		if (num + D > 0f) {
			return false; //front
		}
		num = ((normal.x * vector.x) + (normal.y * vector.y))
				+ (normal.z * vector.z);
		return num + D < 0f ? false //back
				: true;
	}

	public static boolean intersectsWithWithSphere(BoundingBox boundingBox, Vector3f center, float radius) {
		float dmin = 0;

		Vector3f bmin = boundingBox.min;
		Vector3f bmax = boundingBox.max;

		if (center.x < bmin.x) {
			dmin += Math.pow(center.x - bmin.x, 2);
		} else if (center.x > bmax.x) {
			dmin += Math.pow(center.x - bmax.x, 2);
		}

		if (center.y < bmin.y) {
			dmin += Math.pow(center.y - bmin.y, 2);
		} else if (center.y > bmax.y) {
			dmin += Math.pow(center.y - bmax.y, 2);
		}

		if (center.z < bmin.z) {
			dmin += Math.pow(center.z - bmin.z, 2);
		} else if (center.z > bmax.z) {
			dmin += Math.pow(center.z - bmax.z, 2);
		}

		return dmin <= Math.pow(radius, 2);
	}

	private static boolean planeBoxOverlap(Vector3f normal, float d, Vector3f maxbox, TriBoundingBoxVariables v) {

		Vector3f vmin = v.vmin;
		Vector3f vmax = v.vmax;

		if (normal.x > 0.0f) {
			vmin.x = -maxbox.x;
			vmax.x = maxbox.x;
		} else {
			vmin.x = maxbox.x;
			vmax.x = -maxbox.x;
		}

		if (normal.y > 0.0f) {
			vmin.y = -maxbox.y;
			vmax.y = maxbox.y;
		} else {
			vmin.y = maxbox.y;
			vmax.y = -maxbox.y;
		}

		if (normal.z > 0.0f) {
			vmin.z = -maxbox.z;
			vmax.z = maxbox.z;
		} else {
			vmin.z = maxbox.z;
			vmax.z = -maxbox.z;
		}
		if (normal.dot(vmin) + d > 0.0f) {
			return false;
		}
		if (normal.dot(vmax) + d >= 0.0f) {
			return true;
		}
		return false;
	}

	public boolean atLeastOne() {
		return isValid() && (max.x - min.x) >= 1.0f && (max.y - min.y) >= 1.0f && (max.z - min.z) >= 1.0f;
	}

	/**
	 * Calculate half size.
	 *
	 * @return
	 */
	public Vector3f calculateHalfSize(Vector3f out) {
		out.set(FastMath.abs((max.x - min.x) / 2),
				FastMath.abs((max.y - min.y) / 2),
				FastMath.abs((max.z - min.z) / 2));
		return out;
	}

	/**
	 * that clips a segment to inside a cube. returns false if ray misses.
	 *
	 * @param A
	 * @param B
	 * @param tmpA
	 * @param tmpB
	 * @return
	 */
	public boolean clipSegment(Vector3f A, Vector3f B, Vector3f tmpA, Vector3f tmpB) {
		Vector3f S = tmpA;
		Vector3f D = tmpB;
		S.set(A);
		D.sub(B, A);

		t0 = 0.0f;
		t1 = 1.0f;

		if (!ClipSegment(min.x, max.x, A.x, B.x, D.x)) {
			return false;
		}

		if (!ClipSegment(min.y, max.y, A.y, B.y, D.y)) {
			return false;
		}

		if (!ClipSegment(min.z, max.z, A.z, B.z, D.z)) {
			return false;
		}
		A.x = S.x + D.x * t0;
		A.y = S.y + D.y * t0;
		A.z = S.z + D.z * t0;

		B.x = S.x + D.x * t1;
		B.y = S.y + D.y * t1;
		B.z = S.z + D.z * t1;

		return true;
	}

	private boolean ClipSegment(float min, float max, float a, float b, float d) {
		final float threshold = 1.0e-6f;

		if (Math.abs(d) < threshold) {
			if (d > 0.0f) {
				return !(b < min || a > max);
			} else {
				return !(a < min || b > max);
			}
		}

		float u0, u1;

		u0 = (min - a) / (d);
		u1 = (max - a) / (d);

		if (u0 > u1) {
			float s = u0;
			u0 = u1;
			u1 = s;
		}

		if (u1 < t0 || u0 > t1) {
			return false;
		}

		t0 = Math.max(u0, t0);
		t1 = Math.min(u1, t1);

		if (t1 < t0) {
			return false;
		}

		return true;
	}

	public BoundingBox clone(Object object) {
		return new BoundingBox(min, max);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof BoundingBox)) {
			return false;
		}
		return min.equals(((BoundingBox) obj).min) && max.equals(((BoundingBox) obj).max);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ " + min + " | " + max + " ]";
	}

	public void expand(float xMin, float yMin, float zMin,
	                   float xMax, float yMax, float zMax) {
		min.x = Math.min(xMin, min.x);
		min.y = Math.min(yMin, min.y);
		min.z = Math.min(zMin, min.z);

		max.x = Math.max(xMax, max.x);
		max.y = Math.max(yMax, max.y);
		max.z = Math.max(zMax, max.z);

		assert (isValid()) : "MIN: " + xMin + "; " + yMin + "; " + zMin + " MAX: " + xMax + "; " + yMax + "; " + zMax + "; -------> " + min + "; " + max;

	}

	public void expand(Vector3f tmpVecMin, Vector3f tmpVecMax) {
		min.x = Math.min(tmpVecMin.x, min.x);
		min.y = Math.min(tmpVecMin.y, min.y);
		min.z = Math.min(tmpVecMin.z, min.z);

		max.x = Math.max(tmpVecMax.x, max.x);
		max.y = Math.max(tmpVecMax.y, max.y);
		max.z = Math.max(tmpVecMax.z, max.z);

		assert (isValid()) : tmpVecMin + " :: " + min + "; " + max;
	}

	/**
	 * Expands the Bounding box
	 * by taking checking
	 * if the box got bigger on
	 * min or on max
	 * <p/>
	 * min.x = Math.min(tmpVecMin.x, min.x);
	 * min.y = Math.min(tmpVecMin.y, min.y);
	 * min.z = Math.min(tmpVecMin.z, min.z);
	 * <p/>
	 * max.x = Math.max(tmpVecMax.x, max.x);
	 * max.y = Math.max(tmpVecMax.y, max.y);
	 * max.z = Math.max(tmpVecMax.z, max.z);
	 *
	 * @param tmpVecMin
	 * @param tmpVecMax
	 */
	public void expand(Vector3i tmpVecMin, Vector3i tmpVecMax) {
		min.x = Math.min(tmpVecMin.x, min.x);
		min.y = Math.min(tmpVecMin.y, min.y);
		min.z = Math.min(tmpVecMin.z, min.z);

		max.x = Math.max(tmpVecMax.x, max.x);
		max.y = Math.max(tmpVecMax.y, max.y);
		max.z = Math.max(tmpVecMax.z, max.z);
	}

	/**
	 * Gets the center.
	 *
	 * @return the center
	 */
	public Vector3f getCenter(Vector3f out) {
		calculateHalfSize(out);
		out.add(min);
		return out;
	}
	/**
	 * 
	 * @param aBox
	 * @param bBox
	 * @return squared distance between bounding boxes
	 */
	public static float distance2(BoundingBox aBox, BoundingBox bBox) {
		float result = 0;

		for (int i = 0; i < 3; ++i) {
			float aMin = VectorUtil.getCoord(aBox.min, i);
			float aMax = VectorUtil.getCoord(aBox.max, i);
			float bMin = VectorUtil.getCoord(bBox.min, i);
			float bMax = VectorUtil.getCoord(bBox.max, i);

			if (aMin > bMax) {
				float delta = bMax - aMin;

				result += delta * delta;
			} else if (bMin > aMax) {
				float delta = aMax - bMin;

				result += delta * delta;
			}
			// else the projection intervals overlap.
		}

		return result;
	}
	public Vector3f getClosestPoint(Vector3f point, Vector3f out) {
		out.set(point);
		Vector3fTools.clamp(out, min, max);
		return out;
	}
	//	private BoundingBox getIntersection(BoundingBox other, BoundingBox result, boolean again){
	//		System.err.println("INTERSECTING "+this+" AND "+other);
	//		if(isInside(other.max)){
	//			result.max.set(other.max);
	//			if(isInside(other.min)){
	//				result.min.set(other.min);
	//				//the AABB is completle
	//				//inside the other AABB
	//				//-> intersection is smaller AABB
	//				System.err.println("CONTAINS BOTH "+again);
	//				return result;
	//			}
	//			if(min.x >= other.min.x && min.y >= other.min.y){
	//				result.min.set(min.x, min.y, other.min.z);
	//				return result;
	//			}
	//			if(min.x >= other.min.x && min.z >= other.min.z){
	//				result.min.set(min.x, other.min.y, min.z);
	//				return result;
	//			}
	//			if(min.y >= other.min.y && min.z >= other.min.z){
	//				result.min.set(other.min.x, min.y, min.z);
	//				return result;
	//			}
	//			if(min.x >= other.min.x){
	//				result.min.set(min.x, other.min.y, other.min.z);
	//				return result;
	//			}
	//			if(min.y >= other.min.y){
	//				result.min.set(other.min.x, min.y, other.min.z);
	//				return result;
	//			}
	//			if(min.z >= other.min.z){
	//				result.min.set(other.min.x, other.min.y, min.z);
	//				return result;
	//			}
	//
	//		}else if(isInside(other.min)){
	//			result.min.set(other.min);
	//
	//			if(max.x <= other.max.x && max.y <= other.max.y){
	//				result.max.set(max.x, max.y, other.max.z);
	//				return result;
	//			}
	//			if(max.x <= other.max.x && max.z <= other.max.z){
	//				result.max.set(max.x, other.max.y, max.z);
	//				return result;
	//			}
	//			if(max.y <= other.max.y && max.z <= other.max.z){
	//				result.max.set(other.max.x, max.y, max.z);
	//				return result;
	//			}
	//			if(max.x <= other.max.x){
	//				result.max.set(max.x, other.max.y, other.max.z);
	//				return result;
	//			}
	//			if(max.y <= other.max.y){
	//				result.max.set(other.max.x, max.y, other.max.z);
	//				return result;
	//			}
	//			if(max.z <= other.max.z){
	//				result.max.set(other.max.x, other.max.y, max.z);
	//				return result;
	//			}
	//
	//		}else{
	//			if(!again){
	//				return null;
	//			}else{
	//				return other.getIntersection(this, result, false);
	//			}
	//		}
	//
	//		if(again){
	//			//assure this is only run 2 times
	//			BoundingBox snd = new BoundingBox(result);
	//			result.reset();
	//			return other.getIntersection(snd, result, false);
	//		}
	//		return result;
	//	}

	public BoundingBox getIntersection(BoundingBox other, BoundingBox result) {
		if (!intersects(other)) {
			return null;
		}

		result.min.set(min);
		result.max.set(max);

		if (min.x >= other.min.x) result.min.x = min.x;
		if (min.x <= other.min.x) result.min.x = other.min.x;
		if (min.y >= other.min.y) result.min.y = min.y;
		if (min.y <= other.min.y) result.min.y = other.min.y;
		if (min.z >= other.min.z) result.min.z = min.z;
		if (min.z <= other.min.z) result.min.z = other.min.z;

		if (max.x <= other.max.x) result.max.x = max.x;
		if (max.x >= other.max.x) result.max.x = other.max.x;
		if (max.y <= other.max.y) result.max.y = max.y;
		if (max.y >= other.max.y) result.max.y = other.max.y;
		if (max.z <= other.max.z) result.max.z = max.z;
		if (max.z >= other.max.z) result.max.z = other.max.z;

		//		Vector3fTools.makeCeil(result.min, other.min);
		//		Vector3fTools.makeFloor(result.max,  other.max);

		return result;
	}

	public static boolean getIntersection(Vector3f origin, Vector3f dir, Vector3f min, Vector3f max, Vector3f out) {
		// r.dir is unit direction vector of ray
				float dirfracX = 1.0f / dir.x;
				float dirfracY = 1.0f / dir.y;
				float dirfracZ = 1.0f / dir.z;
				// lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
				// r.org is origin of ray
				float t1 = (min.x - origin.x) * dirfracX;
				float t2 = (max.x - origin.x) * dirfracX;
				float t3 = (min.y - origin.y) * dirfracY;
				float t4 = (max.y - origin.y) * dirfracY;
				float t5 = (min.z - origin.z) * dirfracZ;
				float t6 = (max.z - origin.z) * dirfracZ;

				float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
				float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

				float t;

				// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
				if (tmax < 0) {
					t = tmax;
					return false;
				}

				// if tmin > tmax, ray doesn't intersect AABB
				if (tmin > tmax) {
					t = tmax;
					return false;
				}
				//function returns distance ('t' is distance)
				t = tmin;
				out.set(dir);
				out.scale(t);
				out.add(origin);
				return true;
	}
	public boolean getIntersection(Vector3f origin, Vector3f dir, Vector3f out) {
		return getIntersection(origin, dir, min, max, out);
	}

//	public float getMaxRadius() {
//		return Math.max(Math.max(max.x - min.x, max.y - min.y), max.z - min.z);
//	}

	public float getSize() {
		return Vector3fTools.length(max.x - min.x, max.y - min.y, max.z - min.z);
	}

	public boolean intersects(BoundingBox other) {
		return AabbUtil2.testAabbAgainstAabb2(min, max, other.min, other.max);
	}

	public boolean isInitialized() {
		return min.x != Float.POSITIVE_INFINITY && max.x != Float.NEGATIVE_INFINITY;
	}


	/**
	 * Checks if is inside.
	 *
	 * @param point the point
	 * @return true, if is inside
	 */
	public boolean isInside(Vector3f point) {
		boolean in = (point.x >= min.x && point.x <= max.x &&
				point.y >= min.y && point.y <= max.y &&
				point.z >= min.z && point.z <= max.z);
		return in;
	}

	public boolean isInside(float x, float y, float z) {
		boolean in = (x >= min.x && x <= max.x &&
				y >= min.y && y <= max.y &&
				z >= min.z && z <= max.z);
		return in;
	}

	public boolean isInside(BoundingBox o) {
		//enough to test if min and max are in

		return isInside(o.min) && isInside(o.max);

//		return(
//				isInside(o.min.x,o.min.y,o.min.z) &&
//				isInside(o.min.x,o.min.y,o.max.z) &&
//				isInside(o.min.x,o.max.y,o.min.z) &&
//				isInside(o.min.x,o.max.y,o.max.z) &&
//				isInside(o.max.x,o.min.y,o.min.z) &&
//				isInside(o.max.x,o.min.y,o.max.z) &&
//				isInside(o.max.x,o.max.y,o.max.z)
//
//
//
//				);
	}

	/**
	 * Checks if is inside.
	 *
	 * @param point     the point
	 * @param translate the translate
	 * @return true, if is inside
	 */
	public boolean isInside(Vector3f point, Vector3f translate) {
		return (point.x + translate.x >= min.x && point.x + translate.x <= max.x &&
				point.y + translate.y >= min.y && point.y + translate.y <= max.y &&
				point.z + translate.z >= min.z && point.z + translate.z <= max.z);
	}

	public boolean isValid() {
		return (min.x <= max.x)
				&& (min.y <= max.y)
				&& (min.z <= max.z);
	}
	public boolean isValidIncludingZero() {
		return (min.x < max.x)
				&& (min.y < max.y)
				&& (min.z < max.z);
	}

	public boolean rayIntersects(Vector3f org, Vector3f dir) {
		return rayIntersects(org, dir, min, max);
	}

	public void reset() {
		min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	}

	public void set(BoundingBox bb) {
		min.set(bb.min);
		max.set(bb.max);
	}

	public void set(Vector3f min, Vector3f max) {
		this.min.set(min);
		this.max.set(max);

	}

	public void serialize(DataOutput stream) throws IOException {
		stream.writeFloat(min.x);
		stream.writeFloat(min.y);
		stream.writeFloat(min.z);
		stream.writeFloat(max.x);
		stream.writeFloat(max.y);
		stream.writeFloat(max.z);
	}

	public void deserialize(DataInput stream) throws IOException {
		min.x = stream.readFloat();
		min.y = stream.readFloat();
		min.z = stream.readFloat();
		max.x = stream.readFloat();
		max.y = stream.readFloat();
		max.z = stream.readFloat();
	}

	public float sizeX() {
		return max.x - min.x;
	}
	public float sizeY() {
		return max.y - min.y;
	}
	public float sizeZ() {
		return max.z - min.z;
	}
	public float halfSizeX() {
		return (max.x - min.x) * 0.5f;
	}
	public float halfSizeY() {
		return (max.y - min.y) * 0.5f;
	}
	public float halfSizeZ() {
		return (max.z - min.z) * 0.5f;
	}

	public boolean overlapsAny(List<BoundingBox> bbs) {
		for(int i = 0; i < bbs.size(); i++){
			if(intersects(bbs.get(i))){
				return true;
			}
		}
		return false;
	}

	public float maxSize() {
		return Math.max(sizeX(), Math.max(sizeY(), sizeZ()));
	}
	public float maxHalfSize() {
		return Math.max(halfSizeX(), Math.max(halfSizeY(), halfSizeZ()));
	}

	public String toStringSize() {
		return (int)(max.x - min.x)+", "+(int)(max.y - min.y)+", "+(int)(max.z - min.z);
	}
	public String toStringSize(int mod) {
		return "X: "+(int)((max.x - min.x)+mod)+", Y: "+(int)((max.y - min.y)+mod)+", Z: "+(int)((max.z - min.z)+mod);
	}

	public int getVolume() {
		return (int) ((max.x - min.x) * (max.y - min.y) * (max.z - min.z));
	}
}
