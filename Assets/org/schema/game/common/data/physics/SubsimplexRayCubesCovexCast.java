package org.schema.game.common.data.physics;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.physics.ClosestRayCastResultExt;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ConvexCast;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * SubsimplexConvexCast implements Gino van den Bergens' paper
 * "Ray Casting against bteral Convex Objects with Application to Continuous Collision Detection"
 * GJK based Ray Cast, optimized version
 * Objects should not start in overlap, otherwise results are not defined.
 *
 * @author schema
 */
public class SubsimplexRayCubesCovexCast extends ConvexCast {

	private static ThreadLocal<CubeRayVariableSet> threadLocal = new ThreadLocal<CubeRayVariableSet>() {
		@Override
		protected CubeRayVariableSet initialValue() {
			return new CubeRayVariableSet();
		}
	};
	public boolean debug = false;
	ObjectPool<Vector3b> pool = ObjectPool.get(Vector3b.class);
	
	ObjectPool<AABBb> aabbpool = ObjectPool.get(AABBb.class);
	OuterSegmentIterator outerSegmentIterator;
	int hitboxes = 0;
	int casts = 0;
	private ClosestRayCastResultExt rayResult;
	private CubeRayVariableSet v;
	public SubsimplexRayCubesCovexCast() {
		
	}
	public SubsimplexRayCubesCovexCast(ConvexShape shapeA, CollisionObject cubesObject, SimplexSolverInterface simplexSolver, ClosestRayCastResultExt rayResult) {
		this.v = threadLocal.get();

		outerSegmentIterator = new OuterSegmentIterator(v);
		v.shapeA = shapeA;
		v.cubesB = (CubeShape) cubesObject.getCollisionShape();
		v.cubesCollisionObject = cubesObject;
		v.simplexSolver = simplexSolver;
		this.rayResult = rayResult;
		if(debug){
			System.err.println("DEBUG INST: RECORD ALL BLOCKS: "+this.rayResult.isRecordAllBlocks());
		}
		//		assert((!rayResult.hasHit() || rayResult.getSegment() != null));
		v.box0.setMargin(CubeRayVariableSet.margin);
		//		v.lastMinDist = -1;
	}
	public void init(ConvexShape shapeA, CollisionObject cubesObject, SimplexSolverInterface simplexSolver, ClosestRayCastResultExt rayResult) {
		this.v = threadLocal.get();
		
		outerSegmentIterator = new OuterSegmentIterator(v);
		v.shapeA = shapeA;
		v.cubesB = (CubeShape) cubesObject.getCollisionShape();
		v.cubesCollisionObject = cubesObject;
		v.simplexSolver = simplexSolver;
		this.rayResult = rayResult;
		if(debug){
			System.err.println("DEBUG INIT: RECORD ALL BLOCKS: "+this.rayResult.isRecordAllBlocks());
		}
		//		assert((!rayResult.hasHit() || rayResult.getSegment() != null));
		v.box0.setMargin(CubeRayVariableSet.margin);
		//		v.lastMinDist = -1;
	}

