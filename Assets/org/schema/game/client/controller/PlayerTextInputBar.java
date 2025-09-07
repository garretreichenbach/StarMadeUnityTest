package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.InputCharHandler;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.InputHandler;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.TextInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextInput;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

public abstract class PlayerTextInputBar extends PlayerInput implements TextCallback {

	private final GUIElement inputPanel;
	private final TextInput textInput;

	protected PlayerTextInputBar(GameClientState state, int limit, GUIElement inputPanel, GUITextInput guiTextInput) {
		super(state);
		this.inputPanel = inputPanel;
		textInput = new TextInput(limit, this) {

			/* (non-Javadoc)
			 * @see org.schema.schine.common.TextAreaInput#enter()
			 */
			@Override
			public void enter() {
				deactivate();
			}
		};

		guiTextInput.setTextInput(textInput);
		guiTextInput.setTextBox(true);
	}

	protected PlayerTextInputBar(GameClientState state, int limit, int lineLimit, GUIElement inputPanel, GUITextInput guiTextInput) {
		super(state);
		this.inputPanel = inputPanel;
		textInput = new TextInput(limit, lineLimit, this) {
			@Override
			public void enter() {
				deactivate();
			}
		};

		guiTextInput.setTextInput(textInput);
		guiTextInput.setTextBox(true);
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
		//		if(event.pressedLeftMouse()){
		//
		//			if (callingGuiElement.getUserPointer().equals("OK")) {
		//				pressedOK();
		//			}
		//			if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
		//				cancel();
		//			}
		//
		//
		//		}
	}

	public String getText() {
		return textInput.getCache();
	}

	public void setText(String text) {
		textInput.setCache(text);
	}

	/**
	 * @return the textInput
	 */
	public TextInput getTextInput() {
		return textInput;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if(isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
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
	public GUIElement getInputPanel() {
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

		if(onInput(entry)) {
			deactivate();
		}
	}

	@Override
	public void newLine() {

	}

	public void pressedOK() {
		textInput.enter();
	}

	public void reset() {
		textInput.clear();
	}

	public void setErrorMessage(String msg) {
		//		inputPanel.setErrorMessage(msg, 2000);
	}

	public void setInputChecker(InputChecker c) {
		textInput.setInputChecker(c);
	}

	public void setMinimumLength(int minimumLength) {
		textInput.setMinimumLength(minimumLength);
	}

}
