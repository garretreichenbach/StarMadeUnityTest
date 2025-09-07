package org.schema.game.common.data.physics;

import java.util.Collections;

import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.util.ObjectArrayList;

public class CollisionDispatcherExt extends CollisionDispatcher {

	public CollisionDispatcherExt(CollisionConfiguration collisionConfiguration) {
		super(collisionConfiguration);

		setNearCallback(new DefaultNearCallbackExt());
	}

	public void cleanNearCallback() {
//		setNearCallback(null);
		((DefaultNearCallbackExt) getNearCallback()).clean();
	}

	private boolean checkInternalManifoldDestroyed(PersistentManifold manifold) {
		ObjectArrayList<PersistentManifold> internalManifoldPointer = getInternalManifoldPointer();
		for (int i = 0; i < internalManifoldPointer.size(); i++) {
			PersistentManifold p = internalManifoldPointer.getQuick(i);
			//			if(p.getBody0().toString().contains("|CLI") || p.getBody1().toString().contains("|CLI")){
			//				System.err.println("REST MANIFOLD: "+p.getBody0()+" -> "+p.getBody1());
			//			}
			if (p == manifold) {
				return false;
			}

		}
		return true;
	}


	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionDispatcher#findAlgorithm(com.bulletphysics.collision.dispatch.CollisionObject, com.bulletphysics.collision.dispatch.CollisionObject, com.bulletphysics.collision.narrowphase.PersistentManifold)
	 */
	//	@Override
	//	public CollisionAlgorithm findAlgorithm(CollisionObject body0, CollisionObject body1, PersistentManifold sharedManifold) {
	//		CollisionAlgorithm findAlgorithm = super.findAlgorithm(body0, body1, sharedManifold);
	//
	//		System.err.println("ALGORITHM FOR "+body0.getCollisionShape()+" + "+body1.getCollisionShape()+" = "+findAlgorithm.getClass().getSimpleName());
	//
	//		return findAlgorithm;
	//	}

	@Override
	public void freeCollisionAlgorithm(CollisionAlgorithm algo) {
		CollisionAlgorithmCreateFunc createFunc = algo.internalGetCreateFunc();
		algo.internalSetCreateFunc(null);
		createFunc.releaseCollisionAlgorithm(algo);

		algo.destroy();
	}

	@Override
	public PersistentManifold getNewManifold(Object b0, Object b1) {
		//gNumManifold++;

		//btAssert(gNumManifold < 65535);

		CollisionObject body0 = (CollisionObject) b0;
		CollisionObject body1 = (CollisionObject) b1;
		//		if(body0.toString().contains("CLI") && body0.toString().contains("CubesShape") && body1.toString().contains("CubesShape")){
		//			try{
		//				throw new NullPointerException(b0+"; "+b1+"; MANI: "+getInternalManifoldPointer().size());
		//			}catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}
		//		assert(checkForDouble(body0, body1, b0, b1));

		/*
		void* mem = 0;

		if (m_persistentManifoldPoolAllocator->getFreeCount())
		{
			mem = m_persistentManifoldPoolAllocator->allocate(sizeof(btPersistentManifold));
		} else
		{
			mem = btAlignedAlloc(sizeof(btPersistentManifold),16);

		}
		btPersistentManifold* manifold = new(mem) btPersistentManifold (body0,body1,0);
		manifold->m_index1a = m_manifoldsPtr.size();
		m_manifoldsPtr.push_back(manifold);
		 */

		PersistentManifold manifold = manifoldsPool.get();
		manifold.init(body0, body1, 0);

		manifold.index1a = getInternalManifoldPointer().size();
		getInternalManifoldPointer().add(manifold);

		return manifold;
	}

