package org.schema.game.common.data.physics;

import java.util.ArrayList;
import java.util.Comparator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.manager.DebugControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.power.reactor.StabilizerPath;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.common.data.world.Sector;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.physics.ClosestRayCastResultExt;
import org.schema.schine.physics.PhysicsState;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.broadphase.OverlappingPairCache;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.dispatch.SimulationIslandManager;
import com.bulletphysics.collision.narrowphase.ConvexCast;
import com.bulletphysics.collision.narrowphase.ConvexCast.CastResult;
import com.bulletphysics.collision.narrowphase.GjkEpaPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.narrowphase.VoronoiSimplexSolver;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.ContactSolverInfo;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.CProfileManager;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.MiscUtil;
import com.bulletphysics.linearmath.ScalarUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;
import com.bulletphysics.util.ObjectArrayList;

public class ModifiedDynamicsWorld extends DiscreteDynamicsWorld {

	private static final Comparator<TypedConstraint> sortConstraintOnIslandPredicate = (lhs, rhs) -> {
		int rIslandId0, lIslandId0;
		rIslandId0 = getConstraintIslandId(rhs);
		lIslandId0 = getConstraintIslandId(lhs);
		return lIslandId0 < rIslandId0 ? -1 : +1;
	};
	static Vector3f aabbHalfExtent = new Vector3f();//.alloc(Vector3f.class);

	//	/**
	//	 * convexTest performs a swept convex cast on all objects in the {@link CollisionWorld}, and calls the resultCallback
	//	 * This allows for several queries: first hit, all hits, any hit, dependent on the value return by the callback.
	//	 */
	//	public void convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, ConvexResultCallback resultCallback) {
	//
	//
	//		convexFromTrans.set(convexFromWorld);
	//		convexToTrans.set(convexToWorld);
	//
	//
	//
	//		// Compute AABB that encompasses angular movement
	//		{
	//
	//			TransformUtil.calculateVelocity(convexFromTrans, convexToTrans, 1f, linVel, angVel);
	//
	//			R.setIdentity();
	//			R.setRotation(convexFromTrans.getRotation(quat));
	//			castShape.calculateTemporalAabb(R, linVel, angVel, 1f, castShapeAabbMin, castShapeAabbMax);
	//		}
	//
	//
	//		float[] hitLambda = new float[1];
	//
	//		// go over all objects, and if the ray intersects their aabb + cast shape aabb,
	//		// do a ray-shape query using convexCaster (CCD)
	//		for (int i = 0; i < collisionObjects.size(); i++) {
	//			CollisionObject collisionObject = collisionObjects.getQuick(i);
	//
	//			// only perform raycast if filterMask matches
	//			if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
	//				//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
	//				collisionObject.getWorldTransform(tmpTrans);
	//				collisionObject.getCollisionShape().getAabb(tmpTrans, collisionObjectAabbMin, collisionObjectAabbMax);
	//				AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);
	//				hitLambda[0] = 1f; // could use resultCallback.closestHitFraction, but needs testing
	//				hitNormal.set(0,0,0);
	//				if (AabbUtil2.rayAabb(convexFromWorld.origin, convexToWorld.origin, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
	//					ModifiedDynamicsWorld.objectQuerySingle(castShape, convexFromTrans, convexToTrans,
	//					                  collisionObject,
	//					                  collisionObject.getCollisionShape(),
	//					                  tmpTrans,
	//					                  resultCallback,
	//					                  getDispatchInfo().allowedCcdPenetration);
	//				}
	//			}
	//		}
	//	}
	static Vector3f aabbCenter = new Vector3f();//new @Stack Vector3f();
	static Vector3f source = new Vector3f();//new @Stack Vector3f();
	static Vector3f target = new Vector3f();//new @Stack Vector3f();
	static Vector3f r = new Vector3f();//new @Stack Vector3f();
	static Vector3f hitNormalTmp = new Vector3f();//new @Stack Vector3f();
	public final Vector3f iAxis = new Vector3f();
	public final Quat4f iDorn = new Quat4f();
	public final Quat4f iorn0 = new Quat4f();
	public final Quat4f iPredictOrn = new Quat4f();
	public final float[] float4Temp = new float[4];
	private final Transform tmpTTTrans = new Transform();
	private final Vector3f minAabb = new Vector3f();
	private final Vector3f maxAabb = new Vector3f();
	private final Transform tmpTransAABBSingle = new Transform();
	private final Vector3f contactThreshold = new Vector3f();
	private final Transform interpolatedTransform = new Transform();
	private final Transform tmpTrans2 = new Transform();
	private final Vector3f tmpLinVel = new Vector3f();
	private final Vector3f tmpAngVel = new Vector3f();
	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.DiscreteDynamicsWorld#addRigidBody(com.bulletphysics.dynamics.RigidBody)
	 */
	//	@Override
	//	public void addRigidBody(RigidBody body) {
	////		System.err.println("[PHYSICS] added "+body);
	//		super.addRigidBody(body);
	//	}
	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.DiscreteDynamicsWorld#addRigidBody(com.bulletphysics.dynamics.RigidBody, short, short)
	 */
	//	@Override
	//	public void addRigidBody(RigidBody body, short group, short mask) {
	////		System.err.println("[PHYSICS] added "+body);
	//		super.addRigidBody(body, group, mask);
	//	}
	private final Transform childWorldTrans = new Transform();
	private final PhysicsExt physics;
	public it.unimi.dsi.fastutil.objects.ObjectArrayList<BoundingBox> cache = new it.unimi.dsi.fastutil.objects.ObjectArrayList<BoundingBox>();
	public boolean cacheValid = false;
	Transform tmpTrans = new Transform();
	Transform rayFromTrans = new Transform();
	Transform rayToTrans = new Transform();
	Vector3f collisionObjectAabbMin = new Vector3f();
	Vector3f collisionObjectAabbMax = new Vector3f();
	Vector3f collisionSubObjectAabbMin = new Vector3f();
	Vector3f collisionSubObjectAabbMax = new Vector3f();
	Vector3f hitNormal = new Vector3f();
	//	public void updateAabbs() {
	//		BulletStats.pushProfile("updateAabbs");
	//		try {
	//			for (int i=0; i<collisionObjects.size(); i++) {
	//				CollisionObject colObj = collisionObjects.getQuick(i);
	//
	//				// only update aabb of active objects
	//				if (colObj.isActive()) {
	//					System.err.println("UPDATING AABB for "+colObj);
	//					updateSingleAabb(colObj);
	//				}
	//			}
	//		}
	//		finally {
	//			BulletStats.popProfile();
	//		}
	//	}
	Transform convexFromTrans = new Transform();
	Transform convexToTrans = new Transform();
	Vector3f castShapeAabbMin = new Vector3f();
	Vector3f castShapeAabbMax = new Vector3f();
	Vector3f linVel = new Vector3f();
	Vector3f angVel = new Vector3f();
	Transform R = new Transform();
	Quat4f quat = new Quat4f();
	Transform childTrans = new Transform();
	/**
	 * rayTest performs a raycast on all objects in the CollisionWorld, and calls the resultCallback.
	 * This allows for several queries: first hit, all hits, any hit, dependent on the value returned by the callback.
	 */
	float[] hitLambda = new float[1];
	Vector3f dir = new Vector3f();
	Vector3f closest = new Vector3f();
	Vector3f closestDir = new Vector3f();
	Vector3f tmp = new Vector3f();
	private ObjectArrayList<TypedConstraint> sortedConstraints = new ObjectArrayList<TypedConstraint>();
	private final InplaceSolverIslandCallbackExt solverCallback = new InplaceSolverIslandCallbackExt();
	private int actionUpdateNum;
	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.DiscreteDynamicsWorld#updateActions(float)
	 */
	private short lastUpdate;

	public ModifiedDynamicsWorld(Dispatcher dispatcher,
	                             BroadphaseInterface pairCache, ConstraintSolver constraintSolver,
	                             CollisionConfiguration collisionConfiguration, PhysicsExt physics) {
		super(dispatcher, pairCache, constraintSolver, collisionConfiguration);
		this.physics = physics;
		islandManager = new SimulationIslandManagerExt();

	}

