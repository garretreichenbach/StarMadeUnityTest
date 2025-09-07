package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.input.InputState;

public abstract class GUIColoredAnchor extends GUIAnchor {

	public GUIColoredAnchor(InputState state, int width, int height) {
		super(state, width, height);
	}

	public GUIColoredAnchor(InputState state) {
		super(state);
	}

	public abstract Vector4f getColor();

	public abstract void setColor(Vector4f c);
}
