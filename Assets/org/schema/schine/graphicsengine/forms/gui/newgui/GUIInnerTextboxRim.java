package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class GUIInnerTextboxRim extends GUIInnerWindow {

	public GUIInnerTextboxRim(InputState state,
	                          GUIElement element, int cornerDistance) {
		super(state, element, cornerDistance);
	}

	@Override
	protected int getLeftTop() {
		return 16;
	}

	@Override
	protected int getRightTop() {
		return 17;
	}

	@Override
	protected int getBottomLeft() {
		return 18;
	}

	@Override
	protected int getBottomRight() {
		return 19;
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
		return "UI 8px Center_Textbox-gui-";
	}

	@Override
	protected float getTopOffset() {
		return 4f * x32;
	}

	@Override
	protected float getBottomOffset() {
		return 5f * x32;
	}

	@Override
	protected float getLeftOffset() {
		return 4f * x32;
	}

	@Override
	protected float getRightOffset() {
		return 5f * x32;
	}

}
