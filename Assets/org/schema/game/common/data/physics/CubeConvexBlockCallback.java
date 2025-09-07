package org.schema.game.common.data.physics;

import org.schema.common.util.linAlg.Vector3i;

import com.bulletphysics.linearmath.Transform;

public class CubeConvexBlockCallback {
	public final Vector3i blockInfo = new Vector3i();
	public final Vector3i blockPos = new Vector3i();
	public final Transform boxTransform = new Transform();
	public final boolean[][] dodecaOverlap = new boolean[12][6];
	public short blockHp;
}
