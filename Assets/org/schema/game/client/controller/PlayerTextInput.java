package org.schema.game.client.controller;

import org.schema.game.client.view.gui.GUITextInputPanel;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.common.InputCharHandler;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.InputHandler;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.TextInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.input.InputType;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public abstract class PlayerTextInput extends DialogInput implements InputHandler, InputCharHandler, TextCallback {

	protected final GUITextInputPanel inputPanel;

	private final TextInput textInput;

	public PlayerTextInput(String windowId, InputState state, int width, int height, int limit, Object info, Object description) {
		super(state);
		textInput = new TextInput(limit, this);
		inputPanel = new GUITextInputPanel(windowId, state, width, height, this, info, description, textInput);
		inputPanel.setCallback(this);
	}

	public PlayerTextInput(String windowId, InputState state, int limit, Object info, Object description) {
		super(state);
		textInput = new TextInput(limit, this);
		inputPanel = new GUITextInputPanel(windowId, state, this, info, description, textInput);
		inputPanel.setCallback(this);
	}

	public PlayerTextInput(String windowId, InputState state, int limit, Object info, Object description, String predefined) {
		this(windowId, state, limit, info, description);
		if (predefined != null && predefined.length() > 0) {
			textInput.append(predefined);
			textInput.selectAll();
			textInput.update();
		}
	}

	public PlayerTextInput(String windowId, InputState state, int width, int height, int limit, Object info, Object description, String predefined) {
		this(windowId, state, width, height, limit, info, description);
		if (predefined != null && predefined.length() > 0) {
			textInput.append(predefined);
			textInput.selectAll();
			textInput.update();
		}
	}

	public void setText(String s) {
		textInput.clear();
		textInput.append(s);
		textInput.update();
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
				AudioController.fireAudioEventID(273);
				pressedOK();
			}
			if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(274);
				cancel();
			}
		}
	}

	/**
	 * @return the textInput
	 */
	public TextInput getTextInput() {
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
		if((e.getType() == InputType.KEYBOARD || e.getType() == InputType.KEYBOARD_MOD) && e.isCharacterEvent()){ 
			textInput.handleCharEvent(e);
		}else {
			System.err.println("NOT HANDLING INPUT: "+e);
		}
	}

	@Override
	public GUITextInputPanel getInputPanel() {
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

	public void setMinimumLength(int minimumLength) {
		this.textInput.setMinimumLength(minimumLength);
	}

	public void setPassworldField(boolean passworldField) {
		if (inputPanel instanceof GUITextInputPanel) {
			inputPanel.setDisplayAsPassword(passworldField);
		}
	}
}
