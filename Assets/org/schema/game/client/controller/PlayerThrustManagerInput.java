package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.thrustmanagement.ThrustManagementPanelNew;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerThrustManagerInput extends PlayerInput implements GUIActiveInterface {

	private ThrustManagementPanelNew panel;

	public PlayerThrustManagerInput(GameClientState state, Ship ship) {
		super(state);
		panel = new ThrustManagementPanelNew(state, this, ship);
		panel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					deactivate();
					getState().getGlobalGameControlManager().getIngameControlManager().getMessageLogManager().setActive(false);
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		});
		panel.reset();
		panel.activeInterface = this;
	}

	@Override
	public void update(Timer timer) {
		if (panel != null) {
			panel.update(timer);
		}
	}

	public String getCurrentChatPrefix() {
		return "";
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(277);
		}
		// System.err.println("CALLBACK: "+callingGuiElement.getUserPointer());
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(280);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().activateThrustManager(null);
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(279);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().activateThrustManager(null);
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(278);
				deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().activateThrustManager(null);
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
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getThrustManager().setDelayedActive(false);
		panel.cleanUp();
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}
}
