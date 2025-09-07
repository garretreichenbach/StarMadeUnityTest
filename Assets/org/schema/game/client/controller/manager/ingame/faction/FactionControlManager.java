package org.schema.game.client.controller.manager.ingame.faction;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerTextAreaInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionInvite;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

public class FactionControlManager extends AbstractControlManager implements GUICallback {

	public static final String CREATE_FACTION = "CREATE_FACTION";

	public static final String LEAVE_FACTION = "LEAVE_FACTION";

	public static final String PERSONAL = "PERSONAL";

	public static final String HUB = "HUB";

	public static final String LOCAL = "LOCAL";

	public static final String POST_NEWS = "POST_NEWS";

	public static final String EDIT_DESCRIPTION = "EDIT_DESC";

	public static final String VIEW_INCOMING_INVITE_FACTION = "INCOMING_INVITES";

	public static final String VIEW_OUTGOING_INVITE_FACTION = "OUTGOING_INVITES";

	public static final String INVITE_PLAYER_TO_FACTION = "INVITE";

	public static final String FACTION_ROLES = "ROLES";

	public static final String PERSONAL_ENEMIES = "PERSONAL_ENEMIES";

	private PersonalFactionControlManager personalFactionControlManager;

	private FactionHubControlManager factionHubControlManager;

	private LocalFactionControlManager localFactionControlManager;

	public FactionControlManager(GameClientState state) {
		super(state);
		initialize();
	}

	public boolean isOccluded() {
		return false;
	}

	@Override
	public void callback(GUIElement callingGui, MouseEvent event) {
	// if (event.pressedLeftMouse()) {
	// if (PERSONAL.equals(callingGui.getUserPointer())) {
	// activate(personalFactionControlManager);
	// setChanged();
	// notifyObservers();
	// } else if (HUB.equals(callingGui.getUserPointer())) {
	// activate(factionHubControlManager);
	// setChanged();
	// notifyObservers();
	// } else if (LOCAL.equals(callingGui.getUserPointer())) {
	// activate(localFactionControlManager);
	// setChanged();
	// notifyObservers();
	// } else if (VIEW_INCOMING_INVITE_FACTION.equals(callingGui.getUserPointer())) {
	// (new FactionIncomingInvitesPlayerInput(getState(), this)).activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	// 
	// } else if (VIEW_OUTGOING_INVITE_FACTION.equals(callingGui.getUserPointer())) {
	// (new FactionOutgoingInvitesPlayerInput(getState(), this)).activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	// 
	// } else if (EDIT_DESCRIPTION.equals(callingGui.getUserPointer())) {
	// suspend(true);
	// Faction ownFaction = getOwnFaction();
	// PlayerTextAreaInput t = new PlayerTextAreaInput("FactionControlManager_DESCPAN_FAC", getState(), 140, 5, Lng.str("Edit Faction Description"),
	// Lng.str("Enter a description for the faction"),
	// ownFaction != null ? ownFaction.getDescription() : Lng.str("ERROR: NO FACTION")) {
	// @Override
	// public String[] getCommandPrefixes() {
	// return null;
	// }
	// 
	// @Override
	// public boolean isOccluded() {
	// return false;
	// }					@Override
	// public String handleAutoComplete(String s, TextCallback callback,
	// String prefix) throws PrefixNotFoundException {
	// return null;
	// }
	// 
	// @Override
	// public void onDeactivate() {
	// suspend(false);
	// }
	// 
	// 
	// 
	// @Override
	// public void onFailedTextCheck(String msg) {
	// }
	// 
	// @Override
	// public boolean onInput(String entry) {
	// return getState().getPlayer().getFactionController().editDescriptionClient(entry);
	// }
	// };
	// t.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	// } else if (POST_NEWS.equals(callingGui.getUserPointer())) {
	// suspend(true);
	// Faction ownFaction = getOwnFaction();
	// PlayerTextAreaInput t = new PlayerTextAreaInput("FactionControlManager_POST_NEWS_OLD", getState(), 140, 5, Lng.str("Post Faction News"),
	// Lng.str("Enter text for a new News Post"),
	// "") {
	// @Override
	// public String[] getCommandPrefixes() {
	// return null;
	// }
	// 
	// @Override
	// public String handleAutoComplete(String s, TextCallback callback,
	// String prefix) throws PrefixNotFoundException {
	// return null;
	// }
	// 
	// @Override
	// public boolean isOccluded() {
	// return false;
	// }
	// 
	// @Override
	// public void onDeactivate() {
	// suspend(false);
	// }
	// 
	// @Override
	// public void onFailedTextCheck(String msg) {
	// }
	// 
	// @Override
	// public boolean onInput(String entry) {
	// return getState().getPlayer().getFactionController().postNewsClient(Lng.str("No Topic (old)"), entry);
	// }
	// };
	// t.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	// } else if (CREATE_FACTION.equals(callingGui.getUserPointer())) {
	// if (getState().getController().getPlayerInputs().isEmpty()) {
	// FactionDialog d = new FactionDialog(getState(), Lng.str("Change Faction Name"), this);
	// getState().getController().getPlayerInputs().add(d);
	// suspend(true);
	// setChanged();
	// notifyObservers();
	// }
	// } else if (LEAVE_FACTION.equals(callingGui.getUserPointer())) {
	// if (getState().getController().getPlayerInputs().isEmpty()) {
	// 
	// PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("FactionControlManager_LEAVE_FACTION", getState(), Lng.str("Confirm"), Lng.str("Do you really want to leave the faction")) {
	// 
	// @Override
	// public void onDeactivate() {
	// 
	// }						@Override
	// public boolean isOccluded() {
	// return false;
	// }
	// 
	// @Override
	// public void pressedOK() {
	// System.err.println("[CLIENT][FactionControlManager] leaving Faction");
	// getState().getPlayer().getFactionController().leaveFaction();
	// deactivate();
	// }
	// 
	// 
	// };
	// confirm.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	// 
	// }
	// } else if (PERSONAL_ENEMIES.equals(callingGui.getUserPointer())) {
	// if (getState().getController().getPlayerInputs().isEmpty()) {
	// Faction ownFaction = getOwnFaction();
	// if (ownFaction != null) {
	// openPersonalEnemyInput();
	// } else {
	// getState().getController().popupAlertTextMessage(Lng.str("ERROR: not in a faction!"), 0);
	// }
	// }
	// }
	// }
	}

