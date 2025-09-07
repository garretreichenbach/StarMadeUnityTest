package org.schema.game.client.view.gui.faction;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class FactionIncomingInvitesPlayerInput extends PlayerInput {

	private FactionInvitePanel panel;

	private AbstractControlManager caller;

	public FactionIncomingInvitesPlayerInput(GameClientState state, AbstractControlManager caller) {
		super(state);
		this.caller = caller;
		caller.suspend(true);
		panel = new FactionInvitePanel(getState(), this, "Incoming Invites", "");
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(446);
				cancel();
			}
			if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(447);
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
		caller.suspend(false);
	}

	private class FactionInvitePanel extends GUIInputPanel {

		public FactionInvitePanel(InputState state, GUICallback guiCallback, Object info, Object description) {
			super("FACTION_INVITE_PANEL", state, guiCallback, info, description);
			setOkButton(false);
		}

		/* (non-Javadoc)
		 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
		 */
		@Override
		public void onInit() {
			super.onInit();
			FactionIncomingInvitationsPanel factionIncomingInvitationsPanel = new FactionIncomingInvitationsPanel(420, 140, getState());
			factionIncomingInvitationsPanel.onInit();
			getContent().attach(factionIncomingInvitationsPanel);
		}
	}
}
