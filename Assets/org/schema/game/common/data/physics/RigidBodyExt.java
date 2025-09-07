package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

public class RigidBodyExt extends RigidBody implements CollisionObjectInterface{
	private final CollisionType type;
	public RigidBodyExt(CollisionType type, float mass, MotionState motionState,
			CollisionShape collisionShape, Vector3f localInertia) {
		super(mass, motionState, collisionShape, localInertia);
		this.type = type;
	}

	public RigidBodyExt(CollisionType type, float mass, MotionState motionState,
			CollisionShape collisionShape) {
		super(mass, motionState, collisionShape);
		this.type = type;
	}

	public RigidBodyExt(CollisionType type, RigidBodyConstructionInfo constructionInfo) {
		super(constructionInfo);
		this.type = type;
	}

	@Override
	public CollisionType getType() {
		return type;
	}

}
