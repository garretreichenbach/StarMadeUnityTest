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
 * 2. Altered source files must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package org.schema.game.common.data.explosion;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.SegmentBufferIteratorInterface;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeConvexVariableSet;
import org.schema.game.common.data.physics.CubeShape;
import org.schema.game.common.data.physics.GjkPairDetectorExt;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

/**
 * Provides collision detection between two spheres.
 *
 * @author jezek2
 */
public class ExplosionPhysicsSegemtsChecker {

	private static final float margin = 0.05f;
	private static ThreadLocal<CubeConvexVariableSet> threadLocal = new ThreadLocal<CubeConvexVariableSet>() {
		@Override
		protected CubeConvexVariableSet initialValue() {
			return new CubeConvexVariableSet();
		}
	};
	GjkPairDetectorExt gjkPairDetector;
	private CubeConvexVariableSet v;
	private OuterSegmentHandler outerSegmentHandler = new OuterSegmentHandler();

	private void processDistinctCollision(CubeShape cubeShape0, SegmentData data0,
	                                      Transform tmpTrans1, Transform tmpTrans2,
	                                      ExplosionCollisionSegmentCallback res) {
		
		assert (res != null);
		if (!v.intersectionCallBackAwithB.initialized) {
			v.intersectionCallBackAwithB.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);

		}

		v.intersectionCallBackAwithB.reset();

		assert (res.explosionRadius > 0);
		v.intersectionCallBackAwithB = data0.getOctree().findIntersectingAABB(
				v.oSet, v.intersectionCallBackAwithB, data0.getSegment(), tmpTrans1,
				v.absolute, margin, v.shapeMin, v.shapeMax, 1.0f, res.centerOfExplosion, res.explosionRadius);
		// DebugControlManager.boundingBoxes.add(new BoundingBox(othermin,
		// othermax, 1,0,0,1));

