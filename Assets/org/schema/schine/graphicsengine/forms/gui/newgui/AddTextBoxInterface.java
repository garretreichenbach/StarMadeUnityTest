package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public interface AddTextBoxInterface {

	public int getHeight();

	public GUIElement createAndAttach(GUIAnchor content);
}
