package org.schema.game.client.view.gui.playerstats;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class PlayerStatisticsScrollableListNew extends ScrollableTableList<PlayerState> {

	public PlayerStatisticsScrollableListNew(InputState state, GUIAnchor guiAnchor) {
		super(state, 100, 100, guiAnchor);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
	}

	@Override
	public void initColumns() {
		addColumn("Name", 2, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
		addColumn("Faction", 3, (o1, o2) -> o1.getFactionName().compareToIgnoreCase(o2.getFactionName()));
		addFixedWidthColumnScaledUI("Ping", 50, (o1, o2) -> o1.getPing() - o2.getPing());
		addFixedWidthColumnScaledUI("Options", 56, (o1, o2) -> 0);
	}

	@Override
	protected Collection<PlayerState> getElementList() {
		return getState().getOnlinePlayersLowerCaseMap().values();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<PlayerState> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final PlayerState f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable factionText = new GUITextOverlayTable(getState());
			GUITextOverlayTable pingText = new GUITextOverlayTable(getState());
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			GUIClippedRow factionAnchorP = new GUIClippedRow(getState());
			factionAnchorP.attach(factionText);
			GUIClippedRow pingAnchorP = new GUIClippedRow(getState());
			pingAnchorP.attach(pingText);
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			factionText.getPos().y = heightInset;
			pingText.getPos().y = heightInset;
			nameText.setTextSimple(f.getName());
			factionText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getFactionName();
				}
			});
			pingText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.getPing());
				}
			});
			assert (!nameText.getText().isEmpty());
			assert (!factionText.getText().isEmpty());
			assert (!pingText.getText().isEmpty());
			GUIAnchor optionPane = new GUIAnchor(getState(), 50, getDefaultColumnsHeight());
			GUITextButton eText = new GUITextButton(getState(), 50, 20, ColorPalette.CANCEL, new Object() {

				@Override
				public String toString() {
					if (getState().getPlayer().getNetworkObject().isAdminClient.get()) {
						return "options";
					} else {
						return "-";
					}
				}
			}, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse() && getState().getPlayer().getNetworkObject().isAdminClient.get()) {
						PlayerStatisticsScrollableListNew.this.pressedAdminOptions(f);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			});
			if (player.getNetworkObject().isAdminClient.get()) {
				eText.setPos(0, UIScale.getUIScale().smallinset, 0);
				optionPane.attach(eText);
			}
			final WeaponRow r = new WeaponRow(getState(), f, nameAnchorP, factionAnchorP, pingAnchorP, optionPane);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	public boolean isPlayerAdmin() {
		return getState().getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(getState().getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	public WeaponAssignControllerManager getAssignWeaponControllerManager() {
		return getPlayerGameControlManager().getWeaponControlManager();
	}

	public InShipControlManager getInShipControlManager() {
		return getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
	}

	public PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	protected void pressedAdminOptions(final PlayerState player) {
		final GameClientState state = getState();
		final PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("PlayerStatisticsPanel_PLAYER_ADMIN_OPTIONS", state, "Player Admin Options: " + player.getName(), "") {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void pressedOK() {
				deactivate();
			}
		};
		c.getInputPanel().setOkButton(false);
		c.getInputPanel().onInit();
		GUITextButton kick = new GUITextButton(state, UIScale.getUIScale().scale(140), UIScale.getUIScale().h, ColorPalette.CANCEL, new Object() {

			@Override
			public String toString() {
				if (state.getPlayer().getNetworkObject().isAdminClient.get()) {
					return "Kick";
				} else {
					return "-";
				}
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayer().getNetworkObject().isAdminClient.get()) {
					c.deactivate();
					PlayerGameTextInput p = new PlayerGameTextInput("PlayerStatisticsPanel_KICK", (state), 100, "Kick", "Enter Reason") {

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}

						@Override
						public boolean isOccluded() {
							return false;
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
							c.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(618);
						}

						@Override
						public boolean onInput(String entry) {
							state.getController().sendAdminCommand(AdminCommands.KICK_REASON, player.getName(), entry);
							return true;
						}
					};
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(619);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		GUITextButton banAccount = new GUITextButton(state, UIScale.getUIScale().scale(140), UIScale.getUIScale().scale(20), FontSize.TINY_12, new Object() {

			@Override
			public String toString() {
				if (state.getPlayer().getNetworkObject().isAdminClient.get()) {
					return "Ban StarMade Account";
				} else {
					return "-";
				}
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayer().getNetworkObject().isAdminClient.get()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(620);
					state.getController().sendAdminCommand(AdminCommands.BAN_ACCOUNT_BY_PLAYERNAME, player.getName());
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		GUITextButton banName = new GUITextButton(state, UIScale.getUIScale().scale(140), UIScale.getUIScale().scale(20), FontSize.TINY_12, new Object() {

			@Override
			public String toString() {
				if (state.getPlayer().getNetworkObject().isAdminClient.get()) {
					return "Ban Player Name";
				} else {
					return "-";
				}
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayer().getNetworkObject().isAdminClient.get()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(621);
					state.getController().sendAdminCommand(AdminCommands.BAN, player.getName(), false);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		GUITextButton banIp = new GUITextButton(state, UIScale.getUIScale().scale(140), UIScale.getUIScale().scale(20), FontSize.TINY_12, new Object() {

			@Override
			public String toString() {
				if (state.getPlayer().getNetworkObject().isAdminClient.get()) {
					return "Ban IP";
				} else {
					return "-";
				}
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayer().getNetworkObject().isAdminClient.get()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(622);
					state.getController().sendAdminCommand(AdminCommands.BAN_IP_BY_PLAYERNAME, player.getName());
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		int e = UIScale.getUIScale().h;
		c.getInputPanel().getContent().attach(kick);
		banName.getPos().y = e * 1;
		c.getInputPanel().getContent().attach(banName);
		banAccount.getPos().y = e * 2;
		c.getInputPanel().getContent().attach(banAccount);
		banIp.getPos().y = e * 3;
		c.getInputPanel().getContent().attach(banIp);
		c.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(623);
	}

	private class WeaponRow extends Row {

		public WeaponRow(InputState state, PlayerState f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
