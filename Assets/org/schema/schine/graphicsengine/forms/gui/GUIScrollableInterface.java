package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector3f;

public interface GUIScrollableInterface {
	public float getScolledPercentHorizontal();

	public float getScolledPercentVertical();

	public boolean isInside();

	public void scrollHorizontal(float step);

	public void scrollVertical(float step);

	public ScrollingListener getScrollingListener();

	public Vector3f getRelMousePos();

	public void scrollHorizontalPercent(float percent);

	public void scrollVerticalPercent(float percent);

	public float getScrollBarHeight();

	public float getScrollBarWidth();

	public void scrollHorizontalPercentTmp(float v);

	public void scrollVerticalPercentTmp(float v);

	public boolean isVerticalActive();

    public float getContentToPanelPercentageY();

    public float getContentToPanelPercentageX();

	public boolean isActive();
}
