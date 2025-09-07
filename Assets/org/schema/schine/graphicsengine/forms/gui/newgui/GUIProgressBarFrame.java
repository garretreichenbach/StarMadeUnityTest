package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUIProgressBarFrame extends GUIFilledArea {

	public GUIProgressBarFrame(InputState state, int width, int height) {
		super(state, width, height);
	}

	@Override
	protected int getLeftTop() {
		return 19;
	}

	@Override
	protected int getRightTop() {
		return 20;
	}

	@Override
	protected int getBottomLeft() {
		return 21;
	}

	@Override
	protected int getBottomRight() {
		return 22;
	}

	@Override
	protected String getCorners() {
		return "UI 16px-8x8-gui-";
	}

	@Override
	protected String getVertical() {
		return "UI 16px Vertical-8x1-gui-";
	}

	@Override
	protected String getHorizontal() {
		return "UI 16px Horizontal-1x8-gui-";
	}

	@Override
	protected String getBackground() {
		return "UI 16px Center_Case-gui-";
	}

	@Override
	protected float getTopOffset() {
		return 0 * 0.125f;//2f*0.0625f;
	}

	@Override
	protected float getBottomOffset() {
		return 1 * 0.125f;
	}

	@Override
	protected float getLeftOffset() {
		return 0 * 0.125f;//2f*0.0625f;
	}

	@Override
	protected float getRightOffset() {
		return 1 * 0.125f;
	}

}