	@Override
	public void releaseManifold(PersistentManifold manifold) {
		//		try{
		//		throw new NullPointerException("RELEASING: "+manifold);
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//gNumManifold--;

		//printf("releaseManifold: gNumManifold %d\n",gNumManifold);
		clearManifold(manifold);

		// TODO: optimize
		int findIndex = manifold.index1a;
		assert (findIndex < getInternalManifoldPointer().size());
		Collections.swap(getInternalManifoldPointer(), findIndex, getInternalManifoldPointer().size() - 1);
		getInternalManifoldPointer().getQuick(findIndex).index1a = findIndex;
		getInternalManifoldPointer().removeQuick(getInternalManifoldPointer().size() - 1);

		manifold.setBodies(null, null);
		manifoldsPool.release(manifold);
		/*
		manifold->~btPersistentManifold();
		if (m_persistentManifoldPoolAllocator->validPtr(manifold))
		{
			m_persistentManifoldPoolAllocator->freeMemory(manifold);
		} else
		{
			btAlignedFree(manifold);
		}
		 */

		//release bodies so that old cubeShapeData is released
		//		if(manifold.getBody0().toString().contains("|CLI") || manifold.getBody1().toString().contains("|CLI")){
		//			System.err.println("RELEASING MANIFOLD: "+manifold.getBody0()+" -> "+manifold.getBody1());
		//		}

		assert (checkInternalManifoldDestroyed(manifold));

	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionDispatcher#needsCollision(com.bulletphysics.collision.dispatch.CollisionObject, com.bulletphysics.collision.dispatch.CollisionObject)
	 */
	@Override
	public boolean needsCollision(CollisionObject body0, CollisionObject body1) {
		if((body0 instanceof RigidDebrisBody && ((RigidDebrisBody)body0).noCollision)
				|| (body1 instanceof RigidDebrisBody && ((RigidDebrisBody)body1).noCollision)){
			return false;
		}
		if (body0 instanceof RigidDebrisBody && body1 instanceof PairCachingGhostObjectUncollidable
				|| body1 instanceof RigidDebrisBody && body0 instanceof PairCachingGhostObjectUncollidable) {
			return false;
		}
		//dont in broadphase
//		if (body0.getUserPointer() instanceof StabilizerPath || body1.getUserPointer() instanceof StabilizerPath) {
//			return false;
//		}
		if (body0 instanceof RigidDebrisBody && body1 instanceof PairCachingGhostObjectExt
				|| body1 instanceof RigidDebrisBody && body0 instanceof PairCachingGhostObjectExt) {
			return false;
		}
		if ((body0.getCollisionFlags() & CollisionFlags.KINEMATIC_OBJECT) == CollisionFlags.KINEMATIC_OBJECT
				&& (body1.getCollisionFlags() & CollisionFlags.KINEMATIC_OBJECT) == CollisionFlags.KINEMATIC_OBJECT) {
			return false;
		}
		if ((body0 instanceof RigidBodySegmentController && ((RigidBodySegmentController) body0).getSegmentController().isIgnorePhysics()) || (body1 instanceof RigidBodySegmentController && ((RigidBodySegmentController) body1).getSegmentController().isIgnorePhysics())) {
			return false;
		}
		if ((body0 instanceof RigidBodySegmentController && body1 instanceof RigidBodySegmentController) && (((RigidBodySegmentController) body0).isCollisionException() || ((RigidBodySegmentController) body1).isCollisionException())) {
			//if both are segment controllers and one of them ignores physics
			return false;
		}
		if (body0 instanceof RigidBodySegmentController && body1 instanceof RigidBodySegmentController) {
			RigidBodySegmentController c0 = (RigidBodySegmentController) body0;
			RigidBodySegmentController c1 = (RigidBodySegmentController) body1;

			if (c0.getSegmentController().isIgnorePhysics() || c1.getSegmentController().isIgnorePhysics()) {
				return false;
			}

			if (c0.virtualSec != null && c1.virtualSec != null) {
				//dont collide virtual objects (cant be done anyway)
				return false;//!c0.virtualSec.equals(c1.virtualSec);
			}
			if (c0.virtualSec != null && c1.virtualSec == null) {
				return c0.getCollisionShape() != c1.getCollisionShape();
			}
			if (c0.virtualSec == null && c1.virtualSec != null) {
				return c0.getCollisionShape() != c1.getCollisionShape();
			}
		}
		return super.needsCollision(body0, body1);
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionDispatcher#needsResponse(com.bulletphysics.collision.dispatch.CollisionObject, com.bulletphysics.collision.dispatch.CollisionObject)
	 */
	@Override
	public boolean needsResponse(CollisionObject body0, CollisionObject body1) {

		if (body0 instanceof RigidDebrisBody && body1 instanceof PairCachingGhostObjectUncollidable
				|| body1 instanceof RigidDebrisBody && body0 instanceof PairCachingGhostObjectUncollidable) {
			return false;
		} else if (body0 instanceof RigidDebrisBody && body1 instanceof PairCachingGhostObjectExt
				|| body1 instanceof RigidDebrisBody && body0 instanceof PairCachingGhostObjectExt) {
			return false;
		} else if (body0 instanceof PairCachingGhostObjectUncollidable && body1 instanceof PairCachingGhostObjectUncollidable) {
			return false;
		} else if ((body0 instanceof PairCachingGhostObjectExt && body1.getCollisionShape() instanceof CubesCompoundShape) ||
				(body1 instanceof PairCachingGhostObjectExt && body0.getCollisionShape() instanceof CubesCompoundShape)) {
			return false;
		} else if ((body0 instanceof PairCachingGhostObjectExt && body1.getCollisionShape() instanceof CubeShape) ||
				(body1 instanceof PairCachingGhostObjectExt && body0.getCollisionShape() instanceof CubeShape)) {
			return false;
		}
		return super.needsResponse(body0, body1);
	}
}
