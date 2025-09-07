package org.schema.game.common.data.physics;

import org.schema.schine.network.objects.container.PhysicsDataContainer;

import com.bulletphysics.collision.dispatch.CollisionObject;

public class PairCachingGhostObjectUncollidable extends PairCachingGhostObjectExt {
	public PairCachingGhostObjectUncollidable(CollisionType type,
			PhysicsDataContainer physicsDataContainer) {
		super(type, physicsDataContainer);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#checkCollideWith(com.bulletphysics.collision.dispatch.CollisionObject)
	 */
	@Override
	public boolean checkCollideWith(CollisionObject co) {
		return !(co instanceof PairCachingGhostObjectUncollidable) && !(co instanceof RigidDebrisBody);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PCGhostObjUCMissile(" + getUserPointer() + ")";
	}
}
