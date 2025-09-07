package org.schema.game.client.controller.manager.ingame.catalog;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.catalog.CatalogPermissionSettingPanel;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public class CatalogPermissionEditDialog extends PlayerInput {

	private final CatalogPermissionSettingPanel panel;

	private final CatalogPermission permission;

	public CatalogPermissionEditDialog(GameClientState state, CatalogPermission p) {
		super(state);
		this.panel = new CatalogPermissionSettingPanel(getState(), p, 24, this);
		this.panel.onInit();
		this.permission = p;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(107);
				if (getState().getPlayer().getNetworkObject().isAdminClient.get()) {
					panel.getEditingPermission().changeFlagForced = true;
					getState().getCatalogManager().clientRequestCatalogEdit(panel.getEditingPermission());
				} else {
					panel.getEditingPermission().ownerUID = getState().getPlayer().getName();
					getState().getCatalogManager().clientRequestCatalogEdit(panel.getEditingPermission());
				}
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(106);
				cancel();
			}
		}
	}

	/**
	 * @return the permission
	 */
	public CatalogPermission getPermission() {
		return permission;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if (isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
			if (getState().getPlayer().getNetworkObject().isAdminClient.get()) {
				panel.getEditingPermission().changeFlagForced = true;
				getState().getCatalogManager().clientRequestCatalogEdit(panel.getEditingPermission());
			} else {
				panel.getEditingPermission().ownerUID = getState().getPlayer().getName();
				getState().getCatalogManager().clientRequestCatalogEdit(panel.getEditingPermission());
			}
			deactivate();
			return;
		}
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
