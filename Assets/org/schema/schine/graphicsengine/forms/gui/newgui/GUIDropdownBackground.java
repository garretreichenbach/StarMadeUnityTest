package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUIDropdownBackground extends GUIFilledArea {

	public GUIDropdownBackground(InputState state, int width, int height) {
		super(state, width, height);
	}

	@Override
	protected int getLeftTop() {
		return 22;
	}

	@Override
	protected int getRightTop() {
		return 23;
	}

	@Override
	protected int getBottomLeft() {
		return 24;
	}

	@Override
	protected int getBottomRight() {
		return 25;
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
		return "UI 8px Center_Filter-gui-";
	}

	@Override
	protected float getTopOffset() {
		return 10f * x32;
	}

	@Override
	protected float getBottomOffset() {
		return 11f * x32;
	}

	@Override
	protected float getLeftOffset() {
		return 11f * x32;
	}

	@Override
	protected float getRightOffset() {
		return 12f * x32;
	}

}
