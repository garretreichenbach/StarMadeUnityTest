package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;

public class RigidBodySimple extends RigidBodyExt implements GamePhysicsObject {

	SimpleTransformableSendableObject simpelTransformable;

	public RigidBodySimple(SimpleTransformableSendableObject controller, float mass, MotionState motionState,
	                       CollisionShape collisionShape) {
		super(controller.getCollisionType(), mass, motionState, collisionShape);
		this.simpelTransformable = controller;
		interpolationWorldTransform.setIdentity();
	}

	public RigidBodySimple(SimpleTransformableSendableObject controller, float mass, MotionState motionState,
	                       CollisionShape collisionShape, Vector3f localInertia) {
		super(controller.getCollisionType(), mass, motionState, collisionShape, localInertia);
		this.simpelTransformable = controller;
		interpolationWorldTransform.setIdentity();
	}

	public RigidBodySimple(SimpleTransformableSendableObject controller, RigidBodyConstructionInfo constructionInfo) {
		super(controller.getCollisionType(), constructionInfo);
		this.simpelTransformable = controller;
		interpolationWorldTransform.setIdentity();
	}

	@Override
	public SimpleTransformableSendableObject getSimpleTransformableSendableObject() {
		return simpelTransformable;
	}

}
