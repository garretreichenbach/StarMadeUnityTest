package org.schema.game.client.view.gui;

import org.schema.schine.common.TextInput;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUITextInput;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

public class GUITextInputPanel extends GUIInputPanel {
	private GUITextInput guiTextInput;

	public GUITextInputPanel(String windowId, InputState state, GUICallback guiCallback,
	                         Object info, Object description, TextInput textInput) {
		super(windowId, state, guiCallback, info, description);
		guiTextInput = new GUITextInput(256, 32, state);
		guiTextInput.setTextBox(true);
		guiTextInput.setPreText("");
		guiTextInput.setTextInput(textInput);
	}

	public GUITextInputPanel(String windowId, InputState state, int width, int height, GUICallback guiCallback,
	                         Object info, Object description, TextInput textInput) {
		super(windowId, state, width, height, guiCallback, info, description);
		guiTextInput = new GUITextInput(256, 32, state);
		guiTextInput.setTextBox(true);
		guiTextInput.setPreText("");
		guiTextInput.setTextInput(textInput);
	}

	/**
	 * @param displayAsPassword the displayAsPassword to set
	 */
	public void setDisplayAsPassword(boolean displayAsPassword) {
		guiTextInput.setDisplayAsPassword(displayAsPassword);
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		guiTextInput.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#draw()
	 */
	@Override
	public void draw() {
		if (isNewHud()) {
			if (background != null && ((GUIDialogWindow) background).getMainContentPane() != null && ((GUIDialogWindow) background).getMainContentPane().getContent(0) != null) {
				guiTextInput.setPos(12, ((GUIDialogWindow) background).getMainContentPane().getContent(0).getHeight() - (32), 0);

				errorText.setPos(guiTextInput.getPos().x, guiTextInput.getPos().y - 16, 0);
			}
		}
		super.draw();
	}

	@Override
	public void onInit() {
		super.onInit();
		guiTextInput.onInit();
		getBackground().attach(guiTextInput);
		guiTextInput.setPos(55, 181, 0);
	}

	public void setInput(TextInput textInput) {
		if (guiTextInput == null) {
			guiTextInput = new GUITextInput(256, 32, getState());
			guiTextInput.setPreText("");
			guiTextInput.setPos(12, background.getHeight() - 128, 0);
			if (getBackground() != null) {
				getBackground().attach(guiTextInput);
			}

		}
		this.guiTextInput.setTextInput(textInput);

	}

	public String getText() {
		return guiTextInput.getText();
	}

	public void setText(String text) {
		guiTextInput.setText(text);
	}
}
