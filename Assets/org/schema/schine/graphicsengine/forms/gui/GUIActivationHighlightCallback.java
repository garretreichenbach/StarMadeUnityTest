package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.input.InputState;

public interface GUIActivationHighlightCallback extends GUIActivationCallback {
	public boolean isHighlighted(InputState state);

}
