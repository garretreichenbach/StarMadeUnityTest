/**
 * Box-Box collision detection re-distributed under the ZLib license
 * Java port for JBullet Integration by Robin Promesberger
 * Bullet Continuous Collision Detection and Physics Library
 * Bullet is Copyright (c) 2003-2006 Erwin Coumans  http://continuousphysics.com/Bullet/
 * <p/>
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it freely,
 * subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */
package org.schema.game.common.data.physics.sweepandpruneaabb;

import java.util.Arrays;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

public class OOBBDetector {

	public static final boolean USE_CENTER_POINT = false;
	public int contacts;
	public float maxDepth;
	private Vector3f distPoint = new Vector3f();
	private Vector3f pp = new Vector3f();
	private Vector3f normalC = new Vector3f();
	private Vector3f pa = new Vector3f();
	private Vector3f pb = new Vector3f();
	private Vector3f ua = new Vector3f();
	private Vector3f ub = new Vector3f();
	private Vector2f alphaBeta = new Vector2f();
	private Vector3f negNormal = new Vector3f();
	private Vector3f nr = new Vector3f();
	private Vector3f normal2 = new Vector3f();
	private Vector3f anr = new Vector3f();
	private Vector3f ppa = new Vector3f();
	private Vector3f ppb = new Vector3f();
	private Vector3f Sa = new Vector3f();
	private Vector3f Sb = new Vector3f();
	;
	private int[] iret = new int[8];
	;
	private Vector3f pointInWorldFAA = new Vector3f();
	;
	private Vector3f posInWorldFA = new Vector3f();
	;
	private Vector3f center = new Vector3f();
	private Vector3f pointInWorldRes = new Vector3f();
	private Vector3f scaledN = new Vector3f();

	//	private float[] s_buffer = new float[12];
	private CB cb = new CB();
	//private BoxShape m_box1;
	//private BoxShape m_box2;
	private float fudge_factor = 1.05f;
	private float[] s_quadBuffer = new float[16];
	private float[] s_temp1 = new float[12];

	//public static void DMULTIPLY0_331(float[] A, float[] B, float[] C)
	//{
	//    A[0] = DDOT(B, 0, C, 0);
	//    A[1] = DDOT(B, 4, C, 0);
	//    A[2] = DDOT(B, 8, C, 0);
	//}
	private float[] s_temp2 = new float[12];
	private float[] s_quad = new float[8];
	private float[] s_ret = new float[16];
	private float[] s_point = new float[3 * 8];              // penetrating contact points
	private float[] s_dep = new float[8];                    // depths for those points
	private float[] s_A = new float[8];
	private float[] s_rectReferenceFace = new float[2];
	private int[] s_availablePoints = new int[8];
	private Vector3f normal = new Vector3f();
	private Vector3f translationA = new Vector3f();
	private Vector3f translationB = new Vector3f();
	private Vector3f box1Margin = new Vector3f();
	private Vector3f box2Margin = new Vector3f();
	private Vector3f box1MarginCache = new Vector3f();
	private Vector3f box2MarginCache = new Vector3f();
	private Vector3f rowA = new Vector3f();
	private Vector3f rowB = new Vector3f();
	private Vector3f blockA = new Vector3f();
	private Vector3f blockB = new Vector3f();
	private Transform transformA = new Transform();
	private Transform transformB = new Transform();
	private Vector3f pTmp = new Vector3f();
	private Float depth;
	private boolean cachedBoxSize;
	private short typeA;
	private short typeB;
	private int idA;
	private int idB;

	// given n points in the plane (array p, of size 2*n), generate m points that
	// best represent the whole set. the definition of 'best' here is not
	// predetermined - the idea is to select points that give good box-box
	// collision detection behavior. the chosen point indexes are returned in the
	// array iret (of size m). 'i0' is always the first entry in the array.
	// n must be in the range [1..8]. m must be in the range [1..n]. i0 must be
	// in the range [0..n-1].
	/*
	private static float DDOT41(float[] a, int aOffset, float[] b, int bOffset)
	{
		//return DDOTpq(a, b, aOffset, bOffset, 4, 1);
		return (a[aOffset] * b[bOffset] + a[4 + aOffset] * b[1 + bOffset] + a[8 + aOffset] * b[2 + bOffset]);
	}

	 */

	/**
	 * basically length(ab)
	 * @param a
	 * @param aOffset
	 * @param b
	 * @param bOffset
	 * @return
	 */
	private static float DDOT(Vector3f a, int aOffset, float[] b, int bOffset) {
		//return DDOTpq( a, b, aOffset, bOffset, 1, 1);
		return (VectorUtil.getCoord(a, aOffset) * b[bOffset]

				+ VectorUtil.getCoord(a, 1 + aOffset) * b[1 + bOffset]

				+ VectorUtil.getCoord(a, 2 + aOffset) * b[2 + bOffset]);
	}

	/*
	private static float DDOT( Vector3f a, int aOffset,  Vector3f b, int bOffset)
	{
		//return (a[0] * b[0] + a[1] * b[1] + a[2] * b[2]);
		//return DDOTpq( a,  b, aOffset, bOffset, 1, 1);
		return VectorUtil.getCoord(a, aOffset) * VectorUtil.getCoord(b, bOffset) + VectorUtil.getCoord(a, 1 + aOffset) * VectorUtil.getCoord(b, 1 + bOffset) + VectorUtil.getCoord(a, 2 + aOffset) * VectorUtil.getCoord(b, 2 + bOffset);
	}

	private static float DDOT14(float[] a, int aOffset, float[] b, int bOffset)
	{
		//return DDOTpq(a, b, aOffset, bOffset, 1, 4);
		return (a[aOffset] * b[bOffset] + a[1 + aOffset] * b[4 + bOffset] + a[2 + aOffset] * b[8 + bOffset]);
	}
	 */
	// find all the intersection points between the 2D rectangle with vertices
	// at (+/-h[0],+/-h[1]) and the 2D quadrilateral with vertices (p[0],p[1]),
	// (p[2],p[3]),(p[4],p[5]),(p[6],p[7]).
	//
	// the intersection points are returned as x,y pairs in the 'ret' array.
	// the number of intersection points is returned by the function (this will
	// be in the range 0 to 8).
	private static float DDOT14(Vector3f a, int aOffset, float[] b, int bOffset) {
		//return DDOTpq( a, b, aOffset, bOffset, 1, 4);
		return (VectorUtil.getCoord(a, aOffset) * b[bOffset] + VectorUtil.getCoord(a, 1 + aOffset) * b[4 + bOffset] + VectorUtil.getCoord(a, 2 + aOffset) * b[8 + bOffset]);
	}

