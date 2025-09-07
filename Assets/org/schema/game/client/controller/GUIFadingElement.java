package org.schema.game.client.controller;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public abstract class GUIFadingElement extends GUIElement {

	public GUIFadingElement(InputState state) {
		super(state);
	}

	public abstract void setFade(float val);

}
