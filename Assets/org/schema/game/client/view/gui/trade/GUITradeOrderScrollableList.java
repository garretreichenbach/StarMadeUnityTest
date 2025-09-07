package org.schema.game.client.view.gui.trade;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeOrder;
import org.schema.game.common.controller.trade.TradeOrder.TradeOrderElement;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ListColorPalette;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class GUITradeOrderScrollableList extends ScrollableTableList<TradeOrderElement> {

	private final TradeOrder order;

	private TradeNodeClient node;

	private static final Vector4f red = new Vector4f(1f, 0.4f, 0.4f, 1.0f);

	// private static final Vector4f green = new Vector4f(0.2f,1.0f,0.2f,1.0f);
	private static final Vector4f white = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

	private ShopInterface currentClosestShop;

	public GUITradeOrderScrollableList(InputState state, ShopInterface currentClosestShop, TradeOrder order, TradeNodeClient node, GUIElement p) {
		super(state, 100, 100, p);
		this.node = node;
		this.currentClosestShop = currentClosestShop;
		this.order = order;
		order.addObserver(this);
		node.priceChangeListener.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		order.deleteObserver(this);
		node.priceChangeListener.deleteObserver(this);
	}

	@Override
	public void draw() {
		if (order.dirty) {
			order.recalc();
			order.dirty = false;
		}
		super.draw();
	}

	@Override
	public void onChange(boolean updateListDim) {
		order.dirty = true;
		super.onChange(updateListDim);
	}

	@Override
	public void initColumns() {
		addFixedWidthColumnScaledUI(Lng.str("Order"), 40, (o1, o2) -> {
			int compare = CompareTools.compare(o1.isBuyOrder(), o2.isBuyOrder());
			return compare != 0 ? compare : (o1.getInfo().getName()).compareTo(o2.getInfo().getName());
		});
		addColumn(Lng.str("Block"), 3f, (o1, o2) -> (o1.getInfo().getName()).compareTo(o2.getInfo().getName()));
		addFixedWidthColumnScaledUI(Lng.str("Amount"), 120, (o1, o2) -> CompareTools.compare(o1.amount, o2.amount));
		addFixedWidthColumnScaledUI(Lng.str("Edit"), 100, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<TradeOrderElement>() {

			@Override
			public boolean isOk(String input, TradeOrderElement listElement) {
				return listElement.getInfo().getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT);
		addDropdownFilter(new GUIListFilterDropdown<TradeOrderElement, Integer>(new Integer[] { 0, 1, 2 }) {

			@Override
			public boolean isOk(Integer input, TradeOrderElement f) {
				return switch(input) {
					case 0 -> true;
					case 1 -> f.isBuyOrder();
					case 2 -> !f.isBuyOrder();
					default -> true;
				};
			}
		}, new CreateGUIElementInterface<Integer>() {

			@Override
			public GUIElement create(Integer o) {
				GUIAnchor c = new GUIAnchor(getState(), 10, 24);
				GUITextOverlayTableDropDown a = new GUITextOverlayTableDropDown(10, 10, getState());
				switch(o) {
					case 0 -> a.setTextSimple(Lng.str("ALL"));
					case 1 -> a.setTextSimple(Lng.str("BUYING"));
					case 2 -> a.setTextSimple(Lng.str("SELLING"));
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
	protected Collection<TradeOrderElement> getElementList() {
		return order.getElements();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<TradeOrderElement> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TradeOrderElement f : collection) {
			GUITextOverlayTable typeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable blockText = new GUITextOverlayTable(getState());
			GUITextOverlayTable amountText = new GUITextOverlayTable(getState()) {

				@Override
				public void draw() {
					boolean b = (!f.isBuyOrder() && f.isOverAmount(node)) || (f.isBuyOrder() && f.amount > currentClosestShop.getShopInventory().getOverallQuantity(f.type));
					if (b) {
						setColor(red);
					} else {
						setColor(white);
					}
					super.draw();
				}
			};
			typeText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.isBuyOrder() ? Lng.str("BUY") : Lng.str("SELL");
				}
			});
			blockText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getInfo().getName();
				}
			});
			amountText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatSeperated(f.amount);
				}
			});
			GUIClippedRow typeAnchorP = new GUIClippedRow(getState());
			typeAnchorP.attach(typeText);
			GUIClippedRow blockAnchorP = new GUIClippedRow(getState());
			blockAnchorP.attach(blockText);
			GUIClippedRow amountAnchorP = new GUIClippedRow(getState());
			amountAnchorP.attach(amountText);
			GUITextButton b = new GUITextButton(getState(), 50, this.getDefaultColumnsHeight(), Lng.str("Edit"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !GUITradeOrderScrollableList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						openSaleDialog(f);
					}
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
					return !GUITradeOrderScrollableList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(712);
						// remove
						if (f.isBuyOrder()) {
							order.addOrChangeBuy(f.getType(), 0, true);
						} else {
							order.addOrChangeSell(f.getType(), 0, true);
						}
					}
				}
			});
			cross.onInit();
			cross.setUserPointer("X");
			cross.getSprite().setTint(new Vector4f(1, 1, 1, 1));
			GUIClippedRow editAnchorP = new GUIClippedRow(getState());
			editAnchorP.attach(b);
			editAnchorP.attach(cross);
			typeText.getPos().y = 5;
			blockText.getPos().y = 5;
			amountText.getPos().y = 5;
			cross.getPos().x = b.getWidth() + 5;
			cross.getPos().y = 2;
			TradeOrderElementRow r = new TradeOrderElementRow(getState(), f, typeAnchorP, blockAnchorP, amountAnchorP, editAnchorP);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	protected void openSaleDialog(final TradeOrderElement f) {
		PlayerTextInput ip = new PlayerTextInput("ALDNJ", getState(), 64, f.isBuyOrder() ? Lng.str("BUY") : Lng.str("SELL"), Lng.str("How many?"), String.valueOf(f.amount)) {

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
					if(amount > 0) { //Patched to prevent exploit.
					assert (order != null);
					if (f.isBuyOrder()) order.addOrChangeBuy(f.getType(), amount, true);
						 else order.addOrChangeSell(f.getType(), amount, true);

					return true;
				} }catch (NumberFormatException ignored) {
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
		AudioController.fireAudioEventID(713);
	}

	private class TradeOrderElementRow extends Row {

		public TradeOrderElementRow(InputState state, TradeOrderElement f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}

		@Override
		public Vector4f[] getCustomRowColors() {
			return getSort().isBuyOrder() ? customColorsBuy : customColorsSell;
		}

		private Vector4f[] customColorsBuy = new Vector4f[] { ListColorPalette.buyListBackgroundColor, ListColorPalette.buyListBackgroundColorAlternate, ListColorPalette.buyListBackgroundColorSelected };

		private Vector4f[] customColorsSell = new Vector4f[] { ListColorPalette.sellListBackgroundColor, ListColorPalette.sellListBackgroundColorAlternate, ListColorPalette.sellListBackgroundColorSelected };
	}
}
