package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.input.InputState;

public class GUIHideableTextOverlay extends GUITextOverlay implements Hideable{

	private boolean hidden = false;
	public GUIHideableTextOverlay(int width, int height, InputState state) {
		super(state);
	}

	public GUIHideableTextOverlay(int width, int height, FontInterface font,
								  InputState state) {
		super(font, state);
	}

	public GUIHideableTextOverlay(int width, int height, FontInterface font,
                                  Color color, InputState state) {
		super(font, color, state);
	}

	@Override
	public void hide() {
		this.hidden = true;
	}

	@Override
	public void unhide() {
		this.hidden = false;
	}

	@Override
	public void draw() {
		if (!hidden) {
			super.draw();
		}
	}

}
