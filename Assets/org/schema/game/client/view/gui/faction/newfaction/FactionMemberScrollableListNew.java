package org.schema.game.client.view.gui.faction.newfaction;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerMailInputNew;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableInnerDescription;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.GuiDateFormats;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.PlayerStatusColorPalette;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class FactionMemberScrollableListNew extends ScrollableTableList<FactionPermission> {

	private Faction faction;

	public FactionMemberScrollableListNew(InputState state, GUIElement p, Faction f) {
		super(state, 100, 100, p);
		this.faction = f;
		getState().getFactionManager().obs.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		getState().getFactionManager().obs.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 3, (o1, o2) -> o1.playerUID.compareToIgnoreCase(o2.playerUID));
		addColumn(Lng.str("Status"), 1, (o1, o2) -> {
			int a1 = getState().getOnlinePlayersLowerCaseMap().containsKey(o1.playerUID.toLowerCase(Locale.ENGLISH)) ? 2 : (o1.isActiveMember() ? 1 : 0);
			int a2 = getState().getOnlinePlayersLowerCaseMap().containsKey(o2.playerUID.toLowerCase(Locale.ENGLISH)) ? 2 : (o2.isActiveMember() ? 1 : 0);
			return a1 - a2;
		});
		addColumn(Lng.str("Rank"), 1, (o1, o2) -> o1.role - o2.role);
		addColumn(Lng.str("Position"), 1, (o1, o2) -> {
			Vector3i ownPos = new Vector3i(getState().getPlayer().getCurrentSector());
			PlayerState p1 = getState().getOnlinePlayersLowerCaseMap().get(o1.playerUID.toLowerCase(Locale.ENGLISH));
			PlayerState p2 = getState().getOnlinePlayersLowerCaseMap().get(o2.playerUID.toLowerCase(Locale.ENGLISH));
			double dist1 = p1 != null ? Vector3i.getDisatance(ownPos, p1.getCurrentSector()) : Integer.MAX_VALUE - 100;
			double dist2 = p2 != null ? Vector3i.getDisatance(ownPos, p2.getCurrentSector()) : Integer.MAX_VALUE - 100;
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		});
		addTextFilter(new GUIListFilterText<FactionPermission>() {

			@Override
			public boolean isOk(String input, FactionPermission listElement) {
				return listElement.playerUID.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.FULL);
	}

	@Override
	protected Collection<FactionPermission> getElementList() {
		return faction.getMembersUID().values();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<FactionPermission> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final FactionPermission f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable statusText = new GUITextOverlayTable(getState()) {

				/* (non-Javadoc)
				 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable#draw()
				 */
				@Override
				public void draw() {
					if (FactionMemberScrollableListNew.this.getState().getOnlinePlayersLowerCaseMap().containsKey(f.playerUID.toLowerCase(Locale.ENGLISH))) {
						if (f.isActiveMember()) {
							setColor(PlayerStatusColorPalette.onlineActive);
						} else {
							setColor(PlayerStatusColorPalette.onlineInactive);
						}
					} else if (f.isActiveMember()) {
						setColor(PlayerStatusColorPalette.offlineActive);
					} else {
						setColor(PlayerStatusColorPalette.offlineInactive);
					}
					super.draw();
				}
			};
			GUITextOverlayTable rankText = new GUITextOverlayTable(getState());
			GUITextOverlayTable positionText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(f.playerUID);
			statusText.setTextSimple(new Object() {

				@Override
				public String toString() {
					if (FactionMemberScrollableListNew.this.getState().getOnlinePlayersLowerCaseMap().containsKey(f.playerUID.toLowerCase(Locale.ENGLISH))) {
						if (f.isActiveMember()) {
							return Lng.str("ONLINE");
						} else {
							return Lng.str("ONLINE(I)");
						}
					} else if (f.isActiveMember()) {
						return Lng.str("OFFLINE");
					} else {
						return Lng.str("OFFLINE(I)");
					}
				}
			});
			rankText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return faction.getRoles().getRoles()[f.role].name;
				}
			});
			positionText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.lastSeenPosition.x + ", " + f.lastSeenPosition.y + ", " + f.lastSeenPosition.z;
				}
			});
			nameText.getPos().y = 5;
			statusText.getPos().y = 5;
			rankText.getPos().y = 5;
			positionText.getPos().y = 5;
			FactionRow r = new FactionRow(getState(), f, nameText, statusText, rankText, positionText);
			r.expanded = new GUIElementList(getState());
			GUITextOverlayTableInnerDescription lastActive = new GUITextOverlayTableInnerDescription(10, 10, getState());
			lastActive.setTextSimple(new Object() {

				@Override
				public String toString() {
					return Lng.str("Last Seen: ") + GuiDateFormats.factionMemberLastSeenTime.format(f.lastSeenTime);
				}
			});
			GUIAnchor c = new GUIAnchor(getState(), 100, 30);
			GUITextButton mailButton = new GUITextButton(getState(), 50, 24, ColorPalette.OK, Lng.str("MAIL"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerMailInputNew mailInput = new PlayerMailInputNew(getState(), f.playerUID, "");
						mailInput.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(459);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton navButton = new GUITextButton(getState(), 60, 24, ColorPalette.OK, Lng.str("NAV TO"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(460);
						getState().getController().getClientGameData().setWaypoint(f.lastSeenPosition);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton promoteRelation = new GUITextButton(getState(), 60, 24, ColorPalette.OK, Lng.str("PROMOTE"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						FactionPermission factionPermission = faction.getMembersUID().get(getState().getPlayer().getName());
						if (!factionPermission.hasPermissionEditPermission(faction)) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot change role:\npermission denied!"), 0);
						} else if (factionPermission.role == f.role) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot promote members on your own rank!"), 0);
						} else if (factionPermission.role < f.role) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot change\nroles of higher ranked\nplayers!"), 0);
						} else if (f.role >= faction.getRoles().getRoles().length - 1) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot promote any further!"), 0);
						} else if ((f.role + 1) >= faction.getRoles().getRoles().length - 1) {
							PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("Do you really want to promote this member to founder?\nDoing this can only be reverted by a founder\nor if the founder is inactive for over a month.")) {

								@Override
								public void onDeactivate() {
								}

								@Override
								public void pressedOK() {
									faction.addOrModifyMemberClientRequest(getState().getPlayer().getName(), f.playerUID, (byte) (f.role + 1), getState().getGameState());
									deactivate();
								}
							};
							confirm.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(461);
						} else {
							faction.addOrModifyMemberClientRequest(getState().getPlayer().getName(), f.playerUID, (byte) (f.role + 1), getState().getGameState());
						}
					}
				}
			}) {

				@Override
				public void draw() {
					FactionPermission factionPermission = faction.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
					if (!factionPermission.hasPermissionEditPermission(faction)) {
					} else if (factionPermission.role <= f.role) {
					} else if (f.role >= faction.getRoles().getRoles().length - 1) {
					} else {
						super.draw();
					}
				}
			};
			GUITextButton demoteButton = new GUITextButton(getState(), 60, 24, ColorPalette.CANCEL, Lng.str("DEMOTE"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						FactionPermission factionPermission = faction.getMembersUID().get(getState().getPlayer().getName());
						if (factionPermission == null || !factionPermission.hasPermissionEditPermission(faction)) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot change role:\npermission denied!"), 0);
						} else if (factionPermission.role < f.role) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot change\nroles of higher ranked\nplayers!"), 0);
						} else if (f.role == 0) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot demote any further!"), 0);
						} else if (f.role >= faction.getRoles().getRoles().length - 1 && (factionPermission != f || f.isOverInactiveLimit(getState()))) {
							getState().getController().popupAlertTextMessage(Lng.str("You can't demote another\nfounder but yourself!"), 0);
						} else if (f.role >= faction.getRoles().getRoles().length - 1) {
							PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("You are currently a faction founder!\nDo you really want to demote yourself?")) {

								@Override
								public void pressedOK() {
									faction.addOrModifyMemberClientRequest(getState().getPlayer().getName(), f.playerUID, (byte) (f.role - 1), getState().getGameState());
									deactivate();
								}

								@Override
								public void onDeactivate() {
								}
							};
							confirm.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(462);
						} else {
							faction.addOrModifyMemberClientRequest(getState().getPlayer().getName(), f.playerUID, (byte) (f.role - 1), getState().getGameState());
						}
					}
				}
			}) {

				@Override
				public void draw() {
					FactionPermission factionPermission = faction.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
					if (factionPermission == null || !factionPermission.hasPermissionEditPermission(faction)) {
					} else if (f.role < faction.getRoles().getRoles().length - 1 && factionPermission.role <= f.role) {
					} else if (f.role == 0) {
					} else if (f.role >= faction.getRoles().getRoles().length - 1 && (factionPermission != f || f.isOverInactiveLimit((GameStateInterface) getState()))) {
					} else {
						super.draw();
					}
				}
			};
			GUITextButton kickButton = new GUITextButton(getState(), 60, 24, ColorPalette.CANCEL, Lng.str("KICK"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						FactionPermission factionPermission = faction.getMembersUID().get(getState().getPlayer().getName());
						System.err.println("[CLIENT] PERMISSION (will be rechecked on server): " + factionPermission.toString(faction));
						if (factionPermission == null || !factionPermission.hasKickPermission(faction)) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot kick:\npermission denied!"), 0);
						} else if (factionPermission.role <= f.role) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot kick players of this rank!"), 0);
						} else if (f.isFounder(faction)) {
							getState().getController().popupAlertTextMessage(Lng.str("You cannot kick Founders!"), 0);
						} else {
							PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("Do you really want to kick %s\nfrom the faction?", f.playerUID)) {

								@Override
								public void pressedOK() {
									faction.kickMemberClientRequest(getState().getPlayer().getName(), f.playerUID, getState().getGameState());
									deactivate();
								}

								@Override
								public void onDeactivate() {
								}
							};
							confirm.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(463);
						}
					}
				}
			}) {

				@Override
				public void draw() {
					FactionPermission factionPermission = faction.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
					if (!(factionPermission == null || !factionPermission.hasKickPermission(faction) || factionPermission.role <= f.role || f.isFounder(faction))) {
						super.draw();
					}
				}
			};
			c.attach(mailButton);
			c.attach(navButton);
			c.attach(promoteRelation);
			c.attach(demoteButton);
			c.attach(kickButton);
			mailButton.setPos(0, c.getHeight(), 0);
			navButton.setPos(mailButton.getWidth() + 10, c.getHeight(), 0);
			promoteRelation.setPos(mailButton.getWidth() + 10 + navButton.getWidth() + 10, c.getHeight(), 0);
			demoteButton.setPos(mailButton.getWidth() + 10 + promoteRelation.getWidth() + 10 + navButton.getWidth() + 10, c.getHeight(), 0);
			kickButton.setPos(navButton.getWidth() + 10 + mailButton.getWidth() + 10 + promoteRelation.getWidth() + 10 + demoteButton.getWidth() + 10, c.getHeight(), 0);
			c.attach(lastActive);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#getState()
	 */
	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	private class FactionRow extends Row {

		public FactionRow(InputState state, FactionPermission f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
