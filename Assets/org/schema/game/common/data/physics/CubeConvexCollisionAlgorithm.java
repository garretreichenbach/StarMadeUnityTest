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

package org.schema.game.common.data.physics;

import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.CollisionAlgorithmConstructionInfo;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.ConvexPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.util.ObjectPool;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.SegmentBufferIteratorInterface;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.TriggerManagerInterface;
import org.schema.game.common.controller.elements.trigger.TriggerCollectionManager;
import org.schema.game.common.controller.elements.trigger.TriggerElementManager;
import org.schema.game.common.controller.elements.trigger.TriggerUnit;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.physics.shape.DodecahedronShapeExt;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;

/**
 * Provides collision detection between two spheres.
 *
 * @author jezek2
 */
public class CubeConvexCollisionAlgorithm extends CollisionAlgorithm {
	private static final float margin = 0.05f;
	private static ThreadLocal<ObjectArrayList<CubeConvexCollisionAlgorithm>> threadLocalPool = new ThreadLocal<ObjectArrayList<CubeConvexCollisionAlgorithm>>() {
		@Override
		protected ObjectArrayList<CubeConvexCollisionAlgorithm> initialValue() {

			ObjectArrayList<CubeConvexCollisionAlgorithm> p = new ObjectArrayList<CubeConvexCollisionAlgorithm>(512);
			for (int i = 0; i < 512; i++) {
				p.add(new CubeConvexCollisionAlgorithm());
			}

			return p;
		}
	};
	private static ThreadLocal<CubeConvexVariableSet> threadLocal = new ThreadLocal<CubeConvexVariableSet>() {
		@Override
		protected CubeConvexVariableSet initialValue() {
			return new CubeConvexVariableSet();
		}
	};
	protected final ObjectPool<ClosestPointInput> pointInputsPool = ObjectPool
			.get(ClosestPointInput.class);
	public boolean lowLevelOfDetail;
	GjkPairDetectorExt gjkPairDetector;
	/**
	 * indicated unique manifold-
	 * this is not true when compound is used.
	 * in this case a shared manifold is used
	 */
	private boolean ownManifold;
	@SuppressWarnings("unused")
	private boolean onServer;
	private PersistentManifold manifoldPtr;
	private CubeConvexVariableSet v;
	private OuterSegmentHandler outerSegmentHandler;
	public void processDistinctCollision(CubeShape cubeShape0,
	                                     CollisionObject col1, SegmentData data0,
	                                     Transform tmpTrans1, Transform tmpTrans2,
	                                     DispatcherInfo dispatchInfo, ManifoldResult resultOut) {

		if (!v.intersectionCallBackAwithB.initialized) {
			v.intersectionCallBackAwithB.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);

		}

		v.intersectionCallBackAwithB.reset();

		//		OctreeNode rootA = .getRoot();
//		if (v.dodecahedron != null) {
//			v.intersectionCallBackAwithB = data0.getOctree().findIntersectingDodecahedron(
//					v.oSet, v.intersectionCallBackAwithB, data0.getSegment(), tmpTrans1,
//					v.absolute, margin, v.dodecahedron.dodecahedron, 1.0f);
//		} else {

//			v.intersectionCallBackAwithB = data0.getOctree().findIntersectingAABB(
//					v.oSet, v.intersectionCallBackAwithB, data0.getSegment(), tmpTrans1,
//					v.absolute, margin, v.shapeMin, v.shapeMax, 1.0f);
//		}
		// DebugControlManager.boundingBoxes.add(new BoundingBox(othermin,
		// othermax, 1,0,0,1));

