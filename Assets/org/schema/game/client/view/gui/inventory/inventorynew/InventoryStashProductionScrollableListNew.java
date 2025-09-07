package org.schema.game.client.view.gui.inventory.inventorynew;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.common.data.player.inventory.TypeAmountFastMap;
import org.schema.game.network.objects.ShortIntPair;
import org.schema.game.network.objects.remote.RemoteShortIntPair;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InventoryStashProductionScrollableListNew extends ScrollableTableList<InventoryStashProductionRowObject> {

	private final List<InventoryStashProductionRowObject> list = new ObjectArrayList<InventoryStashProductionRowObject>();

	private StashInventory inventory;

	private SegmentController segmentController;

	private SegmentPiece pointUnsave;

	public InventoryStashProductionScrollableListNew(InputState state, GUIElement dependend, StashInventory inventoy, SegmentPiece pointUnsave) {
		super(state, 100, 100, dependend);
		setColumnsHeight(UIScale.getUIScale().scale(32));
		this.inventory = inventoy;
		this.segmentController = ((ManagerContainer<?>) inventoy.getInventoryHolder()).getSegmentController();
		this.pointUnsave = pointUnsave;
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
		addColumn(Lng.str("Name"), 6, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
		addColumn(Lng.str("Amount"), 1, (o1, o2) -> o1.amount > o2.amount ? 1 : (o1.amount < o2.amount ? -1 : 0));
		addColumn(Lng.str("Pull up to"), 1, (o1, o2) -> o1.fillUpTo > o2.fillUpTo ? 1 : (o1.fillUpTo < o2.fillUpTo ? -1 : 0));
		addFixedWidthColumnScaledUI(Lng.str("Options"), 70, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<InventoryStashProductionRowObject>() {

			@Override
			public boolean isOk(String input, InventoryStashProductionRowObject listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<InventoryStashProductionRowObject> getElementList() {
		updateList();
		return list;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<InventoryStashProductionRowObject> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FactionManager factionManager = getState().getGameState().getFactionManager();
		final CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		final PlayerState player = getState().getPlayer();
		int i = 0;
		for (final InventoryStashProductionRowObject f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable amount = new GUITextOverlayTable(getState());
			GUITextOverlayTable upTP = new GUITextOverlayTable(getState());
			amount.setTextSimple(new Object() {

				@Override
				public String toString() {
					return String.valueOf(f.amount);
				}
			});
			upTP.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.fillUpTo > 0 ? String.valueOf(f.fillUpTo) : Lng.str("max");
				}
			});
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(f.getName());
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			amount.getPos().y = heightInset;
			upTP.getPos().y = heightInset;
			assert (!nameText.getText().isEmpty());
			assert (!amount.getText().isEmpty());
			GUIAnchor optionsAnchorP = new GUIAnchor(getState(), 120, getDefaultColumnsHeight());
			GUIClippedRow amountAnchorP = new GUIClippedRow(getState());
			OnInputChangedCallback numCallback = t -> {
				try {
					Integer.parseInt(t);
					return t;
				} catch (NumberFormatException ne) {
					return "";
				}
			};
			GUIActivatableTextBar amountInput = new GUIActivatableTextBar(getState(), FontSize.SMALL_14, String.valueOf(f.amount), amountAnchorP, new TextCallback() {

				@Override
				public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
					try {
						final int amount = Integer.parseInt(entry);
						InventoryStashProductionScrollableListNew.this.apply(f.type, amount, f.fillUpTo, false);
						InventoryStashProductionScrollableListNew.this.flagDirty();
					} catch (NumberFormatException e) {
						entry = String.valueOf(0);
					}
				}

				@Override
				public void onFailedTextCheck(String msg) {
				}

				@Override
				public void newLine() {
				}

				@Override
				public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
					return s;
				}

				@Override
				public String[] getCommandPrefixes() {
					return null;
				}
			}, numCallback) {

				@Override
				protected void onBecomingInactive() {
					try {
						String text = getText();
						int val = Integer.parseInt(text);
						if (val >= -1) {
							InventoryStashProductionScrollableListNew.this.apply(f.type, val, f.fillUpTo, false);
							InventoryStashProductionScrollableListNew.this.flagDirty();
						}
					} catch (NumberFormatException e) {
					}
				}
			};
			amountAnchorP.attach(amountInput);
			GUIClippedRow maxAnchorP = new GUIClippedRow(getState());
			GUIActivatableTextBar maxInput = new GUIActivatableTextBar(getState(), FontSize.SMALL_14, String.valueOf(f.fillUpTo), maxAnchorP, new TextCallback() {

				@Override
				public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
					try {
						final int limit = Integer.parseInt(entry);
						InventoryStashProductionScrollableListNew.this.apply(f.type, f.amount, Math.max(0, limit), false);
						InventoryStashProductionScrollableListNew.this.flagDirty();
					} catch (NumberFormatException e) {
					}
				}

				@Override
				public void onFailedTextCheck(String msg) {
				}

				@Override
				public void newLine() {
				}

				@Override
				public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
					return s;
				}

				@Override
				public String[] getCommandPrefixes() {
					return null;
				}
			}, numCallback) {

				@Override
				protected void onBecomingInactive() {
					try {
						String text = getText();
						int val = Integer.parseInt(text);
						if (val > 0) {
							InventoryStashProductionScrollableListNew.this.apply(f.type, f.amount, Math.max(0, val), false);
							InventoryStashProductionScrollableListNew.this.flagDirty();
						} else {
							setText(Lng.str("MAX"));
						}
					} catch (NumberFormatException e) {
					}
				}
			};
			maxAnchorP.attach(maxInput);
			GUITextButton delButton = new GUITextButton(getState(), 42, 22, ColorPalette.CANCEL, Lng.str("DEL"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse() && getState().getPlayerInputs().size() == 1) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(530);
						apply(f.type, 0, 0, false);
					}
				}
			});
			delButton.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
			optionsAnchorP.attach(delButton);
			final InventoryStashRow r = new InventoryStashRow(getState(), f, nameAnchorP, amountAnchorP, maxAnchorP, optionsAnchorP);
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

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	private PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	public void updateList() {
		list.clear();
		GameClientState state = getState();
		TypeAmountFastMap filter = inventory.getFilter().filter;
		filter.handleLoop((e, amount) -> {
			if (e <= 0 || ElementKeyMap.isValidType(e)) {
				list.add(new InventoryStashProductionRowObject(e, amount, inventory.getFilter().fillUpTo.get(e)));
			}
		});
	}

	public void apply(short dirtyFilter, int dirtyCount, int dirtyFillUpTo, boolean applyAllFlag) {
		if (dirtyFilter != 0) {
			if (dirtyCount == 0) {
				inventory.getFilter().filter.remove(dirtyFilter);
				inventory.getFilter().fillUpTo.remove(dirtyFilter);
			} else {
				System.err.println("[CLIENT] PULL MODIFYING " + ElementKeyMap.toString(dirtyFilter) + ": tick " + dirtyCount + " up to " + dirtyFillUpTo);
				inventory.getFilter().filter.put(dirtyFilter, dirtyCount);
				inventory.getFilter().fillUpTo.put(dirtyFilter, dirtyFillUpTo);
			}
			((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), dirtyFilter, dirtyCount), segmentController.isOnServer()));
			((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), dirtyFilter, dirtyFillUpTo), segmentController.isOnServer()));
			dirtyFilter = 0;
			dirtyFillUpTo = 0;
			dirtyCount = 0;
			flagDirty();
		}
		if (applyAllFlag) {
			if (dirtyCount > 0) {
				System.err.println("[CLIENT] ADDING PULL FILTER FOR EVERYTHING");
				for (short type : ElementKeyMap.keySet) {
					inventory.getFilter().filter.put(type, dirtyCount);
					inventory.getFilter().fillUpTo.put(type, dirtyFillUpTo);
					((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, dirtyCount), segmentController.isOnServer()));
					((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, dirtyFillUpTo), segmentController.isOnServer()));
				}
			} else {
				System.err.println("[CLIENT] CLEARING PULL FILTER FOR EVERYTHING");
				inventory.getFilter().filter.handleLoop((type, amount) -> ((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, 0), segmentController.isOnServer())));
				inventory.getFilter().filter.clear();
				inventory.getFilter().fillUpTo.handleLoop((type, amount) -> ((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, 0), segmentController.isOnServer())));
				inventory.getFilter().fillUpTo.clear();
			}
			dirtyFilter = 0;
			dirtyCount = 0;
			dirtyFillUpTo = 0;
			applyAllFlag = false;
			flagDirty();
		}
	}

	private class InventoryStashRow extends Row {

		public InventoryStashRow(InputState state, InventoryStashProductionRowObject f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