		// no hit in this octree, don't generate a new contact
		if (v.intersectionCallBackAwithB.hitCount == 0) {
			return;
		}
		//		 System.err.println("Hitcount A "+intersectionCallBackAwithB.hitCount);
		int count = 0;
		for (int i = 0; i < v.intersectionCallBackAwithB.hitCount; i++) {

			v.intersectionCallBackAwithB.getHit(i, v.hitMin, v.hitMax, v.startA, v.endA);

			assert (v.startA.x < v.endA.x && v.startA.y < v.endA.y && v.startA.z < v.endA.z);

			doNarrowTest(data0, v.startA, v.endA, res);
		}

	}

	private void doNarrowTest(SegmentData data0,
	                          Vector3b startA, Vector3b endA, ExplosionCollisionSegmentCallback res) {

		for (byte x = startA.x; x < endA.x; x++) {
			for (byte y = startA.y; y < endA.y; y++) {
				for (byte z = startA.z; z < endA.z; z++) {
					int eA = SegmentData.getInfoIndex(
							(byte) (x + SegmentData.SEG_HALF),
							(byte) (y + SegmentData.SEG_HALF),
							(byte) (z + SegmentData.SEG_HALF));

					if (data0.contains(eA)) {
						try {
							data0.checkWritable();
						} catch (SegmentDataWriteException e) {
							e.printStackTrace();
							assert(false);
						}
						ElementInformation info = ElementKeyMap.getInfoFast(data0.getType(eA));

						v.elemPosA.set(x, y, z);
						v.elemPosA.x += data0.getSegment().pos.x;
						v.elemPosA.y += data0.getSegment().pos.y;
						v.elemPosA.z += data0.getSegment().pos.z;
						v.min.set(v.elemPosA);
						v.max.set(v.elemPosA);

						v.min.x -= (0.5f);
						v.min.y -= (0.5f);
						v.min.z -= (0.5f);
						v.max.x += (0.5f);
						v.max.y += (0.5f);
						v.max.z += (0.5f);

						AabbUtil2.transformAabb(v.min, v.max, 0.03f, v.cubeMeshTransform, v.minOut, v.maxOut);

						v.closest.set(res.centerOfExplosion);

						Vector3fTools.clamp(v.closest, v.minOut, v.maxOut);

						v.closest.sub(res.centerOfExplosion);
						boolean aabbTest = v.closest.length() < res.explosionRadius;

						v.box0.setMargin(0);

						if (aabbTest) {
							
							
							res.growCache(res.cubeCallbackPointer);

							v.boxTransformation.set(v.cubeMeshTransform);

							v.nA.set(v.elemPosA);
							v.boxTransformation.basis.transform(v.nA);
							v.boxTransformation.origin.add(v.nA);
							
							
							
							
							res.callbackCache[res.cubeCallbackPointer].blockPos.set(
									x + data0.getSegment().pos.x + SegmentData.SEG_HALF,
									y + data0.getSegment().pos.y + SegmentData.SEG_HALF,
									z + data0.getSegment().pos.z + SegmentData.SEG_HALF);
							
							res.callbackCache[res.cubeCallbackPointer].segmentPos.set(data0.getSegment().pos);
							res.callbackCache[res.cubeCallbackPointer].type = ExplosionCubeConvexBlockCallback.SEGMENT_CONTROLLER;
							res.callbackCache[res.cubeCallbackPointer].segDataIndex = eA;
							res.callbackCache[res.cubeCallbackPointer].segPosX = (byte) (x + SegmentData.SEG_HALF);
							res.callbackCache[res.cubeCallbackPointer].segPosY = (byte) (y + SegmentData.SEG_HALF);
							res.callbackCache[res.cubeCallbackPointer].segPosZ = (byte) (z + SegmentData.SEG_HALF);
							res.callbackCache[res.cubeCallbackPointer].boxTransform.set(v.boxTransformation);
							v.closest.sub(v.boxTransformation.origin, res.centerOfExplosion);
							res.callbackCache[res.cubeCallbackPointer].boxPosToCenterOfExplosion.set(v.closest);
							res.callbackCache[res.cubeCallbackPointer].boxDistToCenterOfExplosion = FastMath.carmackLength(v.closest);
							res.callbackCache[res.cubeCallbackPointer].blockId = info.getId();
							res.callbackCache[res.cubeCallbackPointer].blockActive = data0.isActive(eA);
							res.callbackCache[res.cubeCallbackPointer].blockHp = info.convertToFullHp(data0.getHitpointsByte(eA));
							res.callbackCache[res.cubeCallbackPointer].blockHpOrig = res.callbackCache[res.cubeCallbackPointer].blockHp;
							res.callbackCache[res.cubeCallbackPointer].segEntityId = data0.getSegmentController().getId();
							res.callbackCache[res.cubeCallbackPointer].data = data0;
							res.cubeCallbackPointer++;
						}
					}

				}
			}
		}

	}

	public void processCollision(CubeShape c0, Transform cubeTrans, ConvexShape c1, Transform blastTrans,
	                             ExplosionCollisionSegmentCallback resultOut) {
		v = threadLocal.get();
		CubeShape cubeShape0 = c0;
		ConvexShape convexShape = c1;

		v.cubesShape0 = cubeShape0;
		v.convexShape = convexShape;

		v.oSet = ArrayOctree.getSet(cubeShape0.getSegmentBuffer().getSegmentController().isOnServer());

		v.cubeMeshTransform.set(cubeTrans);
		v.convexShapeTransform.set(blastTrans);

		v.cubeShapeTransformInv.set(v.cubeMeshTransform);
		v.cubeShapeTransformInv.inverse();

		v.absolute.set(v.cubeMeshTransform.basis);
		MatrixUtil.absolute(v.absolute);
		cubeShape0.setMargin(margin);

		v.convexShapeViewFromCubes.set(v.cubeShapeTransformInv);
		v.convexShapeViewFromCubes.mul(v.convexShapeTransform);

		cubeShape0.getAabb(TransformTools.ident, v.outMin, v.outMax);

		convexShape.getAabb(v.convexShapeViewFromCubes, v.inner.min, v.inner.max);

		//save for by segment test
		convexShape.getAabb(v.convexShapeTransform, v.shapeMin, v.shapeMax);

		cubeShape0.setMargin(margin);

		outerSegmentHandler.cubeShape0 = cubeShape0;

		v.outer.min.set(v.outMin);
		v.outer.max.set(v.outMax);

		BoundingBox intersection = v.inner.getIntersection(v.outer, v.outBB);
//		System.err.println("PROCESSING: "+intersection);
		if (intersection == null || !v.outBB.isValid()) {
			return;
		}

		v.minIntA.x = ByteUtil.divSeg((int) (v.outBB.min.x - SegmentData.SEG_HALF)) * SegmentData.SEG;
		v.minIntA.y = ByteUtil.divSeg((int) (v.outBB.min.y - SegmentData.SEG_HALF)) * SegmentData.SEG;
		v.minIntA.z = ByteUtil.divSeg((int) (v.outBB.min.z - SegmentData.SEG_HALF)) * SegmentData.SEG;

		v.maxIntA.x = (FastMath.fastCeil((v.outBB.max.x + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
		v.maxIntA.y = (FastMath.fastCeil((v.outBB.max.y + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
		v.maxIntA.z = (FastMath.fastCeil((v.outBB.max.z + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;

		outerSegmentHandler.res = resultOut;
		cubeShape0.getSegmentBuffer().iterateOverNonEmptyElementRange(outerSegmentHandler, v.minIntA, v.maxIntA, false);
		outerSegmentHandler.res = null;
		v = null;
	}

	private class OuterSegmentHandler implements SegmentBufferIteratorInterface {

		CubeShape cubeShape0;
		ExplosionCollisionSegmentCallback res;

		@Override
		public boolean handle(Segment sOuter, long lastChanged) {
			assert (res != null);
			if (sOuter.getSegmentData() == null || sOuter.isEmpty()) {
				return true; //continue
			}
			cubeShape0.getSegmentAabb(sOuter, v.cubeMeshTransform, v.outMin, v.outMax, v.localMinOut, v.localMaxOut, v.aabbVarSet);

			boolean intersectionOfSegments = AabbUtil2
					.testAabbAgainstAabb2(v.shapeMin, v.shapeMax, v.outMin,
							v.outMax);

			if (intersectionOfSegments) {
				
				
				try {
					sOuter.getSegmentData().checkWritable();
				} catch (SegmentDataWriteException e) {
					//replace data right here so multiple explosions wont be problematic
					SegmentDataWriteException.replaceData(sOuter);
				}
				int point = res.cubeCallbackPointer;
				processDistinctCollision(cubeShape0,
						sOuter.getSegmentData(), v.cubeMeshTransform,
						v.convexShapeTransform, res);

				if (res.cubeCallbackPointer > point) {
					//real collision happened

					res.ownLockedSegments.add(sOuter);
				}
			}

			return true;
		}

	}

}
