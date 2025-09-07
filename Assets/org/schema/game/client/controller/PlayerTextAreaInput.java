package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.GUITextAreaInputPanel;
import org.schema.schine.common.InputCharHandler;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.InputHandler;
import org.schema.schine.common.TextAreaInput;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public abstract class PlayerTextAreaInput extends PlayerInput implements InputHandler, InputCharHandler, TextCallback {

	private final TextAreaInput textInput;

	private final GUIInputPanel inputPanel;

	public PlayerTextAreaInput(String windowId, GameClientState state, int width, int height, int limit, int lineLimit, Object info, Object description, FontInterface font) {
		super(state);
		textInput = new TextAreaInput(limit, lineLimit, this);
		inputPanel = new GUITextAreaInputPanel(windowId, state, width, height, this, info, description, textInput, font, true);
		inputPanel.setCallback(this);
	}

	public PlayerTextAreaInput(String windowId, GameClientState state, int limit, int lineLimit, Object info, Object description) {
		this(windowId, state, 400, 100, limit, lineLimit, info, description, FontSize.MEDIUM_15);
	}

	public PlayerTextAreaInput(String windowId, GameClientState state, int limit, int lineLimit, Object info, Object description, String predefined) {
		this(windowId, state, 400, 100, limit, lineLimit, info, description, predefined, FontSize.MEDIUM_15);
	}

	public PlayerTextAreaInput(String windowId, GameClientState state, int width, int height, int limit, int lineLimit, Object info, Object description, String predefined) {
		this(windowId, state, width, height, limit, lineLimit, info, description, predefined, FontSize.MEDIUM_15);
	}

	public PlayerTextAreaInput(String windowId, GameClientState state, int limit, int lineLimit, Object info, Object description, FontInterface font) {
		this(windowId, state, 400, 100, limit, lineLimit, info, description, font);
	}

	public PlayerTextAreaInput(String windowId, GameClientState state, int limit, int lineLimit, Object info, Object description, String predefined, FontInterface font) {
		this(windowId, state, 400, 100, limit, lineLimit, info, description, predefined, font);
	}

	public PlayerTextAreaInput(String windowId, GameClientState state, int width, int height, int limit, int lineLimit, Object info, Object description, String predefined, FontInterface font) {
		this(windowId, state, width, height, limit, lineLimit, info, description, font);
		if (predefined != null && predefined.length() > 0) {
			textInput.append(predefined);
			textInput.selectAll();
			textInput.update();
		}
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
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(271);
				pressedOK();
			}
			if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(272);
				cancel();
			}
		}
	}

	/**
	 * @return the textInput
	 */
	public TextAreaInput getTextInput() {
		return textInput;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if (isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
			deactivate();
			return;
		}
		textInput.handleKeyEvent(e);
	}

	@Override
	public void handleCharEvent(KeyEventInterface e) {
		textInput.handleCharEvent(e);
	}

	@Override
	public GUIInputPanel getInputPanel() {
		return inputPanel;
	}

	@Override
	public abstract void onDeactivate();

	public abstract boolean onInput(String entry);

	/* (non-Javadoc)
	 * @see org.schema.schine.common.TextCallback#onTextEnter(java.lang.String)
	 */
	@Override
	public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
		if (onInput(entry)) {
			deactivate();
		}
	}

	@Override
	public void newLine() {
		inputPanel.newLine();
	}

	public void pressedOK() {
		textInput.enter();
	}

	public void setErrorMessage(String msg) {
		inputPanel.setErrorMessage(msg, 2000);
	}

	public void setInputChecker(InputChecker c) {
		textInput.setInputChecker(c);
	}
}
