package org.schema.game.common.data.physics.octree;

import javax.vecmath.Vector3f;

public interface ArrayOctreeAABBCallback {
	public void handle(int index, int lvl, Vector3f min, Vector3f max);
}
