package org.schema.game.common.data.physics;

import org.schema.game.common.controller.elements.power.reactor.StabilizerPath;
import org.schema.game.common.data.world.SectorNotFoundRuntimeException;

import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.HashedOverlappingPairCache;
import com.bulletphysics.collision.broadphase.OverlapCallback;
import com.bulletphysics.collision.dispatch.CollisionObject;

public class HashedOverlappingPairCacheExt extends HashedOverlappingPairCache {

	@Override
	public boolean needsBroadphaseCollision(BroadphaseProxy proxy0, BroadphaseProxy proxy1) {
		//		if(Universe.getRandom() != null && proxy0.clientObject.toString().contains("CLI") || proxy1.clientObject.toString().contains("CLI")){
		//			if(proxy0.clientObject.toString().contains("schema_1370260089078") || proxy1.clientObject.toString().contains("schema_1370260089078")){
		//				System.err.println("COLLISION: "+proxy0.clientObject+"; "+proxy1.clientObject);
		//			}
		//		}
		if (proxy0.clientObject instanceof PairCachingGhostObjectUncollidable && proxy1.clientObject instanceof PairCachingGhostObjectUncollidable) {
			return false;
		}
		if((proxy0.clientObject instanceof RigidDebrisBody && ((RigidDebrisBody)proxy0.clientObject).noCollision)
				|| (proxy1.clientObject instanceof RigidDebrisBody && ((RigidDebrisBody)proxy1.clientObject).noCollision)){
			return false;
		}
		if (proxy0.clientObject instanceof RigidDebrisBody && proxy1.clientObject instanceof PairCachingGhostObjectUncollidable
				|| proxy1.clientObject instanceof RigidDebrisBody && proxy0.clientObject instanceof PairCachingGhostObjectUncollidable) {
			return false;
		}
		if ((proxy0.clientObject instanceof CollisionObject && ((CollisionObject)proxy1.clientObject).getUserPointer() instanceof StabilizerPath)
				|| (proxy1.clientObject instanceof CollisionObject && ((CollisionObject)proxy0.clientObject).getUserPointer() instanceof StabilizerPath)) {
			return false;
		}
		if (proxy0.clientObject instanceof RigidDebrisBody && proxy1.clientObject instanceof PairCachingGhostObjectExt
				|| proxy1.clientObject instanceof RigidDebrisBody && proxy0.clientObject instanceof PairCachingGhostObjectExt) {
			return false;
		}
		if (proxy0.clientObject instanceof RigidBodySegmentController && proxy1.clientObject instanceof RigidBodySegmentController) {
			RigidBodySegmentController c0 = (RigidBodySegmentController) proxy0.clientObject;
			RigidBodySegmentController c1 = (RigidBodySegmentController) proxy1.clientObject;

			//if both are segment controllers and one of them ignores physics
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
		if (getOverlapFilterCallback() != null) {
			return getOverlapFilterCallback().needBroadphaseCollision(proxy0, proxy1);
		}
		
		boolean collides = (proxy0.collisionFilterGroup & proxy1.collisionFilterMask) != 0;
		collides = collides && (proxy1.collisionFilterGroup & proxy0.collisionFilterMask) != 0;

		
		
//		System.err.println(collides+" "+proxy0.clientObject+" -> "+proxy1.clientObject+" "
//				+ "US::: "+proxy0.collisionFilterGroup+"; "+proxy0.collisionFilterMask+"; "
//				+ "THEM "+proxy1.collisionFilterGroup+"; "+proxy1.collisionFilterMask+";; "
//				+ "WE -> THEM "+(((proxy0.collisionFilterGroup & proxy1.collisionFilterMask) & 0xFFFF) != 0)+";  THEM -> US "+(((proxy1.collisionFilterGroup & proxy0.collisionFilterMask) & 0xFFFF) != 0));
		
		return collides;
	}

	@Override
	public void processAllOverlappingPairs(OverlapCallback callback, Dispatcher dispatcher) {
		//	printf("m_overlappingPairArray.size()=%d\n",m_overlappingPairArray.size());

		for (int i = 0; i < getOverlappingPairArray().size(); ) {

			BroadphasePair pair = getOverlappingPairArray().getQuick(i);
			try {
				if (callback.processOverlap(pair)) {
					removeOverlappingPair(pair.pProxy0, pair.pProxy1, dispatcher);
					BulletStats.gOverlappingPairs--;
				} else {
					i++;
				}
			} catch (SectorNotFoundRuntimeException e) {
				e.printStackTrace();
				System.err.println("[SERVER][RECOVER] SECTOR NOT FOUND EXCEPTION CATCHED SUCCESSFULLY: Pair: " + pair.pProxy0.clientObject + ", " + pair.pProxy1.clientObject + "; Removing pair to recover");
				removeOverlappingPair(pair.pProxy0, pair.pProxy1, dispatcher);
				BulletStats.gOverlappingPairs--;
			}
		}
	}

}
