package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUIProgressBarDynamicFillableBackground extends GUIFilledArea {


	public GUIProgressBarDynamicFillableBackground(InputState state, int width, int height) {
		super(state, width, height);
	}

	@Override
	protected int getLeftTop() {
		return 0;
	}

	@Override
	protected int getRightTop() {
		return 1;
	}

	@Override
	protected int getBottomLeft() {
		return 2;
	}

	@Override
	protected int getBottomRight() {
		return 3;
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
		return "UI 8px Center_Background-gui-";
	}

	@Override
	protected float getTopOffset() {
		return 0 * x32;
	}

	@Override
	protected float getBottomOffset() {
		return 1f * x32;
	}

	@Override
	protected float getLeftOffset() {
		return 0 * x32;
	}

	@Override
	protected float getRightOffset() {
		return 1f * x32;
	}
}
