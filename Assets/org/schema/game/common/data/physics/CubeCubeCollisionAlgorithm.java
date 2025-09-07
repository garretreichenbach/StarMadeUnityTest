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

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentBufferIteratorInterface;
import org.schema.game.common.controller.SegmentBufferManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.TriggerManagerInterface;
import org.schema.game.common.controller.elements.trigger.TriggerCollectionManager;
import org.schema.game.common.controller.elements.trigger.TriggerElementManager;
import org.schema.game.common.controller.elements.trigger.TriggerUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ActivationTrigger;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.physics.octree.ArrayOctreeTraverse;
import org.schema.game.common.data.physics.sweepandpruneaabb.OverlappingSweepPair;
import org.schema.game.common.data.physics.sweepandpruneaabb.SegmentAabbInterface;
import org.schema.game.common.data.physics.sweepandpruneaabb.SweepPoint;
import org.schema.game.common.data.world.SectorNotFoundRuntimeException;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.StateInterface;

import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.CollisionAlgorithmConstructionInfo;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

/**
 * Provides collision detection between two spheres.
 *
 * @author jezek2
 */
public class CubeCubeCollisionAlgorithm extends CollisionAlgorithm {
	private static final long STUCK_TIME = 20000;
	static float margin = 0.05f;
	private static ThreadLocal<CubeCubeCollisionVariableSet> threadLocal = new ThreadLocal<CubeCubeCollisionVariableSet>() {
		@Override
		protected CubeCubeCollisionVariableSet initialValue() {
			return new CubeCubeCollisionVariableSet();
		}
	};
	protected final ObjectPool<ClosestPointInput> pointInputsPool = ObjectPool
			.get(ClosestPointInput.class);
	public boolean lowLevelOfDetail;
	public CubeCubeCollisionVariableSet v;
	public boolean localSwap;
	private int distinctTests;
	private long distinctTime;
	private GjkPairDetectorExt gjkPairDetector;
	private BoxBoxDetector boxboxPairDetector = new BoxBoxDetector();
	private int slow_threashold = ServerConfig.PHYSICS_SLOWDOWN_THRESHOLD.getInt();
	/**
	 * indicated unique manifold-
	 * this is not true when compound is used.
	 * in this case a shared manifold is used
	 */
	private boolean ownManifold;

	//	private OuterSegmentHandler outerSegmentHandler;
	//
	//	private InnerSegmentHandler innerSegmentHandler;
	private PersistentManifold manifoldPtr;
	private long currentTime;
	private boolean stuck;
	private boolean abort;
	private float currentMaxDepth;
	private long stuckStarted;
	private int stuckTests;
	private boolean actStuck;
	private InnerSegmentAddHandler innerHandler;
	private OuterSegmentAddHandler outerHandler;
	private int hitCount = 0;
	private Transform t0 = new Transform();
	private Transform t1 = new Transform();
	private float t0Mass;
	private float t1Mass;
	private boolean wasStuck;
	private short stuckUN;
	private int narrowTests;
	private boolean protectedByUndocking;
	private Vector3f proxVec = new Vector3f();
	private static final boolean old = false;

	public void cleanUpDispatcher() {
		//fixes a memory leak of dead bodies in the pool
		((CollisionDispatcherExt) dispatcher).cleanNearCallback();
	}

	private void checkStuck(SegmentData data0, SegmentData data1,
	                        Transform tmpTrans1, Transform wtInv1, Vector3f elemPos) {
		v.elemPosTest.set(elemPos.x, elemPos.y, elemPos.z);

		tmpTrans1.transform(v.elemPosTest);
		wtInv1.transform(v.elemPosTest);
		v.elemPosCheck.set((int) v.elemPosTest.x + SegmentData.SEG_HALF, (int) v.elemPosTest.y + SegmentData.SEG_HALF, (int) v.elemPosTest.z + SegmentData.SEG_HALF);

		int existing = 0;

		for (int i = 0; i < 6; i++) {
			v.elemPosCheckD.add(v.elemPosCheck, Element.DIRECTIONSi[i]);
			if (data1.getSegmentController().getSegmentBuffer().existsPointUnsave(v.elemPosCheckD)) {
				existing++;
			}
		}
		if (data1.getSegmentController().getSegmentBuffer().existsPointUnsave(v.elemPosCheck)) {
			existing++;
		}

		if (existing >= 7) {
			actStuck = true; //no need to do follow up tests
			if (stuckStarted == 0 || System.currentTimeMillis() - stuckStarted > STUCK_TIME) {
				System.err.println("[CC] POSSIBLE STUCK (timeframe: 2.5 secs): " + data0.getSegmentController() + " <-> " + data1.getSegmentController() + ": from " + v.elemPosTest + " to " + v.elemPosCheck + "; existing surround: " + existing);
				stuckStarted = System.currentTimeMillis();
			} else {
				if (System.currentTimeMillis() - stuckStarted > 2500) {
					this.stuck = true;

					this.stuckUN = data1.getSegmentController().getState().getNumberOfUpdate();
				}
			}

		} else {
			//			System.err.println("[CC]POSSIBLE NOT STUCK: "+data0.getSegmentController()+" <-> "+data1.getSegmentController()+": from "+v.elemPosTest+" to "+v.elemPosCheck+"; existing surround: "+existing);
		}

	}

	private int doExtCubeCubeCollision(BoxShape min0, BoxShape min1,
	                                   Transform tA, Transform tB, ManifoldResult resultOut,
	                                   DispatcherInfo dispatchInfo, Vector3f elemPosA, Vector3f elemPosB, short typeA, short typeB, int idA, int idB) {

		ClosestPointInput input = pointInputsPool.get();
		input.init();
		//		resultOut.setPersistentManifold(manifoldPtr);

		input.maximumDistanceSquared = min0.getMargin() + min1.getMargin()
				+ manifoldPtr.getContactBreakingThreshold();
		input.maximumDistanceSquared *= input.maximumDistanceSquared;

		input.transformA.set(tA);
		input.transformB.set(tB);
		boxboxPairDetector.GetClosestPoints(min0, min1, input, resultOut,
				dispatchInfo.debugDraw, false, elemPosA, elemPosB, typeA, typeB, idA, idB);
		pointInputsPool.release(input);

		this.currentMaxDepth = boxboxPairDetector.maxDepth;

		return boxboxPairDetector.contacts;
	}

