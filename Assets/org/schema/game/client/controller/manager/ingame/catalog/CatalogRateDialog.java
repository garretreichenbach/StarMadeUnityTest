package org.schema.game.client.controller.manager.ingame.catalog;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.catalog.CatalogRateDialogPanel;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class CatalogRateDialog extends PlayerInput {

	private final CatalogRateDialogPanel panel;

	private final CatalogPermission permission;

	private boolean showAdminOptions;

	public CatalogRateDialog(GameClientState state, CatalogPermission p, boolean showAdminOptions) {
		super(state);
		this.panel = new CatalogRateDialogPanel(state, this, p);
		this.showAdminOptions = showAdminOptions;
		this.panel.onInit();
		this.permission = p;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer() instanceof Integer) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(109);
				int rating = (Integer) callingGuiElement.getUserPointer();
				getState().getCatalogManager().clientRate(getState().getPlayerName(), permission.getUid(), rating + 1);
				// if(showAdminOptions && ((GameClientState)getState()).getPlayer().getNetworkObject().isAdminClient.get()){
				// panel.getEditingPermission().changeFlagForced = true;
				// getState().getCatalogManager().clientRequestCatalogEdit(panel.getEditingPermission());
				// }else{
				// panel.getEditingPermission().ownerUID = ((GameClientState)getState()).getPlayer().getName();
				// getState().getCatalogManager().clientRequestCatalogEdit(panel.getEditingPermission());
				// }
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				cancel();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(108);
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

	/**
	 * @return the showAdminOptions
	 */
	public boolean isShowAdminOptions() {
		return showAdminOptions;
	}
}