	private static float DDOT41(float[] a, int aOffset, Vector3f b, int bOffset) {
		//return DDOTpq(a,  b, aOffset, bOffset, 4, 1);
		return (a[aOffset] * VectorUtil.getCoord(b, bOffset) + a[4 + aOffset] * VectorUtil.getCoord(b, 1 + bOffset) + a[8 + aOffset] * VectorUtil.getCoord(b, 2 + bOffset));
	}

	//#define dDOTpq(a,b,p,q) ((a)[0]*(b)[0] + (a)[p]*(b)[q] + (a)[2*(p)]*(b)[2*(q)])

	private static float DDOT41Spec(float[] a, int aOffset, float xB, float yB, float zB) {
		//return DDOTpq(a,  b, aOffset, bOffset, 4, 1);
		return (a[aOffset] * xB + a[4 + aOffset] * yB + a[8 + aOffset] * zB);
	}

	private static float DDOT44(float[] a, int aOffset, float[] b, int bOffset) {
		//return DDOTpq(a, b, aOffset, bOffset, 4, 4);
		return (a[aOffset] * b[bOffset] + a[4 + aOffset] * b[4 + bOffset] + a[8 + aOffset] * b[8 + bOffset]);
	}

	/*
	private static float DDOT(float[] a, int aOffset,  Vector3f b, int bOffset)
	{
		//return DDOTpq(a,  b, aOffset, bOffset, 1, 1);
		return (a[aOffset] * VectorUtil.getCoord(b, bOffset) + a[1 + aOffset] * VectorUtil.getCoord(b, 1 + bOffset) + a[2 + aOffset] * VectorUtil.getCoord(b, 2 + bOffset));
	}
	 */
	private static float DDOTSpec(float[] a, int aOffset, float xB, float yB, float zB) {
		//return DDOTpq(a,  b, aOffset, bOffset, 1, 1);
		return (a[aOffset] * xB + a[1 + aOffset] * yB + a[2 + aOffset] * zB);
	}

	private static void DLineClosestApproach(Vector3f pa, Vector3f ua,
	                                         Vector3f pb, Vector3f ub,
	                                         Vector2f alphaBeta, Vector3f pTmp) {

		pTmp.sub(pb, pa);

		float uaub = ua.dot(ub);

		float q1 = ua.dot(pTmp);

		float q2 = -(ub.dot(pTmp));

		float d = 1 - uaub * uaub;
		if (d <= 0.0001f) {
			// @@@ this needs to be made more robust
			alphaBeta.x = 0f;
			alphaBeta.y = 0f;
		} else {
			d = 1f / d;
			alphaBeta.x = (q1 + uaub * q2) * d;
			alphaBeta.y = (uaub * q1 + q2) * d;
		}
	}

	public static void DMULTIPLY0_331(Vector3f A, float[] B, Vector3f C) {
		//		A.x = DDOT(B, 0,  C, 0);
		//		A.y = DDOT(B, 4,  C, 0);
		//		A.z = DDOT(B, 8,  C, 0);
		A.x = DDOTSpec(B, 0, C.x, C.y, C.z);
		A.y = DDOTSpec(B, 4, C.x, C.y, C.z);
		A.z = DDOTSpec(B, 8, C.x, C.y, C.z);
	}

	public static void DMULTIPLY1_331(Vector3f A, float[] B, Vector3f C) {

		//		A.x = DDOT41(B, 0,  C, 0);
		//		A.y = DDOT41(B, 1,  C, 0);
		//		A.z = DDOT41(B, 2,  C, 0);
		A.x = DDOT41Spec(B, 0, C.x, C.y, C.z);
		A.y = DDOT41Spec(B, 1, C.x, C.y, C.z);
		A.z = DDOT41Spec(B, 2, C.x, C.y, C.z);
	}