	private void doNarrowTest(SegmentData data0, SegmentData data1,
	                          Vector3b startA, Vector3b endA, Vector3b startB, Vector3b endB, int maskA, int maskB, int nodeIndexA, int nodeIndexB,
	                          BoundingBox bbOctIntersection, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {

		
		this.narrowTests++;
		
		byte[][] bsA = ArrayOctreeTraverse.tMap[maskA];
		byte[][] bsB = ArrayOctreeTraverse.tMap[maskB];
		int[] inA = ArrayOctreeTraverse.tIndexMap[nodeIndexA][maskA];
		int[] inB = ArrayOctreeTraverse.tIndexMap[nodeIndexB][maskB];

		assert (bsA.length <= 8) : bsA.length;
		assert (bsB.length <= 8) : bsB.length;
		/*
		 * loop through the parts of intersecting octrees
		 */

		for (int a = 0; a < bsA.length; a++) {

			int x = (startA.x + bsA[a][0]);
			int y = (startA.y + bsA[a][1]);
			int z = (startA.z + bsA[a][2]);

			int eA = inA[a];

			int lastContacts = resultOut.getPersistentManifold().getNumContacts();

			ElementInformation infoA;
			short typeA;
			boolean activeA;
			if (((typeA = data0.getType(eA)) != 0) && (typeA == ElementKeyMap.SIGNAL_TRIGGER_AREA | ((infoA = ElementKeyMap.getInfo(typeA)).isPhysical(activeA = data0.isActive(eA)))) && infoA.lodCollisionPhysical) {
				byte orientationAOrig = data0.getOrientation(eA);
				byte orientationA = orientationAOrig;
				if(typeA == ElementKeyMap.CARGO_SPACE){
					if(orientationAOrig == 4){
						continue;
					}
					orientationA = Element.TOP;
				}
				//--------------element Pos for A
				v.elemPosA.set(
						x + data0.getSegment().pos.x,
						y + data0.getSegment().pos.y,
						z + data0.getSegment().pos.z);

				v.elemPosAAbs.set(v.elemPosA);
				v.tmpTrans0.transform(v.elemPosAAbs);
				v.min.set(
						v.elemPosA.x - 0.5f,
						v.elemPosA.y - 0.5f,
						v.elemPosA.z - 0.5f);
				v.max.set(
						v.elemPosA.x + 0.5f,
						v.elemPosA.y + 0.5f,
						v.elemPosA.z + 0.5f);
				//--------------------------------

				//				if(!AabbUtil2.testAabbAgainstAabb2(bbOctIntersection.min, bbOctIntersection.max, v.minOut, v.maxOut)){
				//					//no need to test further since this block is not hitting anything
				//					//in this part of the octree
				//					continue;
				//				}

				for (int b = 0; b < bsB.length; b++) {

					int Bx = (startB.x + bsB[b][0]);
					int By = (startB.y + bsB[b][1]);
					int Bz = (startB.z + bsB[b][2]);

					int eB = inB[b];

					ElementInformation infoB;
					boolean activeB;
					short typeB;
					if (((typeB = data1.getType(eB)) != 0) && (typeB == ElementKeyMap.SIGNAL_TRIGGER_AREA | ((infoB = ElementKeyMap.getInfo(typeB)).isPhysical(activeB = data1.isActive(eB)))) && infoB.lodCollisionPhysical) {
						byte orientationBOrig = data1.getOrientation(eB);
						byte orientationB = orientationBOrig;
						if(!infoA.cubeCubeCollision || !infoB.cubeCubeCollision) {
							continue;
						}
						if(typeB == ElementKeyMap.CARGO_SPACE){
							if(orientationBOrig == 4){
								
								continue;
							}
							orientationB = Element.TOP;
						}
						//--------------element Pos for B
						v.elemPosB.set(
								Bx + data1.getSegment().pos.x,
								By + data1.getSegment().pos.y,
								Bz + data1.getSegment().pos.z);

						v.elemPosBAbs.set(v.elemPosB);
						v.tmpTrans1.transform(v.elemPosBAbs);

						v.elemPosBAbs.sub(v.elemPosAAbs);
						
						proxVec.add(v.elemPosBAbs);

						boolean aabbTest = FastMath.carmackLength(v.elemPosBAbs) < 1.733f;
						if (aabbTest) {
							if (data0.getSegmentController().isOnServer()) {
								if (typeA == ElementKeyMap.SIGNAL_TRIGGER_AREA) {
									continue;
								} else if (typeB == ElementKeyMap.SIGNAL_TRIGGER_AREA) {
									continue;
								}
							} else {
								if (typeA == ElementKeyMap.SIGNAL_TRIGGER_AREA) {
									handleTrigger(data0, v.col0, x, y, z, typeA);
									continue;
								} else if (typeB == ElementKeyMap.SIGNAL_TRIGGER_AREA) {
									handleTrigger(data1, v.col1, Bx, By, Bz, typeB);
									continue;
								}
							}

							v.tmpTrans3.set(v.tmpTrans0);
							v.tmpTrans4.set(v.tmpTrans1);

							v.nA.set(v.elemPosA);
							v.nB.set(v.elemPosB);
							v.tmpTrans3.basis.transform(v.nA);
							v.tmpTrans4.basis.transform(v.nB);

							v.tmpTrans3.origin.add(v.nA);
							v.tmpTrans4.origin.add(v.nB);

							ConvexShape shapeA = v.box0;
							ConvexShape shapeB = v.box1;
							boolean needsNonCubeCubeCollision = false;
							if (infoA.blockStyle.solidBlockStyle) {
								shapeA = BlockShapeAlgorithm.getShape(infoA.getBlockStyle(), orientationA);
								needsNonCubeCubeCollision = true;
							}
							if (infoB.blockStyle.solidBlockStyle) {
								shapeB = BlockShapeAlgorithm.getShape(infoB.getBlockStyle(), orientationB);
								needsNonCubeCubeCollision = true;
							}
							
							
							Transform boxTransformA = v.tmpTrans3;
							if(infoA.getSlab(orientationAOrig) > 0){
								boxTransformA = v.BT_A;
								boxTransformA.set(v.tmpTrans3);
								
								v.orientTT.set(Element.DIRECTIONSf[Element.switchLeftRight(orientationA%6)]);
								boxTransformA.basis.transform(v.orientTT);
								switch(infoA.getSlab(orientationAOrig)) {
									case 1 -> {
										v.orientTT.scale(0.125f);
										shapeA = v.box34[orientationA % 6];
									}
									case 2 -> {
										v.orientTT.scale(0.25f);
										shapeA = v.box12[orientationA % 6];
									}
									case 3 -> {
										v.orientTT.scale(0.375f);
										shapeA = v.box14[orientationA % 6];
									}
								}
//								System.err.println("CC SLAPPP "+e.slab+"; "+v.orientTT+"; "+((BoxShape)shape).getHalfExtentsWithMargin(new Vector3f()));
								boxTransformA.origin.sub(v.orientTT);
								boxboxPairDetector.cachedBoxSize = false;
							}
							Transform boxTransformB = v.tmpTrans4;
							if(infoB.getSlab(orientationBOrig) > 0){
								boxTransformB = v.BT_B;
								boxTransformB.set(v.tmpTrans4);
								
								v.orientTT.set(Element.DIRECTIONSf[Element.switchLeftRight(orientationB%6)]);
								boxTransformB.basis.transform(v.orientTT);
								switch(infoB.getSlab(orientationBOrig)) {
									case 1 -> {
										v.orientTT.scale(0.125f);
										shapeB = v.box34[orientationB % 6];
									}
									case 2 -> {
										v.orientTT.scale(0.25f);
										shapeB = v.box12[orientationB % 6];
									}
									case 3 -> {
										v.orientTT.scale(0.375f);
										shapeB = v.box14[orientationB % 6];
									}
								}
//								System.err.println("CC SLAPPP "+e.slab+"; "+v.orientTT+"; "+((BoxShape)shape).getHalfExtentsWithMargin(new Vector3f()));
								boxTransformB.origin.sub(v.orientTT);
								boxboxPairDetector.cachedBoxSize = false;
							}
							
							this.currentMaxDepth = 0;
							int contactsOnThis = 0;
							if (needsNonCubeCubeCollision) {
								contactsOnThis = doNonBlockCollision(shapeA, shapeB,
										boxTransformA, boxTransformB,
										resultOut, dispatchInfo, v.elemPosA, v.elemPosB, typeA, typeB, data0.getSegmentController().getId(), data1.getSegmentController().getId());
							} else {
								contactsOnThis = doExtCubeCubeCollision(
										(BoxShape)shapeA, (BoxShape)shapeB,
										boxTransformA, boxTransformB,
										resultOut, dispatchInfo, v.elemPosA, v.elemPosB, 
										typeA, typeB, 
										data0.getSegmentController().getId(), data1.getSegmentController().getId());
							}

							if (contactsOnThis > 0) {

								lastContacts = resultOut.getPersistentManifold().getNumContacts();
								hitCount++;
								if (protectedByUndocking) {
									resultOut.getPersistentManifold().clearManifold();
									abort = true;
									return;
								}
							}
							//							if(wasStuck && this.currentMaxDepth > 0.65f){
							//								stuck = true;
							//							}
							if (!actStuck && !this.stuck && this.currentMaxDepth > 0.65f) {
								//
								//								stuckTests++;
								//
								//								if(stuckTests > 0 && stuckTests % 500 == 0){
								//									System.err.println("[CC] STUCK TEST #"+stuckTests);
								//									System.err.println("[CC] CHECKING STUCK "+data0.getSegment().getSegmentController().getPhysicsDataContainer().getObject()+" -> "+data1.getSegmentController().getPhysicsDataContainer().getObject()+": hits: "+hitCount+"; numC "+resultOut.getPersistentManifold().getNumContacts()+"; "+currentMaxDepth);
								//									System.err.println("[CC] CHECKING STUCK "+data1.getSegmentController().getPhysicsDataContainer().getObject()+" -> "+data0.getSegmentController().getPhysicsDataContainer().getObject()+": hits: "+hitCount+"; numC "+resultOut.getPersistentManifold().getNumContacts()+"; "+currentMaxDepth);
								//								}
								//
								//
								checkStuck(data0, data1, v.tmpTrans0, v.wtInv1, v.elemPosA);
								checkStuck(data1, data0, v.tmpTrans1, v.wtInv0, v.elemPosB);
								//
								//
							} else {
								if (stuckTests > 0) {
									stuckTests--;
								}
							}
						}
					}
				}
			}
		}

	}

	private void handleTrigger(SegmentData data0, CollisionObject obj, int Bx, int By, int Bz, short type) {
		if (data0.getSegmentController() instanceof ManagedSegmentController<?>) {
			ManagedSegmentController<?> m = (ManagedSegmentController<?>) data0.getSegmentController();
			if (m.getManagerContainer() instanceof TriggerManagerInterface) {
				TriggerManagerInterface tr = (TriggerManagerInterface) m.getManagerContainer();
				ManagerModuleCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> trigger = tr.getTrigger();
				long index = ElementCollection.getIndex(
						Bx + data0.getSegment().pos.x + SegmentData.SEG_HALF,
						By + data0.getSegment().pos.y + SegmentData.SEG_HALF,
						Bz + data0.getSegment().pos.z +SegmentData.SEG_HALF);
				for (TriggerCollectionManager c : trigger.getCollectionManagers()) {
					if(c.rawCollection.contains(index)){
						ActivationTrigger activationTrigger = new ActivationTrigger(c.getControllerIndex(), obj, type);
						ActivationTrigger ret = data0.getSegmentController().getTriggers().get(activationTrigger);

						if (ret == null) {
//								System.err.println("ADDING CubeCube TRIGGER: " + activationTrigger + "; " + data0.getSegmentController().getTriggers().size());
							data0.getSegmentController().getTriggers().add(activationTrigger);
						} else {
//								System.err.println("PING TRIGGER: " + activationTrigger + "; " + data0.getSegmentController().getTriggers().size());
							ret.ping();
						}
					}

				}
			}
		}
	}

	private int doNonBlockCollision(ConvexShape min0, ConvexShape min1,
	                                Transform tA, Transform tB, ManifoldResult resultOut,
	                                DispatcherInfo dispatchInfo, Vector3f elemPosA, Vector3f elemPosB, short typeA, short typeB, int idA, int idB) {
		ClosestPointInput input = pointInputsPool.get();
		input.init();

		resultOut.setPersistentManifold(manifoldPtr);

		// JAVA NOTE: original: TODO: if (dispatchInfo.m_useContinuous)
		gjkPairDetector.setMinkowskiA(min0);
		gjkPairDetector.setMinkowskiB(min1);

		input.maximumDistanceSquared = min0.getMargin() + min1.getMargin()
				+ manifoldPtr.getContactBreakingThreshold();
		input.maximumDistanceSquared *= input.maximumDistanceSquared;
		// input.m_stackAlloc = dispatchInfo.m_stackAllocator;

		// input.m_maximumDistanceSquared = btScalar(1e30);

		input.transformA.set(tA);
		input.transformB.set(tB);
		gjkPairDetector.getClosestPoints(input, resultOut,
				dispatchInfo.debugDraw, false, elemPosA, elemPosB, typeA, typeB, idA, idB);
		// throw new ErrorDialogException(e.getMessage());
		pointInputsPool.release(input);
		// #endif

		//		// schema mod
		//		if (ownManifold) {
		//			resultOut.refreshContactPoints();
		//		}
		this.currentMaxDepth = gjkPairDetector.maxDepth;
		return gjkPairDetector.contacts;
	}

	private void handleSweepAndPrune(CubeShape cubeShape0, CubeShape cubeShape1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		distinctTests = 0;
		distinctTime = 0;
		v.sweeper.fill(cubeShape0, v.tmpTrans0, cubeShape1, v.tmpTrans1, v.outerNonEmptySegments, v.innerNonEmptySegments);
		v.sweeper.getOverlapping();
		for (OverlappingSweepPair<Segment> p : v.sweeper.pairs) {
			//			OverlappingSweepPair p = v.sweeper.xPairs[v.sweeper.indices.getInt(i)];
			SweepPoint<Segment> a = p.a;
			SweepPoint<Segment> b = p.b;
			distinctTests++;
			
			if(old){
				v.bbOuterSeg.min.set(a.minX, a.minY, a.minZ);
				v.bbOuterSeg.max.set(a.maxX, a.maxY, a.maxZ);
	
				v.bbInnerSeg.min.set(b.minX, b.minY, b.minZ);
				v.bbInnerSeg.max.set(b.maxX, b.maxY, b.maxZ);
	
				v.bbOuterSeg.getIntersection(v.bbInnerSeg, v.bbSectorIntersection);
	
				
				long distinctTestTime = System.nanoTime();
				processDistinctCollisionOld(cubeShape0,
						cubeShape1, a.seg.getSegmentData(),
						b.seg.getSegmentData(), v.tmpTrans0,
						v.tmpTrans1, dispatchInfo, resultOut);
				distinctTime += (System.nanoTime() - distinctTestTime);
			}else{
				processDistinctCollision(cubeShape0,
						cubeShape1, a.seg.getSegmentData(),
						b.seg.getSegmentData(), v.tmpTrans0,
						v.tmpTrans1, dispatchInfo, resultOut);
			}
		
			
			
			
			
		}
		v.sweeper.pairs.clear();
		v.innerNonEmptySegments.clear();
		v.outerNonEmptySegments.clear();
		v.bbCacheInner.clear();
	}
	private void processDistinctCollision(SegmentAabbInterface cubeShape0,
			SegmentAabbInterface cubeShape1, SegmentData data0,
			SegmentData data1, Transform tmpTrans0, Transform tmpTrans1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {

		v.octreeTopLevelSweeper.abs0 = v.absolute1;
		v.octreeTopLevelSweeper.abs1 = v.absolute2;
		v.octreeTopLevelSweeper.seg0 = data0.getSegment();
		v.octreeTopLevelSweeper.set = v.oSet;
		v.simpleListA.max = 0;
		v.simpleListB.max = 0;
		v.octreeTopLevelSweeper.debug = false;
		v.octreeTopLevelSweeper.fill(
				data0.getSegment(), tmpTrans0,
				data1.getSegment(), tmpTrans1, 
				v.simpleListA, v.simpleListB);

		v.octreeTopLevelSweeper.getOverlapping();

		final int tpLvlSize = v.octreeTopLevelSweeper.pairs.size();
		for (int j = 0; j < tpLvlSize; j++) {
			OverlappingSweepPair<Integer> pTl = v.octreeTopLevelSweeper.pairs
					.get(j);
			
			
			SweepPoint<Integer> spa = pTl.a;
			SweepPoint<Integer> spb = pTl.b;

			handle16(cubeShape0, cubeShape1, data0, data1, tmpTrans0, tmpTrans1, spa, spb, dispatchInfo, resultOut);
		}
	}
	private void handle16(SegmentAabbInterface cubeShape0,
			SegmentAabbInterface cubeShape1, SegmentData data0,
			SegmentData data1, Transform tmpTrans0, Transform tmpTrans1, 
			SweepPoint<Integer> spa, SweepPoint<Integer> spb, 
			DispatcherInfo dispatchInfo, ManifoldResult resultOut){
		
		int aIndex = spa.seg;
		int bIndex = spb.seg;
		
		if (!v.intersectionCallBackAwithB.initialized) {
			v.intersectionCallBackAwithB
					.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
			v.intersectionCallBackBwithA
					.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
		}

		v.intersectionCallBackAwithB.reset();
		v.intersectionCallBackBwithA.reset();

		v.bbSeg16a.min.set(spa.minX, spa.minY, spa.minZ);
		v.bbSeg16a.max.set(spa.maxX, spa.maxY, spa.maxZ);

		v.bbSeg16b.min.set(spb.minX, spb.minY, spb.minZ);
		v.bbSeg16b.max.set(spb.maxX, spb.maxY, spb.maxZ);


		// find intersection of octree A with AABB of segment B
		v.intersectionCallBackAwithB = data0.getOctree()
				.findIntersectingAABBFromFirstLvl(aIndex, v.oSet,
						v.intersectionCallBackAwithB, data0.getSegment(),
						tmpTrans0, v.absolute1, 0.0f, v.bbSeg16b.min,
						v.bbSeg16b.max, 1.0f);

		// no hit in this octree, don't generate a new contact
		if (v.intersectionCallBackAwithB.hitCount == 0) {
			return;
		}
		v.intersectionCallBackBwithA.reset();

		v.oSet.debug = data1.getSegmentController().isOnServer();

		v.intersectionCallBackBwithA = data1.getOctree()
				.findIntersectingAABBFromFirstLvl(bIndex, v.oSet,
						v.intersectionCallBackBwithA, data1.getSegment(),
						tmpTrans1, v.absolute2, -0.001f, v.bbSeg16a.min,
						v.bbSeg16a.max, 1.0f);
		v.oSet.debug = false;
		if (v.intersectionCallBackBwithA.hitCount == 0) {
			return;
		}
		int count = 0;
		
		
		v.simpleListA.max = v.intersectionCallBackAwithB.hitCount;
		v.simpleListB.max = v.intersectionCallBackBwithA.hitCount;
		
		
		v.octreeSweeper.fill(v.intersectionCallBackAwithB, null,
				v.intersectionCallBackBwithA, null, v.simpleListA,
				v.simpleListB);

		v.octreeSweeper.getOverlapping();
		
		
		
		v.tmpTrans3.set(v.tmpTrans0);
		v.tmpTrans4.set(v.tmpTrans1);

		final int size = v.octreeSweeper.pairs.size();

		for (int i = 0; i < size; i++) {
			OverlappingSweepPair<Integer> p = v.octreeSweeper.pairs.get(i);

			SweepPoint<Integer> a = p.a;
			SweepPoint<Integer> b = p.b;

			int hitMaskA = v.intersectionCallBackAwithB.getHit(p.a.seg,
					v.bbOuterOct.min, v.bbOuterOct.max, v.startA, v.endA);
			
			int nodeIndexA = v.intersectionCallBackAwithB
					.getNodeIndexHit(p.a.seg);

			int hitMaskB = v.intersectionCallBackBwithA.getHit(p.b.seg,
					v.bbInnerOct.min, v.bbInnerOct.max, v.startB, v.endB);
			
			int nodeIndexB = v.intersectionCallBackBwithA
					.getNodeIndexHit(p.b.seg);


			count++;
			doNarrowTest(data0, data1, v.startA, v.endA, v.startB, v.endB,
					hitMaskA, hitMaskB, nodeIndexA, nodeIndexB,
					v.bbOctIntersection, dispatchInfo, resultOut);

		}
	}

	private void processDistinctCollisionOld(CubeShape cubeShape0,
	                                      CubeShape cubeShape1, SegmentData data0, SegmentData data1,
	                                      Transform tmpTrans1, Transform tmpTrans2,
	                                      DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
	
		if (stuck) {
	
			//we are already stuck. no need to waste CPU
			return;
		}
	
		if (!v.intersectionCallBackAwithB.initialized) {
			v.intersectionCallBackAwithB.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
			v.intersectionCallBackBwithA.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
		}
	
		//absolute AABB of trans1
		//		AabbUtil2.transformAabb(v.otherMinIn, v.otherMaxIn, margin, tmpTrans2, v.bMinOut, v.bMaxOut);
	
		v.intersectionCallBackAwithB.reset();
		v.intersectionCallBackBwithA.reset();
	
		//		long tI = System.nanoTime();
	
		//find intersection of octree A with AABB of segment B
		v.intersectionCallBackAwithB = data0.getOctree().findIntersectingAABB(v.oSet,
				v.intersectionCallBackAwithB, data0.getSegment(), tmpTrans1,
				v.absolute1, margin, v.bbSectorIntersection.min, v.bbSectorIntersection.max, 1.0f);
	
		// no hit in this octree, don't generate a new contact
		if (v.intersectionCallBackAwithB.hitCount == 0) {
			return;
		}
		v.intersectionCallBackBwithA.reset();
	
		v.oSet.debug = data1.getSegmentController().isOnServer();
		v.intersectionCallBackBwithA = data1.getOctree().findIntersectingAABB(v.oSet,
				v.intersectionCallBackBwithA, data1.getSegment(), tmpTrans2,
				v.absolute2, margin, v.bbSectorIntersection.min, v.bbSectorIntersection.max, 1.0f);
		v.oSet.debug = false;
		if (v.intersectionCallBackBwithA.hitCount == 0) {
			return;
		}
		// System.err.println("Hitcount A "+intersectionCallBackAwithB.hitCount);
		int count = 0;
		for (int hitsAIndex = 0; hitsAIndex < v.intersectionCallBackAwithB.hitCount; hitsAIndex++) {
			int hitMaskA = v.intersectionCallBackAwithB.getHit(hitsAIndex, v.bbOuterOct.min, v.bbOuterOct.max, v.startA, v.endA);
			int nodeIndexA = v.intersectionCallBackAwithB.getNodeIndexHit(hitsAIndex);
			//			AabbUtil2.transformAabb(v.bbOuterOct.min, v.bbOuterOct.max, 0, v.tmpTrans1, v.bbOuterOctTrans.min, v.bbOuterOctTrans.max);
	
			// reset for new test
	
			for (int hitsBIndex = 0; hitsBIndex < v.intersectionCallBackBwithA.hitCount; hitsBIndex++) {
	
				int hitMaskB = v.intersectionCallBackBwithA.getHit(hitsBIndex, v.bbInnerOct.min, v.bbInnerOct.max, v.startB, v.endB);
				int nodeIndexB = v.intersectionCallBackBwithA.getNodeIndexHit(hitsBIndex);
				if (AabbUtil2.testAabbAgainstAabb2(v.bbInnerOct.min, v.bbInnerOct.max, v.bbOuterOct.min, v.bbOuterOct.max)) {
					count++;
					v.bbInnerOct.getIntersection(v.bbOuterOct, v.bbOctIntersection);
					//					long t = System.nanoTime();
	
					doNarrowTest(data0, data1, v.startA, v.endA, v.startB, v.endB, hitMaskA, hitMaskB, nodeIndexA, nodeIndexB, v.bbOctIntersection, dispatchInfo, resultOut);
					if (abort) {
						return;
					}
					//					narrowTime += (System.nanoTime() - t) ;
				}
				// System.err.println("HIT REPORT: "+b+": "+hitMin+" - "+hitMax);
	
			}
		}
		//		octATreeTime += (System.nanoTime() - tI) ;
		// System.err.println("actual collision Octree: "+count);
		// if(count > 1000){
		// try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
		// }
		// System.err.println("found: "+intersectionCallBackAwithB.hasLeafHit+", "+intersectionCallBackAwithB.min+", "+intersectionCallBackAwithB.max);
	
		// distance (negative means penetration)
	
	}

	@Override
	public void init(CollisionAlgorithmConstructionInfo ci) {
		super.init(ci);
	}

	@Override
	public void destroy() {
		if (ownManifold) {
			if (manifoldPtr != null) {
				dispatcher.releaseManifold(manifoldPtr);
			}
			//only release if this manifold is not shared
		}
		manifoldPtr = null;
	}

	@Override
	public void processCollision(CollisionObject col0, CollisionObject col1,
	                             DispatcherInfo dispatchInfo, ManifoldResult resultOut) {

		long time = System.currentTimeMillis();
		abort = false;
		stuck = false;
		hitCount = 0;
		narrowTests = 0;
		actStuck = false;
		proxVec.set(0,0,0);
		if (!(col0.getCollisionShape() instanceof CubeShape)
				|| !(col1.getCollisionShape() instanceof CubeShape)) {
			return;
		}

		assert (!(col0 instanceof RigidBodySegmentController) ||!((RigidBodySegmentController) col0).isCollisionException()) : col0;
		assert (!(col1 instanceof RigidBodySegmentController) || !((RigidBodySegmentController) col1).isCollisionException()) : col1;

		if (col0.isStaticObject() && col1.isStaticObject()) {
			return;
		}

		if (localSwap) {
			System.err.println("LOCAL SWAP IN CUBE CUBE");
			CollisionObject tmp = col0;
			col0 = col1;
			col1 = tmp;
		}

		this.currentTime = System.currentTimeMillis();
		CubeShape cubeShape0 = (CubeShape) col0.getCollisionShape();
		CubeShape cubeShape1 = (CubeShape) col1.getCollisionShape();


		if (wasStuck && this.stuckUN == cubeShape1.getSegmentBuffer().getSegmentController().getState().getNumberOfUpdate()) {
			return;
		}

		assert (cubeShape0.getSegmentBuffer().getSegmentController() != cubeShape1.getSegmentBuffer().getSegmentController()) :
				((cubeShape1.getSegmentBuffer().getSegmentController().isOnServer() ? "[SERVER]" : "[CLIENT]") + "[CUBECUBE]\n" +
						"on\n" +
						"" + cubeShape0.getSegmentBuffer().getSegmentController() + ": ANonECOUNT " + cubeShape0.getSegmentBuffer().getTotalNonEmptySize() + "; ATOTAl " + cubeShape0.getSegmentBuffer().getSegmentController().getTotalElements() + ";\n" +
						"" + cubeShape1.getSegmentBuffer().getSegmentController() + ": BNonECOUNT " + cubeShape1.getSegmentBuffer().getTotalNonEmptySize() + "; BTOTAl " + cubeShape1.getSegmentBuffer().getSegmentController().getTotalElements() + "\n"
						+ "\n\n" + col0 + "\n <-> \n" + col1 + "\n");

		if (cubeShape0.getSegmentBuffer().getSegmentController().stuckFrom != null || cubeShape1.getSegmentBuffer().getSegmentController().stuckFrom != null) {

			if (!(cubeShape0.getSegmentBuffer().getSegmentController().stuckFrom == col1)) {
				long currentTimeMillis = System.currentTimeMillis();
				if (currentTimeMillis - cubeShape0.getSegmentBuffer().getSegmentController().stuckFromTime < STUCK_TIME) {
					//					System.err.println("OMMITING COLLISION "+cubeShape0+": "+cubeShape1);
					return;
				}

			} else if (!(cubeShape1.getSegmentBuffer().getSegmentController().stuckFrom == col0)) {
				long currentTimeMillis = System.currentTimeMillis();
				if (currentTimeMillis - cubeShape1.getSegmentBuffer().getSegmentController().stuckFromTime < STUCK_TIME) {
					//					System.err.println("OMMITING COLLISION "+cubeShape0+": "+cubeShape1);
					return;
				}
			}
		}

		cubeShape1.getSegmentBuffer().getSegmentController().stuckFrom = null;

		if (cubeShape0 == cubeShape1) {
			System.err.println("[PHYSICS][CUBECUBE] WARNING!! EQUALCOL " + cubeShape0.getSegmentBuffer().getSegmentController());
			return;
		}

		if (cubeShape0.getSegmentBuffer().getSegmentController().isOnServer()) {
			GameServerState state = (GameServerState) cubeShape0.getSegmentBuffer().getSegmentController().getState();
			if (state.getUniverse().getSector(cubeShape0.getSegmentBuffer().getSegmentController().getSectorId()) == null) {
				throw new SectorNotFoundRuntimeException(cubeShape0.getSegmentBuffer().getSegmentController().getSectorId());
			}
			if (state.getUniverse().getSector(cubeShape1.getSegmentBuffer().getSegmentController().getSectorId()) == null) {
				throw new SectorNotFoundRuntimeException(cubeShape1.getSegmentBuffer().getSegmentController().getSectorId());
			}
		}

		v.oSet = ArrayOctree.getSet(cubeShape0.getSegmentBuffer().getSegmentController().isOnServer());

		v.col0 = col0;
		v.col1 = col1;

		v.tmpTrans0 = col0.getWorldTransform(v.tmpTrans0);
		v.tmpTrans1 = col1.getWorldTransform(v.tmpTrans1);

//		if (cubeShape0.getSegmentController().isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
//			Transform t = new Transform();
//			t.setIdentity();
//			col0.getCollisionShape().getAabb(t, v.minOut, v.maxOut);
//
//			DebugDrawer.boxes.add(new DebugBox(new Vector3f(v.minOut.x, v.minOut.y, v.minOut.z),
//					new Vector3f(v.maxOut.x, v.maxOut.y, v.maxOut.z), new Transform(v.tmpTrans0), 0, 1, 1, 1));
//
//			col1.getCollisionShape().getAabb(t, v.minOut, v.maxOut);
//
//			DebugDrawer.boxes.add(new DebugBox(new Vector3f(v.minOut.x, v.minOut.y, v.minOut.z),
//					new Vector3f(v.maxOut.x, v.maxOut.y, v.maxOut.z), new Transform(v.tmpTrans1), 1, 0, 1, 1));
//		}
		boolean eA = v.tmpTrans0.origin.equals(t0.origin) && v.tmpTrans0.basis.epsilonEquals(t0.basis, 1.19209290e-06f);
		boolean eB = v.tmpTrans1.origin.equals(t1.origin) && v.tmpTrans1.basis.epsilonEquals(t1.basis, 1.19209290e-06f);
		boolean mA = cubeShape0.getSegmentBuffer().getSegmentController().getMass() == t0Mass;
		boolean mB = cubeShape1.getSegmentBuffer().getSegmentController().getMass() == t1Mass;
		if (eA && eB && mA && mB) {
//			if(cubeShape0.getSegmentController().isOnServer()){
//				System.err.println("NO COLLISION BET "+cubeShape0.getSegmentController()+" and "+cubeShape1.getSegmentController());
//			}
			//WARNING THIS MUST BE TESTED!!!!!!!!!!
			//no reason to do collision
			return;
		}

		v.absolute1.set(v.tmpTrans0.basis);
		MatrixUtil.absolute(v.absolute1);
		v.absolute2.set(v.tmpTrans1.basis);
		MatrixUtil.absolute(v.absolute2);

		//		cubeShape0.getAabb(v.tmpTrans1, v.outMin, v.outMax);
		//		cubeShape1.getAabb(v.tmpTrans2, v.inMin, v.inMax);

		int distiTests = 0;
		int preSynchTime = 0;
		//		if(AabbUtil2.testAabbAgainstAabb2(v.outMin, v.outMax, v.inMin, v.inMax)){

		
		long distinctTime = 0;

		cubeShape0.getAabb(v.tmpTrans0, v.outerWorld.min, v.outerWorld.max);
		cubeShape1.getAabb(v.tmpTrans1, v.innerWorld.min, v.innerWorld.max);

		v.box0.setMargin(margin);
		v.box1.setMargin(margin);
		cubeShape0.setMargin(margin);
		cubeShape1.setMargin(margin);

		this.t0.set(v.tmpTrans0);
		this.t1.set(v.tmpTrans1);
		this.t0Mass = cubeShape0.getSegmentBuffer().getSegmentController().getMass();
		this.t1Mass = cubeShape1.getSegmentBuffer().getSegmentController().getMass();

		col0.getWorldTransform(v.wtInv0);
		v.wtInv0.inverse();

		col1.getWorldTransform(v.wtInv1);
		v.wtInv1.inverse();

		if (!doAABBIntersection(cubeShape0, cubeShape1)) {
			if (ownManifold) {
				resultOut.refreshContactPoints();
			}
			return;
		}
		
		
		int shouldBeCountA = Math.abs(ByteUtil.divSeg(v.maxIntA.x) - ByteUtil.divSeg(v.minIntA.x));
		shouldBeCountA *= Math.abs(ByteUtil.divSeg(v.maxIntA.y) - ByteUtil.divSeg(v.minIntA.y));
		shouldBeCountA *= Math.abs(ByteUtil.divSeg(v.maxIntA.z) - ByteUtil.divSeg(v.minIntA.z));

		int shouldBeCountB = Math.abs(ByteUtil.divSeg(v.maxIntB.x) - ByteUtil.divSeg(v.minIntB.x));
		shouldBeCountB *= Math.abs(ByteUtil.divSeg(v.maxIntB.y) - ByteUtil.divSeg(v.minIntB.y));
		shouldBeCountB *= Math.abs(ByteUtil.divSeg(v.maxIntB.z) - ByteUtil.divSeg(v.minIntB.z));

		assert (v.minIntA.x <= v.maxIntA.x);
		assert (v.minIntA.y <= v.maxIntA.y);
		assert (v.minIntA.z <= v.maxIntA.z);
		assert (v.minIntB.x <= v.maxIntB.x);
		assert (v.minIntB.y <= v.maxIntB.y);
		assert (v.minIntB.z <= v.maxIntB.z);

		int sizeXA = v.maxIntA.x - v.minIntA.x;
		int sizeYA = v.maxIntA.y - v.minIntA.y;
		int sizeZA = v.maxIntA.z - v.minIntA.z;

		int sizeXB = v.maxIntB.x - v.minIntB.x;
		int sizeYB = v.maxIntB.y - v.minIntB.y;
		int sizeZB = v.maxIntB.z - v.minIntB.z;
		//		System.err.println("CUBE CUBE INTERSECTION "+minInt+"    "+maxInt);
		long t = System.currentTimeMillis();

		assert (v.innerNonEmptySegments.isEmpty());
		assert (v.outerNonEmptySegments.isEmpty());
		assert (v.bbCacheInner.isEmpty());

		long lt = System.currentTimeMillis();
		long assembleA = 0;
		long assembleB = 0;
		long hanlde = 0;
		try {
			((SegmentBufferManager) cubeShape0.getSegmentBuffer()).lastIteration = 0;
			((SegmentBufferManager) cubeShape1.getSegmentBuffer()).lastIteration = 0;
			((SegmentBufferManager) cubeShape0.getSegmentBuffer()).lastDoneIteration = 0;
			((SegmentBufferManager) cubeShape1.getSegmentBuffer()).lastDoneIteration = 0;

			if (sizeXA > 5000 || sizeYA > 5000 || sizeYA > 5000) {
				System.err.println("Exception collision overlap to big " + sizeXA + "; " + sizeYA + "; " + sizeZA + "; for: " + cubeShape0.getSegmentBuffer().getSegmentController() + "; boundingBox: " + cubeShape0.getSegmentBuffer().getBoundingBox());
				return;
			}
			if (sizeXB > 5000 || sizeYB > 5000 || sizeYB > 5000) {
				System.err.println("Exception collision overlap to big " + sizeXB + "; " + sizeYB + "; " + sizeZB + "; for: " + cubeShape0.getSegmentBuffer().getSegmentController() + "; boundingBox: " + cubeShape0.getSegmentBuffer().getBoundingBox());
				return;
			}
			
			v.innerMinBlock.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
			v.innerMaxBlock.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
			
			v.outerMinBlock.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
			v.outerMaxBlock.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
			
			
			long tmA = System.currentTimeMillis();
			cubeShape0.getSegmentBuffer().iterateOverNonEmptyElementRange(outerHandler, v.minIntA, v.maxIntA, false);
			assembleA = System.currentTimeMillis() - tmA;
			long tmB = System.currentTimeMillis();
			cubeShape1.getSegmentBuffer().iterateOverNonEmptyElementRange(innerHandler, v.minIntB, v.maxIntB, false);
			assembleB = System.currentTimeMillis() - tmB;

			
			
			
			long tmH = System.currentTimeMillis();
//			if (GameClientController.isStarted() && Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_F7)) {
//				handleBruteForce(cubeShape0, cubeShape1, dispatchInfo, resultOut);
//			} else {
				handleSweepAndPrune(cubeShape0, cubeShape1, dispatchInfo, resultOut);
//			}
			hanlde = System.currentTimeMillis() - tmH;
		} catch (Exception e) {
			e.printStackTrace();
			v.innerNonEmptySegments.clear();
			v.outerNonEmptySegments.clear();
			v.bbCacheInner.clear();
		}
		long iterationTime = System.currentTimeMillis() - lt;

		distiTests = this.distinctTests;
		distinctTime = this.distinctTime;

		if(narrowTests > 0){
			proxVec.negate();
			proxVec.scale(1f / narrowTests);
			cubeShape0.getSegmentBuffer().getSegmentController().proximityVector.add(proxVec);
			cubeShape0.getSegmentBuffer().getSegmentController().onProximity(cubeShape1.getSegmentBuffer().getSegmentController());
			proxVec.negate();
			cubeShape1.getSegmentBuffer().getSegmentController().onProximity(cubeShape0.getSegmentBuffer().getSegmentController());
			cubeShape1.getSegmentBuffer().getSegmentController().proximityVector.add(proxVec);
		}
		
		String a = cubeShape0.getSegmentBuffer().getSegmentController() + "->-" + cubeShape0.getSegmentBuffer().getSegmentController().getTotalElements();
		String b = cubeShape1.getSegmentBuffer().getSegmentController() + "->#" + cubeShape1.getSegmentBuffer().getSegmentController().getTotalElements();
		int took = (int) (System.currentTimeMillis() - t);

		if (stuck) {
			StateInterface state = cubeShape0.getSegmentBuffer().getSegmentController().getState();
			solveStuck(col0, cubeShape0, col1, cubeShape1);

		}
//		if (took > 25 || (GameClientController.started && Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_HOME))) {
//			String size = "[(" + sizeXA + ", " + sizeYA + ", " + sizeZA + ")" + "(" + sizeXB + ", " + sizeYB + ", " + sizeZB + ")]";
//			String l = cubeShape1.getSegmentBuffer().getSegmentController().isOnServer() ? "[SERVER]" : "[CLIENT]";
//			System.err.println(l + "[CUBECUBE] COL: " + took + " ms; !!!DistiTests[AABBvsAABB]: " + distiTests + "!!!; " +
//					"); on" +
//					" " + cubeShape0.getSegmentBuffer().getSegmentController() + ": ANonECOUNT " + cubeShape0.getSegmentBuffer().getTotalNonEmptySize() + "; ATOTAl " + cubeShape0.getSegmentBuffer().getSegmentController().getTotalElements() + "; " +
//					"" + cubeShape1.getSegmentBuffer().getSegmentController() + ": BNonECOUNT " + cubeShape1.getSegmentBuffer().getTotalNonEmptySize() + "; BTOTAl " + cubeShape1.getSegmentBuffer().getSegmentController().getTotalElements());
//			System.err.println("[CC]INFO#0: " + col0 + ": " + col0.getWorldTransform(new Transform()).origin + "; " + col1 + ": " + col1.getWorldTransform(new Transform()).origin + " UPDATE#: " + cubeShape0.getSegmentBuffer().getSegmentController().getState().getUpdateNumber());
//			System.err.println("[CC]INFO#1: AABB-A: " + v.outerWorld + "; AABB-B: " + v.innerWorld + "; Intersection for A [" + v.minIntA + "; " + v.maxIntA + "]; B [" + v.minIntB + "; " + v.maxIntB + "]");
//			System.err.println("[CC]INFO#2: " + size + "; ItA: " +
//					((SegmentBufferManager) cubeShape0.getSegmentBuffer()).lastDoneIteration + " / " + ((SegmentBufferManager) cubeShape0.getSegmentBuffer()).lastIteration + "; ItB " + ((SegmentBufferManager) cubeShape1.getSegmentBuffer()).lastDoneIteration + " / " + ((SegmentBufferManager) cubeShape1.getSegmentBuffer()).lastIteration);
//			System.err.println("[CC]INFO#3: TotalA: " + ((SegmentBufferManager) cubeShape0.getSegmentBuffer()).getSegmentController().getTotalElements() + "; TotalB " + ((SegmentBufferManager) cubeShape1.getSegmentBuffer()).getSegmentController().getTotalElements());
//			System.err.println("[CC]INFO#4: BufA: " + ((SegmentBufferManager) cubeShape0.getSegmentBuffer()).getBuffer().size() + "; BufB: " + ((SegmentBufferManager) cubeShape1.getSegmentBuffer()).getBuffer().size());
//			System.err.println("[CC]INFO#5: overlappingSingleBlockAABBs: " + aabbBlockOverlapping + "; handle: " + handlesHandled + "/" + handles + " of MAX " + (shouldBeCountA * shouldBeCountB) + "; " +
//					"DistiT: " + distinctTime / 1000000 + "; of that: Sweep: " + sweepTime + "ms" +
//					"NarrT: " + narrowTime / 1000000 + "; OctT: " + octATreeTime / 1000000 + "retrT(" + leafRetrieve / 1000000 + ") " +
//					"aabbT(" + leafAABBTest / 1000000 + ")" + "; asmAT: " + assembleA + " ms; asmBT: " + assembleB + " ms; TotalHandleT(): " + hanlde + " ms;;; leafCalls#(" + leaftCallsA + ")");
//		}

		this.wasStuck = stuck;
		if (!stuck) {
			cubeShape0.getSegmentBuffer().getSegmentController().stuckFrom = null;
			cubeShape1.getSegmentBuffer().getSegmentController().stuckFrom = null;
		}
		// schema mod
		if (ownManifold) {
			resultOut.refreshContactPoints();
		}
		v.col0 = null;
		v.col1 = null;

		if (protectedByUndocking && hitCount == 0) {
			//no more collision with mother ship
			((RigidBodySegmentController) col0).undockingProtection = null;
			((RigidBodySegmentController) col1).undockingProtection = null;
		}

		//
	}


	@Override
	public float calculateTimeOfImpact(CollisionObject body0,
	                                   CollisionObject body1, DispatcherInfo dispatchInfo,
	                                   ManifoldResult resultOut) {
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
	                 GjkEpaPenetrationDepthSolverExt pdSolver) {
		super.init(ci);
		v = threadLocal.get();

		t0.setIdentity();
		t1.setIdentity();
		t0Mass = 0;
		t1Mass = 0;
		gjkPairDetector = new GjkPairDetectorExt(v.gjkVar);
		//		outerSegmentHandler = new OuterSegmentHandler();
		//		innerSegmentHandler = new InnerSegmentHandler();

		outerHandler = new OuterSegmentAddHandler();
		innerHandler = new InnerSegmentAddHandler();

		manifoldPtr = mf;
		gjkPairDetector.init(null, null, simplexSolver, pdSolver);
		if (manifoldPtr == null) {
			manifoldPtr = dispatcher.getNewManifold(col0, col1);
			ownManifold = true;
		} else {
			ownManifold = false;
		}
	}

	private void solveStuck(CollisionObject col0, CubeShape cubeShape0, CollisionObject col1, CubeShape cubeShape1) {

		if (!(col0.isStaticOrKinematicObject() && col1.isStaticOrKinematicObject())) {

			//only force out if at least one object is a dynamic object

			if (col0.isStaticOrKinematicObject()) {
				//			System.err.println("[CC] "+state+" Objects detected stuck --- applying outforce: "+cubeShape0.getSegmentBuffer().getSegmentController()+" AND "+cubeShape1.getSegmentBuffer().getSegmentController());
				applyOutforce(col1, cubeShape1, col0, cubeShape0);
			} else if (col1.isStaticOrKinematicObject()) {
				//			System.err.println("[CC] "+state+" Objects detected stuck --- applying outforce: "+cubeShape0.getSegmentBuffer().getSegmentController()+" AND "+cubeShape1.getSegmentBuffer().getSegmentController());
				applyOutforce(col0, cubeShape0, col1, cubeShape1);
			} else {

				applyOutforce(col0, cubeShape0, col1, cubeShape1);
				applyOutforce(col1, cubeShape1, col0, cubeShape0);
			}

		}
	}

	private void applyOutforce(CollisionObject forcedObj, CubeShape forcedShape,
	                           CollisionObject fromObj, CubeShape fromShape) {

		forcedShape.getSegmentBuffer().getSegmentController().stuckFrom = fromObj;

		forcedShape.getSegmentBuffer().getSegmentController().stuckFromTime = System.currentTimeMillis();

		if(forcedShape.getSegmentBuffer().getSegmentController() instanceof FloatingRock || 
				forcedShape.getSegmentBuffer().getSegmentController() instanceof Ship){
			forcedShape.getSegmentBuffer().getSegmentController().onStuck();
		}else if(fromShape.getSegmentBuffer().getSegmentController() instanceof FloatingRock ||
				fromShape.getSegmentBuffer().getSegmentController() instanceof Ship){
			fromShape.getSegmentBuffer().getSegmentController().onStuck();
		}
		
		StateInterface state = forcedShape.getSegmentBuffer().getSegmentController().getState();

		Transform fromPos = new Transform();
		if (fromShape.getSegmentBuffer().getSegmentController().getDockingController().isDocked()) {
			SegmentController segmentController = fromShape.getSegmentBuffer().getSegmentController().getDockingController().getDockedOn().to
					.getSegment().getSegmentController();

			if (segmentController.getDockingController().isDocked()) {
				SegmentController segmentController2 = segmentController.getDockingController().getDockedOn().to
						.getSegment().getSegmentController();
				fromPos.set(segmentController2.getWorldTransform());
			} else {
				fromPos.set(segmentController.getWorldTransform());
			}
		} else {

			fromPos.set(fromShape.getSegmentBuffer().getSegmentController().getWorldTransform());
		}

		Transform forcedPos = new Transform();
		if (forcedShape.getSegmentBuffer().getSegmentController().getDockingController().isDocked()) {
			SegmentController segmentController = forcedShape.getSegmentBuffer().getSegmentController().getDockingController().getDockedOn().to
					.getSegment().getSegmentController();

			if (segmentController.getDockingController().isDocked()) {
				SegmentController segmentController2 = segmentController.getDockingController().getDockedOn().to
						.getSegment().getSegmentController();
				forcedPos.set(segmentController2.getWorldTransform());
			} else {
				forcedPos.set(segmentController.getWorldTransform());
			}
		} else {

			forcedPos.set(forcedShape.getSegmentBuffer().getSegmentController().getWorldTransform());
		}

		Vector3f dir = new Vector3f();
		dir.sub(forcedPos.origin, fromPos.origin);

		//		Vector3f aabbMin = new Vector3f();
		//
		//		Vector3f aabbMax = new Vector3f();
		//
		//		fromShape.getAabb(fromPos, aabbMin, aabbMax);
		//
		//		BoundingBox b = new BoundingBox(aabbMin, aabbMax);
		//
		//		Vector3f closest = new Vector3f(0,0,0);
		//
		//		if(b.isInside(forcedPos.origin)){
		//			b.getClosestPoint(forcedPos.origin, closest);
		//
		//			closest.sub(forcedPos.origin);
		//		}
		//		closest.scale(100);
		//		dir.add(closest);

		if (Float.compare(dir.lengthSquared(), 0f) != 0) {
			float length = dir.length();

			dir.normalize();

			//			((RigidBodyExt)forcedObj).applyCentralImpulse(dir);
		} else {
			dir.set(new Vector3f(0, 1, 0));
			//			((RigidBodyExt)forcedObj).applyCentralImpulse(new Vector3f(0,1,0));
		}
		dir.scale(1);
		forcedPos.origin.add(dir);
		System.err.println("[CC] " + state + " Objects detected stuck --- applying outforce: " + forcedShape.getSegmentBuffer().getSegmentController() + " from " + fromShape.getSegmentBuffer().getSegmentController() + " DIR: " + dir);

		Vector3f linearVelocity = ((RigidBodySegmentController) forcedObj).getLinearVelocity(new Vector3f());
		linearVelocity.add(dir);
		((RigidBodySegmentController) forcedObj).setLinearVelocity(linearVelocity);

		for (int i = 0; i < manifoldPtr.getNumContacts(); i++) {
			ManifoldPoint contactPoint = manifoldPtr.getContactPoint(i);
			contactPoint.distance1 = 100;
			if (manifoldPtr.getBody0() == forcedObj) {
				contactPoint.normalWorldOnB.set(dir);
			} else {
				dir.negate();
				contactPoint.normalWorldOnB.set(dir);
			}
		}

		//		((RigidBodyExt)forcedObj).getMotionState().setWorldTransform(forcedPos);
		//		((RigidBodyExt)forcedObj).setWorldTransform(forcedPos);

	}

	private boolean doAABBIntersection(CubeShape cubeShape0,
	                                   CubeShape cubeShape1) {
		{
			//do for A (0)

			v.tmpAABBTrans0.set(v.wtInv0);
			v.tmpAABBTrans0.mul(v.tmpTrans1);

			cubeShape1.getAabb(v.tmpAABBTrans0, v.innerAABBforA.min, v.innerAABBforA.max);
			//the bounding box of col1 is now in space of a and aligned to col0.
			//That means we can do an indersection with our local bounding box
			cubeShape0.getAabb(TransformTools.ident, v.intersection.min, v.intersection.max);

			BoundingBox intersectionAB = v.intersection.getIntersection(v.innerAABBforA, v.intersectionInASpaceWithB);

			if (intersectionAB == null) {
				//no intersection
				return false;
			}
			v.minIntA.x = ByteUtil.divSeg((int) (intersectionAB.min.x - SegmentData.SEG_HALF)) * SegmentData.SEG;
			v.minIntA.y = ByteUtil.divSeg((int) (intersectionAB.min.y - SegmentData.SEG_HALF)) * SegmentData.SEG;
			v.minIntA.z = ByteUtil.divSeg((int) (intersectionAB.min.z - SegmentData.SEG_HALF)) * SegmentData.SEG;

			v.maxIntA.x = (FastMath.fastCeil((intersectionAB.max.x + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntA.y = (FastMath.fastCeil((intersectionAB.max.y + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntA.z = (FastMath.fastCeil((intersectionAB.max.z + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;

//			if(cubeShape0.getSegmentController().isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
//
//						DebugDrawer.boxes.add(new DebugBox(new Vector3f(intersectionAB.min),
//								new Vector3f(intersectionAB.max),new Transform(v.tmpTrans0),1,0,0,1));
//
//						DebugDrawer.boxes.add(new DebugBox(new Vector3f(v.minIntA.x-SegmentData.SEG_HALF, v.minIntA.y-SegmentData.SEG_HALF, v.minIntA.z-SegmentData.SEG_HALF),
//								new Vector3f(v.maxIntA.x-SegmentData.SEG_HALF, v.maxIntA.y-SegmentData.SEG_HALF, v.maxIntA.z-SegmentData.SEG_HALF),new Transform(v.tmpTrans0),1,1,1,1));
//			}
		}
		{
			//do for B (1)
			v.tmpAABBTrans1.set(v.wtInv1);
			v.tmpAABBTrans1.mul(v.tmpTrans0);

			cubeShape0.getAabb(v.tmpAABBTrans1, v.outerAABBforB.min, v.outerAABBforB.max);
			//the bounding box of col1 is now in space of a and aligned to col0.
			//That means we can do an indersection with our local bounding box
			cubeShape1.getAabb(TransformTools.ident, v.intersection.min, v.intersection.max);

			BoundingBox intersectionBA = v.intersection.getIntersection(v.outerAABBforB, v.intersectionInBSpaceWithA);

			if (intersectionBA == null) {
				//no intersection
				return false;
			}
			v.minIntB.x = ByteUtil.divSeg((int) (intersectionBA.min.x - SegmentData.SEG_HALF)) * SegmentData.SEG;
			v.minIntB.y = ByteUtil.divSeg((int) (intersectionBA.min.y - SegmentData.SEG_HALF)) * SegmentData.SEG;
			v.minIntB.z = ByteUtil.divSeg((int) (intersectionBA.min.z - SegmentData.SEG_HALF)) * SegmentData.SEG;

			v.maxIntB.x = (FastMath.fastCeil((intersectionBA.max.x + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntB.y = (FastMath.fastCeil((intersectionBA.max.y + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntB.z = (FastMath.fastCeil((intersectionBA.max.z + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
//						if(cubeShape0.getSegmentController().isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
//						DebugDrawer.boxes.add(new DebugBox(new Vector3f(intersectionBA.min),
//								new Vector3f(intersectionBA.max),new Transform(v.tmpTrans1),1,0,0,1));
//
//						DebugDrawer.boxes.add(new DebugBox(new Vector3f(v.minIntB.x-8, v.minIntB.y-8, v.minIntB.z-8),
//								new Vector3f(v.maxIntB.x-8, v.maxIntB.y-8, v.maxIntB.z-8),new Transform(v.tmpTrans1),1,1,0,1));
//						}
		}

		return true;
	}

	public static class CreateFunc extends CollisionAlgorithmCreateFunc {

		private final ObjectPool<CubeCubeCollisionAlgorithm> pool = ObjectPool
				.get(CubeCubeCollisionAlgorithm.class);

		public SimplexSolverInterface simplexSolver;
		public GjkEpaPenetrationDepthSolverExt pdSolver;

		public CreateFunc(SimplexSolverInterface simplexSolver,
		                  GjkEpaPenetrationDepthSolverExt pdSolver) {
			this.simplexSolver = simplexSolver;
			this.pdSolver = pdSolver;
		}

		@Override
		public CollisionAlgorithm createCollisionAlgorithm(
				CollisionAlgorithmConstructionInfo ci, CollisionObject body0,
				CollisionObject body1) {
			CubeCubeCollisionAlgorithm algo = pool.get();
			algo.init(ci.manifold, ci, body0, body1, simplexSolver, pdSolver);
			return algo;
		}

		@Override
		public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
			((CubeCubeCollisionAlgorithm) algo).cleanUpDispatcher();
			pool.release((CubeCubeCollisionAlgorithm) algo);
//			pool.cleanOut(4096*2);
		}
	}

	private class InnerSegmentAddHandler implements SegmentBufferIteratorInterface {

		@Override
		public boolean handle(Segment s, long lastChanged) {
			if (s.getSegmentData() == null || s.isEmpty()) {
				return true;
			}
			v.innerNonEmptySegments.add(s);
			
			v.innerMaxBlock.x = Math.max(v.innerMaxBlock.x, s.getSegmentData().getMax().x + s.pos.x);
			v.innerMaxBlock.y = Math.max(v.innerMaxBlock.y, s.getSegmentData().getMax().y + s.pos.y);
			v.innerMaxBlock.z = Math.max(v.innerMaxBlock.z, s.getSegmentData().getMax().z + s.pos.z);
			
			v.innerMinBlock.x = Math.min(v.innerMinBlock.x, s.getSegmentData().getMin().x + s.pos.x);
			v.innerMinBlock.y = Math.min(v.innerMinBlock.y, s.getSegmentData().getMin().y + s.pos.y);
			v.innerMinBlock.z = Math.min(v.innerMinBlock.z, s.getSegmentData().getMin().z + s.pos.z);
			
			return true;
		}

	}

	private class OuterSegmentAddHandler implements SegmentBufferIteratorInterface {

		@Override
		public boolean handle(Segment s, long lastChanged) {
			if (s.getSegmentData() == null || s.isEmpty()) {
				return true;
			}
			v.outerNonEmptySegments.add(s);
			
			v.outerMaxBlock.x = Math.max(v.outerMaxBlock.x, s.getSegmentData().getMax().x + s.pos.x);
			v.outerMaxBlock.y = Math.max(v.outerMaxBlock.y, s.getSegmentData().getMax().y + s.pos.y);
			v.outerMaxBlock.z = Math.max(v.outerMaxBlock.z, s.getSegmentData().getMax().z + s.pos.z);
			
			v.outerMinBlock.x = Math.min(v.outerMinBlock.x, s.getSegmentData().getMin().x + s.pos.x);
			v.outerMinBlock.y = Math.min(v.outerMinBlock.y, s.getSegmentData().getMin().y + s.pos.y);
			v.outerMinBlock.z = Math.min(v.outerMinBlock.z, s.getSegmentData().getMin().z + s.pos.z);
			
			return true;
		}

	}
	
	// //////////////////////////////////////////////////////////////////////////

	//	private void processDocked(Collection<ElementDocking> docks, CollisionObject col,
	//			DispatcherInfo dispatchInfo, ManifoldResult resultOut){
	//
	//		for(ElementDocking d : docks){
	//			if(d.from.getSegment().getSegmentController() != ((CubeShape)col.getCollisionShape()).getSegmentBuffer().getSegmentController()){
	//				dockedCol = true;
	//				System.err.println("PROCESSING DOCKED");
	//				processCollision(d.from.getSegment().getSegmentController().getPhysicsDataContainer().getObject(), col, dispatchInfo, resultOut);
	//				dockedCol = false;
	//			}
	//		}
	//	};

}
