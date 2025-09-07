package org.schema.game.common.data.physics;

import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.physics.ClosestRayCastResultExt;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ConvexCast.CastResult;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;

public class OuterSegmentIterator implements SegmentTraversalInterface<SegmentController> {

	SegmentController segmentController;
	Transform fromA;
	Transform toA;
	Transform testCubes;
	Transform toCubes;
	CastResult result;
	boolean hitSignal;
	public boolean hit;
	ObjectPool<Vector4i> pool4 = ObjectPool.get(Vector4i.class);
	public boolean debug;
	private CubeRayVariableSet v;
	public ClosestRayCastResultExt rayResult;
	public OuterSegmentIterator(CubeRayVariableSet v) {
		this.v = v;
	}
	public boolean handle(Segment sOuter) {

		checkSegment(sOuter, fromA, toA, testCubes, toCubes, result);
		if (hit) {
			if(debug) {
				System.err.println("[OUTER] not continuing shot since it was marked as hit");
			}
			
			this.hitSignal = true;
			return false; //dont continue iteration
		}
		return true; //continue
	}		@Override
	public SegmentController getContextObj() {
		return segmentController;
	}

	@Override
	public boolean handle(int x, int y, int z, RayTraceGridTraverser traverser) {
		
//		if(rayResult.isDebug()) {
//			System.err.println("HANDLE "+x+", "+y+", "+z);
//		}
		
		SegmentController controller = segmentController;
		x = (x) * SegmentData.SEG;
		y = (y) * SegmentData.SEG;
		z = (z) * SegmentData.SEG;
		
		
		if(!controller.isInboundAbs(x, y, z)){
			//dont continue outside of target
			return true;
		}
		int segmentState = controller.getSegmentBuffer().getSegmentState(x, y, z);

		if (segmentState >= 0) {
			Segment segment = controller.getSegmentBuffer().get(x, y, z);
			if (segment != null) {
				return handle(segment);
			}
		}
		
		return true;
	}

	private void checkSegment(Segment sOuter, Transform fromA, Transform toA, Transform testCubes, Transform toCubes, CastResult result) {
		if (sOuter.getSegmentData() == null || sOuter.isEmpty()) {
			return;
		}
		
		
		v.cubesB.getSegmentAabb(sOuter, testCubes, v.outMin, v.outMax, v.localMinOut, v.localMaxOut, v.aabbVarSet);
		v.hitLambda[0] = 1;
		v.normal.set(0, 0, 0);


		//only test, if either point is inside bb
		boolean hitBox = AabbUtil2.rayAabb(fromA.origin, toA.origin, v.outMin, v.outMax, v.hitLambda, v.normal)
				|| BoundingBox.testPointAABB(fromA.origin, v.outMin, v.outMax)
				|| BoundingBox.testPointAABB(toA.origin, v.outMin, v.outMax);
		
		boolean cast = false;
		if (hitBox) {
			
			cast = performCastTestTrav(v.cubesCollisionObject, sOuter, fromA, toA, testCubes, result);

			final int size = v.takenPoints.size();
			for (int i = 0; i < size; i++) {
				pool4.release(v.takenPoints.get(i));
			}
			v.sorted.clear();
			v.takenPoints.clear();
			hit = hit || cast;
		}
		
		boolean continueShot = ((InnerSegmentIterator) rayResult.innerSegmentIterator).onOuterSegmentHitTest(sOuter, ((InnerSegmentIterator) rayResult.innerSegmentIterator).hitSignal);
		
		if(debug) {
			System.err.println("[OUTER] ON SEGEMNT "+sOuter.pos+": hitBox: "+hitBox+"; continue: "+continueShot+"; cast: "+cast+"; hit "+hit);
		}
		
		if(!continueShot) {
			hit = true;
		}
	}

