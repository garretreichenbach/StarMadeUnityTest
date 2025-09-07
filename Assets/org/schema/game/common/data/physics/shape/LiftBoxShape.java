package org.schema.game.common.data.physics.shape;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;

public class LiftBoxShape extends BoxShape {

	public LiftBoxShape(Vector3f boxHalfExtents) {
		super(boxHalfExtents);
	}

}
