package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUIProgressBarDynamicFillableFilled extends GUIFilledArea {


	public GUIProgressBarDynamicFillableFilled(InputState state, int width, int height) {
		super(state, width, height);
	}

	@Override
	protected int getLeftTop() {
		return 34;
	}

	@Override
	protected int getRightTop() {
		return 35;
	}

	@Override
	protected int getBottomLeft() {
		return 36;
	}

	@Override
	protected int getBottomRight() {
		return 37;
	}
	@Override
	protected String getCorners() {
		return "UI 8px Corners-8x8-gui-";
	}

	@Override
	protected String getVertical() {
		return "UI 8px Vertical-32x1-gui-";
	}

	@Override
	protected String getHorizontal() {
		return "UI 8px Horizontal-1x32-gui-";
	}

	@Override
	protected String getBackground() {
		return "UI 8px Center_BarFull-gui-";
	}

	@Override
	protected float getTopOffset() {
		return 16f * x32;
	}

	@Override
	protected float getBottomOffset() {
		return 17f * x32;
	}

	@Override
	protected float getLeftOffset() {
		return 17f * x32;
	}

	@Override
	protected float getRightOffset() {
		return 18f * x32;
	}

}
