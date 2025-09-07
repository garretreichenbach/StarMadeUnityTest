package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class GUITextOverlayTableDropDown extends GUITextOverlay{

	public GUITextOverlayTableDropDown(int width, int height, InputState state) {
		super(ScrollableTableList.dropdownFontInterface, state);
	}


}
