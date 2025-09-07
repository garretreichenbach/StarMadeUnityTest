package org.schema.schine.graphicsengine.forms;

public interface SelectableSprite {
	public float getSelectionDepth();

	public boolean isSelectable();

	public void onSelect(float depth);

	public void onUnSelect();
}
