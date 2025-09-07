package org.schema.game.common.data.physics;

import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.SegmentBufferIteratorInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.KinematicCharacterControllerExt.KinematicClosestNotMeConvexResultCallback;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.input.Keyboard;

import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ConvexResultCallback;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalConvexResult;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.ConvexCast;
import com.bulletphysics.collision.narrowphase.GjkConvexCast;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.narrowphase.VoronoiSimplexSolver;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectPool;

import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

/**
 * SubsimplexConvexCast implements Gino van den Bergens' paper
 * "Ray Casting against bteral Convex Objects with Application to Continuous Collision Detection"
 * GJK based Ray Cast, optimized version
 * Objects should not start in overlap, otherwise results are not defined.
 *
 * @author jezek2
 */
public class SubsimplexCubesCovexCast extends ConvexCast {

	public static String mode;
	private static ThreadLocal<CubeConvexCastVariableSet> threadLocal = new ThreadLocal<CubeConvexCastVariableSet>() {
		@Override
		protected CubeConvexCastVariableSet initialValue() {
			return new CubeConvexCastVariableSet();
		}
	};
	ObjectPool<Vector3b> pool = ObjectPool.get(Vector3b.class);
	ObjectPool<SortEntry> pool4 = ObjectPool.get(SortEntry.class);
	ObjectPool<AABBb> aabbpool = ObjectPool.get(AABBb.class);
	CubeRayCastResult rayResult;
	ConvexResultCallback resultCallback;
	private CubeConvexCastVariableSet v;
	private OuterSegmentHandler outerSegmentHandler;
	private long time;
	public SubsimplexCubesCovexCast(ConvexShape shapeA, CollisionShape shapeB, CollisionObject cubesObject, SimplexSolverInterface simplexSolver, ConvexResultCallback resultCallback, CubeRayCastResult rayResult) {
		this.v = threadLocal.get();
		v.shapeA = shapeA;
		v.cubesB = (CubeShape) shapeB;
		v.cubesObject = cubesObject;
		this.resultCallback = resultCallback;
		v.simplexSolver = simplexSolver;
		this.rayResult = rayResult;
		this.outerSegmentHandler = new OuterSegmentHandler();
	}

	private void releaseBuffers() {
		for (SortEntry b : v.sorted) {
			pool4.release(b);
		}
		v.sorted.clear();

		for (AABBb aabb : v.sortedAABB.values()) {
			pool.release(aabb.min);
			pool.release(aabb.max);
			aabbpool.release(aabb);
		}
		v.sortedAABB.clear();
	}

