package org.schema.game.client.view.gui.faction;

import java.util.regex.Pattern;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionNotFoundException;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.sound.controller.AudioController;

public class FactionRoleSettingPanel extends GUIElement implements GUICallback {

	private GUIColoredRectangle content;

	private Faction faction;

	private FactionRoles roles;

	private boolean inputActive;

	private boolean settingApplyKickPermission;

	private boolean settingApplyInvitePermission;

	private boolean settingApplyPermissionEditPermission;

	private boolean settingApplyEditPermission;

	private boolean settingApplyFriendlyFireKickPermission;

	private GUIAnchor permissionTable;

	private GUITextButton roleNameButton;

	private GUITextOverlay permissionEditHeadlineText;

	private GUITextOverlay editPermissionText;

	private GUITextOverlay kickPermissionText;

	private GUITextOverlay kickFriendlyFire;

	private GUITextOverlay invitePermissionText;

	private GUITextOverlay permissionEditPermissionText;

	private GUICheckBox firendlyFireKickCBox;

	private GUICheckBox editPermissionCBox;

	private GUICheckBox kickPermissionCBox;

	private GUICheckBox invitePermissionCBox;

	private GUICheckBox permissionEditPermissionCBox;

	private String roleName;

	private int index;

	public FactionRoleSettingPanel(GameClientState state, Faction faction, int index) {
		super(state);
		this.faction = faction;
		this.roles = faction.getRoles();
		this.index = index;
		settingApplyEditPermission = roles.hasRelationshipPermission(index);
		settingApplyKickPermission = roles.hasKickPermission(index);
		settingApplyInvitePermission = roles.hasInvitePermission(index);
		settingApplyPermissionEditPermission = roles.hasPermissionEditPermission(index);
		settingApplyFriendlyFireKickPermission = roles.hasKickOnFriendlyFire(index);
		roleName = roles.getRoles()[index].name;
		content = new GUIColoredRectangle(getState(), 410, 90, index % 2 == 0 ? new Vector4f(0.1f, 0.1f, 0.1f, 1) : new Vector4f(0.2f, 0.2f, 0.2f, 1));
	}

