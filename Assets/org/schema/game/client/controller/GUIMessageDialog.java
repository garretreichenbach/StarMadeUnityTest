package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIMessagePanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class GUIMessageDialog extends PlayerInput {

	GUIMessagePanel panel;

	public GUIMessageDialog(GameClientState state, String string, String title, boolean withCancel) {
		super(state);
		this.panel = new GUIMessagePanel(state, this, string, title, withCancel);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse() && !isDelayedFromMainMenuDeactivation()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(64);
				onOK();
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(63);
				onCancel();
				deactivate();
			} else {
				assert (false) : "not known command: " + callingGuiElement.getUserPointer();
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	}

	@Override
	public GUIElement getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
	}

	public void onCancel() {
	}

	public void onOK() {
	}
}