	@Override
	public boolean calcTimeOfImpact(Transform fromA, Transform toA, Transform testCubes, Transform toCubes, CastResult result) {

		//fromCubes and to cubes are the same since the cubes shape is not handled as a ray

		//		if(this.rayResult != null && this.rayResult.isRespectShields() && checkExplicitCollision(fromA, toA, toCubes, v.cubesB.getSegmentBuffer().getSegmentController(), result)){
		//			return true;
		//		}
		
		v.sorted.clear();

		this.time = System.currentTimeMillis();

		v.shapeA.getAabb(fromA, v.convexFromAABBMin, v.convexFromAABBMax);
		v.shapeA.getAabb(toA, v.convexToAABBMin, v.convexToAABBMax);

		//combine AABB to wrap an AABB around the whole cast
		combineAabb(v.convexFromAABBMin, v.convexFromAABBMax, v.convexToAABBMin, v.convexToAABBMax, v.castedAABBMin, v.castedAABBMax);

		v.castedAABBMin.sub(new Vector3f(0.2f, 0.2f, 0.2f));
		v.castedAABBMax.add(new Vector3f(0.2f, 0.2f, 0.2f));

		outerSegmentHandler.fromA = fromA;
		outerSegmentHandler.result = result;
		outerSegmentHandler.testCubes = testCubes;
		outerSegmentHandler.toA = toA;
		outerSegmentHandler.hitSignal = false;

		v.box0.setMargin(0.01f);

		v.cubesB.getAabb(testCubes, v.outer.min, v.outer.max);
		v.inner.min.set(v.castedAABBMin);
		v.inner.max.set(v.castedAABBMax);

		BoundingBox intersection = v.inner.getIntersection(v.outer, v.outBB);
		if (intersection == null || !v.outBB.isValid()) {
//			if(v.shapeA instanceof SphereShape){
//						System.err.println("Exception intersection is null while AABB overlap");
//			}
			return false;
		}
		
		v.inv.set(testCubes);
		v.inv.inverse();

		v.localOutBB.set(v.outBB);

		AabbUtil2.transformAabb(v.localOutBB.min, v.localOutBB.max, 0.1f,
				v.inv,
				v.outBB.min, v.outBB.max);
		v.outBB.min.x += SegmentData.SEG_HALF;
		v.outBB.min.y += SegmentData.SEG_HALF;
		v.outBB.min.z += SegmentData.SEG_HALF;
		v.outBB.max.x += SegmentData.SEG_HALF;
		v.outBB.max.y += SegmentData.SEG_HALF;
		v.outBB.max.z += SegmentData.SEG_HALF;

		v.minBlockA.x = FastMath.fastFloor(v.outBB.min.x);
		v.minBlockA.y = FastMath.fastFloor(v.outBB.min.y);
		v.minBlockA.z = FastMath.fastFloor(v.outBB.min.z);

		v.maxBlockA.x = FastMath.fastCeil(v.outBB.max.x);
		v.maxBlockA.y = FastMath.fastCeil(v.outBB.max.y);
		v.maxBlockA.z = FastMath.fastCeil(v.outBB.max.z);

		int blocksToCheck = (v.maxBlockA.x - v.minBlockA.x) * (v.maxBlockA.y - v.minBlockA.y) * (v.maxBlockA.z - v.minBlockA.z);
		if (blocksToCheck < 32) {
			return checkSimple(fromA, toA, v.minBlockA, v.maxBlockA, v.cubesB, testCubes, result, v.shapeA);
		} else {
			
			v.minIntA.x = ByteUtil.divUSeg(v.minBlockA.x) * SegmentData.SEG;
			v.minIntA.y = ByteUtil.divUSeg(v.minBlockA.y) * SegmentData.SEG;
			v.minIntA.z = ByteUtil.divUSeg(v.minBlockA.z) * SegmentData.SEG;

			v.maxIntA.x = (FastMath.fastCeil((v.outBB.max.x) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntA.y = (FastMath.fastCeil((v.outBB.max.y) / SegmentData.SEGf)) * SegmentData.SEG;
			v.maxIntA.z = (FastMath.fastCeil((v.outBB.max.z) / SegmentData.SEGf)) * SegmentData.SEG;


			long amountOfSegmentsToTest = ((v.maxIntA.x - v.minIntA.x) * (v.maxIntA.y - v.minIntA.y) * (v.maxIntA.z - v.minIntA.z)) / SegmentData.SEG;
			if (amountOfSegmentsToTest > 10000) {
				System.err.println("[SubSimplexConvexCubes][WARNING] more then 10000 segments to test: " + amountOfSegmentsToTest + " -> intersection [" + v.minIntA + ", " + v.maxIntA + "]");
			}

			v.absolute1.set(testCubes.basis);
			MatrixUtil.absolute(v.absolute1);
			v.cubesB.getSegmentBuffer().iterateOverNonEmptyElementRange(outerSegmentHandler, v.minIntA, v.maxIntA, false);
			
			if (outerSegmentHandler.hitSignal) {
				//			System.err.println("INTERSECTION FOR "+intersection);
				return true;
			}

			return false;
		}
	}

	private boolean checkSimple(Transform fromA, Transform toA,
	                            Vector3i min, Vector3i max, CubeShape cubeShape, Transform cubesTrans, CastResult result, ConvexShape convexShape) {

		for (int z = min.z; z < max.z + 1; z++) {
			for (int y = min.y; y < max.y + 1; y++) {
				for (int x = min.x; x < max.x + 1; x++) {

					v.absPos.set(x, y, z);

					boolean exists = cubeShape.getSegmentBuffer().existsPointUnsave(v.absPos);
					if (exists) {
						SegmentPiece pointUnsave = cubeShape.getSegmentBuffer().getPointUnsave(v.absPos, v.tmpPice);
						short type = pointUnsave.getType();

						ElementInformation info;
						//typeExists AND (door -> isActive)
						if (type != 0 && ElementInformation.isPhysical(pointUnsave) && (info = ElementKeyMap.getInfo(type)).lodCollisionPhysical && info.isPhysical()) {
							assert(info.getId() != ElementKeyMap.SIGNAL_TRIGGER_AREA);
							assert (!pointUnsave.getSegment().isEmpty()) : type;
							//								System.err.println("POINT CHECKED: "+x+", "+y+", "+z+" :: "+type+"; "+cubeShape.getSegmentBuffer().getSegmentController().getState());

							byte orientation = pointUnsave.getOrientation();
							boolean active = pointUnsave.isActive();
							//						if( SegmentData.allNeighborsInside(v.elemA.x, v.elemA.y, v.elemA.z)  &&
							//								data0.containsUnsave((byte) (v.elemA.x+1), v.elemA.y, v.elemA.z) &&
							//								data0.containsUnsave((byte) (v.elemA.x-1), v.elemA.y, v.elemA.z) &&
							//								data0.containsUnsave(v.elemA.x, (byte) (v.elemA.y+1), v.elemA.z) &&
							//								data0.containsUnsave(v.elemA.x, (byte) (v.elemA.y-1), v.elemA.z) &&
							//								data0.containsUnsave(v.elemA.x, v.elemA.y, (byte) (v.elemA.z+1)) &&
							//								data0.containsUnsave(v.elemA.x, v.elemA.y, (byte) (v.elemA.z-1))
							//								){
							//							continue;
							//						}
							v.elemPosA.set(x - SegmentData.SEG_HALF, y - SegmentData.SEG_HALF, z - SegmentData.SEG_HALF);
							//								v.elemPosA.x += data0.getSegment().pos.x;
							//								v.elemPosA.y += data0.getSegment().pos.y;
							//								v.elemPosA.z += data0.getSegment().pos.z;

							v.nA.set(v.elemPosA);
							v.tmpCubesTrans.set(cubesTrans);
							v.tmpCubesTrans.basis.transform(v.nA);
							v.tmpCubesTrans.origin.add(v.nA);

							v.box0.getAabb(v.tmpCubesTrans, v.outMin, v.outMax);

							v.normal.set(0, 0, 0);

							//								if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							//									DebugBox b1 = new DebugBox(
							//											new Vector3f(-0.6f, -0.6f, -0.6f),
							//											new Vector3f(0.6f, 0.6f, 0.6f),
							//											new Transform(v.tmpCubesTrans),
							//											1f, 1f, 1f, 1);
							//									DebugDrawer.boxes.add(b1);
							//								}
							//							System.err.println("testing "+elemA+": "+rayFromTrans.origin+"; "+rayToTrans.origin+"; "+cubesTrans.origin);
							if (AabbUtil2.testAabbAgainstAabb2(v.outMin, v.outMax, v.castedAABBMin, v.castedAABBMax)) {
								if (resultCallback instanceof ClosestConvexResultCallbackExt) {
									ClosestConvexResultCallbackExt c = (ClosestConvexResultCallbackExt) resultCallback;
									if (c.checkHasHitOnly) {
										c.userData = pointUnsave.getSegment().getSegmentController();
										resultCallback.closestHitFraction = 0.5f;
										return false;
									}
								}

								//									if(drawDebug()){
								//										System.err.println("--PERFORMING NARROW TEST OK "+relLinVel+": "+v.elemA);
								//									}
								//							System.err.println("TESTING (contains)"+posCachePointer+": "+elemA);
								v.distTest.set(fromA.origin);
								v.distTest.sub(v.tmpCubesTrans.origin);

								SortEntry point = pool4.get();

								point.x = pointUnsave.x;
								point.y = pointUnsave.y;
								point.z = pointUnsave.z;
								point.info = info;
								if(info.getId() == ElementKeyMap.CARGO_SPACE){
									if(orientation == 4){
										continue;
									}
									point.orientation = Element.TOP;
								}else{
									point.orientation = orientation;
								}
								point.blockStyle = info.blockStyle;
								point.slab = (byte) info.getSlab(orientation);
								point.active = active;
								point.segmentData = pointUnsave.getSegment().getSegmentData();
								assert (point.segmentData != null) : pointUnsave.getSegment() + "; " + pointUnsave.getSegment().isEmpty();
								float length = v.distTest.length();
								point.length = length;
								int tries = 0;
								while (v.sorted.contains(point) && tries < 100) {
									point.length += 0.1f;
									tries++;
								}
								if (tries >= 100) {
									System.err.println("[SUBSIMPLEX][WARNING] more than 100 tries in sorted");
								}
								v.sorted.add(point);
								v.posCachePointer++;
								//										if(data0.getSegmentController().getState() instanceof ServerStateInterface){
								//											System.err.println("added: "+length+": "+elemA +" -- "+sorted);
								//										}
							}

						}
					}

				}
			}
		}
		boolean hit = solveSorted(fromA, toA, cubesTrans, result, cubeShape.getSegmentBuffer().getSegmentController(), convexShape);

		releaseBuffers();

		return hit;
	}

	public float calculateTimeOfImpact(CollisionObject col0, CollisionObject col1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {

		// Rather then checking ALL pairs, only calculate TOI when motion exceeds threshold

		// Linear motion for one of objects needs to exceed m_ccdSquareMotionThreshold
		// col0->m_worldTransform,
		float resultFraction = 1f;

		v.tmp.sub(col0.getInterpolationWorldTransform(v.tmpTrans1).origin, col0.getWorldTransform(v.tmpTrans2).origin);
		float squareMot0 = v.tmp.lengthSquared();

		v.tmp.sub(col1.getInterpolationWorldTransform(v.tmpTrans1).origin, col1.getWorldTransform(v.tmpTrans2).origin);
		float squareMot1 = v.tmp.lengthSquared();

		if (squareMot0 < col0.getCcdSquareMotionThreshold() &&
				squareMot1 < col1.getCcdSquareMotionThreshold()) {
			return resultFraction;
		}

		if (v.disableCcd) {
			return 1f;
		}

		// An adhoc way of testing the Continuous Collision Detection algorithms
		// One object is approximated as a sphere, to simplify things
		// Starting in penetration should report no time of impact
		// For proper CCD, better accuracy and handling of 'allowed' penetration should be added
		// also the mainloop of the physics should have a kind of toi queue (something like Brian Mirtich's application of Timewarp for Rigidbodies)

		// Convex0 against sphere for Convex1
		{
			ConvexShape convex0 = (ConvexShape) col0.getCollisionShape();

			SphereShape sphere1 = new SphereShape(col1.getCcdSweptSphereRadius()); // todo: allow non-zero sphere sizes, for better approximation
			ConvexCast.CastResult result = new ConvexCast.CastResult();
			VoronoiSimplexSolverExt voronoiSimplex = new VoronoiSimplexSolverExt();
			//SubsimplexConvexCast ccd0(&sphere,min0,&voronoiSimplex);
			///Simplification, one object is simplified as a sphere
			GjkConvexCast ccd1 = new GjkConvexCast(convex0, sphere1, voronoiSimplex);
			//ContinuousConvexCollision ccd(min0,min1,&voronoiSimplex,0);
			if (ccd1.calcTimeOfImpact(col0.getWorldTransform(v.tmpTrans1), col0.getInterpolationWorldTransform(v.tmpTrans2),
					col1.getWorldTransform(v.tmpTrans3), col1.getInterpolationWorldTransform(v.tmpTrans4), result)) {
				// store result.m_fraction in both bodies

				if (col0.getHitFraction() > result.fraction) {
					col0.setHitFraction(result.fraction);
				}

				if (col1.getHitFraction() > result.fraction) {
					col1.setHitFraction(result.fraction);
				}

				if (resultFraction > result.fraction) {
					resultFraction = result.fraction;
				}
			}
		}

		// Sphere (for convex0) against Convex1
		{
			ConvexShape convex1 = (ConvexShape) col1.getCollisionShape();

			SphereShape sphere0 = new SphereShape(col0.getCcdSweptSphereRadius()); // todo: allow non-zero sphere sizes, for better approximation
			ConvexCast.CastResult result = new ConvexCast.CastResult();
			VoronoiSimplexSolver voronoiSimplex = new VoronoiSimplexSolver();
			//SubsimplexConvexCast ccd0(&sphere,min0,&voronoiSimplex);
			///Simplification, one object is simplified as a sphere
			GjkConvexCast ccd1 = new GjkConvexCast(sphere0, convex1, voronoiSimplex);
			//ContinuousConvexCollision ccd(min0,min1,&voronoiSimplex,0);
			if (ccd1.calcTimeOfImpact(col0.getWorldTransform(v.tmpTrans1), col0.getInterpolationWorldTransform(v.tmpTrans2),
					col1.getWorldTransform(v.tmpTrans3), col1.getInterpolationWorldTransform(v.tmpTrans4), result)) {
				//store result.m_fraction in both bodies

				if (col0.getHitFraction() > result.fraction) {
					col0.setHitFraction(result.fraction);
				}

				if (col1.getHitFraction() > result.fraction) {
					col1.setHitFraction(result.fraction);
				}

				if (resultFraction > result.fraction) {
					resultFraction = result.fraction;
				}

			}
		}

		return resultFraction;
	}

	private void combineAabb(Vector3f combineAMin, Vector3f combineAMax, Vector3f combineBMin, Vector3f combineBMax, Vector3f outMin, Vector3f outMax) {
		outMin.x = Math.min(combineAMin.x, combineBMin.x);
		outMin.y = Math.min(combineAMin.y, combineBMin.y);
		outMin.z = Math.min(combineAMin.z, combineBMin.z);

		outMax.x = Math.max(combineAMax.x, combineBMax.x);
		outMax.y = Math.max(combineAMax.y, combineBMax.y);
		outMax.z = Math.max(combineAMax.z, combineBMax.z);
	}

	private boolean doNarrorTest(Transform rayFromTrans, Transform rayToTrans, Transform cubesTrans, SegmentData data0, CastResult castResult, Vector3b startA, Vector3b endA) {
		
		//		if(isOnServer()){
		//			Transform t = new Transform(cubesTrans);
		//
		//			Vector3f p = new Vector3f(data0.getSegment().pos.x, data0.getSegment().pos.y, data0.getSegment().pos.z);
		//			t.basis.transform(p);
		//			t.origin.add(p);
		//			DebugBox bb = new DebugBox(
		//					new Vector3f(startA.x-0.5f-0.02f, startA.y-0.5f-0.02f, startA.z-0.5f-0.02f),
		//					new Vector3f(endA.x-0.5f+0.02f, endA.y-0.5f+0.02f, endA.z-0.5f+0.02f ),
		//					t,
		//					1, 0, 0, 1);
		//			DebugDrawer.boxes.add(bb);
		//		}
		v.posCachePointer = 0;
		for (byte x = startA.x; x < endA.x; x++) {
			for (byte y = startA.y; y < endA.y; y++) {
				for (byte z = startA.z; z < endA.z; z++) {
					v.elemA.set((byte) (x + SegmentData.SEG_HALF), (byte) (y + SegmentData.SEG_HALF), (byte) (z + SegmentData.SEG_HALF));

					int eA = SegmentData.getInfoIndex(v.elemA.x, v.elemA.y, v.elemA.z);

					short type = data0.getType(eA);

					ElementInformation info;
					//typeExists AND (door -> isActive)
					if ((type != 0 && (info = ElementKeyMap.getInfo(type)).isPhysical()) && (ElementInformation.isAlwaysPhysical(type) || ElementKeyMap.getInfo(type).isPhysical(data0.isActive(eA)))  && (!info.hasLod() || info.lodCollisionPhysical)) {
						assert(info.getId() != ElementKeyMap.SIGNAL_TRIGGER_AREA);
						byte orientation = data0.getOrientation(eA);
						boolean active = data0.isActive(eA);
						v.elemPosA.set(x, y, z);
						v.elemPosA.x += data0.getSegment().pos.x;
						v.elemPosA.y += data0.getSegment().pos.y;
						v.elemPosA.z += data0.getSegment().pos.z;

						v.nA.set(v.elemPosA);
						v.tmpCubesTrans.set(cubesTrans);
						v.tmpCubesTrans.basis.transform(v.nA);
						v.tmpCubesTrans.origin.add(v.nA);

						v.box0.getAabb(v.tmpCubesTrans, v.outMin, v.outMax);

						v.normal.set(0, 0, 0);

						if (AabbUtil2.testAabbAgainstAabb2(v.outMin, v.outMax, v.castedAABBMin, v.castedAABBMax)) {
							if (resultCallback instanceof ClosestConvexResultCallbackExt) {
								ClosestConvexResultCallbackExt c = (ClosestConvexResultCallbackExt) resultCallback;
								if (c.checkHasHitOnly) {
									c.userData = data0.getSegmentController();
									resultCallback.closestHitFraction = 0.5f;
									return false;
								}
							}


							v.distTest.set(rayFromTrans.origin);
							v.distTest.sub(v.tmpCubesTrans.origin);

							SortEntry point = pool4.get();

							point.x = v.elemA.x;
							point.y = v.elemA.y;
							point.z = v.elemA.z;
							if(info.getId() == ElementKeyMap.CARGO_SPACE){
								if(orientation == 4){
									continue;
								}
								point.orientation = Element.TOP;
							}else{
								point.orientation = orientation;
							}
							point.slab = (byte)info.getSlab(orientation);
							point.blockStyle = info.blockStyle;
							point.info = info;
							point.active = active;
							point.segmentData = data0;

							float length = v.distTest.length();
							point.length = length;
							int tries = 0;
							while (v.sorted.contains(point) && tries < 100) {
								point.length += 0.1f;
								tries++;
							}
							if (tries >= 100) {
								System.err.println("[SUBSIMPLEX][WARNING] more than 100 tries in sorted");
							}
							v.sorted.add(point);
							v.posCachePointer++;
						}

					}
					//
				}
			}
		}
		//				if(sorted.isEmpty()){
		//					if(data0.getSegmentController().getState() instanceof ClientStateInterface){
		//					System.err.println("NO candidates");
		//					}
		//				}
		return true;
	}

	public boolean drawDebug() {
		return v.cubesB.getSegmentBuffer().getSegmentController().isOnServer() && mode.equals("UP")
				&& Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)
				&& !Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
	}

	public boolean isOnServer() {
		return v.cubesB.getSegmentBuffer().getSegmentController().isOnServer();
	}

	private boolean solveSorted(Transform fromA, Transform toA, Transform testCubes, CastResult result, SegmentController segmentController, ConvexShape convexShape) {
		if (v.sorted.size() > 1000) {
			System.err.println("[" + (isOnServer() ? "SERVER" : "CLIENT") + "][CubeConvex] DOING " + v.sorted.size() + " box tests " + segmentController + " -> " + convexShape);
		}

		int hits = 0;
		boolean hit = false;
		ObjectBidirectionalIterator<SortEntry> iterator = v.sorted.iterator();
		while (iterator.hasNext()) {
			SortEntry e = iterator.next();
			v.elemA.set(e.x, e.y, e.z);
			SegmentData data0 = e.segmentData;
			assert (data0 != null);
			assert (data0.getSegment() != null);
			float color = 0;

			BlockStyle blockStyle = e.blockStyle;
			boolean active = e.active;
			int orientation = e.orientation;
			v.elemPosA.set(
					v.elemA.x - SegmentData.SEG_HALF,
					v.elemA.y - SegmentData.SEG_HALF,
					v.elemA.z - SegmentData.SEG_HALF);

			v.elemPosA.x += data0.getSegment().pos.x;// + ((float)Element.HALF_SIZE/2f);
			v.elemPosA.y += data0.getSegment().pos.y;// + ((float)Element.HALF_SIZE/2f);
			v.elemPosA.z += data0.getSegment().pos.z;// + ((float)Element.HALF_SIZE/2f);

			v.nA.set(v.elemPosA);
			v.tmpCubesTrans.set(testCubes);
			v.tmpCubesTrans.basis.transform(v.nA);
			v.tmpCubesTrans.origin.add(v.nA);

			if(this.resultCallback instanceof ClosestConvexResultCallbackExt && ((ClosestConvexResultCallbackExt)this.resultCallback).sphereOverlapping != null){
				((ClosestConvexResultCallbackExt)this.resultCallback).sphereOverlapping.add(new Vector3f(v.tmpCubesTrans.origin));
				
				((ClosestConvexResultCallbackExt)this.resultCallback).sphereOverlappingNormals.add(GlUtil.getUpVector(new Vector3f(), testCubes.basis));

				((ClosestConvexResultCallbackExt)this.resultCallback).closestHitFraction = 0.1f;
				continue;
			}
			
			v.simplexSolver.reset();

			//				if(data0.getSegmentController().isOnServer()){
			//					DebugBox b2 = new DebugBox(
			//							new Vector3f(-0.01f-0.5f, -0.01f-0.5f, -0.01f-0.5f),
			//							new Vector3f(1.01f-0.5f, 1.01f-0.5f, 1.01f-0.5f),
			//							new Transform(v.tmpTrans),
			//							1.0f, 1.0f, 1.0f, 1);
			//							DebugDrawer.boxes.add(b2);
			//				}

			
			CollisionShape cShape = v.box0;
			
			if(e.info.lodUseDetailCollision) {
				cShape = e.info.lodDetailCollision.getShape(e.info.id, (byte) orientation, v.lodBlockTransform);
			}else if (blockStyle.solidBlockStyle) {
				//				System.err.println("COLLIDING EXTRA A "+blockStyle+"; "+orientation);
				cShape = BlockShapeAlgorithm.getShape(blockStyle, (byte) orientation);
			}
			
			if(cShape == null) {
				System.err.println("[PHYSICS][ERROR] InnerSegmentIterator: Shape null: UseDetail: "+e.info.lodUseDetailCollision+"; Type: "+e.info.lodDetailCollision.type.name()+"; Block: "+e.info.name+" ("+e.info.id+"); Orientation: "+orientation);
				cShape = v.box0;
			}
			Transform boxTransform = v.tmpCubesTrans;
			if(e.slab > 0){
				boxTransform = v.BT;
				boxTransform.set(v.tmpCubesTrans);
				
				v.orientTT.set(Element.DIRECTIONSf[Element.switchLeftRight(orientation%6)]);
				boxTransform.basis.transform(v.orientTT);
				switch(e.slab) {
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
//				System.err.println("CC SLAPPP "+e.slab+"; "+v.orientTT+"; "+((BoxShape)shape).getHalfExtentsWithMargin(new Vector3f()));
				boxTransform.origin.sub(v.orientTT);
				
			}
			CastResult castResult = new CastResult();
			castResult.allowedPenetration = result.allowedPenetration;
			castResult.fraction = 1f; // ??
			
			boolean res;
			
			
			if(cShape instanceof CompoundShape) {
				/*
				 * do a regular collision for all subshapes. this is unsave and required the
				 * hirachy to be flat in that the compound shape may only have convexShape
				 * childs
				 */
				CompoundShape cs = (CompoundShape)cShape;
				
				for(int c = 0; c < cs.getNumChildShapes(); c++) {
					CompoundShapeChild child = cs.getChildList().get(c);
					
					v.boxETransform.set(boxTransform);
					v.boxETransform.mul(v.lodBlockTransform);
					v.boxETransform.mul(child.transform); //mul local tranform of compound child
					
					ConvexShape shape = (ConvexShape)child.childShape;
					
					ContinuousConvexCollision convexCaster = new ContinuousConvexCollision(v.shapeA, shape, v.simplexSolver, v.gjkEpaPenetrationDepthSolver);
					res = convexCaster.calcTimeOfImpact(fromA, toA, v.boxETransform, v.boxETransform, castResult, v.gjkVar);
					
					if (res) {
						if (this.resultCallback == null) {
							// this is a rayCast
							if (this.rayResult != null) {
								this.rayResult.setSegment(data0.getSegment());
								this.rayResult.getCubePos().set(v.elemA);
							}
							return true;
						} else {
							// this is a convex cast
							// add hit
							if (castResult.normal.lengthSquared() > 0.0001f) {
								if (castResult.fraction <= this.resultCallback.closestHitFraction) {
									//								if(data0.getSegmentController().isOnServer()){
									//									System.err.println("COL: "+i+": "+result.fraction+" / "+this.resultCallback.closestHitFraction+" :::: "+result.normal+": "+castResult.normal.lengthSquared());
									//								}
									//								if(data0.getSegmentController().isOnServer()){
									//									DebugBox b2 = new DebugBox(
									//											new Vector3f(-0.02f-0.5f, -0.02f-0.5f, -0.02f-0.5f),
									//											new Vector3f(1.02f-0.5f, 1.02f-0.5f, 1.02f-0.5f),
									//											new Transform(v.tmpTrans),
									//											1.0f, color, 0.0f, 1);
									//											DebugDrawer.boxes.add(b2);
									//											color++;
									//								}
									result.normal.normalize();
									LocalConvexResult localConvexResult = new LocalConvexResult(v.cubesObject, null, castResult.normal, castResult.hitPoint, castResult.fraction);

									boolean normalInWorldSpace = true;
									if (this.resultCallback instanceof KinematicClosestNotMeConvexResultCallback) {
										((KinematicClosestNotMeConvexResultCallback) this.resultCallback).addSingleResult(localConvexResult, normalInWorldSpace, data0.getSegment(), v.elemA);
									} else {
										this.resultCallback.addSingleResult(localConvexResult, normalInWorldSpace);
									}
									if (resultCallback instanceof ClosestConvexResultCallbackExt) {
										((ClosestConvexResultCallbackExt) resultCallback).userData = data0.getSegmentController();
									}
									hit = true;
								} else {
									//								if(data0.getSegmentController().isOnServer()){
									//									System.err.println("DISCARDED: "+i+": "+result.fraction+" / "+this.resultCallback.closestHitFraction);
									//								}
								}
							}
						}
					}
				}
			}else {
				//do only one collision
				ConvexShape shape = (ConvexShape)cShape;
				
				ContinuousConvexCollision convexCaster = new ContinuousConvexCollision(v.shapeA, shape, v.simplexSolver, v.gjkEpaPenetrationDepthSolver);
				res = convexCaster.calcTimeOfImpact(fromA, toA, boxTransform, boxTransform, castResult, v.gjkVar);
				if (res) {
					if (this.resultCallback == null) {
						// this is a rayCast
						if (this.rayResult != null) {
							this.rayResult.setSegment(data0.getSegment());
							this.rayResult.getCubePos().set(v.elemA);
						}
						return true;
					} else {
						// this is a convex cast
						// add hit
						if (castResult.normal.lengthSquared() > 0.0001f) {
							if (castResult.fraction <= this.resultCallback.closestHitFraction) {
								//								if(data0.getSegmentController().isOnServer()){
								//									System.err.println("COL: "+i+": "+result.fraction+" / "+this.resultCallback.closestHitFraction+" :::: "+result.normal+": "+castResult.normal.lengthSquared());
								//								}
								//								if(data0.getSegmentController().isOnServer()){
								//									DebugBox b2 = new DebugBox(
								//											new Vector3f(-0.02f-0.5f, -0.02f-0.5f, -0.02f-0.5f),
								//											new Vector3f(1.02f-0.5f, 1.02f-0.5f, 1.02f-0.5f),
								//											new Transform(v.tmpTrans),
								//											1.0f, color, 0.0f, 1);
								//											DebugDrawer.boxes.add(b2);
								//											color++;
								//								}
								result.normal.normalize();
								LocalConvexResult localConvexResult = new LocalConvexResult(v.cubesObject, null, castResult.normal, castResult.hitPoint, castResult.fraction);

								boolean normalInWorldSpace = true;
								if (this.resultCallback instanceof KinematicClosestNotMeConvexResultCallback) {
									((KinematicClosestNotMeConvexResultCallback) this.resultCallback).addSingleResult(localConvexResult, normalInWorldSpace, data0.getSegment(), v.elemA);
								} else {
									this.resultCallback.addSingleResult(localConvexResult, normalInWorldSpace);
								}
								if (resultCallback instanceof ClosestConvexResultCallbackExt) {
									((ClosestConvexResultCallbackExt) resultCallback).userData = data0.getSegmentController();
								}
								hit = true;
							} else {
								//								if(data0.getSegmentController().isOnServer()){
								//									System.err.println("DISCARDED: "+i+": "+result.fraction+" / "+this.resultCallback.closestHitFraction);
								//								}
							}
						}
					}
				}
			}
			

			
		}
		return hit;
	}

	private boolean performCastTest(Segment s, ConvexShape convexShape,
	                                Transform fromA, Transform toA, Transform testCubes,
	                                CastResult result) {

		SegmentData data0 = s.getSegmentData();
		//		System.err.println("RAY HIT BB of "+s.pos+" - "+data0.getSegmentController());
		if (!v.intersectionCallBack.initialized) {
			v.intersectionCallBack.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
		}
		v.intersectionCallBack.reset();

		v.intersectionCallBack = data0.getOctree().findIntersectingAABB(
				data0.getOctree().getSet(), v.intersectionCallBack, data0.getSegment(), testCubes, v.absolute1, 0f, v.castedAABBMin, v.castedAABBMax, 1.0f);

		int hitCount = 0;
		
		boolean doNarrorTest = true;
		if (v.intersectionCallBack.hitCount > 0) {
			for (int i = 0; i < v.intersectionCallBack.hitCount; i++) {
				v.intersectionCallBack.getHit(i, v.minOut, v.maxOut, v.startOut, v.endOut);

				v.dist.set(v.maxOut);
				v.dist.sub(v.minOut);
				float len = v.dist.length();
				v.dist.normalize();
				v.dist.scale(len / 2);
				v.minOut.add(v.dist);

				// from -> middle of AABB
				v.dist.sub(v.minOut, fromA.origin);
				float length = v.dist.lengthSquared() * 1000f;
				int tries = 0;
				while (v.sortedAABB.containsKey(length) && !Float.isNaN(length) && !Float.isInfinite(length) && tries < 1000) {
					length += 0.1f;
					tries++;
				}
				if (tries > 100) {
					System.err.println("[CubesConvex][WARNING] extended more then 100 AABBs length: " + v.sortedAABB.size() + ": " + hitCount);
				}
				if (Float.isNaN(length) || Float.isInfinite(length)) {
					continue;
				}
				if (hitCount > 1000) {
					System.err.println("[CubesConvex][WARNING] testing more then 1000 AABBs: " + v.sortedAABB.size() + ": " + hitCount);
				}
				AABBb aabBb = aabbpool.get();
				Vector3b st = pool.get();
				Vector3b en = pool.get();
				st.set(v.startOut);
				en.set(v.endOut);
				aabBb.min = st;
				aabBb.max = en;
				v.sortedAABB.put(length, aabBb);
				hitCount++;
			}
			if (v.sortedAABB.size() > 1000) {
				System.err.println("[CubesConvex][WARNING] testing more then 1000 AABBs: " + v.sortedAABB.size());
			}
			
			for (Entry<Float, AABBb> aabb : v.sortedAABB.entrySet()) {
				if (doNarrorTest) {
					doNarrorTest = doNarrorTest(fromA, toA, testCubes, data0, result, aabb.getValue().min, aabb.getValue().max);

					//if hitonly, the function will break next loop

				} else {
					break;
				}
				//continue even when narrow test is false
				//to clean the pool
			}

		} else {
			//					if(data0.getSegmentController().getState() instanceof ServerStateInterface){
			//					System.err.println("OCTREE TEST FAILED");
			//					}
		}
		if (!doNarrorTest) {
			//hitonly
			return true;
		}
		
		return solveSorted(fromA, toA, testCubes, result, s.getSegmentController(), convexShape);

	}

	private class OuterSegmentHandler implements SegmentBufferIteratorInterface {

		private Transform fromA;
		private Transform toA;
		private Transform testCubes;
		private CastResult result;
		private boolean hitSignal;

		@Override
		public boolean handle(Segment sOuter, long lastChanged) {

			if (sOuter.getSegmentData() == null || sOuter.isEmpty()) {
				return true;
			}
			v.cubesB.getSegmentAabb(sOuter, testCubes, v.outMin, v.outMax, v.localMinOut, v.localMaxOut, v.aabbVarSet);
			v.hitLambda[0] = 1;
			v.normal.set(0, 0, 0);

			//only test, if either point is inside bb
			boolean hitBox = AabbUtil2.testAabbAgainstAabb2(v.outMin, v.outMax, v.castedAABBMin, v.castedAABBMax);
			//			boolean hitBox = AabbUtil2.rayAabb(fromA.origin, toA.origin, outMin, outMax, hitLambda, normal);

			if (hitBox) {
				
				boolean hit = performCastTest(sOuter, v.shapeA, fromA, toA, testCubes, result);

				
				releaseBuffers();

				if (hit) {
					/*
					 * only return, when hit detected,
					 * else, test other segments for hits.
					 *
					 * If returned in the first hit.
					 * rays might get fooled being in
					 * an empty AABB just before the next one
					 *
					 */

					hitSignal = true;
					if (resultCallback instanceof ClosestConvexResultCallbackExt) {
						if (((ClosestConvexResultCallbackExt) resultCallback).checkHasHitOnly) {
							return false;
						}
					}
					//					return false; //don't continue to iterate
				} else {
					//					System.err.println("cast Test FAILED");
				}
			} else {
				//				if(sOuter.getSegmentController().getState() instanceof ServerStateInterface){
				//					System.err.println("HITBOX FAILED ");
				//				}
			}
			return true;
		}

	}
}
