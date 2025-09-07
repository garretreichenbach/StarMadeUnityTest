package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class GUITextOverlayTable extends GUITextOverlay{

	public GUITextOverlayTable(InputState state) {
		super(ScrollableTableList.tableFontInterface, state);
	}

//	public GUITextOverlayTable(int width, int height, UnicodeFont font, Color color, InputState state) {
//		super(width, height, font, color, state);
//	}

	public GUITextOverlayTable(FontInterface font, InputState state) {
		super(font, state);
	}

}
