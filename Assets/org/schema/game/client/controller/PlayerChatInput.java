package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.chat.ChatInputPanel;
import org.schema.game.client.view.gui.chat.ChatPanel;
import org.schema.game.common.data.chat.ChatCallback;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerChatInput extends PlayerInput {

	private ChatInputPanel panel;

	private GUIActiveInterface actInterface;

	private ChatPanel chatPanel;

	private ChatCallback chatCallback;

	public PlayerChatInput(String invId, GameClientState state, Object title, GUIActiveInterface actInterface, ChatPanel chatPanel, ChatCallback chatCallback) {
		super(state);
		this.actInterface = actInterface;
		this.chatPanel = chatPanel;
		this.chatCallback = chatCallback;
		if (panel == null) {
			int initWidth = 520;
			int initHeight = 510;
			initWidth = 370;
			initHeight = 166;
			panel = new ChatInputPanel(invId, state, title, initWidth, initHeight, chatCallback, this, actInterface, chatPanel);
		}
		panel.setCallback(this);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(218);
		}
		// System.err.println("CALLBACK: "+callingGuiElement.getUserPointer());
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(221);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(220);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(219);
				deactivate();
			} else {
				assert (false) : "not known command: '" + callingGuiElement.getUserPointer() + "'";
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	// nothing to do. just consume the key
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.PlayerInput#deactivate()
	 */
	@Override
	public void deactivate() {
		chatPanel.deactivate(this);
	}

	@Override
	public ChatInputPanel getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		chatCallback.onWindowDeactivate();
	}

	@Override
	public boolean isOccluded() {
		return !actInterface.isActive();
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}
}
