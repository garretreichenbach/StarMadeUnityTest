package org.schema.game.common.data.physics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.network.objects.container.PhysicsDataContainer;

import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.HashedOverlappingPairCache;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.ConvexResultCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

public class PairCachingGhostObjectExt extends PairCachingGhostObject implements CollisionObjectInterface{

	private final Vector3f axis = new Vector3f();
	private final Matrix3f dmat = new Matrix3f();
	private final Quat4f dorn = new Quat4f();
	HashedOverlappingPairCache hashPairCache = new HashedOverlappingPairCacheExt();
	Vector3f castShapeAabbMin = new Vector3f();
	Vector3f castShapeAabbMax = new Vector3f();
	Transform convexFromTrans = new Transform();
	Transform convexToTrans = new Transform();
	Vector3f linVel = new Vector3f();
	Vector3f angVel = new Vector3f();
	Transform tmpTrans = new Transform();
	Vector3f collisionObjectAabbMin = new Vector3f();
	Vector3f collisionObjectAabbMax = new Vector3f();
	Vector3f hitNormal = new Vector3f();
	Transform R = new Transform();
	Quat4f quat = new Quat4f();
	private PhysicsDataContainer pCon;
	private SegmentController attached;
	private Matrix3f tmp = new Matrix3f();
	private final CollisionType type;

	
	public PairCachingGhostObjectExt(CollisionType type, PhysicsDataContainer physicsDataContainer) {
		this.type = type;
		this.pCon = physicsDataContainer;
	}

	

	private void setupSweepTest(Transform convexFromWorld, Transform convexToWorld, ConvexShape castShape) {
		convexFromTrans.set(convexFromWorld);
		convexToTrans.set(convexToWorld);

		quat.x = 0;
		quat.y = 0;
		quat.x = 0;
		quat.w = 0;
		// compute AABB that encompasses angular movement
		{
			assert (convexFromWorld.getMatrix(new Matrix4f()).determinant() != 0) : convexFromWorld.getMatrix(new Matrix4f());
			assert (convexToWorld.getMatrix(new Matrix4f()).determinant() != 0) : convexToWorld.getMatrix(new Matrix4f());
			TransformTools.calculateVelocity(convexFromTrans, convexToTrans, 1f, linVel, angVel, axis, tmp, dmat, dorn);

			R.setIdentity();
			R.setRotation(convexFromTrans.getRotation(quat));
			castShape.calculateTemporalAabb(R, linVel, angVel, 1f, castShapeAabbMin, castShapeAabbMax);
		}
	}

