package org.schema.game.client.view.gui.manualtrade;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.vecmath.Vector4f;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerManualTradeInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.trade.manualtrade.ManualTradeItem;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIManualTradeItemScrollableList extends ScrollableTableList<ManualTradeItem> implements GUIChangeListener {

	private PlayerManualTradeInput input;

	public GUIManualTradeItemScrollableList(InputState state, GUIAnchor guiAnchor, PlayerManualTradeInput input) {
		super(state, 100, 100, guiAnchor);
		input.trade.addObserver(this);
		this.input = input;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		input.trade.deleteObserver(this);
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
		addFixedWidthColumnScaledUI(Lng.str("Amount"), 50, (o1, o2) -> CompareTools.compare(o1.amount, o2.amount));
		addColumn(Lng.str("Item"), 3, (o1, o2) -> o1.toString().compareToIgnoreCase(o2.toString()));
		addFixedWidthColumnScaledUI(Lng.str("Options"), 86, (o1, o2) -> 0);
	}

	@Override
	protected Collection<ManualTradeItem> getElementList() {
		List<ManualTradeItem> list = input.trade.aItems[input.trade.getSide(getState().getPlayer())];
		return list;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<ManualTradeItem> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final ManualTradeItem f : collection) {
			GUITextOverlayTable amount = new GUITextOverlayTable(getState());
			GUITextOverlayTable factionText = new GUITextOverlayTable(getState());
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(amount);
			GUIClippedRow factionAnchorP = new GUIClippedRow(getState());
			factionAnchorP.attach(factionText);
			int heightInset = 5;
			amount.getPos().y = heightInset;
			factionText.getPos().y = heightInset;
			amount.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatSeperated(f.amount);
				}
			});
			factionText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.toString();
				}
			});
			assert (!amount.getText().isEmpty());
			assert (!factionText.getText().isEmpty());
			GUIAnchor optionPane = new GUIAnchor(getState(), 50, getDefaultColumnsHeight());
			GUITextButton eText = new GUITextButton(getState(), 50, 20, ColorPalette.CANCEL, new Object() {

				@Override
				public String toString() {
					return Lng.str("Edit");
				}
			}, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse() && getState().getPlayer().getNetworkObject().isAdminClient.get()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(546);
						GUIManualTradeItemScrollableList.this.pressedEdit(f);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			});
			GUIOverlay cross = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 16px-8x8-gui-"), getState()) {

				@Override
				public void draw() {
					if (isInside() && (getCallback() == null || !getCallback().isOccluded()) && isActive()) {
						getSprite().getTint().set(1.0f, 1.0f, 1.0f, 1.0f);
					} else {
						getSprite().getTint().set(0.8f, 0.8f, 0.8f, 1.0f);
					}
					super.draw();
				}
			};
			cross.setSpriteSubIndex(0);
			cross.setMouseUpdateEnabled(true);
			cross.setCallback(new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUIManualTradeItemScrollableList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(547);
						removeItem(f);
					}
				}
			});
			cross.onInit();
			cross.setUserPointer("X");
			cross.getSprite().setTint(new Vector4f(1, 1, 1, 1));
			eText.setPos(0, UIScale.getUIScale().smallinset, 0);
			cross.setPos(52, 2, 0);
			optionPane.attach(eText);
			optionPane.attach(cross);
			final WeaponRow r = new WeaponRow(getState(), f, nameAnchorP, factionAnchorP, optionPane);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private void removeItem(ManualTradeItem f) {
		input.trade.clientMod(getState().getPlayer(), f.type, f.metaId, 0);
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

	protected void pressedEdit(final ManualTradeItem item) {
		PlayerTextInput t = new PlayerTextInput("AMMMAMM", getState(), 9, Lng.str("Edit"), Lng.str("Enter new amount")) {

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public boolean onInput(String entry) {
				try {
					long l = Long.parseLong(entry);
					int i = (int) Math.max(0, Math.min(l, Integer.MAX_VALUE));
					input.trade.clientMod(((GameClientState) getState()).getPlayer(), item.type, item.metaId, i);
					return true;
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			}

			@Override
			public void onDeactivate() {
			}
		};
		t.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(548);
	}

	private class WeaponRow extends Row {

		public WeaponRow(InputState state, ManualTradeItem f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