	private void CullPoints2(int n, float[] p, int m, int i0, int[] iret) {
		// compute the centroid of the polygon in cx,cy
		int iretIndex = 0;
		int i, j;
		float a, cx, cy, q;
		if (n == 1) {
			cx = p[0];
			cy = p[1];
		} else if (n == 2) {
			cx = 0.5f * (p[0] + p[2]);
			cy = 0.5f * (p[1] + p[3]);
		} else {
			a = 0;
			cx = 0;
			cy = 0;
			for (i = 0; i < (n - 1); i++) {
				q = p[i * 2] * p[i * 2 + 3] - p[i * 2 + 2] * p[i * 2 + 1];
				a += q;
				cx += q * (p[i * 2] + p[i * 2 + 2]);
				cy += q * (p[i * 2 + 1] + p[i * 2 + 3]);
			}
			q = p[n * 2 - 2] * p[1] - p[0] * p[n * 2 - 1];
			if (Math.abs(a + q) > BulletGlobals.SIMD_EPSILON) {
				a = 1f / (3.0f * (a + q));
			} else {
				a = 1e30f;
			}
			cx = a * (cx + q * (p[n * 2 - 2] + p[0]));
			cy = a * (cy + q * (p[n * 2 - 1] + p[1]));
		}

		// compute the angle of each point w.r.t. the centroid
		float[] A = s_A;
		for (i = 0; i < n; i++) {
			A[i] = FastMath.atan2Fast(p[i * 2 + 1] - cy, p[i * 2] - cx);
			//			A[i] = (float)Math.atan2(p[i * 2 + 1] - cy, p[i * 2] - cx);
		}

		// search for points that have angles closest to A[i0] + i*(2*pi/m).
		for (i = 0; i < n; i++) {
			s_availablePoints[i] = 1;
		}
		s_availablePoints[i0] = 0;
		iret[0] = i0;
		iretIndex++;
		for (j = 1; j < m; j++) {
			a = j * (BulletGlobals.SIMD_2_PI / m) + A[i0];
			if (a > BulletGlobals.SIMD_PI) {
				a -= BulletGlobals.SIMD_2_PI;
			}
			boolean f = true;
			float maxdiff = Float.MAX_VALUE, diff;

			iret[iretIndex] = i0;                   // iret is not allowed to keep this value, but it sometimes does, when diff=#QNAN0

			for (i = 0; i < n; i++) {
				if (s_availablePoints[i] != 0) {
					diff = Math.abs(A[i] - a);
					if (diff > BulletGlobals.SIMD_PI) {
						diff = BulletGlobals.SIMD_2_PI - diff;
					}
					if (f || diff < maxdiff) {
						f = false;
						maxdiff = diff;
						iret[iretIndex] = i;
					}
				}
			}
			//#if defined(DEBUG) || defined (_DEBUG)
			//    btAssert (*iret != i0);   // ensure iret got set
			//#endif
			s_availablePoints[iret[iretIndex]] = 0;
			iretIndex++;
		}
	}

