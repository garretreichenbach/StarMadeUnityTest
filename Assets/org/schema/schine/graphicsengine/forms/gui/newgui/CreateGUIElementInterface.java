package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public interface CreateGUIElementInterface<O> {
	public GUIElement create(O o);

	public GUIElement createNeutral();
}
