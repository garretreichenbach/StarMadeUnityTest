package org.schema.game.common.data.physics;

import com.bulletphysics.collision.dispatch.CollisionObject;

public interface NonBlockHitCallback {
	/**
	 * 
	 * @param obj
	 * @return true if the beam should stop on this object or be ignored
	 */
	public boolean onHit(CollisionObject obj, float damage);
}
