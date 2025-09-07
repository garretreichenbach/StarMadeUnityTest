package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.crew.CrewPanelNew;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerCrewMenu extends PlayerInput implements GUIActiveInterface {

	private CrewPanelNew menuPanel;

	public PlayerCrewMenu(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().indexOf(this) == getState().getController().getPlayerInputs().size() - 1;
	}

	@Override
	public CrewPanelNew getInputPanel() {
		if (menuPanel == null) {
			// do it here so we dont call ress loader too early
			this.menuPanel = new CrewPanelNew(getState(), this);
			this.menuPanel.onInit();
			this.menuPanel.crewPanel.setCloseCallback(new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
						AudioController.fireAudioEventID(226);
						deactivate();
					}
				}
			});
			menuPanel.crewPanel.activeInterface = this;
		}
		return menuPanel;
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public boolean isOccluded() {
		return !isActive();
	}
}