	// if (ownPermission.hasKickPermission()) {
	// kickButton = new GUITextButton(getState(), 50, 20,
	// new Vector4f(0.5f, 0.0f, 0.0f, 1), new Vector4f(1.0f,
	// 1.0f, 1.0f, 1),
	// FontSize.SMALL_14, "Kick",
	// new GUICallback() {
	// @Override
	// public boolean isOccluded() {
	// return false;
	// }
	// 
	// @Override
	// public void callback(GUIElement callingGuiElement,
	// MouseEvent event)
	// {
	// if (event.pressedLeftMouse()) {
	// if (!permission.playerUID
	// .equals(((GameClientState) getState())
	// .getPlayer().getName())) {
	// faction.kickMemberClientRequest(
	// ownPermission.playerUID,
	// permission.playerUID,
	// ((GameClientState) getState())
	// .getGameState());
	// deactivate();
	// } else {
	// ((GameClientState) getState())
	// .getController()
	// .popupAlertTextMessage(
	// "You cannot kick yourself",
	// 0);
	// }
	// }
	// }
	// });
	// kickButton.setPos(340, 0, 0);
	// content.attach(kickButton);
	// }
	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		System.err.println("TODO CALLBACK");
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		drawAttached();
	}

	@Override
	public void onInit() {
		FactionPermission ownPermission;
		try {
			ownPermission = getOwnPermission();
			if (ownPermission.hasPermissionEditPermission(faction)) {
				permissionTable = new GUIAnchor(getState(), 400, 100);
				kickPermissionText = new GUITextOverlay(getState());
				kickFriendlyFire = new GUITextOverlay(getState());
				invitePermissionText = new GUITextOverlay(getState());
				permissionEditPermissionText = new GUITextOverlay(getState());
				editPermissionText = new GUITextOverlay(getState());
				permissionEditHeadlineText = new GUITextOverlay(getState());
				editPermissionText.setTextSimple(Lng.str("Edit"));
				kickPermissionText.setTextSimple(Lng.str("Kick"));
				kickFriendlyFire.setTextSimple(Lng.str("Kick on friendly fire"));
				invitePermissionText.setTextSimple(Lng.str("Invite"));
				permissionEditPermissionText.setTextSimple(Lng.str("Permission Edit"));
				permissionEditHeadlineText.setTextSimple(Lng.str("Permissions"));
				editPermissionCBox = new GUICheckBox(getState()) {

					@Override
					protected void activate() throws StateParameterNotFoundException {
						setSettingApplyEditPermission(true);
					}

					@Override
					protected void deactivate() throws StateParameterNotFoundException {
						if (index == FactionRoles.INDEX_ADMIN_ROLE) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot modify rights\nof an admin role!"), 0);
						} else {
							setSettingApplyEditPermission(false);
						}
					}

					@Override
					protected boolean isActivated() {
						return isSettingApplyEditPermission();
					}
				};
				firendlyFireKickCBox = new GUICheckBox(getState()) {

					@Override
					protected void activate() throws StateParameterNotFoundException {
						if (index == FactionRoles.INDEX_ADMIN_ROLE) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot modify rights\nof an admin role!"), 0);
						} else {
							setSettingApplyFriendlyFireKickPermission(true);
						}
					}

					@Override
					protected void deactivate() throws StateParameterNotFoundException {
						if (index == FactionRoles.INDEX_ADMIN_ROLE) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot modify rights\nof an admin role!"), 0);
						} else {
							setSettingApplyFriendlyFireKickPermission(false);
						}
					}

					@Override
					protected boolean isActivated() {
						return isSettingApplyFriendlyFireKickPermission();
					}
				};
				kickPermissionCBox = new GUICheckBox(getState()) {

					@Override
					protected void activate() throws StateParameterNotFoundException {
						setSettingApplyKickPermission(true);
					}

					@Override
					protected void deactivate() throws StateParameterNotFoundException {
						if (index == FactionRoles.INDEX_ADMIN_ROLE) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot modify rights\nof an admin role!"), 0);
						} else {
							setSettingApplyKickPermission(false);
						}
					}

					@Override
					protected boolean isActivated() {
						return isSettingApplyKickPermission();
					}
				};
				invitePermissionCBox = new GUICheckBox(getState()) {

					@Override
					protected void activate() throws StateParameterNotFoundException {
						setSettingApplyInvitePermission(true);
					}

					@Override
					protected void deactivate() throws StateParameterNotFoundException {
						if (index == FactionRoles.INDEX_ADMIN_ROLE) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot modify rights\nof an admin role!"), 0);
						} else {
							setSettingApplyInvitePermission(false);
						}
					}

					@Override
					protected boolean isActivated() {
						return isSettingApplyInvitePermission();
					}
				};
				permissionEditPermissionCBox = new GUICheckBox(getState()) {

					@Override
					protected void activate() throws StateParameterNotFoundException {
						setSettingApplyPermissionEditPermission(true);
					}

					@Override
					protected void deactivate() throws StateParameterNotFoundException {
						if (index == FactionRoles.INDEX_ADMIN_ROLE) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot modify rights\nof an admin role!"), 0);
						} else {
							setSettingApplyPermissionEditPermission(false);
						}
					}

					@Override
					protected boolean isActivated() {
						return isSettingApplyPermissionEditPermission();
					}
				};
				permissionTable.attach(permissionEditHeadlineText);
				int textHeights = 20;
				int boxHeights = 35;
				int xDist = 50;
				editPermissionText.setPos(0 * xDist, textHeights, 0);
				kickPermissionText.setPos(1 * xDist, textHeights, 0);
				invitePermissionText.setPos(2 * xDist, textHeights, 0);
				permissionEditPermissionText.setPos(3 * xDist, textHeights, 0);
				kickFriendlyFire.setPos(5 * xDist, textHeights, 0);
				permissionTable.attach(editPermissionText);
				permissionTable.attach(kickPermissionText);
				permissionTable.attach(kickFriendlyFire);
				permissionTable.attach(invitePermissionText);
				permissionTable.attach(permissionEditPermissionText);
				editPermissionCBox.setPos(0 * xDist, boxHeights, 0);
				kickPermissionCBox.setPos(1 * xDist, boxHeights, 0);
				invitePermissionCBox.setPos(2 * xDist, boxHeights, 0);
				permissionEditPermissionCBox.setPos(3 * xDist, boxHeights, 0);
				firendlyFireKickCBox.setPos(5 * xDist, boxHeights, 0);
				permissionTable.attach(editPermissionCBox);
				permissionTable.attach(kickPermissionCBox);
				permissionTable.attach(invitePermissionCBox);
				permissionTable.attach(permissionEditPermissionCBox);
				permissionTable.attach(firendlyFireKickCBox);
				permissionTable.getPos().y = 20;
				content.attach(permissionTable);
				roleNameButton = new GUITextButton(getState(), 200, 20, new Object() {

					/*
							 * (non-Javadoc)
							 *
							 * @see java.lang.Object#toString()
							 */
					@Override
					public String toString() {
						return roleName;
					}
				}, new GUICallback() {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							setInputActive(true);
							PlayerGameTextInput in = new PlayerGameTextInput("FactionRoleSettingPanel_EDIT_ROLE_NAME", (GameClientState) getState(), 16, Lng.str("Edit Role Name"), Lng.str("Enter a new name for this Role"), roleName) {

								@Override
								public String[] getCommandPrefixes() {
									return null;
								}

								@Override
								public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
									return null;
								}

								@Override
								public void onFailedTextCheck(String msg) {
								}

								@Override
								public void onDeactivate() {
									setInputActive(false);
								}

								@Override
								public boolean onInput(String entry) {
									roleName = entry;
									return true;
								}
							};
							in.setInputChecker((entry, callback) -> {
								if (entry.length() >= 3 && entry.length() <= 16) {
									if (Pattern.matches("[a-zA-Z0-9 _-]+", entry)) {
										return true;
									} else {
										System.err.println("MATCH FOUND ^ALPHANUMERIC");
									}
								}
								callback.onFailedTextCheck(Lng.str("Please only alphanumeric (and space, _, -) values \nand between 3 and 16 long!"));
								return false;
							});
							in.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(456);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				});
				content.attach(roleNameButton);
			}
			if (!ownPermission.hasPermissionEditPermission(faction)) {
				GUITextOverlay none = new GUITextOverlay(getState());
				none.setTextSimple(Lng.str("You don't have permission to edit this member!"));
				content.attach(none);
			}
		} catch (FactionNotFoundException e) {
			GUITextOverlay none = new GUITextOverlay(getState());
			none.setTextSimple(Lng.str("You don't have permission to edit this!"));
			content.attach(none);
			e.printStackTrace();
		}
		attach(content);
	}

	@Override
	public float getHeight() {
		return content.getHeight();
	}

	@Override
	public float getWidth() {
		return content.getWidth();
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

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public boolean isOccluded() {
		return false;
	}

	/**
	 * @return the inputActive
	 */
	public boolean isInputActive() {
		return inputActive;
	}

	/**
	 * @param inputActive the inputActive to set
	 */
	public void setInputActive(boolean inputActive) {
		this.inputActive = inputActive;
	}

	/**
	 * @return the settingApplyEditPermission
	 */
	public boolean isSettingApplyEditPermission() {
		return settingApplyEditPermission;
	}

	/**
	 * @param settingApplyEditPermission the settingApplyEditPermission to set
	 */
	public void setSettingApplyEditPermission(boolean settingApplyEditPermission) {
		this.settingApplyEditPermission = settingApplyEditPermission;
	}

	/**
	 * @return the settingApplyInvitePermission
	 */
	public boolean isSettingApplyInvitePermission() {
		return settingApplyInvitePermission;
	}

	/**
	 * @param settingApplyInvitePermission the settingApplyInvitePermission to set
	 */
	public void setSettingApplyInvitePermission(boolean settingApplyInvitePermission) {
		this.settingApplyInvitePermission = settingApplyInvitePermission;
	}

	/**
	 * @return the settingApplyKickPermission
	 */
	public boolean isSettingApplyKickPermission() {
		return settingApplyKickPermission;
	}

	/**
	 * @param settingApplyKickPermission the settingApplyKickPermission to set
	 */
	public void setSettingApplyKickPermission(boolean settingApplyKickPermission) {
		this.settingApplyKickPermission = settingApplyKickPermission;
	}

	/**
	 * @return the settingApplyPermissionEditPermission
	 */
	public boolean isSettingApplyPermissionEditPermission() {
		return settingApplyPermissionEditPermission;
	}

	/**
	 * @param settingApplyPermissionEditPermission the settingApplyPermissionEditPermission to set
	 */
	public void setSettingApplyPermissionEditPermission(boolean settingApplyPermissionEditPermission) {
		this.settingApplyPermissionEditPermission = settingApplyPermissionEditPermission;
	}

	/**
	 * @return the settingApplyFriendlyFireKickPermission
	 */
	public boolean isSettingApplyFriendlyFireKickPermission() {
		return settingApplyFriendlyFireKickPermission;
	}

	/**
	 * @param settingApplyFriendlyFireKickPermission the settingApplyFriendlyFireKickPermission to set
	 */
	public void setSettingApplyFriendlyFireKickPermission(boolean settingApplyFriendlyFireKickPermission) {
		this.settingApplyFriendlyFireKickPermission = settingApplyFriendlyFireKickPermission;
	}
}