	private int DBoxBox2(Vector3f pos1, float[] rot1,
	                     Vector3f A, Vector3f pos2,
	                     float[] rot2, Vector3f B,
	                     Vector3f normal, Float depth, int return_code,
	                     int maxContacts, int skip, ManifoldResult output) {
		//Vector3f centerDifference = Vector3f.zero, ppv = Vector3f.zero;

		cb.normalR = null;

		//		A.set(boxMargin1);
		//		A.scale(0.5f);
		//		B.set(boxMargin2);
		//		B.scale(0.5f);

		// get vector from centers of box 1 to box 2, relative to box 1

		distPoint.sub(pos2, pos1);

		pp.set(0, 0, 0);

		DMULTIPLY1_331(pp, rot1, distPoint);  // get pp = p relative to body 1

		// for all 15 possible separating axes:
		//   * see if the axis separates the boxes. if so, return 0.
		//   * find the depth of the penetration along the separating axis (s2)
		//   * if this is the largest depth so far, record it.
		// the normal vector will be set to the separating axis with the smallest
		// depth. note: normalR is set to point to a column of R1 or R2 if that is
		// the smallest depth normal so far. otherwise normalR is 0 and normalC is
		// set to a vector relative to body 1. invert_normal is 1 if the sign of
		// the normal should be flipped.

		float R11 = DDOT44(rot1, 0, rot2, 0);
		float R12 = DDOT44(rot1, 0, rot2, 1);
		float R13 = DDOT44(rot1, 0, rot2, 2);

		float R21 = DDOT44(rot1, 1, rot2, 0);
		float R22 = DDOT44(rot1, 1, rot2, 1);
		float R23 = DDOT44(rot1, 1, rot2, 2);

		float R31 = DDOT44(rot1, 2, rot2, 0);
		float R32 = DDOT44(rot1, 2, rot2, 1);
		float R33 = DDOT44(rot1, 2, rot2, 2);

		float Q11 = Math.abs(R11);
		float Q12 = Math.abs(R12);
		float Q13 = Math.abs(R13);

		float Q21 = Math.abs(R21);
		float Q22 = Math.abs(R22);
		float Q23 = Math.abs(R23);

		float Q31 = Math.abs(R31);
		float Q32 = Math.abs(R32);
		float Q33 = Math.abs(R33);
		// for all 15 possible separating axes:
		//   * see if the axis separates the boxes. if so, return 0.
		//   * find the depth of the penetration along the separating axis (s2)
		//   * if this is the largest depth so far, record it.
		// the normal vector will be set to the separating axis with the smallest
		// depth. note: normalR is set to point to a column of R1 or R2 if that is
		// the smallest depth normal so far. otherwise normalR is 0 and normalC is
		// set to a vector relative to body 1. invert_normal is 1 if the sign of
		// the normal should be flipped.

		cb.invert_normal = false;
		cb.code = 0;

		// separating axis = u1,u2,u3
		//            if (TST(pp.x, (A.x + B.x * Q11 + B.y * Q12 + B.z * Q13), R1,  normalR, 0,  normalROffset, 1,  code,  s,  invert_normal)) return 0;
		//            if (TST(pp.y, (A.y + B.x * Q21 + B.y * Q22 + B.z * Q23), R1,  normalR, 1,  normalROffset, 2,  code,  s,  invert_normal)) return 0;
		//            if (TST(pp.z, (A.z + B.x * Q31 + B.y * Q32 + B.z * Q33), R1,  normalR, 2,  normalROffset, 3,  code,  s,  invert_normal)) return 0;
		if (TST(pp.x, (A.x + B.x * Q11 + B.y * Q12 + B.z * Q13), rot1, 0, 1, cb)) {
			return 0;
		}
		if (TST(pp.y, (A.y + B.x * Q21 + B.y * Q22 + B.z * Q23), rot1, 1, 2, cb)) {
			return 0;
		}
		if (TST(pp.z, (A.z + B.x * Q31 + B.y * Q32 + B.z * Q33), rot1, 2, 3, cb)) {
			return 0;
		}

		// separating axis = v1,v2,v3
		//            if (TST(DDOT41(R2, 0,  p, 0), (A.x * Q11 + A.y * Q21 + A.z * Q31 + B.x), R2,  normalR, 0,  normalROffset, 4,  code,  s,  invert_normal)) return 0;
		//            if (TST(DDOT41(R2, 1,  p, 0), (A.x * Q12 + A.y * Q22 + A.z * Q32 + B.y), R2,  normalR, 1,  normalROffset, 5,  code,  s,  invert_normal)) return 0;
		//            if (TST(DDOT41(R2, 2,  p, 0), (A.x * Q13 + A.y * Q23 + A.z * Q33 + B.z), R2,  normalR, 2,  normalROffset, 6,  code,  s,  invert_normal)) return 0;
		if (TST(DDOT41(rot2, 0, distPoint, 0), (A.x * Q11 + A.y * Q21 + A.z * Q31 + B.x), rot2, 0, 4, cb)) {
			return 0;
		}
		if (TST(DDOT41(rot2, 1, distPoint, 0), (A.x * Q12 + A.y * Q22 + A.z * Q32 + B.y), rot2, 1, 5, cb)) {
			return 0;
		}
		if (TST(DDOT41(rot2, 2, distPoint, 0), (A.x * Q13 + A.y * Q23 + A.z * Q33 + B.z), rot2, 2, 6, cb)) {
			return 0;
		}

		// note: cross product axes need to be scaled when s is computed.
		// normal (n1,n2,n3) is relative to box 1.
		// separating axis = u1 x (v1,v2,v3)
		//private static boolean TST2(float expr1,float expr2, Vector3f normal,  Vector3f normalC,int cc, int code)

		normalC.set(0, 0, 0);

		float fudge2 = 1.0e-5f;

		Q11 += fudge2;
		Q12 += fudge2;
		Q13 += fudge2;

		Q21 += fudge2;
		Q22 += fudge2;
		Q23 += fudge2;

		Q31 += fudge2;
		Q32 += fudge2;
		Q33 += fudge2;

		// separating axis = u1 x (v1,v2,v3)
		if (TST2(pp.z * R21 - pp.y * R31, (A.y * Q31 + A.z * Q21 + B.y * Q13 + B.z * Q12), 0, -R31, R21, normalC, 7, cb))
			return 0;
		if (TST2(pp.z * R22 - pp.y * R32, (A.y * Q32 + A.z * Q22 + B.x * Q13 + B.z * Q11), 0, -R32, R22, normalC, 8, cb))
			return 0;
		if (TST2(pp.z * R23 - pp.y * R33, (A.y * Q33 + A.z * Q23 + B.x * Q12 + B.y * Q11), 0, -R33, R23, normalC, 9, cb))
			return 0;

		// separating axis = u2 x (v1,v2,v3)
		if (TST2(pp.x * R31 - pp.z * R11, (A.x * Q31 + A.z * Q11 + B.y * Q23 + B.z * Q22), R31, 0, -R11, normalC, 10, cb))
			return 0;
		if (TST2(pp.x * R32 - pp.z * R12, (A.x * Q32 + A.z * Q12 + B.x * Q23 + B.z * Q21), R32, 0, -R12, normalC, 11, cb))
			return 0;
		if (TST2(pp.x * R33 - pp.z * R13, (A.x * Q33 + A.z * Q13 + B.x * Q22 + B.y * Q21), R33, 0, -R13, normalC, 12, cb))
			return 0;

		// separating axis = u3 x (v1,v2,v3)
		if (TST2(pp.y * R11 - pp.x * R21, (A.x * Q21 + A.y * Q11 + B.y * Q33 + B.z * Q32), -R21, R11, 0, normalC, 13, cb))
			return 0;
		if (TST2(pp.y * R12 - pp.x * R22, (A.x * Q22 + A.y * Q12 + B.x * Q33 + B.z * Q31), -R22, R12, 0, normalC, 14, cb))
			return 0;
		if (TST2(pp.y * R13 - pp.x * R23, (A.x * Q23 + A.y * Q13 + B.x * Q32 + B.y * Q31), -R23, R13, 0, normalC, 15, cb))
			return 0;

		//            if (TST2(pp.z * R21 - pp.y * R31, (A.y * Q31 + A.z * Q21 + B.y * Q13 + B.z * Q12), 0, -R31, R21,  normalC,  normalR, 7,  code,  s,  invert_normal)) return 0;
		//            if (TST2(pp.z * R22 - pp.y * R32, (A.y * Q32 + A.z * Q22 + B.x * Q13 + B.z * Q11), 0, -R32, R22,  normalC,  normalR, 8,  code,  s,  invert_normal)) return 0;
		//            if (TST2(pp.z * R23 - pp.y * R33, (A.y * Q33 + A.z * Q23 + B.x * Q12 + B.y * Q11), 0, -R33, R23,  normalC,  normalR, 9,  code,  s,  invert_normal)) return 0;
		//
		//            // separating axis = u2 x (v1,v2,v3)
		//            if (TST2(pp.x * R31 - pp.z * R11, (A.x * Q31 + A.z * Q11 + B.y * Q23 + B.z * Q22), R31, 0, -R11,  normalC,  normalR, 10,  code,  s,  invert_normal)) return 0;
		//            if (TST2(pp.x * R32 - pp.z * R12, (A.x * Q32 + A.z * Q12 + B.x * Q23 + B.z * Q21), R32, 0, -R12,  normalC,  normalR, 11,  code,  s,  invert_normal)) return 0;
		//            if (TST2(pp.x * R33 - pp.z * R13, (A.x * Q33 + A.z * Q13 + B.x * Q22 + B.y * Q21), R33, 0, -R13,  normalC,  normalR, 12,  code,  s,  invert_normal)) return 0;
		//
		//            // separating axis = u3 x (v1,v2,v3)
		//            if (TST2(pp.y * R11 - pp.x * R21, (A.x * Q21 + A.y * Q11 + B.y * Q33 + B.z * Q32), -R21, R11, 0,  normalC,  normalR, 13,  code,  s,  invert_normal)) return 0;
		//            if (TST2(pp.y * R12 - pp.x * R22, (A.x * Q22 + A.y * Q12 + B.x * Q33 + B.z * Q31), -R22, R12, 0,  normalC,  normalR, 14,  code,  s,  invert_normal)) return 0;
		//            if (TST2(pp.y * R13 - pp.x * R23, (A.x * Q23 + A.y * Q13 + B.x * Q32 + B.y * Q31), -R23, R13, 0,  normalC,  normalR, 15,  code,  s,  invert_normal)) return 0;

		if (cb.code == 0) {
			return 0;
		}
		// if we get to this point, the boxes interpenetrate. compute the normal
		// in global coordinates.
		if (cb.normalR != null) {
			//			System.err.println("INTERPENETRATION!!!!!!!!!!! "+cb.normalROffset);
			normal.x = cb.normalR[0 + cb.normalROffset];
			normal.y = cb.normalR[4 + cb.normalROffset];
			normal.z = cb.normalR[8 + cb.normalROffset];
			normal.normalize();
		} else {
			DMULTIPLY0_331(normal, rot1, normalC);
		}
		if (cb.invert_normal) {
			normal.negate();
		}
		depth = -cb.s;

		// compute contact point(s)

		//EDGE TOUCH
		if (cb.code > 6) {
			//this protion of the code works
			//			System.err.println("EDGE TOUCH (code: "+cb.code+")");
			// an edge from box 1 touches an edge from box 2.
			// find a point pa on the intersecting edge of box 1
			pa.set(pos1);
			for (int j = 0; j < 3; j++) {
				float sign = (DDOT14(normal, 0, rot1, j) > 0) ? 1.0f : -1.0f;

				for (int i = 0; i < 3; i++) {
					VectorUtil.setCoord(pa, i, VectorUtil.getCoord(pa, i) + sign * VectorUtil.getCoord(A, j) * rot1[i * 4 + j]);
				}
			}

			// find a point pb on the intersecting edge of box 2
			pb.set(pos2);
			//for (i = 0; i < 3; i++) pb[i] = p2[i];
			for (int j = 0; j < 3; j++) {
				float sign = (DDOT14(normal, 0, rot2, j) > 0) ? -1.0f : 1.0f;
				for (int i = 0; i < 3; i++) {
					VectorUtil.setCoord(pb, i, VectorUtil.getCoord(pb, i) + sign * VectorUtil.getCoord(B, j) * rot2[i * 4 + j]);
				}
			}

			float alpha, beta;
			//			Vector3f ua = new Vector3f();
			//			Vector3f ub = new Vector3f();
			for (int i = 0; i < 3; i++) {
				VectorUtil.setCoord(ua, i, rot1[((cb.code) - 7) / 3 + i * 4]);
			}
			for (int i = 0; i < 3; i++) {
				VectorUtil.setCoord(ub, i, rot2[((cb.code) - 7) % 3 + i * 4]);
			}

			DLineClosestApproach(pa, ua, pb, ub, alphaBeta, pTmp);

			alpha = alphaBeta.x;
			beta = alphaBeta.y;

			for (int i = 0; i < 3; i++) {
				float val = VectorUtil.getCoord(pa, i) + VectorUtil.getCoord(ua, i) * alpha;
				VectorUtil.setCoord(pa, i, val);
			}
			for (int i = 0; i < 3; i++) {
				float val = VectorUtil.getCoord(pb, i) + VectorUtil.getCoord(ub, i) * beta;
				VectorUtil.setCoord(pb, i, val);
			}

			{
				//contact[0].pos[i] = float(0.5)*(pa[i]+pb[i]);
				//contact[0].depth = *depth;

				if (USE_CENTER_POINT) {
					//	                	Vector3f pointInWorld = new Vector3f();
					//	                	pointInWorld.add(pa, pb);
					//	                	pointInWorld.scale(0.5f);
					//			            output.addContactPoint(-normal,pointInWorld,-depth);
				} else {
					negNormal.set(normal);
					negNormal.negate();
					output.addContactPoint(negNormal, pb, -depth, (int) blockA.x, (int) blockA.y, (int) blockA.z, (int) blockB.x, (int) blockB.y, (int) blockB.z, typeA, typeB, this.idA, this.idB);
					maxDepth = Math.max(depth, maxDepth);
					this.contacts++;
				} //
				return_code = cb.code;
			}
			return 1;
		}

		// okay, we have a face-something intersection (because the separating
		// axis is perpendicular to a face). define face 'a' to be the erence
		// face (i.e. the normal vector is perpendicular to this) and face 'b' to be
		// the incident face (the closest face of the other box).

		float[] Ra;
		float[] Rb;

		if (cb.code <= 3) {
			Ra = rot1;
			Rb = rot2;
			ppa.set(pos1);
			ppb.set(pos2);
			Sa.set(A);
			Sb.set(B);
		} else {
			Ra = rot2;
			Rb = rot1;
			ppa.set(pos2);
			ppb.set(pos1);
			Sa.set(B);
			Sb.set(A);
		}

		// nr = normal vector of erence face dotted with axes of incident box.
		// anr = absolute values of nr.
		nr.set(0, 0, 0);

		if (cb.code <= 3) {
			normal2.set(normal);
		} else {
			normal2.set(normal);
			normal2.negate();
		}
		DMULTIPLY1_331(nr, Rb, normal2);

		anr.absolute(nr);

		// find the largest compontent of anr: this corresponds to the normal
		// for the indident face. the other axis numbers of the indicent face
		// are stored in a1,a2.
		int largestNr, a1, a2;
		if (anr.y > anr.x) {
			if (anr.y > anr.z) {
				a1 = 0;
				largestNr = 1;
				a2 = 2;
			} else {
				a1 = 0;
				a2 = 1;
				largestNr = 2;
			}
		} else {
			if (anr.x > anr.z) {
				largestNr = 0;
				a1 = 1;
				a2 = 2;
			} else {
				a1 = 0;
				a2 = 1;
				largestNr = 2;
			}
		}

		// compute center point of incident face, in erence-face coordinates

		if (VectorUtil.getCoord(nr, largestNr) < 0) {
			for (int i = 0; i < 3; i++) {
				VectorUtil.setCoord(center, i,
						VectorUtil.getCoord(ppb, i) - VectorUtil.getCoord(ppa, i) +
								VectorUtil.getCoord(Sb, largestNr) * Rb[i * 4 + largestNr]);
			}
		} else {
			for (int i = 0; i < 3; i++) {
				VectorUtil.setCoord(center, i,
						VectorUtil.getCoord(ppb, i) - VectorUtil.getCoord(ppa, i) -
								VectorUtil.getCoord(Sb, largestNr) * Rb[i * 4 + largestNr]);
			}
		}

		// find the normal and non-normal axis numbers of the reference box
		int codeN, code1, code2;
		if (cb.code <= 3) {
			codeN = cb.code - 1;
		} else {
			codeN = cb.code - 4;
		}
		if (codeN == 0) {
			code1 = 1;
			code2 = 2;
		} else if (codeN == 1) {
			code1 = 0;
			code2 = 2;
		} else {
			code1 = 0;
			code2 = 1;
		}

		// find the four corners of the incident face, in erence-face coordinates
		float[] quad = s_quad;//new float[8];// s_quad;      // 2D coordinate of incident face (x,y pairs)
		float c1, c2, m11, m12, m21, m22;

		c1 = DDOT14(center, 0, Ra, code1);
		c2 = DDOT14(center, 0, Ra, code2);

		// optimize this? - we have already computed this data above, but it is not
		// stored in an easy-to-index format. for now it's quicker just to recompute
		// the four dot products.
		m11 = DDOT44(Ra, code1, Rb, a1);
		m12 = DDOT44(Ra, code1, Rb, a2);
		m21 = DDOT44(Ra, code2, Rb, a1);
		m22 = DDOT44(Ra, code2, Rb, a2);
		{
			float k1 = m11 * VectorUtil.getCoord(Sb, a1);
			float k2 = m21 * VectorUtil.getCoord(Sb, a1);
			float k3 = m12 * VectorUtil.getCoord(Sb, a2);
			float k4 = m22 * VectorUtil.getCoord(Sb, a2);
			quad[0] = c1 - k1 - k3;
			quad[1] = c2 - k2 - k4;
			quad[2] = c1 - k1 + k3;
			quad[3] = c2 - k2 + k4;
			quad[4] = c1 + k1 + k3;
			quad[5] = c2 + k2 + k4;
			quad[6] = c1 + k1 - k3;
			quad[7] = c2 + k2 - k4;
		}

		// find the size of the reference face
		//float[] s_rectReferenceFace = new float[2];
		s_rectReferenceFace[0] = VectorUtil.getCoord(Sa, code1);
		s_rectReferenceFace[1] = VectorUtil.getCoord(Sa, code2);

		// intersect the incident and reference faces
		float[] ret = s_ret;
		int n = IntersectRectQuad2(s_rectReferenceFace, quad, ret);
		if (n < 1) {
			return 0;               // this should never happen
		}

		// convert the intersection points into erence-face coordinates,
		// and compute the contact position and depth for each point. only keep
		// those points that have a positive (penetrating) depth. delete points in
		// the 'ret' array as necessary so that 'point' and 'ret' correspond.
		float[] point = s_point;//new float[3*8];//            // penetrating contact points
		float[] depths = s_dep; //new float[8];//;                        // depths for those points
		float det1 = 1f / (m11 * m22 - m12 * m21);
		m11 *= det1;
		m12 *= det1;
		m21 *= det1;
		m22 *= det1;
		int contactNum = 0;                       // number of penetrating contact points found
		for (int j = 0; j < n; j++) {
			float k1 = m22 * (ret[j * 2] - c1) - m12 * (ret[j * 2 + 1] - c2);
			float k2 = -m21 * (ret[j * 2] - c1) + m11 * (ret[j * 2 + 1] - c2);
			for (int i = 0; i < 3; i++) {
				point[contactNum * 3 + i] = VectorUtil.getCoord(center, i) + k1 * Rb[i * 4 + a1] + k2 * Rb[i * 4 + a2];
			}
			depths[contactNum] = VectorUtil.getCoord(Sa, codeN) - DDOT(normal2, 0, point, contactNum * 3);
			if (depths[contactNum] >= 0) {
				ret[contactNum * 2] = ret[j * 2];
				ret[contactNum * 2 + 1] = ret[j * 2 + 1];
				contactNum++;
			}
		}
		if (contactNum < 1) {
			return 0;       // this should never happen
		}

		// we can't generate more contacts than we actually have
		if (maxContacts > contactNum) {
			maxContacts = contactNum;
		}
		if (maxContacts < 1) {
			maxContacts = 1;
		}

		if (contactNum <= maxContacts) {
			if (cb.code < 4) {
				// we have less contacts than we need, so we use them all
				for (int j = 0; j < contactNum; j++) {

					for (int i = 0; i < 3; i++) {
						VectorUtil.setCoord(pointInWorldFAA, i, point[j * 3 + i] + VectorUtil.getCoord(ppa, i));
					}
					//                  if (BulletGlobals.g_streamWriter != null && BulletGlobals.debugBoxBoxDetector)
					//                  {
					//                            MathUtil.PrintVector3(BulletGlobals.g_streamWriter, "boxbox get closest", pointInWorldFA);
					//                  }
					negNormal.set(normal);
					negNormal.negate();
					output.addContactPoint(negNormal, pointInWorldFAA, -depths[j], (int) blockA.x, (int) blockA.y, (int) blockA.z, (int) blockB.x, (int) blockB.y, (int) blockB.z, typeA, typeB, this.idA, this.idB);
					maxDepth = Math.max(depths[j], maxDepth);
					this.contacts++;
				}
			} else {
				// we have less contacts than we need, so we use them all
				for (int j = 0; j < contactNum; j++) {

					for (int i = 0; i < 3; i++) {
						VectorUtil.setCoord(pointInWorldRes, i, point[j * 3 + i] + VectorUtil.getCoord(ppa, i));

					}

					//                  if (BulletGlobals.g_streamWriter != null && BulletGlobals.debugBoxBoxDetector)
					//                  {
					//                            MathUtil.PrintVector3(BulletGlobals.g_streamWriter, "boxbox get closest", pointInWorld);
					//                  }
					negNormal.set(normal);
					negNormal.negate();
					output.addContactPoint(negNormal, pointInWorldRes, -depths[j], (int) blockA.x, (int) blockA.y, (int) blockA.z, (int) blockB.x, (int) blockB.y, (int) blockB.z, typeA, typeB, this.idA, this.idB);
					this.contacts++;
					maxDepth = Math.max(depths[j], maxDepth);
				}
			}
		} else {
			// we have more contacts than are wanted, some of them must be culled.
			// find the deepest point, it is always the first contact.
			int i1 = 0;
			float maxdepth = depths[0];
			for (int i = 1; i < contactNum; i++) {
				if (depths[i] > maxdepth) {
					maxdepth = depths[i];
					i1 = i;
				}
			}
			Arrays.fill(iret, 0);

			CullPoints2(contactNum, ret, maxContacts, i1, iret);

			for (int j = 0; j < maxContacts; j++) {
				//      dContactGeom *con = CONTACT(contact,skip*j);
				//    for (i=0; i<3; i++) con->pos[i] = point[iret[j]*3+i] + pa[i];
				//  con->depth = dep[iret[j]];

				for (int i = 0; i < 3; i++) {
					VectorUtil.setCoord(posInWorldFA, i, point[iret[j] * 3 + i] + VectorUtil.getCoord(ppa, i));
				}
				//				Vector3f pointInWorld = new Vector3f(posInWorldFA);

				//                     if (BulletGlobals.g_streamWriter != null && BulletGlobals.debugBoxBoxDetector)
				//                    {
				//                        MathUtil.PrintVector3(BulletGlobals.g_streamWriter, "boxbox get closest", pointInWorld);
				//                    }
				negNormal.set(normal);
				negNormal.negate();

				if (cb.code < 4) {
					output.addContactPoint(negNormal, posInWorldFA, -depths[iret[j]], (int) blockA.x, (int) blockA.y, (int) blockA.z, (int) blockB.x, (int) blockB.y, (int) blockB.z, typeA, typeB, this.idA, this.idB);
					this.contacts++;
				} else {
					//					 output.addContactPoint(-normal,posInWorld-normal*dep[iret[j]],-dep[iret[j]]);
					scaledN.set(normal);
					scaledN.scale(depths[iret[j]]);
					posInWorldFA.sub(scaledN);
					output.addContactPoint(negNormal, posInWorldFA, -depths[iret[j]], (int) blockA.x, (int) blockA.y, (int) blockA.z, (int) blockB.x, (int) blockB.y, (int) blockB.z, typeA, typeB, this.idA, this.idB);
					this.contacts++;
				}

			}
			maxDepth = Math.max(maxdepth, maxDepth);
			contactNum = maxContacts;
		}
		return_code = cb.code;
		return contactNum;
	}

