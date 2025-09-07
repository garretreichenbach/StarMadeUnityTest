package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.mail.GUIMailPanelNew;
import org.schema.game.common.data.player.playermessage.PlayerMessage;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerMessagesPlayerInput extends PlayerInput {

	private GUIElement panel;

	public PlayerMessagesPlayerInput(GameClientState state) {
		super(state);
		panel = new GUIMailPanelNew(state, 700, 500, this);
		panel.setCallback(this);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(254);
		}
		// System.err.println("CALLBACK: "+callingGuiElement.getUserPointer());
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(259);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().activatePlayerMesssages();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().activatePlayerMesssages();
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(258);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().activatePlayerMesssages();
			} else if (callingGuiElement.getUserPointer().equals("new")) {
				PlayerMessageInput p = new PlayerMessageInput(getState(), null);
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(257);
			} else if (callingGuiElement.getUserPointer().equals("delAll")) {
				PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM", getState(), "Delete All", "Do you want to delete all mails?\nThis cannot be undone.") {

					@Override
					public void onDeactivate() {
					}

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void pressedOK() {
						getState().getController().getClientChannel().getPlayerMessageController().deleteAllMessagesClient();
						deactivate();
					}
				};
				c.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(256);
			} else if (callingGuiElement.getUserPointer() instanceof PlayerMessage) {
				PlayerMessageInput p = new PlayerMessageInput(getState(), (PlayerMessage) callingGuiElement.getUserPointer());
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(255);
			} else {
				assert (false) : "not known command: '" + callingGuiElement.getUserPointer() + "'";
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	// nothing to do. just consume the key
	}

	@Override
	public GUIElement getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerMailManager().setDelayedActive(false);
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}
}