	@Override
	public boolean calcTimeOfImpact(Transform fromA, Transform toA, Transform testCubes, Transform toCubes, CastResult result) {
		//		assert((!rayResult.hasHit() || rayResult.getSegment() != null));
		//		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
		//			if(!v.cubesB.getSegmentBuffer().getSegmentController().isOnServer()){
		//				Vector3f f = new Vector3f();
		//				Vector3f t = new Vector3f();
		//				f.set(v.from.origin.x, v.from.origin.y, v.from.origin.z);
		//				t.set(v.to.origin.x, v.to.origin.y, v.to.origin.z);
		//				Transform tr = new Transform();
		//				tr.setIdentity();
		//	//			tr.origin.set(testCubes.origin);
		//				DebugBox b = new DebugBox(f, t, tr, 0, 0, 1, 1);
		//				DebugDrawer.boxes.add(b);
		//			}else{
		//
		//			}

		//		}
		//		oldTest = false;
		//		if(GameClientController.started && Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)){
		//			oldTest = true;
		//		}
		
		if(rayResult.isDamageTest() && !v.cubesB.getSegmentController().isPhysicalForDamage()){
			return false;
		}
		
		this.debug = rayResult.isDebug();

		
		if (debug) {
			System.err.println("[PHYSICS] CHECKING START: " + v.cubesB.getSegmentBuffer().getSegmentController());
		}
		//		v.lastHitpointWorld.set(Float.NaN, 0, 0);
		//fromCubes and to cubes are the same since the cubes shape is not handled as a ray

		if (rayResult instanceof CubeRayCastResult && ((CubeRayCastResult)rayResult).isFiltered(v.cubesB.getSegmentBuffer().getSegmentController())) {
			return false;
		}

		if (rayResult.isHasCollidingBlockFilter()) {

			if (!rayResult.getCollidingBlocks().containsKey(v.cubesB.getSegmentBuffer().getSegmentController().getId())) {
				/*
				 * this case happens when weapon passes through multiple ships
				 */
				rayResult.getCollidingBlocks().put(v.cubesB.getSegmentBuffer().getSegmentController().getId(), new LongOpenHashSet());
			}
		}

		if (rayResult.isRecordAllBlocks()) {
			assert (rayResult.getRecordedBlocks() != null);
			BlockRecorder r = v.getBlockRecorder();
			((Int2ObjectOpenHashMap<BlockRecorder>)rayResult.getRecordedBlocks()).put(v.cubesB.getSegmentBuffer().getSegmentController().getId(),r);
			v.record = r;
			v.recordAmount = rayResult.getBlockDeepness();
		} else {
			v.record = null;
		}
		v.cubesB.getAabb(testCubes, v.outMin, v.outMax);
		v.hitLambda[0] = 1;
		v.normal.set(0, 0, 0);

//		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
//
//			DebugBoundingBox b = new DebugBoundingBox(v.outMin, v.outMax, 0f, 0.2f, 1f, 1f);
//			DebugDrawer.boundingBoxes.add(b);
//		}
		
//		v.cubesB.setMargin(v.cubesB.getMargin() - extraMargin);

		//only test, if either point is inside bb
//		boolean hitBox = AabbUtil2.rayAabb(v.from.origin, v.to.origin, v.outMin, v.outMax, v.hitLambda, v.normal)
//				|| BoundingBox.testPointAABB(v.from.origin, v.outMin, v.outMax)
//				|| BoundingBox.testPointAABB(v.to.origin, v.outMin, v.outMax);
//		if(hitBox && !BoundingBox.testPointAABB(v.to.origin, v.outMin, v.outMax)){
//			//the endpoint must lie behind the AABB
//			//because the start is either in or intersecting
//			//if both points where behind, hitbox would be false
//			
//			Vector3f bk = new Vector3f();
//			bk.sub(v.from.origin, v.to.origin); //vector from end to start
//			
//			bk.normalize();
//			Vector3f backSideIntersection = new Vector3f();
//			if(BoundingBox.getIntersection(v.to.origin, bk, v.outMin, v.outMax, backSideIntersection)){
//				System.err.println("SHORT "+v.to.origin+"; "+backSideIntersection);
//				v.to.origin.set(backSideIntersection);
//				
//			}
//			
//			
//		}
		
		
		v.from.set(fromA);
		v.to.set(toA);
		
//		boolean clip = v.cubesB.getSegmentBuffer().getBoundingBox().clipSegment(fromA.origin, toA.origin, v.tmpA, v.tmpB);
//		if(!clip) {
//			return false;
//		}
//		System.err.println("CLIPPED FROM "+fromA.origin+", "+toA.origin+" -> "+v.from.origin+", "+v.to.origin);
		
		//		v.lastMinDist = -1;
		outerSegmentIterator.hit = false;
		if (v.oSet == null) {
			v.oSet = ArrayOctree.getSet(v.cubesB.getSegmentBuffer().getSegmentController().getState() instanceof ServerStateInterface);
		} else {
			assert (v.oSet == ArrayOctree.getSet(v.cubesB.getSegmentBuffer().getSegmentController().isOnServer()));
		}
		v.absolute.set(testCubes.basis);
		MatrixUtil.absolute(v.absolute);

		outerSegmentIterator.fromA = v.from;
		outerSegmentIterator.hitSignal = false;
		outerSegmentIterator.result = result;
		outerSegmentIterator.debug = debug;
		outerSegmentIterator.rayResult = rayResult;
		outerSegmentIterator.testCubes = testCubes;
		outerSegmentIterator.toA = v.to;
		outerSegmentIterator.toCubes = toCubes;
		v.solve.initializeSegmentGranularity(v.from.origin, v.to.origin, testCubes);
		
		//		if(debugSingle){
		//			System.err.println("not traversing: "+v.cubesB.getSegmentBuffer().getSegmentController());
		//		}
		if (!v.solve.ok) {
			System.err.println("[PHYSICS] SegmentTrav " + v.cubesB.getSegmentBuffer().getSegmentController().getState() + " " + v.from.origin + " -> " + v.to.origin + " for cubes: " + testCubes.origin + ": " + v.cubesB.getSegmentBuffer().getSegmentController());
		}
	
		outerSegmentIterator.segmentController = v.cubesB.getSegmentBuffer().getSegmentController();
		v.solve.traverseSegmentsOnRay(outerSegmentIterator);
		outerSegmentIterator.segmentController = null;
		
		
		
		//		v.cubesB.getSegmentBuffer().iterateOverNonEmptyElementRange(outerSegmentIterator, v.minIntA, v.maxIntA, false);
		if (outerSegmentIterator.hitSignal) {
			//			assert((!rayResult.hasHit() || rayResult.getSegment() != null));
			//			if(rayResult.hasHit() && rayResult.getSegment() == null){
			//				System.err.println("[SUBSIMPLEXRAY] WARNING: segment was null but hit setected!?!");
			//				rayResult.collisionObject = null;
			//				return false;
			//			}
			if (debug) {
				System.err.println("[PHYSICS] CHECKING HIT: " + v.cubesB.getSegmentBuffer().getSegmentController() + " -> " + rayResult.hasHit() + "; " + rayResult.getSegment());
			}
			return true;
		} else {
		}
		//			System.err.println("CASTS DONE: "+casts);
		//			System.err.println("CALCULATING IMPACT WITH "+cubesB.getSegmentBuffer().getSegmentController()+" hit: "+hit+": hitboxes: "+hitboxes+"; casts: "+casts+"; CPOS: "+rayResult.cubePos+"; hit: "+rayResult.hitPointWorld);

		if (debug) {
			System.err.println("[PHYSICS] CHECKING: " + v.cubesB.getSegmentBuffer().getSegmentController() + " -> HIT: " + rayResult.hasHit() + "; " + rayResult.getSegment());
		}

		return outerSegmentIterator.hit;

	}

	

	
	

	

	


}
