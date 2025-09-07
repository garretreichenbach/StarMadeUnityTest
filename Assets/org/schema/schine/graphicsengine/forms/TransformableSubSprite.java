package org.schema.schine.graphicsengine.forms;

import com.bulletphysics.linearmath.Transform;

public interface TransformableSubSprite {
	public float getScale(long time);

	public int getSubSprite(Sprite sprite);

	public boolean canDraw();
	
	public Transform getWorldTransform();
}
