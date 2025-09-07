package org.schema.game.client.controller.manager.ingame.map;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationFilter;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.navigation.NavigationFilterSettingPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class MapFilterEditDialog extends PlayerInput {

	private final NavigationFilterSettingPanel panel;

	public MapFilterEditDialog(GameClientState state, NavigationFilter p, boolean showAdminOptions) {
		super(state);
		this.panel = new NavigationFilterSettingPanel(getState(), p, 0, this, showAdminOptions);
		this.panel.onInit();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(149);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(148);
				cancel();
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
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().hinderInteraction(500);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getCatalogControlManager().suspend(false);
	}
}