	private static void handleCompound(ConvexShape castShape, CompoundShape compoundShape, CollisionObject collisionObject, Transform colObjWorldTransform, float allowedPenetration, Transform convexFromTrans, Transform convexToTrans, ConvexResultCallback resultCallback) {
		//		System.err.println("HANDLE COMPOUNT: "+compoundShape.getNumChildShapes()+"; ");

		for (int i = 0; i < compoundShape.getNumChildShapes(); i++) {
			Transform childTrans = compoundShape.getChildTransform(i, new Transform());
			CollisionShape childCollisionShape = compoundShape.getChildShape(i);
			Transform childWorldTrans = new Transform();
			childWorldTrans.mul(colObjWorldTransform, childTrans);
			// replace collision shape so that callback can determine the triangle
			CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
			collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);

			objectQuerySingle(castShape, convexFromTrans, convexToTrans, collisionObject, childCollisionShape, childWorldTrans, resultCallback, allowedPenetration);

			// restore
			collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
		}
	}

	/**
	 * objectQuerySingle performs a collision detection query and calls the resultCallback. It is used internally by rayTest.
	 */
	public static void objectQuerySingle(ConvexShape castShape, Transform convexFromTrans, Transform convexToTrans, CollisionObject collisionObject, CollisionShape collisionShape, Transform colObjWorldTransform, ConvexResultCallback resultCallback, float allowedPenetration) {

		if (collisionShape instanceof CubeShape) {
			
			CastResult castResult = new CastResult();
			castResult.allowedPenetration = allowedPenetration;
			castResult.fraction = 1f; // ??

			CubeShape convexShape = (CubeShape) collisionShape;

			VoronoiSimplexSolverExt simplexSolver = new VoronoiSimplexSolverExt();
			//			GjkEpaPenetrationDepthSolver gjkEpaPenetrationSolver = new GjkEpaPenetrationDepthSolver();
			// JAVA TODO: should be convexCaster1
			//			ContinuousConvexCollision castPtr = new ContinuousConvexCollision
			//					(castShape,(ConvexShape) collisionShape,simplexSolver,gjkEpaPenetrationSolver);
			ConvexCast castPtr = new SubsimplexCubesCovexCast(castShape, collisionObject.getCollisionShape(), collisionObject, simplexSolver, resultCallback, null);
			//btSubsimplexConvexCast convexCaster3(castShape,convexShapeTransform,&simplexSolver);
			castPtr.calcTimeOfImpact(convexFromTrans, convexToTrans, colObjWorldTransform, colObjWorldTransform, castResult);
//			if (castShape instanceof SphereShape) {
//				System.err.println("DOING SHAPE SPHERE COLLISION "+resultCallback.hasHit());
//			}
		} else if (collisionShape.isCompound()) {
			handleCompound(castShape, (CompoundShape) collisionShape, collisionObject, colObjWorldTransform, allowedPenetration, convexFromTrans, convexToTrans, resultCallback);
		} else {
			if (collisionShape.isConvex()) {
				if (castShape instanceof BoxShape) {
					System.err.println("######DOING CONVEX SHAPE CUBE COLLISION");
				}

				CastResult castResult = new CastResult();
				castResult.allowedPenetration = allowedPenetration;
				castResult.fraction = 1f; // ??

				ConvexShape convexShape = (ConvexShape) collisionShape;
				VoronoiSimplexSolver simplexSolver = new VoronoiSimplexSolver();
				GjkEpaPenetrationDepthSolverExt gjkEpaPenetrationSolver = new GjkEpaPenetrationDepthSolverExt();

				simplexSolver.reset();

				// JAVA TODO: should be convexCaster1
				GjkEpaPenetrationDepthSolver solver = new GjkEpaPenetrationDepthSolver();
				ContinuousConvexCollision convexCaster1 = new ContinuousConvexCollision(castShape, convexShape, simplexSolver, solver);
				//				GjkConvexCast convexCaster2 = new GjkConvexCast(castShape, convexShape, simplexSolver);
				//btSubsimplexConvexCast convexCaster3(castShape,convexShape,&simplexSolver);

				GjkPairDetectorVariables v = new GjkPairDetectorVariables();
				//				ConvexCast castPtr = convexCaster2;

				if (convexCaster1.calcTimeOfImpact(convexFromTrans, convexToTrans, colObjWorldTransform, colObjWorldTransform, castResult, v)) {
					if (castShape instanceof BoxShape) {
						System.err.println("########DOING CONVEX SHAPE CUBE COLLISION -> HIT");
					}
					// add hit
					if (castResult.normal.lengthSquared() > 0.0001f) {
						if (castResult.fraction < resultCallback.closestHitFraction) {
							castResult.normal.normalize();
							LocalConvexResult localConvexResult = new LocalConvexResult(collisionObject, null, castResult.normal, castResult.hitPoint, castResult.fraction);

							boolean normalInWorldSpace = true;
							resultCallback.addSingleResult(localConvexResult, normalInWorldSpace);
							if (resultCallback instanceof ClosestConvexResultCallbackExt) {
								((ClosestConvexResultCallbackExt) resultCallback).userData = collisionObject.getUserPointer();
							}
							//							if(castShape instanceof SphereShape){
							//								System.err.println("NON CUBE COLLISION SINGLE QUERY! "+castShape+" -> "+collisionObject+": "+resultCallback.hasHit()+"::: ");
							//							}
						}
					}
				} else {
					if (castShape instanceof BoxShape) {
						System.err.println("#########DOING CONVEX SHAPE CUBE COLLISION -> NO HIT");
					}
				}
				//TODO include concaves! USER POINTER IS NOT SET FOR THOSE (no missile hits)

			} else {
				CollisionWorld.objectQuerySingle(castShape, convexFromTrans, convexToTrans, collisionObject, collisionShape, colObjWorldTransform, resultCallback, allowedPenetration);
			}

		}

	}

	private static int getConstraintIslandId(TypedConstraint lhs) {
		int islandId;

		CollisionObject rcolObj0 = lhs.getRigidBodyA();
		CollisionObject rcolObj1 = lhs.getRigidBodyB();
		islandId = rcolObj0.getIslandTag() >= 0 ? rcolObj0.getIslandTag() : rcolObj1.getIslandTag();
		return islandId;
	}

	/**
	 * WARNING ###################################################################
	 * <p/>
	 * DESTROY THE ALGORTIHM RETURNED AFTER THE RESULT WAS HANDLED!!
	 *
	 * @param body0
	 * @param body1
	 * @param result
	 * @return
	 */
	public CollisionAlgorithm objectQuerySingle(CollisionObject body0, CollisionObject body1, ManifoldResult result) {
		CollisionAlgorithm findAlgorithm = getDispatcher().findAlgorithm(body0, body1);

		System.err.println("DOING SINGLE COLLISION: " + findAlgorithm);

		findAlgorithm.processCollision(body0, body1, dispatchInfo, result);

		return findAlgorithm;

	}

	public void buildCache() {
		for (int i = 0; i < collisionObjects.size(); i++) {
			CollisionObject collisionObject = collisionObjects.getQuick(i);
			collisionObject.getCollisionShape().getAabb(collisionObject.getWorldTransform(tmpTrans), collisionObjectAabbMin, collisionObjectAabbMax);

			if (!cacheValid) {
				if (cache.size() <= i) {
					cache.add(new BoundingBox());
				}
				cache.get(i).set(collisionObjectAabbMin, collisionObjectAabbMax);
			}
		}
		while (cache.size() > collisionObjects.size()) {
			cache.remove(cache.size() - 1);
		}
		cacheValid = true;
	}

	public boolean checkProdyDestroyed(BroadphaseProxy bp) {
		ObjectArrayList<BroadphasePair> overlappingPairArray = getBroadphase().getOverlappingPairCache().getOverlappingPairArray();
		for (int i = 0; i < overlappingPairArray.size(); i++) {
			BroadphasePair p = overlappingPairArray.getQuick(i);
			if (p.pProxy0 == bp.clientObject || p.pProxy1 == bp.clientObject) {

				System.err.println("Exception: Proxy Has NOT been destroyed completely: " + bp.clientObject);
				return false;
			}
		}
		return true;
	}

	public void clean() {

		ArrayList<CollisionObject> toDel = new ArrayList<CollisionObject>(getCollisionObjectArray().size());
		for (int i = 0; i < getCollisionObjectArray().size(); i++) {
			toDel.add(getCollisionObjectArray().getQuick(i));
		}

		ArrayList<ActionInterface> toDelActions = new ArrayList<ActionInterface>(getNumActions());
		for (int i = 0; i < getNumActions(); i++) {
			toDelActions.add(getAction(i));
		}

		ArrayList<RaycastVehicle> toDelVehicles = new ArrayList<RaycastVehicle>(vehicles.size());

		for (int i = 0; i < vehicles.size(); i++) {
			toDelVehicles.add(vehicles.get(i));
		}

		ArrayList<TypedConstraint> toDelContraints = new ArrayList<TypedConstraint>(constraints.size());

		for (int i = 0; i < constraints.size(); i++) {
			toDelContraints.add(constraints.get(i));
		}

		for (int i = 0; i < toDelContraints.size(); i++) {
			removeConstraint(toDelContraints.get(i));
		}

		for (int i = 0; i < toDelVehicles.size(); i++) {
			removeVehicle(toDelVehicles.get(i));
		}

		for (int i = 0; i < toDel.size(); i++) {
			removeCollisionObject(toDel.get(i));
		}

		for (int i = 0; i < toDelActions.size(); i++) {
			removeAction(toDelActions.get(i));
		}

		((SimulationIslandManagerExt) islandManager).cleanUp();
	}

	private void doInnerRayTest(CollisionObject collisionObject,
	                            RayResultCallback resultCallback, Vector3f rayFromWorld,
	                            Vector3f rayToWorld, boolean debug) {
		if (debug) {
			System.err.println("##############CHECKING ORIG -> " + collisionObject + ": ");
		}
		if (collisionObject.getCollisionShape().isCompound()) {
			CompoundShape compoundShape = (CompoundShape) collisionObject.getCollisionShape();
			handleCompoundRayTest(compoundShape, collisionObject, rayFromWorld, rayToWorld, resultCallback, debug);

		} else if (collisionObject.getCollisionShape() instanceof CubeShape && (!(resultCallback != null) || resultCallback instanceof ClosestRayCastResultExt)) {
			ClosestRayCastResultExt cubeRayCastResult = (ClosestRayCastResultExt) resultCallback;
			;
			CubeShape cubeCollisionShape = (CubeShape) collisionObject.getCollisionShape();

			//Ignore the owner of this test

			if (cubeRayCastResult.getOwner() != null && cubeRayCastResult.getOwner() == cubeCollisionShape.getSegmentBuffer().getSegmentController()) {
				//							System.err.println("OWNER ignored: "+cubeCastResult.getOwner());
				return;
			}
			if(debug){
				System.err.println("#####STARTING RAYTEST SINGLE CUBE MESH");
			}
			rayTestSingleCubeMesh(rayFromTrans, rayToTrans,
					collisionObject,
					cubeCollisionShape,
					collisionObject.getWorldTransform(tmpTrans),
					cubeRayCastResult, debug);
		} else {
			if (resultCallback instanceof ClosestRayCastResultExt && ((ClosestRayCastResultExt) resultCallback).isOnlyCubeMeshes()) {
				//do not test for regular collisions
			} else {
				if(debug){
					System.err.println("#####STARTING RAYTEST SINGLE GENERIC");
				}
				NonBlockHitCallback hitNonblockCallback = null;
				float power = 0;
				if(resultCallback instanceof CubeRayCastResult){
					hitNonblockCallback = ((CubeRayCastResult)resultCallback).getHitNonblockCallback();
					power = ((CubeRayCastResult)resultCallback).power;
				}
				boolean hadHit = resultCallback.hasHit();
				rayTestSingle(rayFromTrans, rayToTrans,
						collisionObject,
						collisionObject.getCollisionShape(),
						collisionObject.getWorldTransform(tmpTrans),
						resultCallback);
				if(!hadHit && resultCallback.hasHit() && hitNonblockCallback != null && !hitNonblockCallback.onHit(collisionObject, power)){
					//remove hit
					resultCallback.closestHitFraction = 1f;
					resultCallback.collisionObject = null;
				}
			}
		}
	}

	public void doRayTest(Object owner, RayResultCallback resultCallback, Vector3f rayFromWorld, Vector3f rayToWorld) {

		boolean ignoreDebris = false;
		boolean debug = false;
		boolean cubesOnly = false;
		boolean checkStabilizer = false;
		if (resultCallback instanceof ClosestRayCastResultExt) {
			debug = ((ClosestRayCastResultExt) resultCallback).isDebug();
			ignoreDebris = ((ClosestRayCastResultExt) resultCallback).isIgnoreDebris();
			cubesOnly = ((ClosestRayCastResultExt) resultCallback).isCubesOnly();
		}
		if (resultCallback instanceof CubeRayCastResult) {
			checkStabilizer = ((CubeRayCastResult) resultCallback).isCheckStabilizerPath();
		}
		for (int i = 0; i < collisionObjects.size(); i++) {
			// terminate further ray tests, once the closestHitFraction reached zero
			if (resultCallback.closestHitFraction == 0f) {
				if(debug){
					System.err.println("### "+i+"/"+collisionObjects.size()+": "+collisionObjects.getQuick(i)+" ZERO HIT FRACTION");
				}
				break;
			}
			
			CollisionObject collisionObject = collisionObjects.getQuick(i);

			if (owner == collisionObject) {
				if(debug){
					System.err.println("### "+i+"/"+collisionObjects.size()+": "+collisionObject+" IS SELF");
				}
				continue;
			}
			if (collisionObject instanceof RigidDebrisBody && (ignoreDebris || ((RigidDebrisBody) collisionObject).shard.isKilled())) {
				if(debug){
					System.err.println("### "+i+"/"+collisionObjects.size()+": "+collisionObject+" IS DEBRIS");
				}
				continue;
			}
			if(!checkStabilizer && collisionObject.getUserPointer() instanceof StabilizerPath){
				continue;
			}
			//			if(collisionObject.toString().contains("SER")){
			//				if(collisionObject.toString().contains("Jaeger")){
			//					System.err.println("##############CHECKING ORIG1 "+collisionObject+": ");
			//				}
			//			}
			if (collisionObject instanceof RigidBodySegmentController) {
				if (resultCallback instanceof CubeRayCastResult && ((CubeRayCastResult)resultCallback).isFilteredRoot(((RigidBodySegmentController) collisionObject).getSegmentController())) {
					continue;
				}
				if (((RigidBodySegmentController) collisionObject).getSegmentController() == owner) {
					if(debug){
						System.err.println("### "+i+"/"+collisionObjects.size()+": "+collisionObject+" IS SELF OWNER");
					}
					continue;
				}
			}else if(cubesOnly){
				if(debug){
					System.err.println("### "+i+"/"+collisionObjects.size()+": "+collisionObject+" NOT CUBE");
				}
				continue;
			}
			//			if(collisionObject.toString().contains("SER")){
			//				if(collisionObject.toString().contains("Jaeger")){
			//					System.err.println("##############CHECKING ORIG2 "+collisionObject+": ");
			//				}
			//			}
			if (collisionObject instanceof PairCachingGhostObjectAlignable) {
				if (((PairCachingGhostObjectAlignable) collisionObject).getObj() == owner) {
					if(debug){
						System.err.println("### "+i+"/"+collisionObjects.size()+": "+collisionObject+" PAICACHING SELF");
					}
					continue;
				}
			}
			//			if(collisionObject.toString().contains("SER")){
			//				if(collisionObject.toString().contains("Jaeger")){
			//					System.err.println("##############CHECKING ORIG3 "+collisionObject+": ");
			//				}
			//			}
			
			// only perform raycast if filterMask matches
			BroadphaseProxy broadphaseHandle = collisionObject.getBroadphaseHandle();
			try {
				if (broadphaseHandle != null && resultCallback.needsCollision(broadphaseHandle)) {
					//					if(collisionObject.toString().contains("SER")){
					//						if(collisionObject.toString().contains("Jaeger")){
					//							System.err.println("##############CHECKING ORIGOK "+collisionObject+": ");
					//						}
					//					}

					//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
					collisionObject.getCollisionShape().getAabb(collisionObject.getWorldTransform(tmpTrans), collisionObjectAabbMin, collisionObjectAabbMax);
//					if(debug){
//						System.err.println("### "+i+"/"+collisionObjects.size()+": "+collisionObject+" CHECKING ");
//					}
					doRayTestNext(broadphaseHandle, collisionObject, resultCallback, rayFromWorld, rayToWorld, debug);
				} else {
					if(debug){
						System.err.println("###### OBJECT "+collisionObject+" DOESNT NEED COLLISION ");
					}
					//					if(collisionObject.toString().contains("SER")){
					//						if(collisionObject.toString().contains("Jaeger")){
					//							System.err.println("##############CHECKING ORIGNO "+collisionObject+": ");
					//						}
					//					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	//	private boolean debugSingle;
	public void doRayTestCached(Object owner, RayResultCallback resultCallback, Vector3f rayFromWorld, Vector3f rayToWorld) {
		boolean debug = false;
		boolean ignoreDebris = false;
		boolean cubesOnly = false;
		boolean checkStabilizer = false;
		if (resultCallback instanceof CubeRayCastResult) {
			checkStabilizer = ((CubeRayCastResult) resultCallback).isCheckStabilizerPath();
		}
		if (resultCallback instanceof ClosestRayCastResultExt) {
			debug = ((ClosestRayCastResultExt) resultCallback).isDebug();
			ignoreDebris = ((ClosestRayCastResultExt) resultCallback).isIgnoreDebris();
			cubesOnly = ((ClosestRayCastResultExt) resultCallback).isCubesOnly();
			
		}

		for (int i = 0; i < collisionObjects.size(); i++) {
			CollisionObject collisionObject = collisionObjects.getQuick(i);

			if (collisionObject instanceof RigidDebrisBody && (ignoreDebris || ((RigidDebrisBody) collisionObject).shard.isKilled())) {
				continue;
			}
			if(!checkStabilizer && collisionObject.getUserPointer() instanceof StabilizerPath){
				continue;
			}
			// terminate further ray tests, once the closestHitFraction reached zero
			if (resultCallback.closestHitFraction == 0f) {
				break;
			}

			BoundingBox bb = cache.get(i);
			dir.sub(rayToWorld, rayFromWorld);
			bb.getClosestPoint(rayFromWorld, closest);
			closestDir.sub(closest, rayFromWorld);
			if (dir.lengthSquared() < closestDir.lengthSquared()) {
				/*
				 * Hackedihack Optimization....
				 *
				 * if the closest distance from start to an AABB
				 * is bigger then the distance between start and end,
				 * it's impossible for the ray to have an intersection.
				 * (by rule of intersection spheres)
				 *
				 */

				continue;
			}

			collisionObjectAabbMin.set(bb.min);
			collisionObjectAabbMax.set(bb.max);

			if (owner == collisionObject) {
				continue;
			}

			if (collisionObject instanceof RigidBodySegmentController) {
				if (((RigidBodySegmentController) collisionObject).getSegmentController() == owner) {
					continue;
				}
			}else if(cubesOnly){
				continue;
			}
			if (collisionObject instanceof PairCachingGhostObjectAlignable) {
				if (((PairCachingGhostObjectAlignable) collisionObject).getObj() == owner) {
					continue;
				}
			}
			// only perform raycast if filterMask matches
			BroadphaseProxy broadphaseHandle = collisionObject.getBroadphaseHandle();
			try {
				if (broadphaseHandle != null && resultCallback.needsCollision(broadphaseHandle)) {
					//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
					doRayTestNext(broadphaseHandle, collisionObject, resultCallback, rayFromWorld, rayToWorld, debug);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void doRayTestNext(BroadphaseProxy broadphaseHandle, CollisionObject collisionObject, RayResultCallback resultCallback, Vector3f rayFromWorld, Vector3f rayToWorld, boolean debug) {

		
		hitLambda[0] = resultCallback.closestHitFraction;
		
		boolean hitAABB = rayAabb(rayFromWorld, rayToWorld, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal);
//		if(debug){
//			if(hitAABB) {
//				System.err.println("#--# HIT! AABB CHECK "+collisionObject+" AABB: "+hitAABB);
//			}else {
//				System.err.println("#### MISS AABB CHECK "+collisionObject+" AABB: "+hitAABB);
//			}
//		}
		if (hitAABB) {

//			Transform t = new Transform();
//			t.setIdentity();
//
//			DebugDrawer.boxes.add(new DebugBox(new Vector3f(collisionObjectAabbMin),
//					new Vector3f(collisionObjectAabbMax), t,1.0f,0.0f,0.0f,1));
//			System.err.println("INNER RAY: "+collisionObject);
			doInnerRayTest(collisionObject, resultCallback, rayFromWorld, rayToWorld, debug);
		} else {
		}
	}

	//	/**
	//	 * convexTest performs a swept convex cast on all objects in the {@link CollisionWorld}, and calls the resultCallback
	//	 * This allows for several queries: first hit, all hits, any hit, dependent on the value return by the callback.
	//	 */
	//	public void convexSweepTestWith(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, CollisionObject withCollisionObject, ConvexResultCallback resultCallback) {
	//		Transform convexFromTrans = new Transform();
	//		Transform convexToTrans = new Transform();
	//
	//		convexFromTrans.set(convexFromWorld);
	//		convexToTrans.set(convexToWorld);
	//
	//		Vector3f castShapeAabbMin = new Vector3f();
	//		Vector3f castShapeAabbMax = new Vector3f();
	//
	//		// Compute AABB that encompasses angular movement
	//		{
	//			Vector3f linVel = new Vector3f();
	//			Vector3f angVel = new Vector3f();
	//			TransformUtil.calculateVelocity(convexFromTrans, convexToTrans, 1f, linVel, angVel);
	//			Transform R = new Transform();
	//			R.setIdentity();
	//			R.setRotation(convexFromTrans.getRotation(new Quat4f()));
	//			castShape.calculateTemporalAabb(R, linVel, angVel, 1f, castShapeAabbMin, castShapeAabbMax);
	//		}
	//
	//		Transform tmpTrans = new Transform();
	//		Vector3f collisionObjectAabbMin = new Vector3f();
	//		Vector3f collisionObjectAabbMax = new Vector3f();
	//		float[] hitLambda = new float[1];
	//
	//		// go over all objects, and if the ray intersects their aabb + cast shape aabb,
	//		// do a ray-shape query using convexCaster (CCD)
	//		CollisionObject collisionObject = withCollisionObject;
	//
	//		// only perform raycast if filterMask matches
	//		if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
	//
	//			System.err.println("####### CHECKING COLLISION WITH "+convexFromTrans.origin);
	//
	//			//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
	//			collisionObject.getWorldTransform(tmpTrans);
	//			collisionObject.getCollisionShape().getAabb(tmpTrans, collisionObjectAabbMin, collisionObjectAabbMax);
	//			AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);
	//			hitLambda[0] = 1f; // could use resultCallback.closestHitFraction, but needs testing
	//			Vector3f hitNormal = new Vector3f();
	//			objectQuerySingle(castShape, convexFromTrans, convexToTrans,
	//			                  collisionObject,
	//			                  collisionObject.getCollisionShape(),
	//			                  tmpTrans,
	//			                  resultCallback,
	//			                  getDispatchInfo().allowedCcdPenetration);
	//		}
	//	}

	private void handleCompoundRayTest(CompoundShape compoundShape, CollisionObject collisionObject, Vector3f rayFromWorld, Vector3f rayToWorld, RayResultCallback resultCallback, boolean debug) {

		for (int j = 0; j < compoundShape.getNumChildShapes(); j++) {
			compoundShape.getChildTransform(j, childTrans);
			CollisionShape childCollisionShape = compoundShape.getChildShape(j);
			childWorldTrans.set(collisionObject.getWorldTransform(tmpTrans));

			Matrix4fTools.transformMul(childWorldTrans, childTrans);
			// replace collision shape so that callback can determine the triangle
			CollisionShape saveCollisionShape = collisionObject.getCollisionShape();

			if (childCollisionShape instanceof CubeShape &&
					(!(resultCallback != null) || resultCallback instanceof ClosestRayCastResultExt)) {
				ClosestRayCastResultExt cubeRayCastResult = (ClosestRayCastResultExt) resultCallback;
				;

				//				if(cubeRayCastResult.hasHit() && cubeRayCastResult.getSegment() == null){
				//					System.err.println("[PHYSICS] WARNING CubeRayResult has collision on NonCube: "+cubeRayCastResult.collisionObject);
				//					cubeRayCastResult.closestHitFraction = 1f;
				//					cubeRayCastResult.collisionObject = null;
				//				}

				CubeShape cubeCollisionShape = (CubeShape) childCollisionShape;

				//Ignore the owner of this test
				if (cubeRayCastResult.getOwner() instanceof SegmentController && (((SegmentController)cubeRayCastResult.getOwner()) == cubeCollisionShape.getSegmentBuffer().getSegmentController()) || (cubeRayCastResult.considerAllDockedAsOwner  && ((SegmentController)cubeRayCastResult.getOwner()).railController.getRoot() == cubeCollisionShape.getSegmentBuffer().getSegmentController().railController.getRoot())) {
					//					System.err.println("OWNER ignored: "+cubeCastResult.getOwner());
					continue;
				}
//				if(cubeRayCastResult.getOwner() instanceof SegmentController) {
//					System.err.println("OWNER::::: "+cubeRayCastResult.getOwner());
//				}
				collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);

				rayTestSingleCubeMesh(rayFromTrans, rayToTrans,
						collisionObject,
						childCollisionShape,
						childWorldTrans,
						cubeRayCastResult, debug);
				collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);

			} else if (childCollisionShape instanceof CompoundShape) {
				handleCompoundRayTest((CompoundShape) childCollisionShape, collisionObject, rayFromWorld, rayToWorld, resultCallback, debug);
			} else {

				collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);
				assert (false) : childCollisionShape + ": " + childCollisionShape.getClass().getSimpleName() + "; " +
						(resultCallback != null ? resultCallback.getClass().getSimpleName() : "null-ResultCallback");
				rayTestSingle(rayFromTrans, rayToTrans,
						collisionObject,
						childCollisionShape,
						childWorldTrans,
						resultCallback);
				collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
			}

			// restore

		}
	}

	@Override
	public void performDiscreteCollisionDetection() {
		BulletStats.pushProfile("performDiscreteCollisionDetection");
		try {
			//DispatcherInfo dispatchInfo = getDispatchInfo();
//			long ll = System.nanoTime();
			updateAabbs();
//			long aaBBTime = (System.nanoTime() - ll);
//			if(getState() instanceof GameClientState){
//				System.err.println(getState()+" AABBTIME TIME: "+(aaBBTime/1000000L));
//			}
			
			long time = System.currentTimeMillis();
			BulletStats.pushProfile("calculateOverlappingPairs");
			try {
				broadphasePairCache.calculateOverlappingPairs(dispatcher1);
				//				ObjectArrayList<BroadphasePair> overlappingPairArray = getBroadphase().getOverlappingPairCache().getOverlappingPairArray();

				//				boolean a = false;
				//				for(int i = 0; i< overlappingPairArray.size(); i++){
				//					BroadphasePair p = overlappingPairArray.getQuick(i);
				//					if(p.pProxy0.clientObject.toString().contains("|CLI") || p.pProxy1.clientObject.toString().contains("|CLI")){
				//						System.err.println("Broadphase pair: "+p.pProxy0.clientObject+"; "+p.pProxy1.clientObject);
				//						a = true;
				//					}
				//				}
				//				if(a){
				//					System.err.println(".........-------------END");
				//				}
			} finally {
				BulletStats.popProfile();
			}
			long takenBroadphase = System.currentTimeMillis() - time;
			if (takenBroadphase > 30) {
				System.err.println(getState() + " Broadphase of " + getState() + " took: " + takenBroadphase+"; Objects in physics context: "+getCollisionObjectArray().size());
			}
			time = System.currentTimeMillis();
			Dispatcher dispatcher = getDispatcher();
			{
				BulletStats.pushProfile("dispatchAllCollisionPairs");
				try {
					if (dispatcher != null) {
						dispatcher.dispatchAllCollisionPairs(broadphasePairCache.getOverlappingPairCache(), dispatchInfo, dispatcher1);
					}
				} finally {
					BulletStats.popProfile();
				}
			}

			long takenNarrowphase = System.currentTimeMillis() - time;

			ArrayList<CollisionObject> toRem = null;
			if (takenNarrowphase > 30 || (DebugControlManager.requestPhysicsCheck && getState() instanceof Sector)) {
				System.err.println(getState() + " Narrowphase of " + getState() + " took: " + takenNarrowphase+"; Objects in physics context: "+getCollisionObjectArray().size());
				if (takenNarrowphase > 100 || (DebugControlManager.requestPhysicsCheck && getState() instanceof Sector)) {
					//System.err.println(getState() + " LISTING OBJECTS:");
					for (int i = 0; i < getNumCollisionObjects(); i++) {
						CollisionObject collisionObject = getCollisionObjectArray().get(i);
						//System.err.println(i + "# OBJECT: " + collisionObject);
						if (collisionObject instanceof RigidBodySegmentController) {
							RigidBodySegmentController o = (RigidBodySegmentController) collisionObject;
							if (o.getSegmentController().isOnServer() && !o.isVirtual()) {
								Sector sectorOnServer = o.getSectorOnServer();
								boolean remove = false;
								if (sectorOnServer != null) {
									if (sectorOnServer != getState()) {
										//System.err.println("Exception: object " + o + " is self in wrong sector (sectorId: " + sectorOnServer + " but should be PhysicsState" + this.getState() + "). Removing");
										remove = true;
										((Sector) getState()).getState().getController().broadcastMessageAdmin(Lng.astr("(ADMIN) Exception:\n%s\nphysics inconsistency detected (dif)\nPerforming cleanup on object to recover...", o.getSegmentController()), ServerMessage.MESSAGE_TYPE_ERROR);
										;
									}
								} else {
									//System.err.println("Exception: object " + o + " has no physical sector. Removing");
									((Sector) getState()).getState().getController().broadcastMessageAdmin(Lng.astr("(ADMIN) Exception:\n%s\nphysics inconsistency detected (miss)\nPerforming cleanup on object to recover...",  o.getSegmentController()), ServerMessage.MESSAGE_TYPE_ERROR);
									;
									remove = true;
								}
								if (remove) {
									if (toRem == null) {
										toRem = new ArrayList<CollisionObject>();
									}
									toRem.add(collisionObject);
								}

							}
						}
					}
				}
			}

			if (toRem != null) {
				for (CollisionObject o : toRem) {
					removeCollisionObject(o);
				}
			}
		} finally {
			BulletStats.popProfile();
		}
	}

	@Override
	public void removeCollisionObject(CollisionObject collisionObject) {
		//bool removeFromBroadphase = false;

		{
			BroadphaseProxy bp = collisionObject.getBroadphaseHandle();
			if (bp != null) {

				//				System.err.println("DESROYING PROXY "+bp.clientObject);
				//
				// only clear the cached algorithms
				//
				getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(bp, dispatcher1);
				getBroadphase().destroyProxy(bp, dispatcher1);
				collisionObject.setBroadphaseHandle(null);

				ObjectArrayList<PersistentManifold> pp = dispatcher1.getInternalManifoldPointer();
				for (int i = 0; i < pp.size(); i++) {
					PersistentManifold persistentManifold = pp.get(i);
					if (persistentManifold.getBody0() == collisionObject || persistentManifold.getBody1() == collisionObject) {
						dispatcher1.releaseManifold(persistentManifold);

						//						System.err.println("[PHYSICS] Removed invalid Manifold: "+persistentManifold.getBody0()+"; "+persistentManifold.getBody1());
						i = 0; //reset
					}
				}

				assert (checkProdyDestroyed(bp));
			}
		}

		//swapremove
		collisionObjects.remove(collisionObject);
	}

	@Override
	public final OverlappingPairCache getPairCache() {
		return broadphasePairCache.getOverlappingPairCache();
	}

	// JAVA NOTE: ported from 2.74, missing contact threshold stuff
	@Override
	public void updateSingleAabb(CollisionObject colObj) {

		colObj.getCollisionShape().getAabb(colObj.getWorldTransform(tmpTransAABBSingle), minAabb, maxAabb);
		// need to increase the aabb for contact thresholds
		contactThreshold.set(BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold());
		minAabb.sub(contactThreshold);
		maxAabb.add(contactThreshold);

		if(colObj.getCollisionShape() instanceof CubesCompoundShape){
			((CubesCompoundShape)colObj.getCollisionShape()).lastPhysicsAABBMin.set(minAabb);
			((CubesCompoundShape)colObj.getCollisionShape()).lastPhysicsAABBMax.set(maxAabb);
		}
		
		BroadphaseInterface bp = broadphasePairCache;

		// moving objects should be moderately sized, probably something wrong if not
		if (colObj.isStaticObject() || (Vector3fTools.diffLengthSquared(maxAabb, minAabb) < 1e12f)) {
			bp.setAabb(colObj.getBroadphaseHandle(), minAabb, maxAabb, dispatcher1);
		} else {
			// something went wrong, investigate
			// this assert is unwanted in 3D modelers (danger of loosing work)
			colObj.setActivationState(CollisionObject.DISABLE_SIMULATION);
			
			System.err.println("Exception!!! Overflow in AABB, object removed from simulation " + colObj + "; " + minAabb + "; " + maxAabb);
			System.err.println("OBJ WORLD MATRIX: \n"+colObj.getWorldTransform(new Transform()).getMatrix(new Matrix4f()));
			//				if (updateAabbs_reportMe && debugDrawer != null) {
			//					updateAabbs_reportMe = false;
			//					debugDrawer.reportErrorWarning("Overflow in AABB, object removed from simulation");
			//					debugDrawer.reportErrorWarning("If you can reproduce this, please email bugs@continuousphysics.com\n");
			//					debugDrawer.reportErrorWarning("Please include above information, your Platform, version of OS.\n");
			//					debugDrawer.reportErrorWarning("Thanks.\n");
			//				}
		}
	}

	@Override
	public void updateAabbs() {
		BulletStats.pushProfile("updateAabbs");
		try {
			for (int i = 0; i < collisionObjects.size(); i++) {
				CollisionObject colObj = collisionObjects.getQuick(i);

				// only update aabb of active objects
				if (colObj.isActive()) {
					//					if(colObj.toString().contains("CLI") && colObj.toString().contains("schema_1370260089078")){
					//						System.err.println("UPDATING "+colObj+": "+colObj.getWorldTransform(new Transform()).origin);
					//						}
					updateSingleAabb(colObj);

				} else {
					//					if(colObj.toString().contains("CLI") && colObj.toString().contains("schema_1370260089078")){
					//					System.err.println("NOT UPDATING "+colObj+": "+colObj.getActivationState());
					//					}
				}
			}
		} finally {
			BulletStats.popProfile();
		}
	}

	@Override
	public void rayTest(Vector3f rayFromWorld, Vector3f rayToWorld, RayResultCallback resultCallback) {

		rayFromTrans.basis.setIdentity();
		rayFromTrans.origin.set(rayFromWorld);

		rayToTrans.basis.setIdentity();
		rayToTrans.origin.set(rayToWorld);

		// go over all objects, and if the ray intersects their aabb, do a ray-shape query using convexCaster (CCD)
		hitLambda[0] = 0;
		Object owner = null;

		if (resultCallback != null && resultCallback instanceof ClosestRayCastResultExt && ((ClosestRayCastResultExt) resultCallback).getOwner() != null) {
			owner = ((ClosestRayCastResultExt) resultCallback).getOwner();
		}
//		if(resultCallback instanceof ClosestRayCastResultExt && ((ClosestRayCastResultExt) resultCallback).isDebug()){
//			System.err.println("######## DEBUG RAYTEST "+cacheValid);
//		}
		if (cacheValid) {
			doRayTestCached(owner, resultCallback, rayFromWorld, rayToWorld);
		} else {
			doRayTest(owner, resultCallback, rayFromWorld, rayToWorld);
		}

	}

	/**
	 * convexTest performs a swept convex cast on all objects in the {@link CollisionWorld}, and calls the resultCallback
	 * This allows for several queries: first hit, all hits, any hit, dependent on the value return by the callback.
	 */
	@Override
	public void convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, ConvexResultCallback resultCallback) {
		Transform convexFromTrans = this.convexFromTrans;//new @Stack Transform();
		Transform convexToTrans = this.convexToTrans;//new @Stack Transform();
		convexFromTrans.set(convexFromWorld);
		convexToTrans.set(convexToWorld);

		Vector3f castShapeAabbMin = this.castShapeAabbMin;//new @Stack Vector3f();
		Vector3f castShapeAabbMax = this.castShapeAabbMax;//new @Stack Vector3f();

		// Compute AABB that encompasses angular movement
		{
			Vector3f linVel = this.linVel;//new @Stack Vector3f();
			Vector3f angVel = this.angVel;//new @Stack Vector3f();
			TransformUtil.calculateVelocity(convexFromTrans, convexToTrans, 1f, linVel, angVel);
			Transform R = this.R;//new @Stack Transform();
			R.setIdentity();
			R.setRotation(convexFromTrans.getRotation(this.quat)); //new @Stack Quat4f())
			castShape.calculateTemporalAabb(R, linVel, angVel, 1f, castShapeAabbMin, castShapeAabbMax);
		}

		Transform tmpTrans = this.tmpTrans;//new @Stack Transform();
		Vector3f collisionObjectAabbMin = this.collisionObjectAabbMin;//new @Stack Vector3f();
		Vector3f collisionObjectAabbMax = this.collisionObjectAabbMax;//new @Stack Vector3f();
		float[] hitLambda = new float[1];

		CollisionObject self = null;
		if(resultCallback instanceof ClosestConvexResultCallbackExt && ((ClosestConvexResultCallbackExt)resultCallback).sphereDontHitOwner){
			self = ((ClosestConvexResultCallbackExt)resultCallback).ownerObject;
		}
		// go over all objects, and if the ray intersects their aabb + cast shape aabb,
		// do a ray-shape query using convexCaster (CCD)
		for (int i = 0; i < collisionObjects.size(); i++) {
			CollisionObject collisionObject = collisionObjects.getQuick(i);

			
			// only perform raycast if filterMask matches
			if (self != collisionObject && resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
				//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
				collisionObject.getWorldTransform(tmpTrans);
				collisionObject.getCollisionShape().getAabb(tmpTrans, collisionObjectAabbMin, collisionObjectAabbMax);
				AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);
				hitLambda[0] = 1f; // could use resultCallback.closestHitFraction, but needs testing
				Vector3f hitNormal = this.hitNormal;//new @Stack Vector3f();
				
				
				if (castShape instanceof SphereShape || AabbUtil2.rayAabb(convexFromWorld.origin, convexToWorld.origin, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
					objectQuerySingle(castShape, convexFromTrans, convexToTrans,
							collisionObject,
							collisionObject.getCollisionShape(),
							tmpTrans,
							resultCallback,
							getDispatchInfo().allowedCcdPenetration);
				}
			}
		}
	}

	public boolean rayAabb(Vector3f rayFrom, Vector3f rayTo, Vector3f aabbMin, Vector3f aabbMax, float[] param, Vector3f normal) {

		aabbHalfExtent.sub(aabbMax, aabbMin);
		aabbHalfExtent.scale(0.5f);

		aabbCenter.add(aabbMax, aabbMin);
		aabbCenter.scale(0.5f);

		source.sub(rayFrom, aabbCenter);
		target.sub(rayTo, aabbCenter);

		int sourceOutcode = AabbUtil2.outcode(source, aabbHalfExtent);
		int targetOutcode = AabbUtil2.outcode(target, aabbHalfExtent);
		if ((sourceOutcode & targetOutcode) == 0x0) {
			float lambda_enter = 0f;
			float lambda_exit = param[0];
			r.sub(target, source);

			float normSign = 1f;
			hitNormalTmp.set(0f, 0f, 0f);
			int bit = 1;

			if ((sourceOutcode & bit) != 0) {
				float lambda = (-source.x - aabbHalfExtent.x * normSign) / r.x;
				if (lambda_enter <= lambda) {
					lambda_enter = lambda;
					hitNormalTmp.set(0f, 0f, 0f);
					hitNormalTmp.x = normSign;
				}
			} else if ((targetOutcode & bit) != 0) {
				float lambda = (-source.x - aabbHalfExtent.x * normSign) / r.x;
				//btSetMin(lambda_exit, lambda);
				lambda_exit = Math.min(lambda_exit, lambda);
			}
			bit <<= 1;

			if ((sourceOutcode & bit) != 0) {
				float lambda = (-source.y - aabbHalfExtent.y * normSign) / r.y;
				if (lambda_enter <= lambda) {
					lambda_enter = lambda;
					hitNormalTmp.set(0f, 0f, 0f);
					hitNormalTmp.y = normSign;
				}
			} else if ((targetOutcode & bit) != 0) {
				float lambda = (-source.y - aabbHalfExtent.y * normSign) / r.y;
				//btSetMin(lambda_exit, lambda);
				lambda_exit = Math.min(lambda_exit, lambda);
			}
			bit <<= 1;

			if ((sourceOutcode & bit) != 0) {
				float lambda = (-source.z - aabbHalfExtent.z * normSign) / r.z;
				if (lambda_enter <= lambda) {
					lambda_enter = lambda;
					hitNormalTmp.set(0f, 0f, 0f);
					hitNormalTmp.z = normSign;
				}
			} else if ((targetOutcode & bit) != 0) {
				float lambda = (-source.z - aabbHalfExtent.z * normSign) / r.z;
				//btSetMin(lambda_exit, lambda);
				lambda_exit = Math.min(lambda_exit, lambda);
			}

			bit <<= 1;

			normSign = -1f;

			if ((sourceOutcode & bit) != 0) {
				float lambda = (-source.x - aabbHalfExtent.x * normSign) / r.x;
				if (lambda_enter <= lambda) {
					lambda_enter = lambda;
					hitNormalTmp.set(0f, 0f, 0f);
					hitNormalTmp.x = normSign;
				}
			} else if ((targetOutcode & bit) != 0) {
				float lambda = (-source.x - aabbHalfExtent.x * normSign) / r.x;
				//btSetMin(lambda_exit, lambda);
				lambda_exit = Math.min(lambda_exit, lambda);
			}
			bit <<= 1;

			if ((sourceOutcode & bit) != 0) {
				float lambda = (-source.y - aabbHalfExtent.y * normSign) / r.y;
				if (lambda_enter <= lambda) {
					lambda_enter = lambda;
					hitNormalTmp.set(0f, 0f, 0f);
					hitNormalTmp.y = normSign;
				}
			} else if ((targetOutcode & bit) != 0) {
				float lambda = (-source.y - aabbHalfExtent.y * normSign) / r.y;
				//btSetMin(lambda_exit, lambda);
				lambda_exit = Math.min(lambda_exit, lambda);
			}
			bit <<= 1;

			if ((sourceOutcode & bit) != 0) {
				float lambda = (-source.z - aabbHalfExtent.z * normSign) / r.z;
				if (lambda_enter <= lambda) {
					lambda_enter = lambda;
					hitNormalTmp.set(0f, 0f, 0f);
					hitNormalTmp.z = normSign;
				}
			} else if ((targetOutcode & bit) != 0) {
				float lambda = (-source.z - aabbHalfExtent.z * normSign) / r.z;
				//btSetMin(lambda_exit, lambda);
				lambda_exit = Math.min(lambda_exit, lambda);
			}
			bit <<= 1;

			if (lambda_enter <= lambda_exit) {
				param[0] = lambda_enter;
				normal.set(hitNormalTmp);
				return true;
			}
		}
		return false;
	}
	private final VoronoiSimplexSolverExt simplexSolver = new VoronoiSimplexSolverExt();
	private final SubsimplexRayCubesCovexCast convexCaster = new SubsimplexRayCubesCovexCast();
	private final CastResult castResult = new CastResult();
	SphereShape pointShape = new SphereShape(0f);
	{
		pointShape.setMargin(0f);
	}
	private void rayTestSingleCubeMesh(Transform rayFromTrans,
	                                   Transform rayToTrans, CollisionObject collisionObject,
	                                   CollisionShape collisionShape, Transform colObjWorldTransform,
	                                   ClosestRayCastResultExt resultRayCallback, boolean debug) {
		
		
//		if(debug){
//			try {
//				throw new Exception("DEBUG "+resultRayCallback.isDebug()+"; "+resultRayCallback.isRecordAllBlocks());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		//do another ray AABB test to check if the child shape is actually hit before moving on
		collisionShape.getAabb(colObjWorldTransform, collisionSubObjectAabbMin, collisionSubObjectAabbMax);
		
		hitLambda[0] = resultRayCallback.closestHitFraction;
		if (rayAabb(rayFromTrans.origin, rayToTrans.origin, collisionSubObjectAabbMin, collisionSubObjectAabbMax, hitLambda, hitNormal)) {
			
			castResult.normal.set(0,0,0);
			castResult.hitPoint.set(0,0,0);
			castResult.allowedPenetration = 0;
			
			castResult.fraction = resultRayCallback.closestHitFraction;
	
			CubeShape convexShape = (CubeShape) collisionShape;
			
			ConvexShape castShape = pointShape;
			//#define USE_SUBSIMPLEX_CONVEX_CAST 1
			//#ifdef USE_SUBSIMPLEX_CONVEX_CAST
			convexCaster.init(castShape, collisionObject, simplexSolver, resultRayCallback);
			//#else
			//btGjkConvexCast	convexCaster(castShape,convexShapeTransform,&simplexSolver);
			//btContinuousConvexCollision convexCaster(castShape,convexShapeTransform,&simplexSolver,0);
			//#endif //#USE_SUBSIMPLEX_CONVEX_CAST
			if(resultRayCallback.isDebug()){
				System.err.println("CALC TIME OF IMPACT: RECORD ALL: "+resultRayCallback.isRecordAllBlocks());
			}
			convexCaster.calcTimeOfImpact(rayFromTrans, rayToTrans, colObjWorldTransform, colObjWorldTransform, castResult);
			//		System.err.println("CTOI took "+(System.currentTimeMillis() - t));
		}else{
//			System.err.println("NO AABB WITH CHILD "+collisionObject);
		}
	}

	private void updateDockedChilds(SegmentController segmentController) {
		segmentController.railController.updateDockedFromPhysicsWorld();
		for (int i = 0; i < segmentController.getDockingController().getDockedOnThis().size(); i++) {
			ElementDocking l = segmentController.getDockingController().getDockedOnThis().get(i);
			SegmentController c = l.from.getSegment().getSegmentController();
			c.getPhysicsDataContainer().updatePhysical(segmentController.getState().getUpdateTime());
			updateDockedChilds(c);
		}
	}

	@Override
	protected void synchronizeMotionStates() {

		// todo: iterate over awake simulation islands!
		for (int i = 0; i < collisionObjects.size(); i++) {
			CollisionObject colObj = collisionObjects.getQuick(i);
			RigidBody body = RigidBody.upcast(colObj);

			if (body != null && body.getMotionState() != null && !body.isStaticOrKinematicObject()) {
				// we need to call the update at least once, even for sleeping objects
				// otherwise the 'graphics' transform never updates properly
				// so todo: add 'dirty' flag
				//if (body->getActivationState() != ISLAND_SLEEPING)
				{

					TransformTools.integrateTransform(
							body.getInterpolationWorldTransform(tmpTrans2),
							body.getInterpolationLinearVelocity(tmpLinVel),
							body.getInterpolationAngularVelocity(tmpAngVel),
							localTime * body.getHitFraction(), interpolatedTransform,
							iAxis, iDorn, iorn0, iPredictOrn, float4Temp);

					//					if(body instanceof RigidBodyExt){
					//						RigidBodyExt b = (RigidBodyExt)body;
					//						if(b.virtualString == null &&  b.getSegmentController().toString().contains("schema") && !b.getSegmentController().isOnServer()){
					//							System.err.println("--------->0 MMMMA UP "+GlUtil.getUpVector(new Vector3f(),body.getMotionState().getWorldTransform(new Transform())));
					//						}
					//					}

					body.getMotionState().setWorldTransform(interpolatedTransform);

					//					if(body instanceof RigidBodyExt){
					//						RigidBodyExt b = (RigidBodyExt)body;
					//						if(b.virtualString == null &&  b.getSegmentController().toString().contains("schema") && !b.getSegmentController().isOnServer()){
					//							System.err.println("--------->1 MMMMA UP "+GlUtil.getUpVector(new Vector3f(),body.getMotionState().getWorldTransform(new Transform())));
					//						}
					//					}
				}
			}

		}

		if (getDebugDrawer() != null && (getDebugDrawer().getDebugMode() & DebugDrawModes.DRAW_WIREFRAME) != 0) {
			for (int i = 0; i < vehicles.size(); i++) {
				for (int v = 0; v < vehicles.getQuick(i).getNumWheels(); v++) {
					// synchronize the wheels with the (interpolated) chassis worldtransform
					vehicles.getQuick(i).updateWheelTransform(v, true);
				}
			}
		}
	}

	@Override
	public int stepSimulation(float timeStep, int maxSubSteps, float fixedTimeStep) {
		startProfiling(timeStep);

		long t0 = System.nanoTime();
		int clampedSimulationSteps = 0;
		BulletStats.pushProfile("stepSimulation");
		try {
			int numSimulationSubSteps = 0;

			if (maxSubSteps != 0) {
				// fixed timestep with interpolation
				localTime += timeStep;
				if (localTime >= fixedTimeStep) {
					numSimulationSubSteps = (int) (localTime / fixedTimeStep);
					localTime -= numSimulationSubSteps * fixedTimeStep;
				}
			} else {
				//variable timestep
				fixedTimeStep = timeStep;
				localTime = timeStep;
				if (ScalarUtil.fuzzyZero(timeStep)) {
					numSimulationSubSteps = 0;
					maxSubSteps = 0;
				} else {
					numSimulationSubSteps = 1;
					maxSubSteps = 1;
				}
			}

			// process some debugging flags
			if (getDebugDrawer() != null) {
				BulletGlobals.setDeactivationDisabled((getDebugDrawer().getDebugMode() & DebugDrawModes.NO_DEACTIVATION) != 0);
			}
			//			for (int i = 0; i < collisionObjects.size(); i++) {
			//				CollisionObject colObj = collisionObjects.getQuick(i);
			//				if(colObj.toString().contains("SER") && colObj.toString().contains("Jaeger") ){
			//					System.err.println("PHYSICS: "+state+"; "+colObj+"; ");
			//				}
			//			}
			if (numSimulationSubSteps != 0) {
				saveKinematicState(fixedTimeStep);

				applyGravity();

				// clamp the number of substeps, to prevent simulation grinding spiralling down to a halt
				clampedSimulationSteps = (numSimulationSubSteps > maxSubSteps) ? maxSubSteps : numSimulationSubSteps;

				if(clampedSimulationSteps > 3 && getState() instanceof GameClientState){
					((GameClientState)getState()).getWorldDrawer().getShards().onSimulationStepBurst();
				}
				
				for (int i = 0; i < clampedSimulationSteps; i++) {
					//					for(int j = 0; j < getNumCollisionObjects(); j++){
					//						CollisionObject o = getCollisionObjectArray().getQuick(j);
					//						assert(!o.getCollisionShape().isConcave()):o;
					//						if(o instanceof RigidBodyExt){
					//							SegmentController c = ((RigidBodyExt)o).getSegmentController();
					//							if(c instanceof ManagedSegmentController<?>){
					//								ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>)c).getManagerContainer();
					//								if(managerContainer instanceof ManagerThrustInterface){
					//									ManagerThrustInterface m = (ManagerThrustInterface)managerContainer;
					//									m.getThrusterElementManager().orientate(fixedTimeStep);
					//								}
					//							}
					//						}
					//					}

					internalSingleStepSimulation(fixedTimeStep);
					synchronizeMotionStates();
				}
			}

			synchronizeMotionStates();

			clearForces();

			//#ifndef BT_NO_PROFILE
			CProfileManager.incrementFrameCounter();
			//#endif //BT_NO_PROFILE

			return numSimulationSubSteps;
		} finally {
			BulletStats.popProfile();

			BulletStats.stepSimulationTime = (System.nanoTime() - t0) / 1000000;
		}
		
		
	}
	
	private static class ClosestNotMeConvexResultCallback extends ClosestConvexResultCallback {
		private CollisionObject me;
		private float allowedPenetration = 0f;
		private OverlappingPairCache pairCache;
		private Dispatcher dispatcher;

		public ClosestNotMeConvexResultCallback(CollisionObject me, Vector3f fromA, Vector3f toA, OverlappingPairCache pairCache, Dispatcher dispatcher) {
			super(fromA, toA);
			this.me = me;
			this.pairCache = pairCache;
			this.dispatcher = dispatcher;
		}
		Vector3f linVelA = new Vector3f();
		Vector3f linVelB = new Vector3f();
		Vector3f relativeVelocity = new Vector3f();
		@Override
		public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
			if (convexResult.hitCollisionObject == me) {
				return 1f;
			}

//			Vector3f linVelA = new @Stack Vector3f(), linVelB = new @Stack Vector3f();
			linVelA.sub(convexToWorld, convexFromWorld);
			linVelB.set(0f, 0f, 0f);//toB.getOrigin()-fromB.getOrigin();

//			Vector3f relativeVelocity = new @Stack Vector3f();
			relativeVelocity.sub(linVelA, linVelB);
			// don't report time of impact for motion away from the contact normal (or causes minor penetration)
			if (convexResult.hitNormalLocal.dot(relativeVelocity) >= -allowedPenetration) {
				return 1f;
			}

			return super.addSingleResult(convexResult, normalInWorldSpace);
		}

		@Override
		public boolean needsCollision(BroadphaseProxy proxy0) {
			// don't collide with itself
			if (proxy0.clientObject == me) {
				return false;
			}

			// don't do CCD when the collision filters are not matching
			if (!super.needsCollision(proxy0)) {
				return false;
			}

			CollisionObject otherObj = (CollisionObject)proxy0.clientObject;

			// call needsResponse, see http://code.google.com/p/bullet/issues/detail?id=179
			if (dispatcher.needsResponse(me, otherObj)) {
				// don't do CCD when there are already contact points (touching contact/penetration)
				ObjectArrayList<PersistentManifold> manifoldArray = new ObjectArrayList<PersistentManifold>();
				BroadphasePair collisionPair = pairCache.findPair(me.getBroadphaseHandle(), proxy0);
				if (collisionPair != null) {
					if (collisionPair.algorithm != null) {
						//manifoldArray.resize(0);
						collisionPair.algorithm.getAllContactManifolds(manifoldArray);
						for (int j=0; j<manifoldArray.size(); j++) {
							PersistentManifold manifold = manifoldArray.getQuick(j);
							if (manifold.getNumContacts() > 0) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}
	}
	Vector3f tmpIT = new Vector3f();//new @Stack Vector3f();
	Transform tmpTransIT = new Transform();//new @Stack Transform();

	Transform predictedTransIT = new Transform();//new @Stack Transform();
	@Override
	protected void integrateTransforms(float timeStep) {
		BulletStats.pushProfile("integrateTransforms");
		try {
			
			for (int i=0; i<collisionObjects.size(); i++) {
				CollisionObject colObj = collisionObjects.getQuick(i);
				RigidBody body = RigidBody.upcast(colObj);
				if (body != null) {
					body.setHitFraction(1f);

					if (body.isActive() && (!body.isStaticOrKinematicObject())) {
//						System.err.println("NOW: "+body+"; \n"+body.getWorldTransform(new Transform()).getMatrix(new Matrix4f()));
						body.predictIntegratedTransform(timeStep, predictedTransIT);

						tmpIT.sub(predictedTransIT.origin, body.getWorldTransform(tmpTransIT).origin);
						float squareMotion = tmpIT.lengthSquared();

						if (body.getCcdSquareMotionThreshold() != 0f && body.getCcdSquareMotionThreshold() < squareMotion) {
							BulletStats.pushProfile("CCD motion clamping");
							try {
								if (body.getCollisionShape().isConvex()) {
									BulletStats.gNumClampedCcdMotions++;

									ClosestNotMeConvexResultCallback sweepResults = new ClosestNotMeConvexResultCallback(body, body.getWorldTransform(tmpTransIT).origin, predictedTransIT.origin, getBroadphase().getOverlappingPairCache(), getDispatcher());
									//ConvexShape convexShape = (ConvexShape)body.getCollisionShape();
									SphereShape tmpSphere = new SphereShape(body.getCcdSweptSphereRadius()); //btConvexShape* convexShape = static_cast<btConvexShape*>(body->getCollisionShape());

									sweepResults.collisionFilterGroup = body.getBroadphaseProxy().collisionFilterGroup;
									sweepResults.collisionFilterMask = body.getBroadphaseProxy().collisionFilterMask;

									convexSweepTest(tmpSphere, body.getWorldTransform(tmpTransIT), predictedTransIT, sweepResults);
									// JAVA NOTE: added closestHitFraction test to prevent objects being stuck
									if (sweepResults.hasHit() && (sweepResults.closestHitFraction > 0.0001f)) {
										body.setHitFraction(sweepResults.closestHitFraction);
										body.predictIntegratedTransform(timeStep * body.getHitFraction(), predictedTransIT);
										body.setHitFraction(0f);
										//System.out.printf("clamped integration to hit fraction = %f\n", sweepResults.closestHitFraction);
									}
								}
							}
							finally {
								BulletStats.popProfile();
							}
						}

						body.proceedToTransform(predictedTransIT);
					}
				}
			}
		}
		finally {
			BulletStats.popProfile();
		}
	}
	@Override
	protected void internalSingleStepSimulation(float timeStep) {
		BulletStats.pushProfile("internalSingleStepSimulation");
		try {
			// apply gravity, predict motion
			predictUnconstraintMotion(timeStep);

			//			if(state instanceof GameClientState){
			//				if(((GameClientState)state).getCharacter() != null){
			//					((GameClientState)state).getCharacter().inPlaceAttachedUpdate();
			//				}
			//			}

			DispatcherInfo dispatchInfo = getDispatchInfo();

			dispatchInfo.timeStep = timeStep;
			dispatchInfo.stepCount = 0;
			dispatchInfo.debugDraw = getDebugDrawer();

			// perform collision detection
			long ll = System.nanoTime();
			performDiscreteCollisionDetection();
			long distTime = (System.nanoTime() - ll);
calculateSimulationIslands();

			getSolverInfo().timeStep = timeStep;

			// solve contact and other joint constraints
			solveConstraints(getSolverInfo());

			//CallbackTriggers();
			
			// integrate transforms
			integrateTransforms(timeStep);

			// update vehicle simulation
			updateActions(timeStep);

			// update vehicle simulation
			updateVehicles(timeStep);

			updateActivationState(timeStep);

			if (internalTickCallback != null) {
				internalTickCallback.internalTick(this, timeStep);
			}
		} finally {
			BulletStats.popProfile();
		}
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.dynamics.DiscreteDynamicsWorld#addRigidBody(com.bulletphysics.dynamics.RigidBody, short, short)
	 */
	@Override
	public void addRigidBody(RigidBody body, short group, short mask) {

		super.addRigidBody(body, group, mask);
	}

	@Override
	public void updateActions(float timeStep) {
		boolean writeLastTransform = getState().getNumberOfUpdate() != lastUpdate;
		lastUpdate = getState().getNumberOfUpdate();
		for (int i = 0; i < collisionObjects.size(); i++) {
			CollisionObject colObj = collisionObjects.getQuick(i);
			RigidBody body = RigidBody.upcast(colObj);
			if (body != null) {
//				if(writeLastTransform && body instanceof GamePhysicsObject){
//					((GamePhysicsObject)body).getSimpleTransformableSendableObject().getPhysicsDataContainer().lastTransform.set(((GamePhysicsObject)body).getSimpleTransformableSendableObject().getWorldTransform());
//				}
				if (body instanceof GamePhysicsObject) {
					GameTransformable s = ((GamePhysicsObject) body).getSimpleTransformableSendableObject();
					s.getPhysicsDataContainer().updatePhysical(s.getState().getUpdateTime());
				}
				if (body instanceof RigidBodySegmentController) {
					updateDockedChilds(((RigidBodySegmentController) body).getSegmentController());
				}
			}
		}
		int actionUpdate = actionUpdateNum++;
		BulletStats.pushProfile("updateActions");
		try {
			for (int i = 0; i < actions.size(); i++) {
				actions.getQuick(i).updateAction(this, timeStep);
				if (actions.getQuick(i) instanceof KinematicCharacterControllerExt) {
					KinematicCharacterControllerExt k = (KinematicCharacterControllerExt) actions.getQuick(i);
					if (k.getUpdateNum() == actionUpdate) {
						System.err.println("Excpetion: " + getState() + " double character.  " + k + "; LISTING ALL");
						for (int j = 0; j < actions.size(); j++) {
							System.err.println("#" + j + ": " + actions.getQuick(i));
						}
						if (getState() instanceof GameClientState) {

							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("CLIENT ERROR\nDouble character update\ndetected:\n%s\nPlease restart and send in logs.",  k.toString()), 0);
						} else {
							((Sector) getState()).getState().getController().broadcastMessageAdmin(Lng.astr("SERVER ERROR\nDouble character update\ndetected:\n%s\nPlease send in server logs.",  k.toString()), ServerMessage.MESSAGE_TYPE_ERROR);
						}
					}
					k.setUpdateNum(actionUpdate);
				}
			}
		} finally {
			BulletStats.popProfile();
		}
	}

	@Override
	protected void updateActivationState(float timeStep) {
		BulletStats.pushProfile("updateActivationState");
		try {

			for (int i = 0; i < collisionObjects.size(); i++) {
				CollisionObject colObj = collisionObjects.getQuick(i);
				RigidBody body = RigidBody.upcast(colObj);
				if (body != null) {
					body.updateDeactivation(timeStep);

					if (body.wantsSleeping()) {

						//						if(body instanceof RigidDebrisBody){
						//							System.err.println("BODY WANTS SLWWPING: "+body);
						//						}
						if (body.isStaticOrKinematicObject()) {
							body.setActivationState(CollisionObject.ISLAND_SLEEPING);
						} else {
							if (body.getActivationState() == CollisionObject.ACTIVE_TAG) {
								body.setActivationState(CollisionObject.WANTS_DEACTIVATION);
							}
							if (body.getActivationState() == CollisionObject.ISLAND_SLEEPING) {
								tmp.set(0f, 0f, 0f);
								body.setAngularVelocity(tmp);
								body.setLinearVelocity(tmp);
							}
						}
					} else {
						if (body.getActivationState() != CollisionObject.DISABLE_DEACTIVATION) {
							body.setActivationState(CollisionObject.ACTIVE_TAG);
						}
					}
				}
			}
		} finally {
			BulletStats.popProfile();
		}
	}

	@Override
	protected void solveConstraints(ContactSolverInfo solverInfo) {
		BulletStats.pushProfile("solveConstraints");
		try {
			// sorted version of all btTypedConstraint, based on islandId
			sortedConstraints.clear();
			for (int i = 0; i < constraints.size(); i++) {
				sortedConstraints.add(constraints.getQuick(i));
			}
			//Collections.sort(sortedConstraints, sortConstraintOnIslandPredicate);
			MiscUtil.quickSort(sortedConstraints, sortConstraintOnIslandPredicate);

			ObjectArrayList<TypedConstraint> constraintsPtr = getNumConstraints() != 0 ? sortedConstraints : null;

			solverCallback.init(solverInfo, constraintSolver, constraintsPtr, sortedConstraints.size(), debugDrawer/*,m_stackAlloc*/, dispatcher1, getState());

			constraintSolver.prepareSolve(getCollisionWorld().getNumCollisionObjects(), getCollisionWorld().getDispatcher().getNumManifolds());

			// solve all the constraints for this island
			islandManager.buildAndProcessIslands(getCollisionWorld().getDispatcher(), getCollisionWorld().getCollisionObjectArray(), solverCallback);

			constraintSolver.allSolved(solverInfo, debugDrawer/*, m_stackAlloc*/);
		} finally {
			BulletStats.popProfile();
		}
	}

	@Override
	protected void predictUnconstraintMotion(float timeStep) {
		BulletStats.pushProfile("predictUnconstraintMotion");
		try {
			Transform tmpTrans = tmpTTTrans;

			for (int i = 0; i < collisionObjects.size(); i++) {
				CollisionObject colObj = collisionObjects.getQuick(i);
				RigidBody body = RigidBody.upcast(colObj);
				if (body != null) {
					if (!body.isStaticOrKinematicObject()) {
						if (body.isActive()) {
							if (body.getCollisionShape() instanceof CubesCompoundShape) {
								((CubesCompoundShape) body.getCollisionShape()).getSegmentController()
										.getPhysicsDataContainer().checkCenterOfMass(body);
							}
							body.integrateVelocities(timeStep);
							// damping
							body.applyDamping(timeStep);
							
							body.predictIntegratedTransform(timeStep, body.getInterpolationWorldTransform(tmpTrans));
						}
					}
				}
			}
		} finally {
			BulletStats.popProfile();
		}
	}

	//	/* (non-Javadoc)
	//	 * @see com.bulletphysics.dynamics.DiscreteDynamicsWorld#internalSingleStepSimulation(float)
	//	 */
	//	@Override
	//	protected void internalSingleStepSimulation(float timeStep) {
	//		System.err.println("INTERNAL TIME STEP");
	//		super.internalSingleStepSimulation(timeStep);
	//	}
	//
	//	@Override
	//	public int stepSimulation(float timeStep, int maxSubSteps, float fixedTimeStep) {
	//		int i = super.stepSimulation(timeStep, maxSubSteps, fixedTimeStep);
	//		System.err.println("TS: "+i);
	//		return i;
	//	}
	//

	//	protected void predictUnconstraintMotion(float timeStep) {
	//		BulletStats.pushProfile("predictUnconstraintMotion");
	//		try {
	//			Transform tmpTrans = new Transform();
	//
	//			for (int i = 0; i < collisionObjects.size(); i++) {
	//				CollisionObject colObj = collisionObjects.getQuick(i);
	//				RigidBody body = RigidBody.upcast(colObj);
	//
	//				if (body != null) {
	////					System.err.println("Predicting: "+body.getCollisionShape()+" "+body.isStaticOrKinematicObject()+ ", active: "+body.isActive());
	//					if (body.isActive() && !body.isStaticOrKinematicObject()) {
	//							System.err.println("Predicting2: "+body.getCollisionShape());
	//							body.integrateVelocities(timeStep);
	//							// damping
	//							body.applyDamping(timeStep);
	//
	//							body.predictIntegratedTransform(timeStep, body.getInterpolationWorldTransform(tmpTrans));
	//					}
	//				}
	//			}
	//		}
	//		finally {
	//			BulletStats.popProfile();
	//		}
	//	}
	//
	//	// JAVA NOTE: ported from 2.74, missing contact threshold stuff
	//		public void updateSingleAabb(CollisionObject colObj) {
	//			Vector3f minAabb = new Vector3f(), maxAabb = new Vector3f();
	//			Vector3f tmp = new Vector3f();
	//			Transform tmpTrans = new Transform();
	//			colObj.getCollisionShape().getAabb(colObj.getWorldTransform(tmpTrans), minAabb, maxAabb);
	//			// need to increase the aabb for contact thresholds
	//			Vector3f contactThreshold = new Vector3f();
	//			contactThreshold.set(BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold());
	//			minAabb.sub(contactThreshold);
	//			maxAabb.add(contactThreshold);
	//
	//			BroadphaseInterface bp = broadphasePairCache;
	//
	//			// moving objects should be moderately sized, probably something wrong if not
	//			tmp.sub(maxAabb, minAabb); // TODO: optimize
	//			if (colObj.isStaticObject() || (tmp.lengthSquared() < 1e12f)) {
	//				bp.setAabb(colObj.getBroadphaseHandle(), minAabb, maxAabb, dispatcher1);
	//			}
	//			else {
	//				float[] t = new float[16];
	//				tmpTrans.getOpenGLMatrix(t);
	//				System.err.println("AABB: "+minAabb+", "+maxAabb+": "+colObj.getCollisionShape()+": "+Arrays.toString(t));
	//				try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
	//				// something went wrong, investigate
	//				// this assert is unwanted in 3D modelers (danger of loosing work)
	////				colObj.setActivationState(CollisionObject.DISABLE_SIMULATION);
	//
	//
	//			}
	//		}
	//
	//	protected void integrateTransforms(float timeStep) {
	//		BulletStats.pushProfile("integrateTransforms");
	//		try {
	//			Vector3f tmp = new Vector3f();
	//			Transform tmpTrans = new Transform();
	//
	//			Transform predictedTrans = new Transform();
	//			for (int i=0; i<collisionObjects.size(); i++) {
	//				CollisionObject colObj = collisionObjects.getQuick(i);
	//				RigidBody body = RigidBody.upcast(colObj);
	//				if (body != null) {
	//					body.setHitFraction(1f);
	//
	//					if (body.isActive() && (!body.isStaticOrKinematicObject())) {
	//						body.predictIntegratedTransform(timeStep, predictedTrans);
	//
	//						tmp.sub(predictedTrans.origin, body.getWorldTransform(tmpTrans).origin);
	//						float squareMotion = tmp.lengthSquared();
	//
	//						if (body.getCcdSquareMotionThreshold() != 0f && body.getCcdSquareMotionThreshold() < squareMotion) {
	//							BulletStats.pushProfile("CCD motion clamping");
	//							try {
	//								if (body.getCollisionShape().isConvex()) {
	//									BulletStats.gNumClampedCcdMotions++;
	//
	//									ClosestNotMeConvexResultCallback sweepResults = new ClosestNotMeConvexResultCallback(body, body.getWorldTransform(tmpTrans).origin, predictedTrans.origin, getBroadphase().getOverlappingPairCache(), getDispatcher());
	//									//ConvexShape convexShapeTransform = (ConvexShape)body.getCollisionShape();
	//									SphereShape tmpSphere = new SphereShape(body.getCcdSweptSphereRadius()); //btConvexShape* convexShapeTransform = static_cast<btConvexShape*>(body->getCollisionShape());
	//
	//									sweepResults.collisionFilterGroup = body.getBroadphaseProxy().collisionFilterGroup;
	//									sweepResults.collisionFilterMask = body.getBroadphaseProxy().collisionFilterMask;
	//
	//									convexSweepTest(tmpSphere, body.getWorldTransform(tmpTrans), predictedTrans, sweepResults);
	//									// JAVA NOTE: added closestHitFraction test to prevent objects being stuck
	//									if (sweepResults.hasHit() && (sweepResults.closestHitFraction > 0.0001f)) {
	//										body.setHitFraction(sweepResults.closestHitFraction);
	//										body.predictIntegratedTransform(timeStep * body.getHitFraction(), predictedTrans);
	//										body.setHitFraction(0f);
	//										//System.out.printf("clamped integration to hit fraction = %f\n", sweepResults.closestHitFraction);
	//									}
	//								}
	//							}
	//							finally {
	//								BulletStats.popProfile();
	//							}
	//						}
	//
	//						body.proceedToTransform(predictedTrans);
	//					}
	//				}
	//			}
	//		}
	//		finally {
	//			BulletStats.popProfile();
	//		}
	//	}
	//
	//	private static class ClosestNotMeConvexResultCallback extends ClosestConvexResultCallback {
	//		private CollisionObject me;
	//		private float allowedPenetration = 0f;
	//		private OverlappingPairCache pairCache;
	//		private Dispatcher dispatcher;
	//
	//		public ClosestNotMeConvexResultCallback(CollisionObject me, Vector3f fromA, Vector3f toA, OverlappingPairCache pairCache, Dispatcher dispatcher) {
	//			super(fromA, toA);
	//			this.me = me;
	//			this.pairCache = pairCache;
	//			this.dispatcher = dispatcher;
	//		}
	//
	//		@Override
	//		public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
	//			if (convexResult.hitCollisionObject == me) {
	//				return 1f;
	//			}
	//
	//			Vector3f linVelA = new @Stack Vector3f(), linVelB = new @Stack Vector3f();
	//			linVelA.sub(convexToWorld, convexFromWorld);
	//			linVelB.set(0f, 0f, 0f);//toB.getOrigin()-fromB.getOrigin();
	//
	//			Vector3f relativeVelocity = new @Stack Vector3f();
	//			relativeVelocity.sub(linVelA, linVelB);
	//			// don't report percentage of impact for motion away from the contact normal (or causes minor penetration)
	//			if (convexResult.hitNormalLocal.dot(relativeVelocity) >= -allowedPenetration) {
	//				return 1f;
	//			}
	//
	//			return super.addSingleResult(convexResult, normalInWorldSpace);
	//		}
	//
	//		@Override
	//		public boolean needsCollision(BroadphaseProxy proxy0) {
	//			// don't collide with itself
	//			if (proxy0.clientObject == me) {
	//				return false;
	//			}
	//
	//			// don't do CCD when the collision filters are not matching
	//			if (!super.needsCollision(proxy0)) {
	//				return false;
	//			}
	//
	//			CollisionObject otherObj = (CollisionObject)proxy0.clientObject;
	//
	//			// call needsResponse, see http://code.google.com/p/bullet/issues/detail?id=179
	//			if (dispatcher.needsResponse(me, otherObj)) {
	//				// don't do CCD when there are already contact points (touching contact/penetration)
	//				ObjectArrayList<PersistentManifold> manifoldArray = new ObjectArrayList<PersistentManifold>();
	//				BroadphasePair collisionPair = pairCache.findPair(me.getBroadphaseHandle(), proxy0);
	//				if (collisionPair != null) {
	//					if (collisionPair.algorithm != null) {
	//						//manifoldArray.resize(0);
	//						collisionPair.algorithm.getAllContactManifolds(manifoldArray);
	//						for (int j=0; j<manifoldArray.size(); j++) {
	//							PersistentManifold manifold = manifoldArray.getQuick(j);
	//							if (manifold.getNumContacts() > 0) {
	//								return false;
	//							}
	//						}
	//					}
	//				}
	//			}
	//			return true;
	//		}
	//	}

	/**
	 * @return the state
	 */
	public PhysicsState getState() {
		//getState() is null when it's cleaned up
		return physics.getState();
	}

	private static class InplaceSolverIslandCallbackExt extends SimulationIslandManager.IslandCallback {
		public ContactSolverInfo solverInfo;
		public ConstraintSolver solver;
		public ObjectArrayList<TypedConstraint> sortedConstraints;
		public int numConstraints;
		public IDebugDraw debugDrawer;
		//public StackAlloc* m_stackAlloc;
		public Dispatcher dispatcher;

		public void init(ContactSolverInfo solverInfo, ConstraintSolver solver, ObjectArrayList<TypedConstraint> sortedConstraints, int numConstraints, IDebugDraw debugDrawer, Dispatcher dispatcher, PhysicsState state) {
			this.solverInfo = solverInfo;
			this.solver = solver;
			this.sortedConstraints = sortedConstraints;
			this.numConstraints = numConstraints;
			this.debugDrawer = debugDrawer;
			this.dispatcher = dispatcher;

		}

		@Override
		public void processIsland(ObjectArrayList<CollisionObject> bodies, int numBodies, ObjectArrayList<PersistentManifold> manifolds, int manifolds_offset, int numManifolds, int islandId) {
			if (islandId < 0) {
				//				if(state instanceof ClientState){
				//					System.err.println("############# NO_SPLITTT");
				//				}
				// we don't split islands, so all constraints/contact manifolds/bodies are passed into the solver regardless the island id
				solver.solveGroup(bodies, numBodies, manifolds, manifolds_offset, numManifolds, sortedConstraints, 0, numConstraints, solverInfo, debugDrawer/*,m_stackAlloc*/, dispatcher);
			} else {
				//				if(state instanceof ClientState){
				//					System.err.println("-------------    SPLITTT");
				//				}
				// also add all non-contact constraints/joints for this island
				//ObjectArrayList<TypedConstraint> startConstraint = null;
				int startConstraint_idx = -1;
				int numCurConstraints = 0;
				int i;

				// find the first constraint for this island
				for (i = 0; i < numConstraints; i++) {
					if (getConstraintIslandId(sortedConstraints.getQuick(i)) == islandId) {
						//startConstraint = &m_sortedConstraints[i];
						//startConstraint = sortedConstraints.subList(i, sortedConstraints.size());
						startConstraint_idx = i;
						break;
					}
				}
				// count the number of constraints in this island
				for (; i < numConstraints; i++) {
					if (getConstraintIslandId(sortedConstraints.getQuick(i)) == islandId) {
						numCurConstraints++;
					}
				}

				// only call solveGroup if there is some work: avoid virtual function call, its overhead can be excessive
				if ((numManifolds + numCurConstraints) > 0) {
					solver.solveGroup(bodies, numBodies, manifolds, manifolds_offset, numManifolds, sortedConstraints, startConstraint_idx, numCurConstraints, solverInfo, debugDrawer/*,m_stackAlloc*/, dispatcher);
				}
			}
		}
	}

}
