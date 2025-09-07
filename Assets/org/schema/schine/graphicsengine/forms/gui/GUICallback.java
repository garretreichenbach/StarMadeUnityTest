package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.MouseEvent;

public interface GUICallback {
	public void callback(GUIElement callingGuiElement, MouseEvent event);

	public boolean isOccluded();
}
