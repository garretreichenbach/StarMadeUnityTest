package org.schema.game.common.data.physics;

import org.schema.common.util.linAlg.Vector3b;

public class AABBb {
	Vector3b min, max;

	public AABBb() {
	}

	public AABBb(Vector3b startOut, Vector3b endOut) {
		min = startOut;
		max = endOut;
	}
}
