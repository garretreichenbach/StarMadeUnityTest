package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.mail.GUICreateMailPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerMailInputNew extends PlayerInput {

	private GUICreateMailPanel mailInputPanel;

	public PlayerMailInputNew(GameClientState state, String predefinedTo, String predefinedTopic) {
		super(state);
		this.mailInputPanel = new GUICreateMailPanel(state, this, predefinedTo, predefinedTopic);
		mailInputPanel.setCallback(this);
	}

	public PlayerMailInputNew(GameClientState state) {
		this(state, "", "");
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (!isOccluded()) {
			if (event.pressedLeftMouse()) {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(239);
					if (sendMail()) {
						deactivate();
					}
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(240);
					cancel();
				}
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	@Override
	public GUIElement getInputPanel() {
		return mailInputPanel;
	}

	@Override
	public void onDeactivate() {
		mailInputPanel.cleanUp();
	}

	public String getTo() {
		return mailInputPanel.getTo().trim();
	}

	public String getSubject() {
		return mailInputPanel.getSubject().trim();
	}

	public String getMessage() {
		return mailInputPanel.getMessage().trim();
	}

	private boolean sendMail() {
		return getState().getController().getClientChannel().getPlayerMessageController().clientSend(getState().getPlayerName(), getTo(), getSubject(), getMessage());
	}
}
