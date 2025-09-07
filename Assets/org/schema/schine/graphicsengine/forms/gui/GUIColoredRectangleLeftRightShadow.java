package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.gui.newgui.GUILeftRigthShadow;
import org.schema.schine.input.InputState;

public class GUIColoredRectangleLeftRightShadow extends GUIColoredRectangle {
	private GUILeftRigthShadow lwshadow;

	public GUIColoredRectangleLeftRightShadow(InputState state, int width,
	                                          int height, Vector4f color) {
		super(state, width, height, color);
		lwshadow = new GUILeftRigthShadow(getState(), (int) getWidth(), (int) getHeight());
		lwshadow.onInit();
	}

	@Override
	protected void doDrawRect() {
		super.doDrawRect();
		lwshadow.setWidth(getWidth());
		lwshadow.setHeight(getHeight());
		lwshadow.draw();
	}

}