	public void GetClosestPoints(BoxShape box1, BoxShape box2, ClosestPointInput input, ManifoldResult output, IDebugDraw debugDraw, boolean swapResults, Vector3f elemPosA, Vector3f elemPosB, short typeA, short typeB, int idA, int idB) {
		contacts = 0;
		maxDepth = 0;
		transformA.set(input.transformA);
		transformB.set(input.transformB);

		blockA.set(elemPosA);
		blockB.set(elemPosB);
		this.typeA = typeA;
		this.typeB = typeB;

		this.idA = idA;
		this.idB = idB;

		int skip = 0;

		normal.set(0, 0, 0);
		depth = 0f;
		int return_code = -1;
		int maxc = 4;

		translationA.set(transformA.origin);
		translationB.set(transformB.origin);

		if (!cachedBoxSize) {
			box1.getHalfExtentsWithMargin(box1MarginCache);
			//		box1Margin.scale(2);

			box2.getHalfExtentsWithMargin(box2MarginCache);

			//		box2Margin.scale(2);
			cachedBoxSize = true;
		}
		box1Margin.set(box1MarginCache);
		box2Margin.set(box2MarginCache);
		//Vector3f box1Margin = 2f * debugExtents;
		//Vector3f box2Margin = 2f * debugExtents;

		for (int j = 0; j < 3; j++) {
			/* s_temp1[0 + 4 * j] = transformA.basis[j].x;
	            s_temp2[0 + 4 * j] = transformB.basis[j].x;

                s_temp1[1 + 4 * j] = transformA.basis[j].y;
                s_temp2[1 + 4 * j] = transformB.basis[j].y;


                s_temp1[2 + 4 * j] = transformA.basis[j].z;
                s_temp2[2 + 4 * j] = transformB.basis[j].z;*/
			transformA.basis.getRow(j, rowA);
			transformB.basis.getRow(j, rowB);

			s_temp1[0 + 4 * j] = rowA.x;
			s_temp2[0 + 4 * j] = rowB.x;

			s_temp1[1 + 4 * j] = rowA.y;
			s_temp2[1 + 4 * j] = rowB.y;

			s_temp1[2 + 4 * j] = rowA.z;
			s_temp2[2 + 4 * j] = rowB.z;

		}

		//s_temp1[0] = rotateA._Row0.x;
		//s_temp1[1] = rotateA._Row0.y;
		//s_temp1[2] = rotateA._Row0.z;

		//s_temp1[4] = rotateA._Row1.x;
		//s_temp1[5] = rotateA._Row1.y;
		//s_temp1[6] = rotateA._Row1.z;

		//s_temp1[8] = rotateA._Row2.x;
		//s_temp1[9] = rotateA._Row2.x;
		//s_temp1[10] = rotateA._Row2.x;

		//s_temp2[0] = rotateB._Row0.x;
		//s_temp2[1] = rotateB._Row0.y;
		//s_temp2[2] = rotateB._Row0.z;

		//s_temp2[4] = rotateB._Row1.x;
		//s_temp2[5] = rotateB._Row1.y;
		//s_temp2[6] = rotateB._Row1.z;

		//s_temp2[8] = rotateB._Row2.x;
		//s_temp2[9] = rotateB._Row2.y;
		//s_temp2[10] = rotateB._Row2.z;
		cb.reset();
		int dBoxBox2 = DBoxBox2(translationA,
				s_temp1,
				box1Margin,
				translationB,
				s_temp2,
				box2Margin,
				normal, depth, return_code,
				maxc, skip, output);
	}

