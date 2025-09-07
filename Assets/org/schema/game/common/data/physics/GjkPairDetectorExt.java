package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.ConvexPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * GjkPairDetector uses GJK to implement the {@link DiscreteCollisionDetectorInterface}.
 *
 * @author jezek2
 */
public class GjkPairDetectorExt extends DiscreteCollisionDetectorInterface {

	//protected final BulletStack stack = BulletStack.get();

	// must be above the machine epsilon
	private static final float REL_ERROR2 = 1.0e-6f;

	private final Vector3f cachedSeparatingAxis = new Vector3f();
	public ObjectArrayList<String> log;
	public int contacts;
	// some debugging to fix degeneracy problems
	public int lastUsedMethod;
	public int curIter;
	public int degenerateSimplex;
	public int catchDegeneracies;
	public boolean fastContactOnly;
	public float maxDepth;
	private ConvexPenetrationDepthSolver penetrationDepthSolver;
	private SimplexSolverInterface simplexSolver;
	private ConvexShape minkowskiA;
	private ConvexShape minkowskiB;
	private boolean ignoreMargin;
	private GjkPairDetectorVariables v;

	public boolean useExtraPenetrationCheck = false;

	public GjkPairDetectorExt(GjkPairDetectorVariables v) {
		this.v = v;
	}

