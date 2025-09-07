package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.input.InputState;

public interface GUIActivationCallback {
	public boolean isVisible(InputState state);

	public boolean isActive(InputState state);
}
