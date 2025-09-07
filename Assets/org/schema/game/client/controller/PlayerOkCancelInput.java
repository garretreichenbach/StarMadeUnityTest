package org.schema.game.client.controller;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public abstract class PlayerOkCancelInput extends DialogInput {

	private final GUIInputPanel inputPanel;

	public PlayerOkCancelInput(String windowid, InputState state, Object info, Object description) {
		super(state);
		inputPanel = new GUIInputPanel(windowid, state, this, info, description);
		inputPanel.setCallback(this);
	}

	public PlayerOkCancelInput(String windowid, InputState state, int initialWidth, int initialHeight, Object info, Object description) {
		super(state);
		inputPanel = new GUIInputPanel(windowid, state, initialWidth, initialHeight, this, info, description);
		inputPanel.setCallback(this);
	}

	public PlayerOkCancelInput(String windowid, InputState state, Object info, Object description, FontStyle style) {
		super(state);
		inputPanel = new GUIInputPanel(windowid, state, this, info, description, style);
		inputPanel.setCallback(this);
	}

	public PlayerOkCancelInput(String windowid, InputState state, int initialWidth, int initialHeight, Object info, Object description, FontStyle style) {
		super(state);
		inputPanel = new GUIInputPanel(windowid, state, initialWidth, initialHeight, this, info, description, style);
		inputPanel.setCallback(this);
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
		if (!isOccluded()) {
			if (event.pressedLeftMouse()) {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(264);
					pressedOK();
				}
				if (callingGuiElement.getUserPointer().equals("SECOND_OPTION")) {
					pressedSecondOption();
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					cancel();
				}
			}
		}
	}

	public void pressedSecondOption() {
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if (isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
			AudioController.fireAudioEventID(265);
			deactivate();
			return;
		}
		if (!allowChat() && e.isTriggered(KeyboardMappings.DIALOG_CONFIRM)) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
			AudioController.fireAudioEventID(266);
			pressedOK();
			return;
		}
	}

	@Override
	public GUIInputPanel getInputPanel() {
		return inputPanel;
	}

	@Override
	public abstract void onDeactivate();

	public abstract void pressedOK();

	public void setErrorMessage(String msg) {
		inputPanel.setErrorMessage(msg, 2000);
	}
}
