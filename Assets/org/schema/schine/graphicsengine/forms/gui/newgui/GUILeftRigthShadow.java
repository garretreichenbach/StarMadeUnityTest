package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUILeftRigthShadow extends GUILeftRightArea {

	public GUILeftRigthShadow(InputState state, int width, int height) {
		super(state, width, height);
	}

	@Override
	public int getPXWidth() {
		return 32;
	}

	@Override
	public int getPXHeight() {
		return 32;
	}

	@Override
	protected String getVertical() {
		return "UI 32px Vertical-8x1-gui-";
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