	private boolean performCastTestTrav(CollisionObject collisionObject, Segment s, Transform fromA, Transform toA,
			Transform testCubes, CastResult result) {

		v.segTrans.set(testCubes);
		v.segPos.set(s.pos.x - SegmentData.SEG_HALF, s.pos.y - SegmentData.SEG_HALF, s.pos.z - SegmentData.SEG_HALF);

		
		
		v.segAABB.set(v.outMin, v.outMax);
		v.segAABB.min.x -= SegmentData.SEG_HALF;
		v.segAABB.min.y -= SegmentData.SEG_HALF;
		v.segAABB.min.z -= SegmentData.SEG_HALF;
//		
		v.segAABB.max.x += SegmentData.SEG_HALF;
		v.segAABB.max.y += SegmentData.SEG_HALF;
		v.segAABB.max.z += SegmentData.SEG_HALF;
		
		if(debug) {
			System.err.println("BB "+v.segAABB);
		}
		

		v.rayModFrom.set(fromA.origin);
		v.rayModTo.set(toA.origin);

		
		boolean insideFrom = v.segAABB.isInside(fromA.origin);
		boolean insideTo = v.segAABB.isInside(toA.origin);
		
		if(insideFrom && insideTo) {
			//nothing to change
		}else {
			if(!insideTo) {
				//check from to->from intersection
				v.dirTmp.sub(fromA.origin, toA.origin);
				v.dirTmp.normalize();
				boolean intersection = v.segAABB.getIntersection(toA.origin, v.dirTmp, v.outInt);
				if(!intersection) {
					return false;
				}
				v.rayModTo.set(v.outInt);
			}
			if(!insideFrom) {
				//check from from->to intersection
				v.dirTmp.sub(toA.origin, fromA.origin);
				v.dirTmp.normalize();
				boolean intersection = v.segAABB.getIntersection(fromA.origin, v.dirTmp, v.outInt);
				if(!intersection) {
					return false;
				}
				v.rayModFrom.set(v.outInt);
			}
		}
		
//		if(debug) {
//			DebugBoundingBox debugBoundingBox = new DebugBoundingBox(v.outMin, v.outMax, 0, 1, 0, 1);
//			debugBoundingBox.LIFETIME = 10000;
//			DebugDrawer.boundingBoxes.add(debugBoundingBox);
////			DebugLine db = new DebugLine(new Vector3f(fromA.origin), new Vector3f(toA.origin), new Vector4f(0, 1, 0, 1));
////			db.LIFETIME = 10000;
////			DebugDrawer.lines.add(db);
//			
//			DebugLine db2 = new DebugLine(new Vector3f(v.rayModFrom), new Vector3f(v.rayModTo), new Vector4f(0, 1, 0, 1));
//			db2.LIFETIME = 10000;
//			DebugDrawer.lines.add(db2);
//		}
//		if (!v.segAABB.clipSegment(v.rayModFrom, v.rayModTo, v.rayTmp0, v.rayTmp1)) {
//			if(debug) {
//				System.err.println("[OUTER] NOT CLIPPING SEGEMNT "+s.pos);
//			}
//			return false;
//		}
//		if(debug) {
//			System.err.println("[OUTER] CLIPPING SEGEMNT "+s.pos);
//		}
		
		InnerSegmentIterator innerSegmentIterator = (InnerSegmentIterator) rayResult.innerSegmentIterator;
		innerSegmentIterator.tests = 0;
		innerSegmentIterator.currentSeg = s;
		innerSegmentIterator.collisionObject = collisionObject;
		innerSegmentIterator.fromA = fromA;
		innerSegmentIterator.toA = toA;
		innerSegmentIterator.hitSignal = false;
		innerSegmentIterator.result = result;
		innerSegmentIterator.testCubes = testCubes;
		innerSegmentIterator.debug = debug;
		innerSegmentIterator.v = v;
		innerSegmentIterator.rayResult = rayResult;
//		System.err.println("RAY SCALE: "+Vector3fTools.diffLength(fromA.origin, toA.origin)+"; "+Vector3fTools.diffLength(v.rayModFrom, v.rayModTo));
		v.solveBlock.initializeBlockGranularity(v.rayModFrom, v.rayModTo, v.segTrans);
		if (!v.solve.ok) {
			System.err.println("[PHYSICS] CastTestTrav " + v.cubesB.getSegmentBuffer().getSegmentController().getState()
					+ " " + fromA.origin + " -> " + toA.origin + " for chubes: " + testCubes.origin + ": "
					+ v.cubesB.getSegmentBuffer().getSegmentController());
		}
		
		innerSegmentIterator.segmentController = v.cubesB.getSegmentBuffer().getSegmentController();
		v.solveBlock.traverseSegmentsOnRay(innerSegmentIterator);
		innerSegmentIterator.segmentController = null;

		if(debug) {
			System.err.println("[OUTER] AFTER INNER TEST. HIS SIGNAL: "+innerSegmentIterator.hitSignal+"; InnerHit: "+innerSegmentIterator.rayResult.hasHit()+"; InnerHitSeg: "+innerSegmentIterator.rayResult.getSegment());
		}
		
		return innerSegmentIterator.hitSignal;
	}

}