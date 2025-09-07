package org.schema.schine.graphicsengine.forms.gui.newgui;

public interface GUIWindowInterface {
	public abstract int getInnerCornerDistX();

	public abstract int getInnerCornerTopDistY();

	public abstract int getInnerCornerBottomDistY();

	public abstract int getInnerHeigth();

	public abstract int getInnerWidth();

	public abstract int getInnerOffsetX();

	public abstract int getInnerOffsetY();

	public abstract int getInset();

	public abstract int getTopDist();

	public abstract void cleanUp();

	public abstract float getHeight();

	public abstract float getWidth();

	public abstract boolean isActive();
}
