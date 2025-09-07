package org.schema.game.client.view.gui.faction;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionNotFoundException;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class FactionMemberEditDialog extends PlayerInput implements GUICallback {

	private Faction faction;

	private FactionMemberEditPanel panel;

	private FactionPermission factionPermission;

	public FactionMemberEditDialog(GameClientState state, Faction faction, FactionPermission permission) {
		super(state);
		this.faction = faction;
		panel = new FactionMemberEditPanel(state, this, Lng.str("Settings for %s", permission.playerUID), "", faction, permission);
		this.factionPermission = permission;
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
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(448);
				cancel();
			}
			if (callingGuiElement instanceof GUIRoleButton) {
				final int role = (Integer) callingGuiElement.getUserPointer();
				final FactionPermission ownPermission = faction.getMembersUID().get(getState().getPlayer().getName());
				if (ownPermission == null) {
					getState().getController().popupAlertTextMessage(Lng.str("You are not in this faction!"), 0);
				} else {
					try {
						if (ownPermission.playerUID.equals(factionPermission.playerUID) && ownPermission.role == 4) {
							deactivate();
							PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("You are admin of this faction.\nDo you really want to change your role?")) {

								@Override
								public void onDeactivate() {
								}

								@Override
								public boolean isOccluded() {
									return false;
								}

								@Override
								public void pressedOK() {
									if (!ownPermission.hasPermissionEditPermission(faction)) {
										getState().getController().popupAlertTextMessage(Lng.str("You cannot change role:\npermission denied!"), 0);
									} else if (ownPermission.role < factionPermission.role) {
										getState().getController().popupAlertTextMessage(Lng.str("You cannot change\nroles of higher ranked\nplayers!"), 0);
									} else {
										faction.addOrModifyMemberClientRequest(getState().getPlayer().getName(), factionPermission.playerUID, (byte) role, getState().getGameState());
									}
									deactivate();
								}
							};
							confirm.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(449);
						} else {
							if (!ownPermission.hasPermissionEditPermission(faction)) {
								getState().getController().popupAlertTextMessage(Lng.str("You cannot change role:\npermission denied!"), 0);
							} else if (ownPermission.role < factionPermission.role) {
								getState().getController().popupAlertTextMessage(Lng.str("You cannot change\nroles of higher ranked\nplayers!"), 0);
							} else {
								faction.addOrModifyMemberClientRequest(getState().getPlayer().getName(), factionPermission.playerUID, (byte) role, getState().getGameState());
							}
							deactivate();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class FactionMemberEditPanel extends GUIInputPanel {

		private GUITextButton kickButton;

		private GUIRoleButton[] roleButtons = new GUIRoleButton[FactionRoles.ROLE_COUNT];

		private Faction faction;

		public FactionMemberEditPanel(InputState state, GUICallback guiCallback, Object info, Object description, Faction faction, FactionPermission factionPermission) {
			super("FACTION_MEMBER_EDIT_PANEL", state, guiCallback, info, description);
			this.faction = faction;
			setOkButton(false);
		}

		@Override
		public float getHeight() {
			return 0;
		}

		@Override
		public float getWidth() {
			return 0;
		}

		@Override
		public void cleanUp() {
		}

		@Override
		public void draw() {
			int factionId = ((GameClientState) getState()).getPlayer().getFactionController().getFactionId();
			Faction f = ((GameClientState) getState()).getGameState().getFactionManager().getFaction(factionId);
			if (f == faction) {
				super.draw();
			} else {
				deactivate();
			}
		}

		@Override
		public void onInit() {
			super.onInit();
			final FactionPermission ownPermission;
			try {
				ownPermission = getOwnPermission();
				if (ownPermission.hasKickPermission(faction)) {
					kickButton = new GUITextButton(getState(), 50, 20, new Vector4f(0.5f, 0.0f, 0.0f, 1), new Vector4f(1.0f, 1.0f, 1.0f, 1), FontSize.SMALL_14, Lng.str("Kick"), new GUICallback() {

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								if (!factionPermission.playerUID.equals(((GameClientState) getState()).getPlayer().getName())) {
									if (!ownPermission.hasKickPermission(faction)) {
										((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You don't have kick permission!"), 0);
									} else if (ownPermission.role < factionPermission.role) {
										((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You cannot kick players with this rank!"), 0);
									} else if (factionPermission.role >= faction.getRoles().getRoles().length - 1) {
										((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You cannot kick founders!"), 0);
									} else {
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
										AudioController.fireAudioEventID(450);
										faction.kickMemberClientRequest(ownPermission.playerUID, factionPermission.playerUID, ((GameClientState) getState()).getGameState());
									}
									deactivate();
								} else {
									((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You cannot kick yourself!"), 0);
								}
							}
						}

						@Override
						public boolean isOccluded() {
							return false;
						}
					});
					kickButton.setPos(340, 0, 0);
					getContent().attach(kickButton);
				}
				if (ownPermission.hasPermissionEditPermission(faction)) {
					GUIAnchor back = new GUIAnchor(getState());
					GUITextOverlay o = new GUITextOverlay(getState());
					o.setTextSimple(Lng.str("Choose a role for this player."));
					for (int i = 0; i < FactionRoles.ROLE_COUNT; i++) {
						roleButtons[i] = new GUIRoleButton(getState(), i);
						roleButtons[i].onInit();
						roleButtons[i].getPos().x = (i * 130) % (3 * 130);
						roleButtons[i].getPos().y = (i / 3) * 30 + 20;
						back.attach(roleButtons[i]);
					}
					back.attach(o);
					GUIScrollablePanel p = new GUIScrollablePanel(406, 80, getState());
					p.setContent(back);
					p.getPos().y = 25;
					getContent().attach(p);
				// content.attach(permissionTable);
				}
				if (!ownPermission.hasKickPermission(faction) && !ownPermission.hasPermissionEditPermission(faction)) {
					GUITextOverlay none = new GUITextOverlay(getState());
					none.setTextSimple(Lng.str("You don't have permission to edit this member!"));
					getContent().attach(none);
				}
			} catch (FactionNotFoundException e) {
				e.printStackTrace();
				deactivate();
			}
		}

		private FactionPermission getOwnPermission() throws FactionNotFoundException {
			int factionId = ((GameClientState) getState()).getPlayer().getFactionController().getFactionId();
			Faction f = ((GameClientState) getState()).getGameState().getFactionManager().getFaction(factionId);
			if (f == null) {
				throw new FactionNotFoundException(factionId);
			}
			FactionPermission fp = f.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
			if (fp == null) {
				throw new FactionNotFoundException(factionId);
			}
			return fp;
		}
	}

	private class GUIRoleButton extends GUITextButton {

		public GUIRoleButton(InputState state, int role) {
			super(state, 120, 20, faction.getRoles().getRoles()[role].name, FactionMemberEditDialog.this);
			setUserPointer(new Integer(role));
		}
	}
}