	int IntersectRectQuad2(float[] h, float[] p, float[] ret) {
		// q (and r) contain nq (and nr) coordinate points for the current (and
		// chopped) polygons
		int nq = 4, nr = 0;

		float[] buffer = s_quadBuffer;
		float[] q = p;
		float[] r = ret;

		outerloop:
		for (int dir = 0; dir <= 1; dir++) {
			// direction notation: xy[0] = x axis, xy[1] = y axis
			for (int sign = -1; sign <= 1; sign += 2) {
				// chop q along the line xy[dir] = sign*h[dir]
				float[] pq = q;
				float[] pr = r;
				int pqIndex = 0;
				int prIndex = 0;
				nr = 0;
				for (int i = nq; i > 0; i--) {
					// go through all points in q and all lines between adjacent points
					if (sign * pq[pqIndex + dir] < h[dir]) {
						// this point is inside the chopping line
						pr[prIndex] = pq[pqIndex];
						pr[prIndex + 1] = pq[pqIndex + 1];
						prIndex += 2;
						nr++;
						if ((nr & 8) != 0) {
							q = r;
							break outerloop;
						}
					}
					float nextQDir = 0f;
					float nextQ1MinusDir = 0f;
					if (i > 1) {
						nextQDir = pq[pqIndex + 2 + dir];
						nextQ1MinusDir = pq[pqIndex + 2 + (1 - dir)];
					} else {
						nextQDir = q[dir];
						nextQ1MinusDir = q[1 - dir];
					}

					if ((sign * pq[pqIndex + dir] < h[dir]) ^ (sign * nextQDir < h[dir])) {
						// this line crosses the chopping line
						pr[prIndex + (1 - dir)] = pq[pqIndex + (1 - dir)] + (nextQ1MinusDir - pq[pqIndex + (1 - dir)]) /
								(nextQDir - pq[pqIndex + dir]) * (sign * h[dir] - pq[pqIndex + dir]);
						pr[prIndex + dir] = sign * h[dir];
						prIndex += 2;
						nr++;
						if ((nr & 8) != 0) {
							q = r;
							break outerloop;
						}
					}
					pqIndex += 2;
				}
				q = r;
				r = (q == ret) ? buffer : ret;
				nq = nr;
			}
		}
		// data already in ret
		if (q != ret) {
			for (int i = 0; i < nr * 2; i++) {
				ret[i] = q[i];
			}
		}
		return nr;
	}

