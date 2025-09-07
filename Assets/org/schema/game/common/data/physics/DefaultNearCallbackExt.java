package org.schema.game.common.data.physics;

import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.DispatchFunc;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.dispatch.NearCallback;

public class DefaultNearCallbackExt extends NearCallback {
	private ManifoldResult contactPointResult = new ManifoldResult();

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.DefaultNearCallback#handleCollision(com.bulletphysics.collision.broadphase.BroadphasePair, com.bulletphysics.collision.dispatch.CollisionDispatcher, com.bulletphysics.collision.broadphase.DispatcherInfo)
	 */
	@Override
	public void handleCollision(BroadphasePair collisionPair,
	                            CollisionDispatcher dispatcher, DispatcherInfo dispatchInfo) {
		CollisionObject colObj0 = (CollisionObject) collisionPair.pProxy0.clientObject;
		CollisionObject colObj1 = (CollisionObject) collisionPair.pProxy1.clientObject;

		if (dispatcher.needsCollision(colObj0, colObj1)) {

			// dispatcher will keep algorithms persistent in the collision pair
			if (collisionPair.algorithm == null) {
				//				long t = System.currentTimeMillis();
				//				try{
				//					throw new NullPointerException(collisionPair+" CREATED ALGORITHM: "+colObj0+"; "+colObj1);
				//				}catch (Exception e) {
				//					e.printStackTrace();
				//				}
				collisionPair.algorithm = dispatcher.findAlgorithm(colObj0, colObj1);
				//				long took = System.currentTimeMillis() - t;
				//				System.err.println("Algo creation took: "+took+" "+colObj0+"; "+colObj1);
			}

			if (collisionPair.algorithm != null) {
				//ManifoldResult contactPointResult = new ManifoldResult(colObj0, colObj1);
				contactPointResult.init(colObj0, colObj1);
				//				System.err.println("CONTACT POINT RESULT "+colObj0+" -- "+colObj1);
				if (dispatchInfo.dispatchFunc == DispatchFunc.DISPATCH_DISCRETE) {
					//					System.err.println("ALGORITHM DISPATCHED FOR "+colObj0.getCollisionShape()+" + "+colObj1.getCollisionShape()+" -> "+collisionPair.algorithm);
					// discrete collision detection query
					collisionPair.algorithm.processCollision(colObj0, colObj1, dispatchInfo, contactPointResult);
				} else {
					// continuous collision detection query, time of impact (toi)
					float toi = collisionPair.algorithm.calculateTimeOfImpact(colObj0, colObj1, dispatchInfo, contactPointResult);
					if (dispatchInfo.timeOfImpact > toi) {
						dispatchInfo.timeOfImpact = toi;
					}
				}
			}

		}
	}

	public void clean() {
		//use a new one so the collision objects are no longer referenced
		//which causes memory leaks with the pools
		contactPointResult = new ManifoldResult();
	}

}