	@Override
	public void getClosestPoints(ClosestPointInput input, Result output, IDebugDraw debugDraw, boolean swapResults) {

		contacts = 0;
		maxDepth = 0;

		Vector3f tmp = v.tmp;//new @Stack Vector3f();

		float distance = 0f;
		Vector3f normalInB = v.normalInB;//new @Stack Vector3f();
		normalInB.set(0f, 0f, 0f);
		Vector3f pointOnA = v.pointOnA;//new @Stack Vector3f(),
		Vector3f pointOnB = v.pointOnB;//new @Stack Vector3f();
		Transform localTransA = v.localTransA;//Stack.alloc(input.transformA);
		Transform localTransB = v.localTransB;//Stack.alloc(input.transformB);
		localTransA.set(input.transformA);
		localTransB.set(input.transformB);
		Vector3f positionOffset = v.positionOffset;//new @Stack Vector3f();
		positionOffset.add(localTransA.origin, localTransB.origin);
		positionOffset.scale(0.5f);
		localTransA.origin.sub(positionOffset);
		localTransB.origin.sub(positionOffset);

		float marginA = minkowskiA.getMargin();
		float marginB = minkowskiB.getMargin();

		BulletStats.gNumGjkChecks++;

		// for CCD we don't use margins
		if (ignoreMargin) {
			marginA = 0f;
			marginB = 0f;
		}

		curIter = 0;
		int gGjkMaxIter = 1000; // this is to catch invalid input, perhaps check for #NaN?
		cachedSeparatingAxis.set(0f, 1f, 0f);

		boolean isValid = false;
		boolean checkSimplex = false;
		boolean checkPenetration = true;
		degenerateSimplex = 0;

		lastUsedMethod = -1;

		{
			float squaredDistance = BulletGlobals.SIMD_INFINITY;
			float delta = 0f;

			float margin = marginA + marginB;

			simplexSolver.reset();

			Vector3f seperatingAxisInA = v.seperatingAxisInA;//new @Stack Vector3f();
			Vector3f seperatingAxisInB = v.seperatingAxisInB;//new @Stack Vector3f();

			Vector3f pInA = v.pInA;//new @Stack Vector3f();
			Vector3f qInB = v.qInB;//new @Stack Vector3f();

			Vector3f pWorld = v.pWorld;//new @Stack Vector3f();
			Vector3f qWorld = v.qWorld;//new @Stack Vector3f();
			Vector3f w = v.w;//new @Stack Vector3f();

			Vector3f tmpPointOnA = v.tmpPointOnA;//new @Stack Vector3f(),
			Vector3f tmpPointOnB = v.tmpPointOnB;//new @Stack Vector3f();
			Vector3f tmpNormalInB = v.tmpNormalInB;//new @Stack Vector3f();

			for (; ; ) //while (true)
			{
				seperatingAxisInA.negate(cachedSeparatingAxis);
				MatrixUtil.transposeTransform(seperatingAxisInA, seperatingAxisInA, input.transformA.basis);

				seperatingAxisInB.set(cachedSeparatingAxis);
				MatrixUtil.transposeTransform(seperatingAxisInB, seperatingAxisInB, input.transformB.basis);

				minkowskiA.localGetSupportingVertexWithoutMargin(seperatingAxisInA, pInA);
				minkowskiB.localGetSupportingVertexWithoutMargin(seperatingAxisInB, qInB);

				pWorld.set(pInA);
				localTransA.transform(pWorld);

				qWorld.set(qInB);
				localTransB.transform(qWorld);

				w.sub(pWorld, qWorld);

				delta = cachedSeparatingAxis.dot(w);

				if(useExtraPenetrationCheck){
					// potential exit, they don't overlap
					if ((delta > 0f) && (delta * delta > squaredDistance * input.maximumDistanceSquared)) {
						degenerateSimplex = 10;
						//#StarMade 0.19196
						/*
						 * in some cases the delta between the points gets to low to be recognized
						 * causing a collision miss when there should be collision detected.
						 * in this case we have to actually check for it.
						 */
						checkPenetration = true; //original from bullet is false here
						break;
					}
				}else{
					// potential exit, they don't overlap
					if ((delta > 0f) && (delta * delta > squaredDistance * input.maximumDistanceSquared)) {
						checkPenetration = false;
						break;
					}
				}

				// exit 0: the new point is already in the simplex, or we didn't come any closer
				if (simplexSolver.inSimplex(w)) {
					degenerateSimplex = 1;
					checkSimplex = true;
					break;
				}
				// are we getting any closer ?
				float f0 = squaredDistance - delta;
				float f1 = squaredDistance * REL_ERROR2;

				if (f0 <= f1) {
					if (f0 <= 0f) {
						degenerateSimplex = 2;
					} else {
						degenerateSimplex = 11;
					}
					checkSimplex = true;
					break;
				}
				// add current vertex to simplex
				simplexSolver.addVertex(w, pWorld, qWorld);

				// calculate the closest point to the origin (update vector v)
				if (!simplexSolver.closest(cachedSeparatingAxis)) {
					degenerateSimplex = 3;
					checkSimplex = true;
					break;
				}

				if (cachedSeparatingAxis.lengthSquared() < REL_ERROR2) {
					degenerateSimplex = 6;
					checkSimplex = true;
					break;
				}

				float previousSquaredDistance = squaredDistance;
				squaredDistance = cachedSeparatingAxis.lengthSquared();

				// redundant m_simplexSolver->compute_points(pointOnA, pointOnB);

				// are we getting any closer ?
				if (previousSquaredDistance - squaredDistance <= BulletGlobals.SIMD_EPSILON * previousSquaredDistance) {
					simplexSolver.backup_closest(cachedSeparatingAxis);
					degenerateSimplex = 12;
					checkSimplex = true;
					break;
				}

				// degeneracy, this is typically due to invalid/uninitialized worldtransforms for a CollisionObject
				if (curIter++ > gGjkMaxIter) {
					//#if defined(DEBUG) || defined (_DEBUG)
					if (BulletGlobals.DEBUG) {
						System.err.printf("btGjkPairDetector maxIter exceeded:%i\n", curIter);
						System.err.printf("sepAxis=(%f,%f,%f), squaredDistance = %f, shapeTypeA=%i,shapeTypeB=%i\n",
								cachedSeparatingAxis.x,
								cachedSeparatingAxis.y,
								cachedSeparatingAxis.z,
								squaredDistance,
								minkowskiA.getShapeType(),
								minkowskiB.getShapeType());
					}
					//#endif
					break;

				}

				boolean check = (!simplexSolver.fullSimplex());
				//bool check = (!m_simplexSolver->fullSimplex() && squaredDistance > SIMD_EPSILON * m_simplexSolver->maxVertex());

				if (!check) {
					degenerateSimplex = 13;
					// do we need this backup_closest here ?
					simplexSolver.backup_closest(cachedSeparatingAxis);
					break;
				}
			}
			if (checkSimplex) {
				simplexSolver.compute_points(pointOnA, pointOnB);
				normalInB.sub(pointOnA, pointOnB);
				float lenSqr = cachedSeparatingAxis.lengthSquared();
				// valid normal
				if (lenSqr < 0.0001f) {
					degenerateSimplex = 5;
				}
				if (lenSqr > BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON) {
					float rlen = FastMath.carmackInvSqrt(lenSqr);//(float) Math.sqrt(lenSqr);
					normalInB.scale(rlen); // normalize
					float s = FastMath.carmackInvSqrt(squaredDistance);//(float) Math.sqrt(squaredDistance);

					assert (s > 0f);

					tmp.scale((marginA * s), cachedSeparatingAxis);
					pointOnA.sub(tmp);

					tmp.scale((marginB * s), cachedSeparatingAxis);
					pointOnB.add(tmp);

					distance = ((1f / rlen) - margin);
					isValid = true;

					lastUsedMethod = 1;
				} else {
					lastUsedMethod = 2;
				}
			}

			boolean catchDegeneratePenetrationCase =
					(catchDegeneracies != 0 && penetrationDepthSolver != null && degenerateSimplex != 0 && ((distance + margin) < 0.01f));

			//if (checkPenetration && !isValid)
			if (checkPenetration && (!isValid || catchDegeneratePenetrationCase)) {
				// penetration case

				// if there is no way to handle penetrations, bail out
				if (penetrationDepthSolver != null) {
					// Penetration depth case.
					BulletStats.gNumDeepPenetrationChecks++;

					boolean isValid2 = penetrationDepthSolver.calcPenDepth(
							simplexSolver,
							minkowskiA, minkowskiB,
							localTransA, localTransB,
							cachedSeparatingAxis, tmpPointOnA, tmpPointOnB,
							debugDraw/*,input.stackAlloc*/);

					if (isValid2) {
						tmpNormalInB.sub(tmpPointOnB, tmpPointOnA);

						float lenSqr = tmpNormalInB.lengthSquared();
						if (lenSqr > (BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON)) {
							tmpNormalInB.scale(FastMath.carmackInvSqrt(lenSqr)); //(float) Math.sqrt(lenSqr)
							tmp.sub(tmpPointOnA, tmpPointOnB);
							float distance2 = -tmp.length();
							// only replace valid penetrations when the result is deeper (check)
							if (!isValid || (distance2 < distance)) {
								distance = distance2;
								pointOnA.set(tmpPointOnA);
								pointOnB.set(tmpPointOnB);
								normalInB.set(tmpNormalInB);
								isValid = true;
								lastUsedMethod = 3;
							} else {

							}
						} else {
							//isValid = false;
							lastUsedMethod = 4;
						}
					} else {
						lastUsedMethod = 5;
					}

				}
			}
		}

		if (isValid) {
			contacts++;
			if (fastContactOnly) {
				return;
			}
			//#ifdef __SPU__
			//		//spu_printf("distance\n");
			//#endif //__CELLOS_LV2__

			tmp.add(pointOnB, positionOffset);
			output.addContactPoint(
					normalInB,
					tmp,
					distance);
			maxDepth = Math.max(distance, maxDepth);

			//printf("gjk add:%f",distance);
		}
	}

