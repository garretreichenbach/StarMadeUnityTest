package org.schema.game.client.view.gui;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.network.client.ClientState;

public class BigTitleMessage extends AbstractTitleMessage {

	public BigTitleMessage(String id, ClientState state, String message, Color color) {
		super(id, state, message, color);
	}

	@Override
	public int getPosX() {
		return 100;
	}

	@Override
	public int getPosY() {
		return 100;
	}

	@Override
	public FontInterface getFont() {
		return FontSize.BIG_20;
	}

}