	private boolean TST(float expr1, float expr2, float[] norm, int offset, int cc, CB cb) {
		float s2 = Math.abs(expr1) - expr2;
		if (s2 > 0) {
			return true;
		}
		if (s2 > cb.s) {
			cb.s = s2;
			cb.normalR = norm;
			//			new float[norm.length];
			//			for(int i = 0; i< norm.length; i++){
			//				cb.normalR[i] = norm[i];
			//			}
			cb.normalROffset = offset;
			cb.invert_normal = expr1 < 0;
			cb.code = cc;
		}
		return false;
	}

	// note: cross product axes need to be scaled when s is computed.
	// normal (n1,n2,n3) is relative to box 1.
	private boolean TST2(float expr1, float expr2, float n1, float n2, float n3, Vector3f normalC, int cc, CB cb) {
		float s2 = Math.abs(expr1) - (expr2);
		if (s2 > BulletGlobals.SIMD_EPSILON) {
			return true;
		}
		float l = (float) Math.sqrt((n1 * n1) + (n2 * n2) + (n3 * n3));
		if (l > BulletGlobals.SIMD_EPSILON) {
			s2 /= l;
			if (s2 * fudge_factor > cb.s) {
				cb.s = s2;
				cb.normalR = null;
				normalC.x = (n1) / l;
				normalC.y = (n2) / l;
				normalC.z = (n3) / l;
				cb.invert_normal = ((expr1) < 0);
				cb.code = (cc);
			}
		}
		return false;
	}

	private class CB {
		float[] normalR = null;
		//		int normalROffsetResult = 0;
		int code;
		boolean invert_normal;
		int normalROffset = 0;
		float s = Float.NEGATIVE_INFINITY;

		public void reset() {

			normalR = null;
			//			normalROffsetResult = 0;
			code = 0;
			invert_normal = false;
			normalROffset = 0;
			s = Float.NEGATIVE_INFINITY;
		}
	}

}





