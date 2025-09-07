package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.messagelog.GUIClientLogPanel;
import org.schema.game.client.view.gui.messagelog.messagelognew.MessageLogPanelNew;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerMessageLogPlayerInput extends PlayerInput implements GUIActiveInterface {

	private static GUIElement panel;

	public PlayerMessageLogPlayerInput(GameClientState state) {
		super(state);
		if (panel == null) {
			if (GUIElement.isNewHud()) {
				panel = new MessageLogPanelNew(state, this);
				((MessageLogPanelNew) panel).setCloseCallback(new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
							AudioController.fireAudioEventID(249);
							deactivate();
							getState().getGlobalGameControlManager().getIngameControlManager().getMessageLogManager().setActive(false);
						}
					}

					@Override
					public boolean isOccluded() {
						return !isActive();
					}
				});
			} else {
				panel = new GUIClientLogPanel(state);
				panel.setCallback(this);
			}
		}
		if (GUIElement.isNewHud()) {
			((MessageLogPanelNew) panel).reset();
			((MessageLogPanelNew) panel).activeInterface = this;
		}
	}

	public String getCurrentChatPrefix() {
		if (GUIElement.isNewHud()) {
			return "";
		} else {
			return ((GUIClientLogPanel) panel).getCurrentChatPrefix();
		}
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(250);
		}
		// System.err.println("CALLBACK: "+callingGuiElement.getUserPointer());
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(253);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().activateMesssageLog();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(252);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().activateMesssageLog();
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(251);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().activateMesssageLog();
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
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == panel;
	}

	@Override
	public GUIElement getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		getState().getGlobalGameControlManager().getIngameControlManager().getMessageLogManager().setDelayedActive(false);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.PlayerInput#allowChat()
	 */
	@Override
	public boolean allowChat() {
		return true;
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}
}
