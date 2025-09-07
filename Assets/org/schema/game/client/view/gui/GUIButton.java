package org.schema.game.client.view.gui;

import org.schema.game.client.view.GameResourceLoader.StandardButtons;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.input.InputState;

public class GUIButton extends GUIOverlay {
	private final StandardButtons buttonType;

	public GUIButton(Sprite sprite, InputState state, StandardButtons buttonType, String callBackString, GUICallback guiCallback) {
		super(sprite, state);
		this.buttonType = buttonType;
		setMouseUpdateEnabled(true);
		setUserPointer(callBackString);
		setCallback(guiCallback);
	}

	@Override
	public void draw() {
		if (isInvisible()) {

			return;
		}
		setSpriteSubIndex(buttonType.getSpriteNum(this.isInside()));
		super.draw();
	}

	/**
	 * @return the buttonType
	 */
	public StandardButtons getButtonType() {
		return buttonType;
	}

}