	@Override
	public void convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, CollisionWorld.ConvexResultCallback resultCallback, float allowedCcdPenetration) {

		setupSweepTest(convexFromWorld, convexToWorld, castShape);

		//		System.err.println("overlapping objects: "+overlappingObjects);

		// go over all objects, and if the ray intersects their aabb + cast shape aabb,
		// do a ray-shape query using convexCaster (CCD)
		long time = System.currentTimeMillis();

		ObjectArrayList<CollisionObject> overlappingObjects = this.overlappingObjects;
		boolean completeCheck = false;
		CollisionObject owner = null;
		if (resultCallback instanceof ClosestConvexResultCallbackExt) {
			ClosestConvexResultCallbackExt r = (ClosestConvexResultCallbackExt) resultCallback;
			owner = r.ownerObject;
			if (r.completeCheck) {
				ModifiedDynamicsWorld world = r.dynamicsWorld;
				overlappingObjects = world.getCollisionObjectArray();
				completeCheck = true;
			}

		}
		for (int i = 0; i < overlappingObjects.size(); i++) {
			CollisionObject collisionObject = overlappingObjects.getQuick(i);
			testObject(collisionObject, resultCallback, convexFromWorld, convexToWorld, castShape, allowedCcdPenetration, completeCheck, owner);

		}

		int pk = (int) (System.currentTimeMillis() - time);
		if (pk > 21) {
			System.err.println("[GHOST-OBJECT] SWEEP TEST TIME: " + pk);
		}
	}

	private void testObject(CollisionObject collisionObject, ConvexResultCallback resultCallback, Transform convexFromWorld, Transform convexToWorld, ConvexShape castShape, float allowedCcdPenetration, boolean completeCheck, CollisionObject owner) {
		if (collisionObject == owner || collisionObject == pCon.getObject() || (owner != null && owner instanceof RigidBodySegmentController && ((RigidBodySegmentController) owner).isRelatedTo(collisionObject))) {
			return;
		}

		// only perform raycast if filterMask matches
		if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {

			//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();

			collisionObject.getCollisionShape().getAabb(collisionObject.getWorldTransform(tmpTrans), collisionObjectAabbMin, collisionObjectAabbMax);

			AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);

			float[] hitLambda = new float[]{1f}; // could use resultCallback.closestHitFraction, but needs testing
			hitNormal.set(0, 0, 0);
			if (AabbUtil2.rayAabb(convexFromWorld.origin, convexToWorld.origin, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
				ModifiedDynamicsWorld.objectQuerySingle(castShape, convexFromTrans, convexToTrans,
						collisionObject,
						collisionObject.getCollisionShape(),
						collisionObject.getWorldTransform(tmpTrans),
						resultCallback,
						allowedCcdPenetration);

				if (completeCheck) {
					//					System.err.println("ADDING TO OVERLAP "+collisionObject);
					ClosestConvexResultCallbackExt r = (ClosestConvexResultCallbackExt) resultCallback;
					r.overlapping.add(collisionObject);
				}
			}
		}
	}

	/**
	 * @return the attached
	 */
	public SegmentController getAttached() {
		return attached;
	}

	/**
	 * @param attached the attached to set
	 */
	public void setAttached(SegmentController attached) {
		this.attached = attached;
	}

	@Override
	public void setWorldTransform(Transform worldTransform) {
		//		assert(worldTransform.getMatrix(new Matrix4f()).determinant() != 0):worldTransform.getMatrix(new Matrix4f());
		//		try{
		//
		//			if(worldTransform.origin.length() < 10){
		//				throw new NullPointerException("SERVER SETTING "+worldTransform.origin);
		//			}
		//		}catch(Exception e){
		//			e.printStackTrace();
		//		}
		//		System.err.println("SET WORLD TRANS FOR GHHOST "+worldTransform.origin);
		this.worldTransform.set(worldTransform);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PCGhostObjExt(" + getUserPointer() + ")@" + hashCode();
	}

	/**
	 * This method is mainly for expert/internal use only.
	 */
	@Override
	public void addOverlappingObjectInternal(BroadphaseProxy otherProxy, BroadphaseProxy thisProxy) {
		BroadphaseProxy actualThisProxy = thisProxy != null ? thisProxy : getBroadphaseHandle();
		assert (actualThisProxy != null);

		CollisionObject otherObject = (CollisionObject) otherProxy.clientObject;
		assert (otherObject != null);

		// if this linearSearch becomes too slow (too many overlapping objects) we should add a more appropriate data structure
		int index = overlappingObjects.indexOf(otherObject);
		if (index == -1) {
			overlappingObjects.add(otherObject);
			hashPairCache.addOverlappingPair(actualThisProxy, otherProxy);
		}
	}

	@Override
	public void removeOverlappingObjectInternal(BroadphaseProxy otherProxy, Dispatcher dispatcher, BroadphaseProxy thisProxy1) {
		CollisionObject otherObject = (CollisionObject) otherProxy.clientObject;
		BroadphaseProxy actualThisProxy = thisProxy1 != null ? thisProxy1 : getBroadphaseHandle();
		assert (actualThisProxy != null);

		assert (otherObject != null);
		int index = overlappingObjects.indexOf(otherObject);
		if (index != -1) {
			overlappingObjects.setQuick(index, overlappingObjects.getQuick(overlappingObjects.size() - 1));
			overlappingObjects.removeQuick(overlappingObjects.size() - 1);
			hashPairCache.removeOverlappingPair(actualThisProxy, otherProxy, dispatcher);
		}

	}

	@Override
	public HashedOverlappingPairCache getOverlappingPairCache() {
		return hashPairCache;
	}



	@Override
	public CollisionType getType() {
		return type;
	}

}
