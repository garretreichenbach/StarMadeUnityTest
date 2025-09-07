package org.schema.game.client.view.gui.faction;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.faction.FactionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class FactionOutgoingInvitesPlayerInput extends PlayerInput {

	private FactionInvitePanel panel;

	private AbstractControlManager caller;

	public FactionOutgoingInvitesPlayerInput(GameClientState state, AbstractControlManager caller) {
		super(state);
		panel = new FactionInvitePanel(getState(), this, "Outgoing Invites", "");
		this.caller = caller;
		caller.suspend(true);
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	}

	@Override
	public GUIElement getInputPanel() {
		return panel;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(454);
				cancel();
			}
			if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(455);
				System.err.println("CANCEL");
				cancel();
			}
		}
	}

	@Override
	public void onDeactivate() {
		caller.suspend(false);
	}

	private class FactionInvitePanel extends GUIInputPanel implements GUICallback {

		public FactionInvitePanel(InputState state, GUICallback guiCallback, Object info, Object description) {
			super("FACTION_INVITE_PANEL", state, guiCallback, info, description);
			setOkButton(false);
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			if (event.pressedLeftMouse()) {
				((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().openInvitePlayerDialog();
				deactivate();
			}
		}

		/* (non-Javadoc)
		 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
		 */
		@Override
		public void onInit() {
			super.onInit();
			FactionOutgoingInvitationsPanel factionOutgoingInvitationsPanel = new FactionOutgoingInvitationsPanel(410, 140, getState());
			factionOutgoingInvitationsPanel.onInit();
			getContent().attach(factionOutgoingInvitationsPanel);
			final GUITextOverlay cannotInvite = new GUITextOverlay(getState());
			cannotInvite.setTextSimple("No Permission to invite");
			GUITextButton inviteButton = new GUITextButton(getState(), 120, 30, "Invite Player", this) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
				 */
				@Override
				public void draw() {
					int factionId = ((GameClientState) getState()).getPlayer().getFactionId();
					Faction faction = ((GameClientState) getState()).getFactionManager().getFaction(factionId);
					if (faction != null) {
						FactionPermission factionPermission = faction.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
						if (factionPermission != null && factionPermission.hasInvitePermission(faction)) {
							super.draw();
						} else {
							// System.err.println("PERMISSION FAILED: "+factionPermission);
							GlUtil.glPushMatrix();
							transform();
							cannotInvite.draw();
							GlUtil.glPopMatrix();
						}
					} else {
						// System.err.println("FACTION FAILED: "+faction);
						GlUtil.glPushMatrix();
						transform();
						cannotInvite.draw();
						GlUtil.glPopMatrix();
					}
				}
			};
			inviteButton.setUserPointer(FactionControlManager.INVITE_PLAYER_TO_FACTION);
			inviteButton.setPos(235, 213, 0);
			getBackground().attach(inviteButton);
		}
	}
}
