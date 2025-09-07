package org.schema.game.common.util;

import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public interface Collisionable {
	public boolean needsManifoldCollision();

	public void onCollision(ManifoldPoint pt, Sendable sendableB);
}