	public void getClosestPoints(ClosestPointInput input, Result output, IDebugDraw debugDraw, boolean swapResults, Vector3f blockA, Vector3f blockB, short typeA, short typeB, int idA, int idB) {

		contacts = 0;
		maxDepth = 0;

		Vector3f tmp = v.tmp;//new @Stack Vector3f();

		float distance = 0f;
		Vector3f normalInB = v.normalInB;//new @Stack Vector3f();
		normalInB.set(0f, 0f, 0f);
		Vector3f pointOnA = v.pointOnA;//new @Stack Vector3f(),
		Vector3f pointOnB = v.pointOnB;//new @Stack Vector3f();
		Transform localTransA = v.localTransA;//Stack.alloc(input.transformA);
		Transform localTransB = v.localTransB;//Stack.alloc(input.transformB);
		localTransA.set(input.transformA);
		localTransB.set(input.transformB);
		Vector3f positionOffset = v.positionOffset;//new @Stack Vector3f();
		positionOffset.add(localTransA.origin, localTransB.origin);
		positionOffset.scale(0.5f);
		localTransA.origin.sub(positionOffset);
		localTransB.origin.sub(positionOffset);

		float marginA = minkowskiA.getMargin();
		float marginB = minkowskiB.getMargin();

		BulletStats.gNumGjkChecks++;

		// for CCD we don't use margins
		if (ignoreMargin) {
			marginA = 0f;
			marginB = 0f;
		}

		curIter = 0;
		int gGjkMaxIter = 1000; // this is to catch invalid input, perhaps check for #NaN?
		cachedSeparatingAxis.set(0f, 1f, 0f);

		boolean isValid = false;
		boolean checkSimplex = false;
		boolean checkPenetration = true;
		degenerateSimplex = 0;

		lastUsedMethod = -1;

		{
			float squaredDistance = BulletGlobals.SIMD_INFINITY;
			float delta = 0f;

			float margin = marginA + marginB;

			simplexSolver.reset();

			Vector3f seperatingAxisInA = v.seperatingAxisInA;//new @Stack Vector3f();
			Vector3f seperatingAxisInB = v.seperatingAxisInB;//new @Stack Vector3f();

			Vector3f pInA = v.pInA;//new @Stack Vector3f();
			Vector3f qInB = v.qInB;//new @Stack Vector3f();

			Vector3f pWorld = v.pWorld;//new @Stack Vector3f();
			Vector3f qWorld = v.qWorld;//new @Stack Vector3f();
			Vector3f w = v.w;//new @Stack Vector3f();

			Vector3f tmpPointOnA = v.tmpPointOnA;//new @Stack Vector3f(),
			Vector3f tmpPointOnB = v.tmpPointOnB;//new @Stack Vector3f();
			Vector3f tmpNormalInB = v.tmpNormalInB;//new @Stack Vector3f();

			for (; ; ) //while (true)
			{
				seperatingAxisInA.negate(cachedSeparatingAxis);
				MatrixUtil.transposeTransform(seperatingAxisInA, seperatingAxisInA, input.transformA.basis);

				seperatingAxisInB.set(cachedSeparatingAxis);
				MatrixUtil.transposeTransform(seperatingAxisInB, seperatingAxisInB, input.transformB.basis);

				minkowskiA.localGetSupportingVertexWithoutMargin(seperatingAxisInA, pInA);
				minkowskiB.localGetSupportingVertexWithoutMargin(seperatingAxisInB, qInB);

				pWorld.set(pInA);
				localTransA.transform(pWorld);

				qWorld.set(qInB);
				localTransB.transform(qWorld);

				w.sub(pWorld, qWorld);

				delta = cachedSeparatingAxis.dot(w);
				if(useExtraPenetrationCheck){
					// potential exit, they don't overlap
					if ((delta > 0) && (delta * delta > squaredDistance * input.maximumDistanceSquared)) {
						degenerateSimplex = 10;
						//#StarMade 0.19196
						/*
						 * in some cases the delta between the points gets to low to be recognized
						 * causing a collision miss when there should be collision detected.
						 * in this case we have to actually check for it. 
						 */
						checkPenetration = true; //original from bullet is false here
						break;
					}
				}else{
					// potential exit, they don't overlap
					if ((delta > 0f) && (delta * delta > squaredDistance * input.maximumDistanceSquared)) {
						checkPenetration = false;
						break;
					}
				}
				// exit 0: the new point is already in the simplex, or we didn't come any closer
				if (simplexSolver.inSimplex(w)) {
					degenerateSimplex = 1;
					checkSimplex = true;
					break;
				}
				// are we getting any closer ?
				float f0 = squaredDistance - delta;
				float f1 = squaredDistance * REL_ERROR2;

				if (f0 <= f1) {
					if (f0 <= 0f) {
						degenerateSimplex = 2;
					} else {
						degenerateSimplex = 11;
					}
					checkSimplex = true;
					break;
				}
				// add current vertex to simplex
				simplexSolver.addVertex(w, pWorld, qWorld);

				// calculate the closest point to the origin (update vector v)
				if (!simplexSolver.closest(cachedSeparatingAxis)) {
					degenerateSimplex = 3;
					checkSimplex = true;
					break;
				}

				if (cachedSeparatingAxis.lengthSquared() < REL_ERROR2) {
					degenerateSimplex = 6;
					checkSimplex = true;
					break;
				}

				float previousSquaredDistance = squaredDistance;
				squaredDistance = cachedSeparatingAxis.lengthSquared();

				// redundant m_simplexSolver->compute_points(pointOnA, pointOnB);

				// are we getting any closer ?
				if (previousSquaredDistance - squaredDistance <= BulletGlobals.SIMD_EPSILON * previousSquaredDistance) {
					simplexSolver.backup_closest(cachedSeparatingAxis);
					degenerateSimplex = 12;
					checkSimplex = true;
					break;
				}

				// degeneracy, this is typically due to invalid/uninitialized worldtransforms for a CollisionObject
				if (curIter++ > gGjkMaxIter) {
					//#if defined(DEBUG) || defined (_DEBUG)
					if (BulletGlobals.DEBUG) {
						System.err.printf("btGjkPairDetector maxIter exceeded:%i\n", curIter);
						System.err.printf("sepAxis=(%f,%f,%f), squaredDistance = %f, shapeTypeA=%i,shapeTypeB=%i\n",
								cachedSeparatingAxis.x,
								cachedSeparatingAxis.y,
								cachedSeparatingAxis.z,
								squaredDistance,
								minkowskiA.getShapeType(),
								minkowskiB.getShapeType());
					}
					//#endif
					break;

				}

				boolean check = (!simplexSolver.fullSimplex());
				//bool check = (!m_simplexSolver->fullSimplex() && squaredDistance > SIMD_EPSILON * m_simplexSolver->maxVertex());

				if (!check) {
					degenerateSimplex = 13;
					// do we need this backup_closest here ?
					simplexSolver.backup_closest(cachedSeparatingAxis);
					break;
				}
			}
//			if(log != null){
//				log.add("VALID -1:::: "+isValid+"; luM "+lastUsedMethod+"; degSim "+degenerateSimplex+"; (check Simplex "+checkSimplex+") delta: "+delta+"; d2 "+df.format(delta*delta)+"; maxD: "+df.format(squaredDistance*input.maximumDistanceSquared)+";;; CC: "+(cachedSeparatingAxis.lengthSquared() < REL_ERROR2));
//			}
			if (checkSimplex) {
				simplexSolver.compute_points(pointOnA, pointOnB);
				normalInB.sub(pointOnA, pointOnB);
				float lenSqr = cachedSeparatingAxis.lengthSquared();
				// valid normal
				if (lenSqr < 0.0001f) {
//					System.err.println("DEGENERATE");
					degenerateSimplex = 5;
				}
				if (lenSqr > BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON) {
					float rlen = FastMath.carmackInvSqrt(lenSqr);
					normalInB.scale(rlen); // normalize
					float s = FastMath.carmackSqrt(squaredDistance);

					assert (s > 0f);

					tmp.scale((marginA / s), cachedSeparatingAxis);
					pointOnA.sub(tmp);

					tmp.scale((marginB / s), cachedSeparatingAxis);
					pointOnB.add(tmp);

					distance = ((1f / rlen) - margin);
					isValid = true;

					lastUsedMethod = 1;
				} else {
					lastUsedMethod = 2;
				}
			}
//			if(log != null){
//				log.add("VALID 0:::: "+isValid+"; luM "+lastUsedMethod+"; degSim "+degenerateSimplex+"; (check Simplex "+checkSimplex+") delta: "+delta+"; d2 "+delta*delta+"; maxD: "+input.maximumDistanceSquared);
//			}
			boolean catchDegeneratePenetrationCase =
					(catchDegeneracies != 0 && penetrationDepthSolver != null && degenerateSimplex != 0 && ((distance + margin) < 0.01f));

			//if (checkPenetration && !isValid)
			if (checkPenetration && (!isValid || catchDegeneratePenetrationCase)) {
				// penetration case

				// if there is no way to handle penetrations, bail out
				if (penetrationDepthSolver != null) {
					// Penetration depth case.
					BulletStats.gNumDeepPenetrationChecks++;

					boolean isValid2 = penetrationDepthSolver.calcPenDepth(
							simplexSolver,
							minkowskiA, minkowskiB,
							localTransA, localTransB,
							cachedSeparatingAxis, tmpPointOnA, tmpPointOnB,
							debugDraw/*,input.stackAlloc*/);

					if (isValid2) {
						tmpNormalInB.sub(tmpPointOnB, tmpPointOnA);

						float lenSqr = tmpNormalInB.lengthSquared();
						if (lenSqr > (BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON)) {
							tmpNormalInB.scale(FastMath.carmackInvSqrt(lenSqr));
							tmp.sub(tmpPointOnA, tmpPointOnB);
							float distance2 = -tmp.length();
							// only replace valid penetrations when the result is deeper (check)
							if (!isValid || (distance2 < distance)) {
								distance = distance2;
								pointOnA.set(tmpPointOnA);
								pointOnB.set(tmpPointOnB);
								normalInB.set(tmpNormalInB);
								isValid = true;
								lastUsedMethod = 3;
							} else {

							}
						} else {
							//isValid = false;
							lastUsedMethod = 4;
						}
					} else {
						lastUsedMethod = 5;
					}

				}
			}
		}
//		if(log != null){
//			log.add("VALID 1:::: "+isValid+"; lUM "+lastUsedMethod);
//		}
		if (isValid) {
			//#ifdef __SPU__
			//		//spu_printf("distance\n");
			//#endif //__CELLOS_LV2__

			if (distance <= BulletGlobals.getContactBreakingThreshold()) {
				contacts++;
			}

			if (fastContactOnly) {
//				if(Math.abs(distance) > 0.001f ){
//					if(log != null){
//						log.add("CONTACT: "+distance);
//					}
//					
//					
//				}else{
//					contacts = 0;
//				}
				return;
			}

			tmp.add(pointOnB, positionOffset);
			((ManifoldResult) output).addContactPoint(
					normalInB,
					tmp,
					distance, (int) blockA.x, (int) blockA.y, (int) blockA.z, (int) blockB.x, (int) blockB.y, (int) blockB.z, typeA, typeB, idA, idB);
			maxDepth = Math.max(distance, maxDepth);
			//printf("gjk add:%f",distance);
		}
	}

