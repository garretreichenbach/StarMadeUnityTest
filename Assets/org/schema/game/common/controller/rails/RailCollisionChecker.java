package org.schema.game.common.controller.rails;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.common.controller.SegmentBufferIteratorInterface;
import org.schema.game.common.controller.SegmentBufferManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.BoxBoxDetector;
import org.schema.game.common.data.physics.CubeShape;
import org.schema.game.common.data.physics.GjkPairDetectorExt;
import org.schema.game.common.data.physics.ObjectPool;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.physics.octree.ArrayOctreeTraverse;
import org.schema.game.common.data.physics.sweepandpruneaabb.OverlappingSweepPair;
import org.schema.game.common.data.physics.sweepandpruneaabb.SegmentAabbInterface;
import org.schema.game.common.data.physics.sweepandpruneaabb.SweepPoint;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RailCollisionChecker {

	private static ThreadLocal<RailCollisionVars> threadLocal = new ThreadLocal<RailCollisionVars>() {
		@Override
		protected RailCollisionVars initialValue() {
			return new RailCollisionVars();
		}
	};
	protected final ObjectPool<ClosestPointInput> pointInputsPool = ObjectPool
			.get(ClosestPointInput.class);
	private final List<SegmentController> testFrom = new ObjectArrayList<SegmentController>();
	private final List<SegmentController> testTo = new ObjectArrayList<SegmentController>();
	private final RailCollisionVars v;
	private final InnerSegmentAddHandler innerHandler = new InnerSegmentAddHandler();
	private final OuterSegmentAddHandler outerHandler = new OuterSegmentAddHandler();
	private final GjkPairDetectorExt gjkPairDetector;
	ManifoldResult resultOut = new ManifoldResult();
	private SegmentController mainTestDocked;
	private boolean hasCollision;
	private boolean detailedTest;
	private ObjectArrayList<String> log = new ObjectArrayList<String>();
	private BoxBoxDetector boxboxPairDetector = new BoxBoxDetector();
	private long lastTimeDebug;
	private boolean rTest;
	private boolean uTest;
	private boolean fTest;

	private HashMap<Integer, HashMap<Integer, AtomicBoolean>> collisionRequest = new HashMap<>();
	private HashMap<Integer, HashMap<Integer, AtomicBoolean>> collisionResults = new HashMap<>();

	public RailCollisionChecker() {
		v = threadLocal.get();
		boxboxPairDetector.fastContactOnly = true;
		gjkPairDetector = new GjkPairDetectorExt(v.gjkVariables);
		gjkPairDetector.useExtraPenetrationCheck = true;
		gjkPairDetector.fastContactOnly = true;
		gjkPairDetector.init(null, null, v.simplexSolver, v.pdSolver);
	}

	private boolean _checkPotentialCollisionWithRail(SegmentController docked, SegmentController[] exceptControllers, boolean axisAligned) {
		int tests = 0;

		long t = System.currentTimeMillis();
		
		if(t - lastTimeDebug > 3000) {
			lastTimeDebug = t;
		}
		
		this.detailedTest = !axisAligned;
		this.mainTestDocked = docked;

		SegmentController root = docked.railController.getRoot();

		addToTestsRecusively(root, testTo, exceptControllers);

		for (int a = 0; a < testFrom.size(); a++) {
			for (int b = 0; b < testTo.size(); b++) {
				log.clear();
				SegmentController segmentControllerA = testFrom.get(a);
				SegmentController segmentControllerB = testTo.get(b);

				Transform aTrans = segmentControllerA.railController.getPotentialRelativeToRootLocalTransform();
				Transform bTrans = segmentControllerB.railController.getPotentialRelativeToRootLocalTransform();

//				System.err.println("TEST "+segmentControllerA+"; WITH "+segmentControllerB+": "+aTrans.origin+" -- "+bTrans.origin);
				//since turrets can have non aligned states, they always need the more detailed test
				this.detailedTest = !axisAligned || (segmentControllerA.railController.isTurretDocked() || segmentControllerB.railController.isTurretDocked());

				boolean collides;

				synchronized (v) {
					collides = processCollision(segmentControllerA, segmentControllerB, aTrans, bTrans);
				}

				if (collides) {
					testFrom.clear();
					testTo.clear();
					for (int i = 0; i < log.size(); i++) {
						System.err.println(log.get(i));
					}
					return true;
				} else {
//					for(int i = 0; i < log.size(); i++){
//						System.err.println(log.get(i));
//					}
//					System.err.println(segmentControllerA.getState()+" no col:::: "+segmentControllerA+"; "+segmentControllerB+"; "+aTrans.origin+" "+bTrans.origin	+"\n"+aTrans.basis+" ---- \n"+bTrans.basis	);
				}
				tests++;
			}
		}
		testFrom.clear();
		testTo.clear();

		return false;
	}

	public boolean checkPotentialCollisionWithRail(SegmentController docked, SegmentController[] exceptControllers, boolean axisAligned) {
		if (VoidElementManager.DISABLE_RAIL_COLLISIONS){
			return false;
		}

// 		// The 3 lines below re-enable single thread collisions
//		boolean res = _checkPotentialCollisionWithRail(docked, exceptControllers, axisAligned);
//		System.err.println("[" + Thread.currentThread().getId() + "] COLLISION OF "+ docked.getId() + " = " + res);
//		return res;

		if (!collisionResults.containsKey(docked.getId())) {
			collisionResults.put(docked.getId(), new HashMap<>());
			collisionRequest.put(docked.getId(), new HashMap<>());
		}
		// This id allows to get different collision computation depending on input parameters
		int dissociationIds = 31 * Arrays.hashCode(exceptControllers) + (Boolean.hashCode(axisAligned));

		if (!collisionResults.get(docked.getId()).containsKey(dissociationIds)) {
			collisionResults.get(docked.getId()).put(dissociationIds, new AtomicBoolean(false));
			collisionRequest.get(docked.getId()).put(dissociationIds, new AtomicBoolean(false));
		}

		if (!collisionRequest.get(docked.getId()).get(dissociationIds).get()) {
			// Hardcoded poolsize, max 8 thread for now, TODO move the number in a config or somewhere ?
			RailCollisionCheckerThreadPool.getInstance(8).submitTask(() -> {
				collisionRequest.get(docked.getId()).get(dissociationIds).set(true);
				collisionResults.get(docked.getId()).get(dissociationIds).set(_checkPotentialCollisionWithRail(docked, exceptControllers, axisAligned));
				collisionRequest.get(docked.getId()).get(dissociationIds).set(false);
			});
		}
//		System.err.println("[" + Thread.currentThread().getId() + "] COLLISION OF "+ docked.getId() + " = " + collisionResults.get(docked.getId()).get());

		return collisionResults.get(docked.getId()).get(dissociationIds).get();
	}

	private void addToTestsRecusively(SegmentController root, List<SegmentController> testTo, SegmentController[] exceptControllers) {
		boolean except = false;
		if (exceptControllers != null) {
			for (SegmentController c : exceptControllers) {
				if (c == root) {
					except = true;
				}
			}
		}
		if (!except) {
			testTo.add(root);
		}

		for (int i = 0; i < root.railController.next.size(); i++) {
			RailRelation railRelation = root.railController.next.get(i);
			if (railRelation.docked.getSegmentController() == mainTestDocked) {
				addToTestsRecusively(railRelation.docked.getSegmentController(), testFrom, exceptControllers);
			} else {
				addToTestsRecusively(railRelation.docked.getSegmentController(), testTo, exceptControllers);
			}

		}
	}

	private boolean processCollision(SegmentController a, SegmentController b, Transform aTrans, Transform bTrans) {

		hasCollision = false;
		
		v.oSet = ArrayOctree.getSet(a.isOnServer());
		
		CubeShape cubeShape0 = (CubeShape) ((CompoundShape) a.getPhysicsDataContainer().getShape()).getChildShape(0);
		CubeShape cubeShape1 = (CubeShape) ((CompoundShape) b.getPhysicsDataContainer().getShape()).getChildShape(0);
		cubeShape0.getAabb(aTrans, v.outerWorld.min, v.outerWorld.max);
		cubeShape1.getAabb(bTrans, v.innerWorld.min, v.innerWorld.max);
		
		if(!AabbUtil2.testAabbAgainstAabb2(v.outerWorld.min, v.outerWorld.max, v.innerWorld.min, v.innerWorld.max)){
			return false;
		}
		
		
		
		
		assert (a != b);

		

		v.tmpTrans0.set(aTrans);
		v.tmpTrans1.set(bTrans);
		
		
		v.tmpTrans0Actual.set(aTrans);
		v.tmpTrans1Actual.set(bTrans);
		
		//transform of B relative to A
		v.tmpTrans1Rel.set(aTrans);
		v.tmpTrans1Rel.inverse();
		v.tmpTrans1Rel.mul(bTrans);
		
		v.tmpTrans0Rel.setIdentity();
		
		
		v.tmpTrans0.set(v.tmpTrans0Rel);
		v.tmpTrans1.set(v.tmpTrans1Rel);
		
		v.absolute1.set(v.tmpTrans0.basis);
		MatrixUtil.absolute(v.absolute1);
		v.absolute2.set(v.tmpTrans1.basis);
		MatrixUtil.absolute(v.absolute2);

		v.innerMinBlock.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		v.innerMaxBlock.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		v.outerMinBlock.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		v.outerMaxBlock.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		
		Vector3f rightVector = GlUtil.getRightVector(v.right0, v.tmpTrans0);
		Vector3f rightVector2 = GlUtil.getRightVector(v.right1, v.tmpTrans1);
		
		Vector3f upVector = GlUtil.getUpVector(v.up0, v.tmpTrans0);
		Vector3f upVector2 = GlUtil.getUpVector(v.up1, v.tmpTrans1);
		
		Vector3f forwVector = GlUtil.getForwardVector(v.forw0, v.tmpTrans0);
		Vector3f forwVector2 = GlUtil.getForwardVector(v.forw1, v.tmpTrans1);
		
		this.rTest = rightVector.epsilonEquals(rightVector2, 0.005f);
		this.uTest = upVector.epsilonEquals(upVector2, 0.005f);
		this.fTest = forwVector.epsilonEquals(forwVector2, 0.005f);
		
		int distiTests = 0;
		int preSynchTime = 0;

		long distinctTime = 0;

		

		v.box0.setMargin(0.0f);
		v.box1.setMargin(0.0f);
		cubeShape0.setMargin(0.0f);
		cubeShape1.setMargin(0.0f);

		v.wtInv0.set(v.tmpTrans0);
		v.wtInv0.inverse();

		v.wtInv1.set(v.tmpTrans1);
		v.wtInv1.inverse();

		if (!doAABBIntersection(cubeShape0, cubeShape1)) {
//			if(!a.isOnServer() && a.toString().contains("l0") && b.toString().contains("l0")) {
//				System.err.println("NOT INTERSECTED (NO AABB AT ALL) "+a+"; "+b);
//			}
			return false;
		} else {
//			System.err.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
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

		assert (v.innerNonEmptySegments.isEmpty());
		assert (v.outerNonEmptySegments.isEmpty());
		assert (v.bbCacheInner.isEmpty());

		long assembleA = 0;
		long assembleB = 0;
		long hanlde = 0;
		boolean col = false;
		try {
			((SegmentBufferManager) cubeShape0.getSegmentBuffer()).lastIteration = 0;
			((SegmentBufferManager) cubeShape1.getSegmentBuffer()).lastIteration = 0;
			((SegmentBufferManager) cubeShape0.getSegmentBuffer()).lastDoneIteration = 0;
			((SegmentBufferManager) cubeShape1.getSegmentBuffer()).lastDoneIteration = 0;

			if (sizeXA > 5000 || sizeYA > 5000 || sizeYA > 5000) {
				System.err.println("Exception collision overlap to big " + sizeXA + "; " + sizeYA + "; " + sizeZA + "; for: " + cubeShape0.getSegmentBuffer().getSegmentController() + "; boundingBox: " + cubeShape0.getSegmentBuffer().getBoundingBox());
				return false;
			}
			if (sizeXB > 5000 || sizeYB > 5000 || sizeYB > 5000) {
				System.err.println("Exception collision overlap to big " + sizeXB + "; " + sizeYB + "; " + sizeZB + "; for: " + cubeShape0.getSegmentBuffer().getSegmentController() + "; boundingBox: " + cubeShape0.getSegmentBuffer().getBoundingBox());
				return false;
			}
			cubeShape0.getSegmentBuffer().iterateOverNonEmptyElementRange(outerHandler, v.minIntA, v.maxIntA, false);
			cubeShape1.getSegmentBuffer().iterateOverNonEmptyElementRange(innerHandler, v.minIntB, v.maxIntB, false);

			
//			boolean doSecondAABBIntersection = GameClientController.isStarted() && Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_F10);
//			if(doSecondAABBIntersection){
//				if (!doSecondAABBIntersection(cubeShape0, cubeShape1)) {
////					System.err.println("NO SEC");
//					return false;
//				}
//				int iterationsBruteBef = v.innerNonEmptySegments.size() * v.outerNonEmptySegments.size();
//				
//				v.innerNonEmptySegments.clear();
//				v.outerNonEmptySegments.clear();
//				
//				cubeShape0.getSegmentBuffer().iterateOverNonEmptyElementRange(outerHandler, v.minIntA2, v.maxIntA2, false);
//				cubeShape1.getSegmentBuffer().iterateOverNonEmptyElementRange(innerHandler, v.minIntB2, v.maxIntB2, false);
//				int iterationsBruteAfter = v.innerNonEmptySegments.size() * v.outerNonEmptySegments.size();
//				assert((iterationsBruteBef - iterationsBruteAfter) >= 0);
//				
////				if((iterationsBruteBef - iterationsBruteAfter) > 0){
////					System.err.println("INTERATIONS SAVED: "+(iterationsBruteBef - iterationsBruteAfter)+"; "+iterationsBruteBef+" -> "+iterationsBruteAfter);
////				}
//			}
			
			

			col = handleSweepAndPrune(cubeShape0, cubeShape1);
//			if(!a.isOnServer() && a.toString().contains("l0") && b.toString().contains("l0")) {
				
//				System.err.println("COL "+a+"; "+b+": "+col);
//				try{
//					throw new Exception();
//				}catch(Exception e) {
//					e.printStackTrace();
//				}
//				System.err.println("::: 0000\n"+v.tmpTrans0.basis);
//				System.err.println("::: 1111\n"+v.tmpTrans1.basis);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			v.innerNonEmptySegments.clear();
			v.outerNonEmptySegments.clear();
			v.bbCacheInner.clear();
		}
		return col;
	}
	private final static int margin = 1; //SegmentData.SEG_HALF
	private boolean doAABBIntersection(SegmentAabbInterface cubeShape0,
	                                   SegmentAabbInterface cubeShape1) {
		{
			//do for A (0)

			v.tmpAABBTrans0.set(v.wtInv0);
			v.tmpAABBTrans0.mul(v.tmpTrans1);

			cubeShape1.getAabbUncached(v.tmpAABBTrans0, v.innerAABBforA.min, v.innerAABBforA.max, false);
			//the bounding box of col1 is now in space of a and aligned to col0.
			//That means we can do an indersection with our local bounding box
			cubeShape0.getAabbIdent(v.intersection.min, v.intersection.max);

			BoundingBox intersectionAB = v.intersection.getIntersection(v.innerAABBforA, v.intersectionInASpaceWithB);

			if (intersectionAB == null) {
				//no intersection
				return false;
			}
			v.minIntA.x = ByteUtil.divSeg((int) (intersectionAB.min.x - margin)) * SegmentData.SEG;
			v.minIntA.y = ByteUtil.divSeg((int) (intersectionAB.min.y - margin)) * SegmentData.SEG;
			v.minIntA.z = ByteUtil.divSeg((int) (intersectionAB.min.z - margin)) * SegmentData.SEG;

			v.maxIntA.x = (FastMath.fastCeil((intersectionAB.max.x + margin) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntA.y = (FastMath.fastCeil((intersectionAB.max.y + margin) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntA.z = (FastMath.fastCeil((intersectionAB.max.z + margin) / SegmentData.SEGf)) * SegmentData.SEG;

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
			v.minIntB.x = ByteUtil.divSeg((int) (intersectionBA.min.x - margin)) * SegmentData.SEG;
			v.minIntB.y = ByteUtil.divSeg((int) (intersectionBA.min.y - margin)) * SegmentData.SEG;
			v.minIntB.z = ByteUtil.divSeg((int) (intersectionBA.min.z - margin)) * SegmentData.SEG;

			v.maxIntB.x = (FastMath.fastCeil((intersectionBA.max.x + margin) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntB.y = (FastMath.fastCeil((intersectionBA.max.y + margin) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntB.z = (FastMath.fastCeil((intersectionBA.max.z + margin) / SegmentData.SEGf)) * SegmentData.SEG;
		}

		return true;
	}
	private static final boolean old = false;
	private boolean handleSweepAndPrune(SegmentAabbInterface cubeShape0, SegmentAabbInterface cubeShape1) {
		v.sweeper.fill(cubeShape0, v.tmpTrans0, cubeShape1, v.tmpTrans1, v.outerNonEmptySegments, v.innerNonEmptySegments);
		v.sweeper.getOverlapping();

//		System.err.println("PROCESSING OVERLAPPING AABB::::: "+v.sweeper.xPairs.size());
		for (OverlappingSweepPair<Segment> p : v.sweeper.pairs) {
			SweepPoint<Segment> a = p.a;
			SweepPoint<Segment> b = p.b;

			if(old){
				//no AABB test necessary since we are sure they overlap
	
				v.bbOuterSeg.min.set(a.minX, a.minY, a.minZ);
				v.bbOuterSeg.max.set(a.maxX, a.maxY, a.maxZ);
	
				v.bbInnerSeg.min.set(b.minX, b.minY, b.minZ);
				v.bbInnerSeg.max.set(b.maxX, b.maxY, b.maxZ);
	
				v.bbOuterSeg.getIntersection(v.bbInnerSeg, v.bbSectorIntersection);
	
				if (a.seg.getSegmentController().isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
					a.seg.debugDraw(0, 1, 1, 1, 1, 5);
					b.seg.debugDraw(0, 1, 1, 1, 1, 5);
				}
				processDistinctCollisionOld(cubeShape0,
						cubeShape1, a.seg.getSegmentData(),
						b.seg.getSegmentData(), v.tmpTrans0,
						v.tmpTrans1);
			}else{

				processDistinctCollision(cubeShape0,
						cubeShape1, a.seg.getSegmentData(),
						b.seg.getSegmentData(), v.tmpTrans0,
						v.tmpTrans1);
			
			}
			if (hasCollision) {
				return true;
			}
		}
		v.sweeper.pairs.clear();
		v.innerNonEmptySegments.clear();
		v.outerNonEmptySegments.clear();
		v.bbCacheInner.clear();

		return false;
	}

	private void processDistinctCollision(SegmentAabbInterface cubeShape0,
			SegmentAabbInterface cubeShape1, SegmentData data0,
			SegmentData data1, Transform tmpTrans0, Transform tmpTrans1) {

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

			handle16(cubeShape0, cubeShape1, data0, data1, tmpTrans0, tmpTrans1, spa, spb);
		}
	}
	private void handle16(SegmentAabbInterface cubeShape0,
			SegmentAabbInterface cubeShape1, SegmentData data0,
			SegmentData data1, Transform tmpTrans0, Transform tmpTrans1, SweepPoint<Integer> spa, SweepPoint<Integer> spb){
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
					v.bbOctIntersection);
			if (hasCollision) {
				break;
			}

		}
	}
	private void processDistinctCollisionOld(SegmentAabbInterface cubeShape0,
	                                      SegmentAabbInterface cubeShape1,
	                                      SegmentData data0, SegmentData data1,
	                                      Transform tmpTrans1, Transform tmpTrans2) {

//		v.octreeTopLevelSweeper.abs0 = v.absolute1; 
//		v.octreeTopLevelSweeper.abs1 = v.absolute2; 
//		v.octreeTopLevelSweeper.seg0 = data0.getSegment();
//		v.simpleListA.max = 0;
//		v.simpleListB.max = 0;
//		v.octreeTopLevelSweeper.fill(data0.getSegment(), tmpTrans1, data1.getSegment(), tmpTrans2, v.simpleListA, v.simpleListB);
//		
//		v.octreeTopLevelSweeper.getOverlapping();
//		
//		final int tpLvlSize = v.octreeTopLevelSweeper.pairs.size();
//		
//		for (int j = 0; j < tpLvlSize; j++) {
//			OverlappingSweepPair<Integer> p = v.octreeTopLevelSweeper.pairs.get(j);
//			SweepPoint<Integer> a = p.a;
//			SweepPoint<Integer> b = p.b;
//			
//		}
		
		
		if (!v.intersectionCallBackAwithB.initialized) {
			v.intersectionCallBackAwithB.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
			v.intersectionCallBackBwithA.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
		}

		v.intersectionCallBackAwithB.reset();
		v.intersectionCallBackBwithA.reset();

		//find intersection of octree A with AABB of segment B
		v.intersectionCallBackAwithB = data0.getOctree().findIntersectingAABB(v.oSet,
				v.intersectionCallBackAwithB, data0.getSegment(), tmpTrans1,
				v.absolute1, 0.0f, v.bbSectorIntersection.min, v.bbSectorIntersection.max, 1.0f);

		// no hit in this octree, don't generate a new contact
		if (v.intersectionCallBackAwithB.hitCount == 0) {
			return;
		}
		v.intersectionCallBackBwithA.reset();

		v.oSet.debug = data1.getSegmentController().isOnServer();
		v.intersectionCallBackBwithA = data1.getOctree().findIntersectingAABB(v.oSet,
				v.intersectionCallBackBwithA, data1.getSegment(), tmpTrans2,
				v.absolute2, -0.001f, v.bbSectorIntersection.min, v.bbSectorIntersection.max, 1.0f);
		v.oSet.debug = false;
		if (v.intersectionCallBackBwithA.hitCount == 0) {
			return;
		}
		int count = 0;
		v.simpleListA.max = v.intersectionCallBackAwithB.hitCount;
		v.simpleListB.max = v.intersectionCallBackBwithA.hitCount;
		v.octreeSweeper.fill(v.intersectionCallBackAwithB, null, v.intersectionCallBackBwithA, null, v.simpleListA, v.simpleListB);
		
		v.octreeSweeper.getOverlapping();
		
		v.tmpTrans3.set(v.tmpTrans0);
		v.tmpTrans4.set(v.tmpTrans1);
		
		final int size = v.octreeSweeper.pairs.size();
		
		
		
		for (int i = 0; i < size; i++) {
			OverlappingSweepPair<Integer> p = v.octreeSweeper.pairs.get(i);
			
			SweepPoint<Integer> a = p.a;
			SweepPoint<Integer> b = p.b;
			
			int hitMaskA = v.intersectionCallBackAwithB.getHit(p.a.seg, v.bbOuterOct.min, v.bbOuterOct.max, v.startA, v.endA);
			int nodeIndexA = v.intersectionCallBackAwithB.getNodeIndexHit(p.a.seg);
			
			int hitMaskB = v.intersectionCallBackBwithA.getHit(p.b.seg, v.bbInnerOct.min, v.bbInnerOct.max, v.startB, v.endB);
			int nodeIndexB = v.intersectionCallBackBwithA.getNodeIndexHit(p.b.seg);
			
//			if(deb && data0.getSegmentController().isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() ){
//				
//				Transform t = new Transform(data0.getSegmentController().railController.getRoot().getWorldTransform());
//				t.mul(v.tmpTrans0Actual);
//				DebugBox debugBox = new DebugBox(new Vector3f(v.bbInnerOct.min),
//						new Vector3f(v.bbInnerOct.max), t, 0,0,1,1);
//				debugBox.LIFETIME = 3000;
//				DebugDrawer.boxes.add(debugBox);
//				
//				Transform t2 = new Transform(data0.getSegmentController().railController.getRoot().getWorldTransform());
//				t2.mul(v.tmpTrans0Actual); //use 0Actual because 1 is relative to 0 already
//				DebugBox debugBox2 = new DebugBox(new Vector3f(v.bbOuterOct.min),
//						new Vector3f(v.bbOuterOct.max), t2,1,0,0,1);
//				debugBox2.LIFETIME = 3000;
//				
//				DebugDrawer.boxes.add(debugBox2);
//			}
			
			count++;
//			v.bbInnerOct.getIntersection(v.bbOuterOct, v.bbOctIntersection);
			doNarrowTest(data0, data1, v.startA, v.endA, v.startB, v.endB, 
					hitMaskA, hitMaskB, nodeIndexA, nodeIndexB, v.bbOctIntersection);
			if (hasCollision) {
				break;
			}
			
		}

	}

	private void doNarrowTest(SegmentData data0, SegmentData data1,
	                          Vector3b startA, Vector3b endA, Vector3b startB,
	                          Vector3b endB, int maskA, int maskB, int nodeIndexA, int nodeIndexB,
	                          BoundingBox bbOctIntersection) {

		final byte[][] bsA = ArrayOctreeTraverse.tMap[maskA];
		final byte[][] bsB = ArrayOctreeTraverse.tMap[maskB];
		final int[] inA = ArrayOctreeTraverse.tIndexMap[nodeIndexA][maskA];
		final int[] inB = ArrayOctreeTraverse.tIndexMap[nodeIndexB][maskB];

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

			ElementInformation infoA;
			short typeA;
			boolean activeA;
			if (((typeA = data0.getType(eA)) != 0) && (typeA == ElementKeyMap.SIGNAL_TRIGGER_AREA | ((infoA = ElementKeyMap.getInfo(typeA)).isPhysical(activeA = data0.isActive(eA))))) {
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
				//--------------------------------

				for (int b = 0; b < bsB.length; b++) {

					int Bx = (startB.x + bsB[b][0]);
					int By = (startB.y + bsB[b][1]);
					int Bz = (startB.z + bsB[b][2]);

					int eB = inB[b];

					ElementInformation infoB;
					boolean activeB;
					short typeB;
					if (((typeB = data1.getType(eB)) != 0) && (typeB == ElementKeyMap.SIGNAL_TRIGGER_AREA | ((infoB = ElementKeyMap.getInfo(typeB)).isPhysical(activeB = data1.isActive(eB))))) {
						if (typeA == ElementKeyMap.SIGNAL_TRIGGER_AREA || typeB == ElementKeyMap.SIGNAL_TRIGGER_AREA) {
							continue;
						}
						
						
						
						byte orientationBOrig = data1.getOrientation(eB);
						byte orientationB = orientationBOrig;
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

						v.elemPosDist.sub(v.elemPosBAbs, v.elemPosAAbs);

						
						//do a detailed test when both blocks are non cubes
						boolean forceDetailed = infoA.blockStyle.solidBlockStyle && infoB.blockStyle.solidBlockStyle;

						if (!forceDetailed && !detailedTest) {
							boolean aabbTest = FastMath.carmackLength(v.elemPosDist) < 0.8f;
							if (aabbTest) {
//								System.err.println("AABBTEST BLOCK OVERLAP: "+v.elemPosA+", "+v.elemPosB+"; "+FastMath.carmackLength(v.elemPosBAbs));
								hasCollision = true;
								return;
							}
						} else {
							boolean aabbTest = FastMath.carmackLength(v.elemPosDist) < 1.733f;
							
							
							
							final float max = 0.999f;
							
							boolean rTestF = aabbTest && (!this.rTest || Math.abs(Vector3fTools.projectScalar(v.elemPosDist, v.right0)) < max);
							boolean uTestF = rTestF && (!this.uTest || Math.abs(Vector3fTools.projectScalar(v.elemPosDist, v.up0)) < max);
							boolean fTestF = uTestF && (!this.fTest || Math.abs(Vector3fTools.projectScalar(v.elemPosDist, v.forw0)) < max);
							
							
//							if(!data0.getSegmentController().isOnServer() && aabbTest) {
//								System.err.println("NARROW "+ElementKeyMap.toString(typeA)+" "+ElementKeyMap.toString(typeB)+"; aabb "+aabbTest+"; r "+rTestF+"; u "+uTestF+" f "+fTestF);	
//							}
							if (fTestF) {

								v.tmpTrans3.origin.set(v.elemPosAAbs);
								v.tmpTrans4.origin.set(v.elemPosBAbs);
								

								ConvexShape shapeA = v.box0;
								ConvexShape shapeB = v.box1;
								boolean needsNonCubeCubeCollision = false;
								if (infoA.blockStyle.solidBlockStyle) {
									shapeA = BlockShapeAlgorithm.getSmallShape(infoA.getBlockStyle(), orientationA);
									needsNonCubeCubeCollision = true;

									assert (shapeA != null) : BlockShapeAlgorithm.getAlgo(infoA.getBlockStyle(), orientationA);
									shapeB = v.box1M;
//									System.err.println("PA: "+((org.schema.game.common.data.physics.ConvexHullShapeExt)shapeA).getPoints());
								}
								if (infoB.blockStyle.solidBlockStyle) {
									shapeB = BlockShapeAlgorithm.getSmallShape(infoB.getBlockStyle(), orientationB);

									assert (shapeB != null) : BlockShapeAlgorithm.getAlgo(infoB.getBlockStyle(), orientationB);
//									System.err.println("PB: "+((org.schema.game.common.data.physics.ConvexHullShapeExt)shapeB).getPoints());
									needsNonCubeCubeCollision = true;
									
									if(shapeA == v.box0){
										shapeA = v.box0M;
									}
								}
								
								
								Transform boxTransformA = v.tmpTrans3;
								if(infoA.getSlab(orientationAOrig) > 0){
									boxTransformA = v.BT_A;
									boxTransformA.set(v.tmpTrans3);
									
									v.orientTT.set(Element.DIRECTIONSf[Element.switchLeftRight(orientationA%6)]);
									boxTransformA.basis.transform(v.orientTT);
									
									if(needsNonCubeCubeCollision){
										switch(infoA.getSlab(orientationAOrig)) {
											case 1 -> {
												v.orientTT.scale(0.125f);
												shapeA = v.box34M[orientationA % 6];
											}
											case 2 -> {
												v.orientTT.scale(0.25f);
												shapeA = v.box12M[orientationA % 6];
											}
											case 3 -> {
												v.orientTT.scale(0.375f);
												shapeA = v.box14M[orientationA % 6];
											}
										}
									}else{
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
									}
//									System.err.println("CC SLAPPP "+e.slab+"; "+v.orientTT+"; "+((BoxShape)shape).getHalfExtentsWithMargin(new Vector3f()));
									boxTransformA.origin.sub(v.orientTT);
									boxboxPairDetector.cachedBoxSize = false;
								}
								Transform boxTransformB = v.tmpTrans4;
								if(infoB.getSlab(orientationBOrig) > 0){
									boxTransformB = v.BT_B;
									boxTransformB.set(v.tmpTrans4);
									
									v.orientTT.set(Element.DIRECTIONSf[Element.switchLeftRight(orientationB%6)]);
									boxTransformB.basis.transform(v.orientTT);
									
									if(needsNonCubeCubeCollision){
										switch(infoB.getSlab(orientationBOrig)) {
											case 1 -> {
												v.orientTT.scale(0.125f);
												shapeB = v.box34M[orientationB % 6];
											}
											case 2 -> {
												v.orientTT.scale(0.25f);
												shapeB = v.box12M[orientationB % 6];
											}
											case 3 -> {
												v.orientTT.scale(0.375f);
												shapeB = v.box14M[orientationB % 6];
											}
										}
									}else{
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
									}
//									System.err.println("CC SLAPPP "+e.slab+"; "+v.orientTT+"; "+((BoxShape)shape).getHalfExtentsWithMargin(new Vector3f()));
									boxTransformB.origin.sub(v.orientTT);
									boxboxPairDetector.cachedBoxSize = false;
								}
								
								int contactsOnThis = 0;
								if (needsNonCubeCubeCollision) {
									contactsOnThis = doNonBlockCollision(shapeA , shapeB,
											boxTransformA, boxTransformB, v.elemPosA, v.elemPosB, typeA, typeB, data0.getSegmentController().getId(), data1.getSegmentController().getId());
								} else {
									contactsOnThis = doExtCubeCubeCollision((BoxShape)shapeA, (BoxShape)shapeB,
											boxTransformA, boxTransformB,
											v.elemPosA, v.elemPosB, typeA, typeB, data0.getSegmentController().getId(), data1.getSegmentController().getId());
								}

								if (contactsOnThis > 0) {
									hasCollision = true;
									return;

								} else {
								}
							}
						}
					}
				}
			}
		}
	}

	private int doNonBlockCollision(ConvexShape min0, ConvexShape min1,
	                                Transform tA, Transform tB, Vector3f elemPosA, Vector3f elemPosB, short typeA, short typeB, int idA, int idB) {
		ClosestPointInput input = pointInputsPool.get();
		input.init();

//		resultOut.setPersistentManifold(manifoldPtr);

		// JAVA NOTE: original: TODO: if (dispatchInfo.m_useContinuous)
		gjkPairDetector.setMinkowskiA(min0);
		gjkPairDetector.setMinkowskiB(min1);

//		log.add("doing NONCUBE :::: "+elemPosA+"; "+elemPosB);
		gjkPairDetector.log = log;

//		input.maximumDistanceSquared = 0;
		input.maximumDistanceSquared = min0.getMargin() + min1.getMargin()
				+ 0.02f;
		input.maximumDistanceSquared *= input.maximumDistanceSquared;
		// input.m_stackAlloc = dispatchInfo.m_stackAllocator;

		// input.m_maximumDistanceSquared = btScalar(1e30);

		input.transformA.set(tA);
		input.transformB.set(tB);
		gjkPairDetector.getClosestPoints(input, resultOut,
				null, false, elemPosA, elemPosB, typeA, typeB, idA, idB);
		// throw new ErrorDialogException(e.getMessage());
		pointInputsPool.release(input);
		// #endif

		//		// schema mod
		//		if (ownManifold) {
		//			resultOut.refreshContactPoints();
		//		}
		return gjkPairDetector.contacts;
	}

	private int doExtCubeCubeCollision(BoxShape min0, BoxShape min1,
	                                   Transform tA, Transform tB, Vector3f elemPosA, Vector3f elemPosB, short typeA, short typeB, int idA, int idB) {

		ClosestPointInput input = pointInputsPool.get();
		input.init();
		//		resultOut.setPersistentManifold(manifoldPtr);

		input.maximumDistanceSquared = min0.getMargin() + min1.getMargin()
				+ 0.02f;
		input.maximumDistanceSquared *= input.maximumDistanceSquared;

		input.transformA.set(tA);
		input.transformB.set(tB);
		boxboxPairDetector.GetClosestPoints(min0, min1, input, resultOut,
				null, false, elemPosA, elemPosB, typeA, typeB, idA, idB);
		pointInputsPool.release(input);

		return boxboxPairDetector.contacts;
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

}
