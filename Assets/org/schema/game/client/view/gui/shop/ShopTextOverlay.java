package org.schema.game.client.view.gui.shop;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.network.client.ClientState;

public class ShopTextOverlay extends GUITextOverlay {

	public ShopTextOverlay(int width, int height, ClientState state) {
		super(state);
		// TODO Auto-generated constructor stub
	}

	public ShopTextOverlay(int width, int height, FontInterface font,
	                       ClientState state) {
		super(font, state);
		// TODO Auto-generated constructor stub
	}

	public ShopTextOverlay(int width, int height, FontInterface font,
	                       Color color, ClientState state) {
		super(font, color, state);
		// TODO Auto-generated constructor stub
	}


}