	public void init(ConvexShape objectA, ConvexShape objectB, SimplexSolverInterface simplexSolver, ConvexPenetrationDepthSolver penetrationDepthSolver) {
		this.cachedSeparatingAxis.set(0f, 0f, 1f);
		this.ignoreMargin = false;
		this.lastUsedMethod = -1;
		this.catchDegeneracies = 1;

		this.penetrationDepthSolver = penetrationDepthSolver;
		this.simplexSolver = simplexSolver;
		this.minkowskiA = objectA;
		this.minkowskiB = objectB;
	}

	public void setCachedSeperatingAxis(Vector3f seperatingAxis) {
		cachedSeparatingAxis.set(seperatingAxis);
	}

	/**
	 * Don't use setIgnoreMargin, it's for Bullet's internal use.
	 */
	public void setIgnoreMargin(boolean ignoreMargin) {
		this.ignoreMargin = ignoreMargin;
	}

	public void setMinkowskiA(ConvexShape minkA) {
		minkowskiA = minkA;
	}

	public void setMinkowskiB(ConvexShape minkB) {
		minkowskiB = minkB;
	}

	public void setPenetrationDepthSolver(ConvexPenetrationDepthSolver penetrationDepthSolver) {
		this.penetrationDepthSolver = penetrationDepthSolver;
	}

}

