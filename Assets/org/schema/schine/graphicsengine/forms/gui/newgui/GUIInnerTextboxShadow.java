package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class GUIInnerTextboxShadow extends GUIInnerWindow {

	private static float f32 = 1f / 32f;

	public GUIInnerTextboxShadow(InputState state,
	                             GUIElement element, int cornerDistance) {
		super(state, element, cornerDistance);
		hasBackground = false;
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
		return "UI 32px Corners-8x8-gui-";
	}

	@Override
	protected String getVertical() {
		return "UI 32px Vertical-8x1-gui-";
	}

	@Override
	protected String getHorizontal() {
		return "UI 32px-horizontals-1x32-gui-";
	}

	@Override
	protected String getBackground() {
		return "UI 8px Center_Textbox-gui-";
	}

	@Override
	protected float getTopOffset() {
		return 0f * f32;
	}

	@Override
	protected float getBottomOffset() {
		return 1f * f32;
	}

	@Override
	protected float getLeftOffset() {
		return 0f * 0.125f;
	}

	@Override
	protected float getRightOffset() {
		return 1f * 0.125f;
	}

}
