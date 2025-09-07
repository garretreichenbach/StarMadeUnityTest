package org.schema.game.common.data.physics.shape;

import com.bulletphysics.collision.shapes.SphereShape;

public class ChangableSphereShape extends SphereShape {

	public ChangableSphereShape(float radius) {
		super(radius);
	}

	public void setRadius(float radius) {
		implicitShapeDimensions.x = radius;
		collisionMargin = radius;
	}
}
