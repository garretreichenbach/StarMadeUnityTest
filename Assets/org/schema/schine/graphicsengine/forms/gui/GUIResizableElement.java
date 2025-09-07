package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.input.InputState;

public abstract class GUIResizableElement extends GUIElement {

	public GUIResizableElement(InputState state) {
		super(state);
	}

	public abstract void setWidth(float width);

	public abstract void setHeight(float height);

	public void setWidth(int width) {
		setWidth((float) width);
	}

	public void setHeight(int height) {
		setHeight((float) height);
	}

}
