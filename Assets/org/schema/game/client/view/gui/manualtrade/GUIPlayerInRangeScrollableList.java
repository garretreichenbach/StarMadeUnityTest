package org.schema.game.client.view.gui.manualtrade;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.trade.manualtrade.ManualTrade;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.network.objects.remote.RemoteManualTrade;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIPlayerInRangeScrollableList extends ScrollableTableList<PlayerState> {

	private PlayerInput input;

	public GUIPlayerInRangeScrollableList(InputState state, GUIAnchor guiAnchor, PlayerInput input) {
		super(state, 100, 100, guiAnchor);
		getState().getController().sectorEntitiesChangeObservable.addObserver(this);
		this.input = input;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		getState().getController().sectorEntitiesChangeObservable.deleteObserver(this);
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
		addColumn(Lng.str("Name"), 2, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
		addColumn(Lng.str("Faction"), 3, (o1, o2) -> o1.getFactionName().compareToIgnoreCase(o2.getFactionName()));
		addFixedWidthColumnScaledUI(Lng.str("Options"), 56, (o1, o2) -> 0);
	}

	@Override
	protected Collection<PlayerState> getElementList() {
		Map<String, PlayerState> onlinePlayersLowerCaseMap = getState().getOnlinePlayersLowerCaseMap();
		List<PlayerState> pls = new ObjectArrayList<PlayerState>(onlinePlayersLowerCaseMap.size());
		for (PlayerState p : onlinePlayersLowerCaseMap.values()) {
			if (p != getState().getPlayer() && !getState().getPlayer().isInTestSector() && !p.isInTestSector() && Sector.isNeighbor(p.getCurrentSector(), getState().getPlayer().getCurrentSector())) {
				pls.add(p);
			}
		}
		return pls;
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
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			GUIClippedRow factionAnchorP = new GUIClippedRow(getState());
			factionAnchorP.attach(factionText);
			int heightInset = UIScale.getUIScale().scale(5);
			nameText.getPos().y = heightInset;
			factionText.getPos().y = heightInset;
			nameText.setTextSimple(f.getName());
			factionText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getFactionName();
				}
			});
			assert (!nameText.getText().isEmpty());
			assert (!factionText.getText().isEmpty());
			GUIAnchor optionPane = new GUIAnchor(getState(), 50, getDefaultColumnsHeight());
			GUITextButton eText = new GUITextButton(getState(), 50, 20, ColorPalette.CANCEL, new Object() {

				@Override
				public String toString() {
					return Lng.str("Trade");
				}
			}, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse() && getState().getPlayer().getNetworkObject().isAdminClient.get()) {
						GUIPlayerInRangeScrollableList.this.pressedTrade(f);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			});
			eText.setPos(0, UIScale.getUIScale().smallinset, 0);
			optionPane.attach(eText);
			final WeaponRow r = new WeaponRow(getState(), f, nameAnchorP, factionAnchorP, optionPane);
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

	protected void pressedTrade(final PlayerState player) {
		if (!getState().getPlayer().activeManualTrades.isEmpty()) {
			final PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("PlayerStatisticsPanel_PLAYER_ADMIN_OPTIONS", getState(), Lng.str("Active Trade Requests"), Lng.str("You still have active trade requests.\nDo you want to cancel them and start a new one?")) {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
					for (ManualTrade t : getState().getPlayer().activeManualTrades) {
						t.clientCancel(getState().getPlayer());
					}
					startTrade(player);
					deactivate();
				}
			};
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(549);
		} else {
			startTrade(player);
		}
	}

	private void startTrade(PlayerState player) {
		ManualTrade t = new ManualTrade(getState().getPlayer(), player);
		getState().getController().getClientChannel().getNetworkObject().manualTradeBuffer.add(new RemoteManualTrade(t, false));
		input.deactivate();
	}

	private class WeaponRow extends Row {

		public WeaponRow(InputState state, PlayerState f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
