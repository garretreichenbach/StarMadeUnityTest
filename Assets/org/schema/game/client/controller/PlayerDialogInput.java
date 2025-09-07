package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIAwnserInterface;
import org.schema.game.client.view.gui.GUIInputInfoPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public abstract class PlayerDialogInput extends PlayerInput implements GUIAwnserInterface {

	private final GUIInputInfoPanel inputPanel;

	public PlayerDialogInput(GameClientState state, Object info, Object description, Object... awnsers) {
		super(state);
		inputPanel = new GUIInputInfoPanel("PlayerDialogInput", state, this, info, description, awnsers, this);
		inputPanel.setCallback(this);
		inputPanel.setOkButton(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.schema.schine.graphicsengine.forms.gui.GUICallback#callback(org.schema
	 * .schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer() != null && callingGuiElement.getUserPointer() instanceof Integer) {
				pressedAwnser(((Integer) callingGuiElement.getUserPointer()).intValue());
			} else {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(227);
					pressedOK();
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(228);
					cancel();
				}
			}
		}
	}

	@Override
	public abstract void pressedAwnser(int intValue);

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if (isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
			cancel();
			return;
		}
	}

	@Override
	public GUIInputInfoPanel getInputPanel() {
		return inputPanel;
	}

	@Override
	public abstract void onDeactivate();

	public abstract void pressedOK();

	public void setErrorMessage(String msg) {
		inputPanel.setErrorMessage(msg, 2000);
	}
}