		// no hit in this octree, don't generate a new contact
		if (v.intersectionCallBackAwithB.hitCount == 0) {
			resultOut.refreshContactPoints();
			return;
		}
		//		 System.err.println("Hitcount A "+intersectionCallBackAwithB.hitCount);
		int count = 0;
		for (int i = 0; i < v.intersectionCallBackAwithB.hitCount; i++) {

			v.intersectionCallBackAwithB.getHit(i, v.hitMin, v.hitMax, v.startA, v.endA);

			assert (v.startA.x < v.endA.x && v.startA.y < v.endA.y && v.startA.z < v.endA.z);

			doNarrowTest(data0, col1, v.startA, v.endA,
					dispatchInfo, resultOut);
		}

	}
	public static ObjectArrayFIFOQueue<CubeConvexBlockCallback[]> pool = new ObjectArrayFIFOQueue<CubeConvexBlockCallback[]>();
	public static void releaseCB(CubeConvexBlockCallback[] c){
		synchronized(pool){
			pool.enqueue(c);
		}
			
	}
	public static CubeConvexBlockCallback[] getCB(){
		synchronized(pool){
			if(pool.isEmpty()){
				CubeConvexBlockCallback[] c = new CubeConvexBlockCallback[4096];
				for (int i = 0; i < c.length; i++) {
					c[i] = new CubeConvexBlockCallback();
				}
				return c;
			}else{
				return pool.dequeue();
			}
		}
	}

	
	private CubeConvexBlockCallback[] callbackCache;
	private void doNarrowTest(SegmentData data0, CollisionObject col1,
	                          Vector3b startA, Vector3b endA,
	                          DispatcherInfo dispatchInfo, ManifoldResult resultOut) {

		col1.getCollisionShape().getAabb(v.convexShapeTransform, v.otherminOut, v.othermaxOut);
		Vector3i segPos = data0.getSegment().pos;
		for (byte x = startA.x; x < endA.x; x++) {
			for (byte y = startA.y; y < endA.y; y++) {
				for (byte z = startA.z; z < endA.z; z++) {
					int eA = SegmentData.getInfoIndex(
							(byte) (x + SegmentData.SEG_HALF),
							(byte) (y + SegmentData.SEG_HALF),
							(byte) (z + SegmentData.SEG_HALF));
					ElementInformation info = null;
					if (data0.contains(eA) && ((info = ElementKeyMap.getInfo(data0.getType(eA))).isPhysical(data0.isActive(eA)) || info.getId() == ElementKeyMap.SIGNAL_TRIGGER_AREA) && !(info.hasLod() && info.lodCollisionPhysical)) {

						byte orientation = data0.getOrientation(eA);
						v.elemPosA.set(x, y, z);
						v.elemPosA.x += segPos.x;
						v.elemPosA.y += segPos.y;
						v.elemPosA.z += segPos.z;
						v.min.set(v.elemPosA);
						v.max.set(v.elemPosA);

						v.min.x -= (0.5f);
						v.min.y -= (0.5f);
						v.min.z -= (0.5f);
						v.max.x += (0.5f);
						v.max.y += (0.5f);
						v.max.z += (0.5f);

						// cubeMeshTransform.transform(minThis);
						// cubeMeshTransform.transform(maxThis);

						AabbUtil2.transformAabb(v.min, v.max, 0.03f, v.cubeMeshTransform, v.minOut, v.maxOut);

						boolean aabbTest;
						if (v.dodecahedron != null) {

//							aabbTest = v.dodecahedron.dodecahedron.testAABB(v.minOut, v.maxOut, v.dodecaOverlap);
							aabbTest = false;
							//							if(aabbTest && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							//								DebugDrawer.boundingBoxes.add(new DebugBoundingBox(new Vector3f(v.minOut), new Vector3f(v.maxOut), 0, 1, 0, 1));
							//							}
						} else {
							aabbTest = AabbUtil2
									.testAabbAgainstAabb2(v.minOut, v.maxOut, v.shapeMin, v.shapeMax);
							//							if(aabbTest && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							//								DebugDrawer.boundingBoxes.add(new DebugBoundingBox(new Vector3f(v.minOut), new Vector3f(v.maxOut), 0, 1, 0, 1));
							//							}
						}

						// System.err.println("B range "+minThis+" + "+maxThis);

						v.box0.setMargin(0);

						// System.err.println("texting "+eA+" + "+eB+": "+minOut+" - "+maxOut+";  "+otherminOut+", "+othermaxOut);
						if (aabbTest) {
							growCache(v.cubeCallbackPointer);

							v.boxTransformation.set(v.cubeMeshTransform);

							v.nA.set(v.elemPosA);
							v.boxTransformation.basis.transform(v.nA);
							v.boxTransformation.origin.add(v.nA);

							callbackCache[v.cubeCallbackPointer].blockPos.set(
									x + data0.getSegment().pos.x + SegmentData.SEG_HALF,
									y + data0.getSegment().pos.y + SegmentData.SEG_HALF,
									z + data0.getSegment().pos.z + SegmentData.SEG_HALF);
							callbackCache[v.cubeCallbackPointer].boxTransform.set(v.boxTransformation);
							callbackCache[v.cubeCallbackPointer].blockInfo.set(info.getId(), orientation, data0.isActive(eA) ? 1 : 0);
							if (v.dodecahedron != null) {
								for (int i = 0; i < v.dodecaOverlap.length; i++) {
									for (int j = 0; j < v.dodecaOverlap[i].length; j++) {
										callbackCache[v.cubeCallbackPointer].dodecaOverlap[i][j] = v.dodecaOverlap[i][j];
									}
								}
							}

							//							System.err.println("COLLISION WITH: "+(byte) (x + SegmentData.SEG / 2)+", "+
							//									(byte) (y + SegmentData.SEG / 2)+", "+
							//									(byte) (z + SegmentData.SEG / 2) +":   "+resultOut.getPersistentManifold().getNumContacts());
							v.cubeCallbackPointer++;
						}
					}

				}
			}
		}

	}

	private void growCache(int index) {
		if (index >= this.callbackCache.length) {
			CubeConvexBlockCallback[] ncallbackCache = new CubeConvexBlockCallback[this.callbackCache.length * 2];
			for (int i = 0; i < this.callbackCache.length; i++) {
				ncallbackCache[i] = this.callbackCache[i];
			}
			for (int i = this.callbackCache.length; i < ncallbackCache.length; i++) {
				ncallbackCache[i] = new CubeConvexBlockCallback();
			}
			this.callbackCache = ncallbackCache;
		}
	}
	private void doRegularCollision(ConvexShape min0, ConvexShape min1,
	                                Transform tA, Transform tB, ManifoldResult resultOut, PersistentManifold manifoldPtr,
	                                DispatcherInfo dispatchInfo, int type, Vector3i blockPos, int segmentControllerId) {

		ClosestPointInput input = pointInputsPool.get();
		input.init();

		//		resultOut.setPersistentManifold(manifoldPtr);
		// JAVA NOTE: original: TODO: if (dispatchInfo.m_useContinuous)
		gjkPairDetector.setMinkowskiA(min0);
		gjkPairDetector.setMinkowskiB(min1);

		input.maximumDistanceSquared = min0.getMargin() + /*min1.getMargin()*/ +manifoldPtr.getContactBreakingThreshold();

		input.maximumDistanceSquared *= input.maximumDistanceSquared;
		// input.m_stackAlloc = dispatchInfo.m_stackAllocator;

		// input.m_maximumDistanceSquared = btScalar(1e30);

		input.transformA.set(tA);
		input.transformB.set(tB);

		
		v.blockA.set(blockPos.x, blockPos.y, blockPos.z);
		
		//cubes is col0
		gjkPairDetector.getClosestPoints(input, resultOut,
				dispatchInfo.debugDraw, false, v.blockA, v.blockB, (short)type, (short)0, segmentControllerId, convexObjectId );

		//		if(onServer){
		//			for(int i = 0; i < resultOut.getPersistentManifold().getNumContacts(); i++){
		//				ManifoldPoint contactPoint = resultOut.getPersistentManifold().getContactPoint(i);
		//				System.err.println("CONTACT: "+i+": "+contactPoint.localPointA+"; "+contactPoint.normalWorldOnB);
		//			}
		//		}

		pointInputsPool.release(input);

		resultOut.refreshContactPoints();
	}

	@Override
	public void init(CollisionAlgorithmConstructionInfo ci) {
		super.init(ci);
		manifoldPtr = ci.manifold;
	}

	// //////////////////////////////////////////////////////////////////////////

	@Override
	public void destroy() {
		//		if (ownManifold) {
		//			if (manifoldPtr != null) {
		//				dispatcher.releaseManifold(manifoldPtr);
		//			}
		//			manifoldPtr = null;
		////			System.err.println("DESTROYED ALGORITHM CubeConvex");
		//		}
	}
	private int convexObjectId = 0;
	private short lastUpdateNumDrawPP;
	@Override
	public void processCollision(CollisionObject c0, CollisionObject c1,
	                             DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		//		PersistentManifold localPtr;
		if (manifoldPtr == null) {
			// swapped?
			//			manifoldPtr = dispatcher.getNewManifold(c0, c1);
			//
			//			if(manifoldPtr == null){
			//				System.err.println("Exception: no maniforld in CubeConvexCollisionAlgorithm: "+c0+"; "+c1);
			//				return;
			//			}
			//

			ownManifold = true;
		}
		convexObjectId = 0;
		assert (!(c0 instanceof RigidBodySegmentController) || !((RigidBodySegmentController) c0).isCollisionException()) : c0;
		assert (!(c1 instanceof RigidBodySegmentController) || !((RigidBodySegmentController) c1).isCollisionException()) : c1;

		//		System.err.println("COLLIDE convex "+c0+" with "+c1);
		manifoldPtr = resultOut.getPersistentManifold();
		ownManifold = true;
		//		resultOut.setPersistentManifold(manifoldPtr);
		//		localPtr = manifoldPtr;

		long t = System.currentTimeMillis();
		CollisionObject col0 = c0;
		CollisionObject col1 = c1;
		//		if(col0.toString().contains("|SER") || col1.toString().contains("|SER")){
		//		System.err.println("CALCULATE TOI: "+col0+" "+col1);
		//		try{
		//		throw new NullPointerException();
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		}
		//		if(c1.getCollisionShape() instanceof CubeShape){
		//			col0 = c1;
		//			col1 = c0;
		//		}else{
		//			col0 = c0;
		//			col1 = c1;
		//		}

		if (col1 instanceof PairCachingGhostObjectUncollidable) {
			return;
		}
		v.cubeCallbackPointer = 0;
		resultOut.setPersistentManifold(manifoldPtr);
		CubeShape cubeShape0 = (CubeShape) col0.getCollisionShape();
		ConvexShape convexShape = (ConvexShape) col1.getCollisionShape();

		v.cubesShape0 = cubeShape0;
		v.convexShape = convexShape;
		
		
		if(col1.getUserPointer() != null && col1.getUserPointer() instanceof Integer){
			convexObjectId = (Integer)col1.getUserPointer();
		}else{
			convexObjectId = 0;
		}

		if (v.convexShape instanceof DodecahedronShapeExt) {
			v.dodecahedron = (DodecahedronShapeExt) v.convexShape;
		} else {
			v.dodecahedron = null;
		}

		onServer = cubeShape0.getSegmentBuffer().getSegmentController().isOnServer();

		//		System.err.println("DOING COLLISION OF "+cubeShape0+" with "+convexShape);

		v.oSet = ArrayOctree.getSet(cubeShape0.getSegmentBuffer().getSegmentController().isOnServer());

		v.cubeMeshTransform = col0.getWorldTransform(v.cubeMeshTransform);
		v.convexShapeTransform = col1.getWorldTransform(v.convexShapeTransform);

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

		outerSegmentHandler.col1 = col1;
		outerSegmentHandler.cubeShape0 = cubeShape0;
		outerSegmentHandler.dispatchInfo = dispatchInfo;
		outerSegmentHandler.resultOut = resultOut;

		v.outer.min.set(v.outMin);
		v.outer.max.set(v.outMax);

		BoundingBox intersection = v.inner.getIntersection(v.outer, v.outBB);

		if (intersection == null || !v.outBB.isValid()) {
			//			System.err.println("Exception intersection is null while AABB overlap");
			return;
		}

		//		System.err.println("INTERSECTION FOR "+getSegmentController()+": "+self+" -> "+requestBox+" = "+intersection);
		v.minIntA.x = ByteUtil.divSeg((int) (v.outBB.min.x - SegmentData.SEG_HALF)) * SegmentData.SEG;
		v.minIntA.y = ByteUtil.divSeg((int) (v.outBB.min.y - SegmentData.SEG_HALF)) * SegmentData.SEG;
		v.minIntA.z = ByteUtil.divSeg((int) (v.outBB.min.z - SegmentData.SEG_HALF)) * SegmentData.SEG;

		v.maxIntA.x = (FastMath.fastCeil((v.outBB.max.x + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
		v.maxIntA.y = (FastMath.fastCeil((v.outBB.max.y + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
		v.maxIntA.z = (FastMath.fastCeil((v.outBB.max.z + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;

		//		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
		//		}
		callbackCache = getCB();
		cubeShape0.getSegmentBuffer().iterateOverNonEmptyElementRange(outerSegmentHandler, v.minIntA, v.maxIntA, false);

		handleCubeCallbacks(cubeShape0, convexShape, col0, col1, resultOut, dispatchInfo);
		
		
		
		releaseCB(callbackCache);
		callbackCache = null;
		outerSegmentHandler.col1 = null;
		outerSegmentHandler.cubeShape0 = null;
		outerSegmentHandler.dispatchInfo = null;
		outerSegmentHandler.resultOut = null;
	}

	//	private Vector3f otherMinIn = new Vector3f();
	//	private Vector3f otherMaxIn = new Vector3f();

	@Override
	public float calculateTimeOfImpact(CollisionObject body0,
	                                   CollisionObject body1, DispatcherInfo dispatchInfo,
	                                   ManifoldResult resultOut) {
		System.err.println("CALCULATING CONVEX CUBE TOI");
		return 1f;
	}

	@Override
	public void getAllContactManifolds(
			ObjectArrayList<PersistentManifold> manifoldArray) {
		if (manifoldPtr != null && ownManifold) {
			manifoldArray.add(manifoldPtr);
		}
	}

	public void init(PersistentManifold mf,
	                 CollisionAlgorithmConstructionInfo ci, CollisionObject col0,
	                 CollisionObject col1, SimplexSolverInterface simplexSolver,
	                 ConvexPenetrationDepthSolver pdSolver, boolean swapped) {
		super.init(ci);
		manifoldPtr = mf;
		v = threadLocal.get();

		gjkPairDetector = new GjkPairDetectorExt(v.gjkVars);
		gjkPairDetector.useExtraPenetrationCheck = false;
		gjkPairDetector.init(null, null, simplexSolver, pdSolver);
		if (manifoldPtr == null) {
			if (!swapped) {
				manifoldPtr = dispatcher.getNewManifold(col0, col1);
			} else {
				manifoldPtr = dispatcher.getNewManifold(col1, col0);
			}
			ownManifold = true;
		} else {
			ownManifold = false;
		}
		outerSegmentHandler = new OuterSegmentHandler();
	}

	private void handleCubeCallbacks(CubeShape cubeShape0, ConvexShape convexShape, CollisionObject col0, CollisionObject col1, ManifoldResult resultOut, DispatcherInfo dispatchInfo) {
				int size = v.cubeCallbackPointer;
		for (int i = 0; i < size; i++) {
			CubeConvexBlockCallback cb = callbackCache[i];
			Transform boxTransform = cb.boxTransform;
			Vector3i info = cb.blockInfo;
			Vector3i pos = cb.blockPos;

			boolean[][] dodecaOverlap = cb.dodecaOverlap;

			int type = info.x;

			ElementInformation in = ElementKeyMap.getInfoFast((short) type);
			BlockStyle blockStyle = in.getBlockStyle();
			int orientation;
			if(type == ElementKeyMap.CARGO_SPACE){
				if(info.y == 4){
					continue;
				}
				orientation = Element.TOP;
			}else{
				orientation = info.y;
			}
			 
			boolean active = info.z == 1;

			CollisionShape cShape = v.box0;
			if(in.lodUseDetailCollision) {
				cShape = in.lodDetailCollision.getShape((short)type, (byte) orientation, v.lodBlockTransform);
			}else if (!in.isNormalBlockStyle() && blockStyle != BlockStyle.SPRITE) {
				cShape = BlockShapeAlgorithm.getShape(blockStyle, (byte) orientation);
			}
			if(in.getSlab(info.y) > 0){
				boxTransform = v.BT;
				boxTransform.set(cb.boxTransform);
				
				v.orientTT.set(Element.DIRECTIONSf[Element.switchLeftRight(orientation%6)]);
				boxTransform.basis.transform(v.orientTT);
				switch(in.getSlab(info.y)) {
					case 1 -> {
						v.orientTT.scale(0.125f);
						cShape = v.box34[orientation % 6];
					}
					case 2 -> {
						v.orientTT.scale(0.25f);
						cShape = v.box12[orientation % 6];
					}
					case 3 -> {
						v.orientTT.scale(0.375f);
						cShape = v.box14[orientation % 6];
					}
				}
//				System.err.println("SLAPPP "+in.slab+"; "+v.orientTT+"; "+((BoxShape)shape).getHalfExtentsWithMargin(new Vector3f()));
				boxTransform.origin.sub(v.orientTT);
			}
			
			if(cShape instanceof CompoundShape) {
				/*
				 * do a regular collision for all subshapes. this is unsave and required the
				 * hirachy to be flat in that the compound shape may only have convexShape
				 * childs
				 */
				CompoundShape cs = (CompoundShape)cShape;
				for(int c = 0; c < cs.getNumChildShapes(); c++) {
					CompoundShapeChild child = cs.getChildList().get(c);
					
					v.lodBlockTransform.origin.set(0,0,0);
					
					v.boxETransform.set(boxTransform);
					v.boxETransform.mul(v.lodBlockTransform);
					v.boxETransform.mul(child.transform); //mul local tranform of compound child
					
					ConvexShape shape = (ConvexShape)child.childShape;
					
//					if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && !cubeShape0.getSegmentController().isOnServer() && ((GameClientState)cubeShape0.getSegmentController().getState()).getNumberOfUpdate() > lastUpdateNumDrawPP+10) {
//						
//						ConvexHullShapeExt e = (ConvexHullShapeExt)shape;
//						
//						Vector4f color = new Vector4f(1,1,1,1);
//						if(c == 0) {
//							color.set(1,0,0,1);
//						}else if(c == 1) {
//							color.set(0,1,0,1);
//						}else if(c == 2) {
//							color.set(0,0,1,1);
//						}else if(c == 3) {
//							color.set(1,0,1,1);
//						}else if(c == 4) {
//							color.set(0,1,1,1);
//						}else if(c == 6) {
//							color.set(1,1,0,1);
//						}
//						
//						for(Vector3f p : e.getPoints()) {
//							Vector3f dp = new Vector3f(p);
//							v.boxETransform.transform(dp);
//							
//							DebugPoint debugPoint = new DebugPoint(dp, color, 0.1f);
//							debugPoint.LIFETIME = 10000;
//							DebugDrawer.points.add(debugPoint);
//							
//						}
//					}
					
					doDetailedCollision(col0, col1, shape, in, convexShape, pos, v.boxETransform, dodecaOverlap, resultOut, dispatchInfo, type, cb, cubeShape0);
				}
				if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && !cubeShape0.getSegmentController().isOnServer()){
					lastUpdateNumDrawPP = ((GameClientState)cubeShape0.getSegmentController().getState()).getNumberOfUpdate();
				}
			}else {
				//do only one collision
				ConvexShape shape = (ConvexShape)cShape;
				
				doDetailedCollision(col0, col1, shape, in, convexShape, pos, boxTransform, dodecaOverlap, resultOut, dispatchInfo, type, cb, cubeShape0);
			}
			
			
		}
		if (ownManifold) {
			resultOut.refreshContactPoints();
		} else {
			assert false;
		}
	}

	private void doDetailedCollision(CollisionObject col0, CollisionObject col1, ConvexShape shape,
			ElementInformation in, ConvexShape convexShape, Vector3i pos, Transform boxTransform, boolean[][] dodecaOverlap, ManifoldResult resultOut, DispatcherInfo dispatchInfo, int type,
			CubeConvexBlockCallback cb, CubeShape cubeShape0) {
		if (col1 instanceof RigidDebrisBody) {
			
			doRegularCollision(shape, convexShape,
					boxTransform, v.convexShapeTransform,
					resultOut, manifoldPtr, dispatchInfo, type, cb.blockPos, cubeShape0.getSegmentController().getId());
		} else {

			try {
				if (in.getId() == ElementKeyMap.SIGNAL_TRIGGER_AREA) {

					v.dist.sub(boxTransform.origin, v.convexShapeTransform.origin);
					if (v.dist.length() < 0.69f) {

						if (!cubeShape0.getSegmentBuffer().getSegmentController().isOnServer()) {

							ManifoldResult r = new ManifoldResult(col0, col1);
							PersistentManifold persistentManifold = new PersistentManifold();
							r.setPersistentManifold(persistentManifold);

							doRegularCollision(shape, convexShape,
									boxTransform, v.convexShapeTransform,
									r, persistentManifold, dispatchInfo, type, cb.blockPos, cubeShape0.getSegmentController().getId());

							if (persistentManifold.getNumContacts() > 0 && cubeShape0.getSegmentBuffer().getSegmentController() instanceof ManagedSegmentController<?>) {
								ManagedSegmentController<?> m = (ManagedSegmentController<?>) cubeShape0.getSegmentBuffer().getSegmentController();
								if (m.getManagerContainer() instanceof TriggerManagerInterface) {
									TriggerManagerInterface tr = (TriggerManagerInterface) m.getManagerContainer();
									ManagerModuleCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> trigger = tr.getTrigger();

									long index = ElementCollection.getIndex(pos);
									for (TriggerCollectionManager c : trigger.getCollectionManagers()) {
										if(c.rawCollection.contains(index)){

											ActivationTrigger activationTrigger = new ActivationTrigger(c.getControllerIndex(), col1, in.getId());
											ActivationTrigger ret = cubeShape0.getSegmentBuffer().getSegmentController().getTriggers().get(activationTrigger);

											if (ret == null) {
												cubeShape0.getSegmentBuffer().getSegmentController().getTriggers().add(activationTrigger);
											} else {
												ret.ping();
											}
										}
									}
								}
							}

						}
					}
				} else {

					if (v.dodecahedron != null) {
						for (int k = 0; k < dodecaOverlap.length; k++) {
							if (dodecaOverlap[k][0]) {
								//								System.err.println("CHECKING PP "+k);
								for (int j = 1; j < dodecaOverlap[k].length; j++) {
									if (dodecaOverlap[k][j]) {
										doRegularCollision(shape, v.dodecahedron.dodecahedron.shapes[k][j - 1],
												boxTransform, v.convexShapeTransform,
												resultOut, manifoldPtr, dispatchInfo, type, cb.blockPos, cubeShape0.getSegmentController().getId());
									}
								}
							}
						}
					} else {

						doRegularCollision(shape, convexShape,
								boxTransform, v.convexShapeTransform,
								resultOut, manifoldPtr, dispatchInfo, type, cb.blockPos, cubeShape0.getSegmentController().getId());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(v.box0 + ", " + convexShape + ", " +
						boxTransform + ", " + v.convexShapeTransform + ", " +
						resultOut + ", " + dispatchInfo);
			}
		}		
	}

	public static class CreateFunc extends CollisionAlgorithmCreateFunc {

		private final ObjectArrayList<CubeConvexCollisionAlgorithm> pool = threadLocalPool.get();

		public SimplexSolverInterface simplexSolver;
		public ConvexPenetrationDepthSolver pdSolver;

		public CreateFunc(SimplexSolverInterface simplexSolver,
		                  ConvexPenetrationDepthSolver pdSolver) {
			this.simplexSolver = simplexSolver;
			this.pdSolver = pdSolver;
		}

		@Override
		public CollisionAlgorithm createCollisionAlgorithm(
				CollisionAlgorithmConstructionInfo ci, CollisionObject body0,
				CollisionObject body1) {
			CubeConvexCollisionAlgorithm algo;
			if (pool.isEmpty()) {
				algo = new CubeConvexCollisionAlgorithm();
			} else {
				algo = pool.remove(pool.size() - 1);
			}
			algo.init(ci.manifold, ci, body0, body1, simplexSolver, pdSolver, swapped);
			return algo;
		}

		@Override
		public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
			pool.add((CubeConvexCollisionAlgorithm) algo);
		}
	}

	private class OuterSegmentHandler implements SegmentBufferIteratorInterface {

		CubeShape cubeShape0;
		CollisionObject col1;
		DispatcherInfo dispatchInfo;
		ManifoldResult resultOut;

		@Override
		public boolean handle(Segment sOuter, long lastChanged) {
			if (sOuter.getSegmentData() == null || sOuter.isEmpty()) {
				return true; //continue
			}
			cubeShape0.getSegmentAabb(sOuter, v.cubeMeshTransform, v.outMin, v.outMax, v.localMinOut, v.localMaxOut, v.aabbVarSet);

			boolean intersectionOfSegments;

			if (v.dodecahedron != null) {
				intersectionOfSegments = false;

				Dodecahedron d = v.dodecahedron.dodecahedron;

				//check intersection with square around dodecahedron first
				//as that test costs far less then checking with the triangles
				if (d.intersectsOuterRadius(v.convexShapeTransform, v.outMin, v.outMax, 1.0f)) {
					intersectionOfSegments = d.testAABB(v.outMin, v.outMax);
				}

			} else {
				intersectionOfSegments = AabbUtil2
						.testAabbAgainstAabb2(v.shapeMin, v.shapeMax, v.outMin,
								v.outMax);
			}
			// DebugControlManager.boundingBoxes.add(new
			// BoundingBox(new Vector3f(shapeMin), new
			// Vector3f(shapeMax),1,0,1,1));

			//			if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
			////				System.err.println("INTER: "+intersectionOfSegments+"; "+v.convexShape+" -> "+cubeShape0.getSegmentBuffer().getSegmentController()+"; "+cubeShape0.getSegmentBuffer().getSegmentController().getState());
			//				sOuter.debugDraw(0.01f,
			//						intersectionOfSegments ? 0f : 1f,
			//						intersectionOfSegments ? 1f : 0f,
			//						0f,1f, 1f);
			//
			////				DebugDrawer.boxes.add(new DebugBox(new Vector3f(v.outMin), new Vector3f(v.outMax), v.cubeMeshTransform,intersectionOfSegments ? 1 : 0,intersectionOfSegments ? 0 : 1,0,1));
			//			}

			if (intersectionOfSegments) {

				processDistinctCollision(cubeShape0,
						col1, sOuter.getSegmentData(), v.cubeMeshTransform,
						v.convexShapeTransform, dispatchInfo, resultOut);
			}

			return true;
		}

	}

	// //////////////////////////////////////////////////////////////////////////
	;

}
