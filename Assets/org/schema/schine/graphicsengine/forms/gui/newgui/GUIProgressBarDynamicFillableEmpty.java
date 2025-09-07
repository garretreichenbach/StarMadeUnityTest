package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUIProgressBarDynamicFillableEmpty extends GUIFilledArea {


	public GUIProgressBarDynamicFillableEmpty(InputState state, int width, int height) {
		super(state, width, height);
	}

	@Override
	protected int getLeftTop() {
		return 38;
	}

	@Override
	protected int getRightTop() {
		return 39;
	}

	@Override
	protected int getBottomLeft() {
		return 40;
	}

	@Override
	protected int getBottomRight() {
		return 41;
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
		return "UI 8px Center_BarEmpty-gui-";
	}

	@Override
	protected float getTopOffset() {
		return 18f * x32;
	}

	@Override
	protected float getBottomOffset() {
		return 19f * x32;
	}

	@Override
	protected float getLeftOffset() {
		return 19f * x32;
	}

	@Override
	protected float getRightOffset() {
		return 20f * x32;
	}

}
