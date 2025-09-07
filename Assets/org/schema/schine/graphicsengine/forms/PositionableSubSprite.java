package org.schema.schine.graphicsengine.forms;

public interface PositionableSubSprite extends Positionable {
	public float getScale(long time);

	public int getSubSprite(Sprite sprite);

	public boolean canDraw();
}
