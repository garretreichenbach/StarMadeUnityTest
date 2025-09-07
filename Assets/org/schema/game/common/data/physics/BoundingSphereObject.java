package org.schema.game.common.data.physics;

import org.schema.schine.graphicsengine.forms.BoundingSphere;

import com.bulletphysics.linearmath.Transform;

public interface BoundingSphereObject {
	public BoundingSphere getBoundingSphereTotal();
	public Transform getWorldTransform();
}