	public FactionHubControlManager getFactionHubControlManager() {
		return factionHubControlManager;
	}

	public LocalFactionControlManager getLocalFactionControlManager() {
		return localFactionControlManager;
	}

	public Faction getOwnFaction() {
		int factionId = getState().getPlayer().getFactionId();
		return getState().getFactionManager().getFaction(factionId);
	}

	public PersonalFactionControlManager getPersonalFactionControlManager() {
		return personalFactionControlManager;
	}

	private void initialize() {
		personalFactionControlManager = new PersonalFactionControlManager(getState());
		factionHubControlManager = new FactionHubControlManager(getState());
		localFactionControlManager = new LocalFactionControlManager(getState());
		getControlManagers().add(personalFactionControlManager);
		getControlManagers().add(factionHubControlManager);
		getControlManagers().add(localFactionControlManager);
		personalFactionControlManager.setActive(true);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		if (active) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(117);
			notifyObservers();
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DEACTIVATE)*/
			AudioController.fireAudioEventID(116);
		}
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
		super.onSwitch(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(false);
		super.update(timer);
	}

	public void openDeclareWarDialog(final Faction from, final Faction to) {
		suspend(true);
		(new PlayerTextAreaInput("FactionControlManager_openDeclareWarDialog", getState(), 140, 5, Lng.str("Declare war"), "") {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
				suspend(false);
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				getState().getFactionManager().sendRelationshipOffer(getState().getPlayerName(), from.getIdFaction(), to.getIdFaction(), RType.ENEMY.code, entry, false);
				return true;
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(118);
	}

	// private void openFactionRolsPanel(Faction faction) {
	// suspend(true);
	// (new FactionRolesDialog(getState(), faction) {
	// 
	// /* (non-Javadoc)
	// * @see org.schema.game.client.controller.manager.ingame.faction.FactionRolesDialog#onDeactivate()
	// */
	// @Override
	// public void onDeactivate() {
	// super.onDeactivate();
	// suspend(false);
	// }
	// 
	// }).activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	// }
	public void openInvitePlayerDialog() {
		if (getState().getPlayer().getFactionController().getInvitesOutgoing().size() >= 5) {
			getState().getController().popupAlertTextMessage(Lng.str("You can only have five\ninvitations pending\nat the same time."), 0);
			return;
		}
		suspend(true);
		(new PlayerGameTextInput("FactionControlManager_openInvitePlayerDialog", getState(), 50, Lng.str("Create Invitation"), Lng.str("Enter Name of the player you want to invite")) {

			@Override
			public void onDeactivate() {
				suspend(false);
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public boolean onInput(String entry) {
				if (getState().getPlayer().getFactionId() != 0) {
					FactionInvite invite = new FactionInvite(getState().getPlayerName(), entry, getState().getPlayer().getFactionId(), System.currentTimeMillis());
					getState().getFactionManager().sendInviteClient(invite);
					getState().getController().popupInfoTextMessage(Lng.str("Invitation sent to %s.", entry), 0);
				}
				return true;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return getState().onAutoComplete(s, this, prefix);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(119);
	}

	public void openOfferAllyDialog(final Faction from, final Faction to) {
		suspend(true);
		(new PlayerTextAreaInput("FactionControlManager_openOfferAllyDialog", getState(), 140, 5, Lng.str("Alliance Offer"), "") {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
				suspend(false);
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				getState().getFactionManager().sendRelationshipOffer(getState().getPlayerName(), from.getIdFaction(), to.getIdFaction(), RType.FRIEND.code, entry, false);
				return true;
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(120);
	}

	public void openOfferPeaceDialog(final Faction from, final Faction to) {
		suspend(true);
		(new PlayerTextAreaInput("FactionControlManager_openOfferPeaceDialog", getState(), 140, 5, Lng.str("Peace Treaty Offer"), "") {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
				suspend(false);
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				getState().getFactionManager().sendRelationshipOffer(getState().getPlayerName(), from.getIdFaction(), to.getIdFaction(), RType.NEUTRAL.code, entry, false);
				return true;
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(121);
	}

	public void openOpenOffers() {
		suspend(true);
		(new FactionOfferDialog(getState())).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(122);
	}

	public void openPersonalEnemyInput() {
		if (getState().getPlayer().getFactionId() == 0) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot modify:\nYou are not in any faction!"), 0);
			return;
		}
		Faction from = getState().getFactionManager().getFaction(getState().getPlayer().getFactionId());
		if (from == null) {
			getState().getController().popupAlertTextMessage(Lng.str("Your faction is corrupted\nand does not exist!"), 0);
			return;
		}
		suspend(true);
		(new FactionPersonalEnemyDialog(getState(), from)).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(123);
	}

	public void openRelationShipInput(int toId) {
		if (getState().getPlayer().getFactionId() == 0) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot modify:\nYou are not in any faction!"), 0);
			return;
		}
		Faction from = getState().getFactionManager().getFaction(getState().getPlayer().getFactionId());
		Faction to = getState().getFactionManager().getFaction(toId);
		if (from == null) {
			getState().getController().popupAlertTextMessage(Lng.str("Your faction is corrupted\nand does not exist!"), 0);
			return;
		}
		if (to == null) {
			getState().getController().popupAlertTextMessage(Lng.str("Target faction is corrupted\nand does not exist!"), 0);
			return;
		}
		if (from == to) {
			getState().getController().popupAlertTextMessage(Lng.str("Your faction can't have\nrelations with itself."), 0);
			return;
		}
		if (to.getIdFaction() < 0) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot modify relations\nto a NPC faction!"), 0);
			return;
		}
		FactionPermission factionPermission = from.getMembersUID().get(getState().getPlayer().getName());
		if (factionPermission == null || !factionPermission.hasRelationshipPermission(from)) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot change relation:\nPermission denied!"), 0);
			return;
		}
		suspend(true);
		(new FactionRelationDialog(getState(), from, to)).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(124);
	}

	public void revokeAlly(Faction from, Faction to) {
		getState().getFactionManager().sendRelationshipOffer(getState().getPlayerName(), from.getIdFaction(), to.getIdFaction(), RType.FRIEND.code, "REVOKE", true);
	}

	public void revokePeaceOffer(Faction from, Faction to) {
		getState().getFactionManager().sendRelationshipOffer(getState().getPlayerName(), from.getIdFaction(), to.getIdFaction(), RType.NEUTRAL.code, "REVOKE", true);
	}

	public void revokeAllyOffer(Faction from, Faction to) {
		getState().getFactionManager().sendRelationshipOffer(getState().getPlayerName(), from.getIdFaction(), to.getIdFaction(), RType.FRIEND.code, "REVOKE", true);
	}
}
