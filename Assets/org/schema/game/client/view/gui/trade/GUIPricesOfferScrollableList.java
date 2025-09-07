package org.schema.game.client.view.gui.trade;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShoppingAddOn.PriceRep;
import org.schema.game.common.controller.trade.TradeActive;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeOrder;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.CreateGUIElementInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterDropdown;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTableDropDown;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIPricesOfferScrollableList extends ScrollableTableList<TradePriceInterface> {

	private TradeNodeClient node;

	private boolean weWantToBuy;

	private final ShopInterface own;

	private TradeOrder order;

	private long lastUpdate;

	private GUITextOverlayTable loadText;

	private TradeNodeClient ownNode;

	public GUIPricesOfferScrollableList(InputState state, GUIElement p, TradeOrder order, TradeNodeClient node, boolean buy) throws ShopNotFoundException {
		super(state, 100, 100, p);
		this.weWantToBuy = buy;
		this.node = node;
		this.order = order;
		assert (order != null);
		node.priceChangeListener.addObserver(this);
		ShopInterface currentClosestShop = ((GameClientState) state).getCurrentClosestShop();
		if (currentClosestShop == null) {
			throw new ShopNotFoundException(Lng.str("Own shop not available."));
		}
		own = currentClosestShop;
		ownNode = (TradeNodeClient) ((GameClientState) state).getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().get(own.getSegmentController().getDbId());
		if (ownNode == null) {
			throw new ShopNotFoundException(Lng.str("Own shop not available."));
		}
		loadText = new GUITextOverlayTable(getState());
		loadText.setPos(5, 5, 0);
		loadText.setTextSimple(Lng.str("Requesting Prices..."));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		node.priceChangeListener.deleteObserver(this);
	}

	@Override
	protected boolean isFiltered(TradePriceInterface e) {
		// has to be buy == e.isBuy(), since if we are buying we want the SELL prices to be listed
		return super.isFiltered(e) || e.getPrice() <= 0 || weWantToBuy == e.isBuy();
	}

	public ShopControllerManager getShopControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}

	@Override
	public void draw() {
		if (node.isRequesting()) {
			GlUtil.glPushMatrix();
			transform();
			loadText.draw();
			GlUtil.glPopMatrix();
		} else {
			long time = System.currentTimeMillis();
			if (time - lastUpdate > 2000) {
				flagDirty();
				lastUpdate = time;
			}
			if (order.dirty) {
				order.recalc();
				order.dirty = false;
			}
			super.draw();
		}
	}

	public int getAvailable(TradePriceInterface o) {
		if (weWantToBuy) {
			return node.getMax(weWantToBuy, o);
		} else {
			// max of own stock and what the other shop can buy
			return Math.min(own.getShopInventory().getOverallQuantity(o.getType()), node.getMax(weWantToBuy, o));
		}
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Block"), 3f, (o1, o2) -> (o1.getInfo().getName()).compareTo(o2.getInfo().getName()));
		addFixedWidthColumnScaledUI(Lng.str("Price"), 140, (o1, o2) -> CompareTools.compare(o1.getPrice(), o2.getPrice()));
		addFixedWidthColumnScaledUI(Lng.str("Available"), 140, (o1, o2) -> CompareTools.compare(getAvailable(o1), getAvailable(o2)));
		addFixedWidthColumnScaledUI(Lng.str(""), 84, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<TradePriceInterface>() {

			@Override
			public boolean isOk(String input, TradePriceInterface listElement) {
				return listElement.getInfo().getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT);
		addDropdownFilter(new GUIListFilterDropdown<TradePriceInterface, Integer>(new Integer[] { 0, 1 }) {

			@Override
			public boolean isOk(Integer input, TradePriceInterface f) {
				return switch(input) {
					case 0 -> getAvailable(f) > 0;
					case 1 -> true;
					default -> true;
				};
			}
		}, new CreateGUIElementInterface<Integer>() {

			@Override
			public GUIElement create(Integer o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 24);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				switch(o) {
					case 0 -> a.setTextSimple(Lng.str("ONLY AVAILABLE"));
					case 1 -> a.setTextSimple(Lng.str("ALL"));
				}
				a.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				c.setUserPointer(o);
				c.attach(a);
				return c;
			}

			@Override
			public GUIElement createNeutral() {
				// default is all
				return null;
			}
		}, FilterRowStyle.RIGHT);
		activeSortColumnIndex = 1;
	}

	@Override
	protected Collection<TradePriceInterface> getElementList() {
		List<TradePriceInterface> tradePricesClient = node.getTradePricesClient();
		return tradePricesClient;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<TradePriceInterface> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TradePriceInterface f : collection) {
			GUITextOverlayTable blockText = new GUITextOverlayTable(getState());
			GUITextOverlayTable priceText = new GUITextOverlayTable(getState());
			GUITextOverlayTable limitText = new GUITextOverlayTable(getState());
			blockText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getInfo().getName() + (f instanceof PriceRep ? "(*)" : "");
				}
			});
			priceText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatSeperated(f.getPrice());
				}
			});
			limitText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatSeperated(getAvailable(f));
				}
			});
			GUIClippedRow sysAnchorP = new GUIClippedRow(getState());
			sysAnchorP.attach(blockText);
			GUIClippedRow priceAnchorP = new GUIClippedRow(getState());
			priceAnchorP.attach(priceText);
			GUIClippedRow limitAnchorP = new GUIClippedRow(getState());
			limitAnchorP.attach(limitText);
			blockText.getPos().y = 5;
			priceText.getPos().y = 5;
			limitText.getPos().y = 5;
			GUITextButton b = new GUITextButton(getState(), 50, this.getDefaultColumnsHeight(), weWantToBuy ? Lng.str("BUY") : Lng.str("SELL"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUIPricesOfferScrollableList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						TradeActive t;
						if (!weWantToBuy && (t = TradeOrder.checkActiveRoutesCanSellTypeTo(node.getEntityDBId(), f.getType(), ((GameClientState) getState()).getGameState().getTradeManager().getTradeActiveMap())) != null) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot sell %s.\n" + "The other shop is currently awaiting a shipment of this block\n" + "and holds off from buying any more\nuntil it arrives: ~%s", ElementKeyMap.getNameSave(f.getType()), StringTools.formatTimeFromMS(t.getEstimatedDuration())), 0);
						} else {
							openSaleDialog(f);
						}
					}
				}
			});
			GUIClippedRow orderAnchorP = new GUIClippedRow(getState());
			orderAnchorP.attach(b);
			final TradePriceInterfaceRow r = new TradePriceInterfaceRow(getState(), f, sysAnchorP, priceAnchorP, limitAnchorP, orderAnchorP);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	protected void openSaleDialog(final TradePriceInterface f) {
		PlayerTextInput ip = new PlayerTextInput("ALDNJ", getState(), 64, weWantToBuy ? Lng.str("BUY") : Lng.str("SELL"), Lng.str("How many?"), String.valueOf(getAvailable(f))) {

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return s;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public boolean onInput(String entry) {
				try {
					int amount = Integer.parseInt(entry);
					if (amount > 0) {
						assert (f != null);
						assert (order != null);
						if (weWantToBuy) {
							// on the sale prices of others
							order.addOrChangeBuy(f.getType(), amount, true);
						} else {
							order.addOrChangeSell(f.getType(), amount, true);
						}
						return true;
					}
				} catch (NumberFormatException e) {
				}
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Please enter a positive number for the amount."), 0);
				return false;
			}

			@Override
			public void onDeactivate() {
			}
		};
		ip.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(704);
	}

	private class TradePriceInterfaceRow extends Row {

		public TradePriceInterfaceRow(InputState state, TradePriceInterface f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
