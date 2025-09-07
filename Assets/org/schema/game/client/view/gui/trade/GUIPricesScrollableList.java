package org.schema.game.client.view.gui.trade;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.vecmath.Vector4f;

import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerBlockCategoryDropdownInputNew;
import org.schema.game.client.controller.PlayerBlockTypeDropdownInputNew;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.TradePrice;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterPos;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIPricesScrollableList extends ScrollableTableList<TradePriceInterface> {

	private TradeNodeClient node;

	private boolean weWantToBuy;

	private Vector4f red = new Vector4f(1f, 0.4f, 0.4f, 1.0f);

	private Vector4f green = new Vector4f(0.2f, 1.0f, 0.2f, 1.0f);

	private Vector4f white = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

	public GUIPricesScrollableList(InputState state, GUIElement p, ShopInterface currentShop, TradeNodeClient node, boolean buy) {
		super(state, 100, 100, p);
		this.weWantToBuy = buy;
		this.node = node;
		node.priceChangeListener.addObserver(this);
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
	public void draw() {
		// TradeNodeClient n = (TradeNodeClient)((GameClientState)getState())
		// .getController().getClientChannel().getGalaxyManagerClient()
		// .getTradeNodeDataById().get(currentShop.getSegmentController().getDbId());
		// 
		// assert(n == node):n+"; "+node;
		super.draw();
	}

	@Override
	protected boolean isFiltered(TradePriceInterface e) {
		// stuff that is NOT in the list
		return super.isFiltered(e) || e.getPrice() <= 0 || (weWantToBuy != e.isBuy());
	}

	private void clearOfferDialog() {
		(new PlayerOkCancelInput("CONFIRM", getState(), 300, 200, Lng.str("Confirm clear"), Lng.str("Really remove all offers?")) {

			@Override
			public void pressedOK() {
				List<TradePriceInterface> pr = node.getTradePricesClient();
				for (TradePriceInterface p : pr) {
					if (p.isBuy() == weWantToBuy) {
						sendPriceRequest(p, -1);
					}
				}
				deactivate();
			}

			@Override
			public void onDeactivate() {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(705);
	}

	private void addOfferCategoryDialog() {
		PlayerBlockCategoryDropdownInputNew blockdrop = new PlayerBlockCategoryDropdownInputNew("BBLA", ((GameClientState) getState()), Lng.str("ADD CATEGORY OFFER"), 0, 1, false) {

			@Override
			public void onAdditionalElementOk(Object userPointer) {
			}

			@Override
			public void onOk(ElementCategory c) {
				if (!list.isEmpty()) {
					List<ElementInformation> infos = c.getInfoElementsRecursive(new ObjectArrayList<ElementInformation>());
					for (ElementInformation info : infos) {
						if (!info.isDeprecated() && info.isShoppable()) {
							int price = ((int) info.getPrice(true));
							sendPriceRequest(info, price);
						}
					}
				}
				deactivate();
			}

			@Override
			public void onOkMeta() {
			}
		};
		if (weWantToBuy) {
			blockdrop.getInputPanel().getDescriptionText().setTextSimple(Lng.str("Prices will be set to default for the selected category."));
		} else {
			blockdrop.getInputPanel().getDescriptionText().setTextSimple(Lng.str("Prices will be set to default for the selected category."));
		}
		blockdrop.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(706);
	}

	private void addOfferDialog() {
		PlayerBlockTypeDropdownInputNew blockdrop = new PlayerBlockTypeDropdownInputNew("BBLA", ((GameClientState) getState()), Lng.str("ADD OFFER"), 1, 1, false) {

			@Override
			public void onOkMeta(MetaObject object) {
			}

			@Override
			public void onOk(ElementInformation info) {
				sendPriceRequest(info, getNumberValue());
				deactivate();
			}

			@Override
			public void onAdditionalElementOk(Object userPointer) {
			}

			@Override
			protected boolean includeInfo(ElementInformation info) {
				return !info.isDeprecated() && info.isShoppable();
			}

			@Override
			public void onSelectionChanged(GUIListElement element) {
				super.onSelectionChanged(element);
				if (element.getContent().getUserPointer() != null && element.getContent().getUserPointer() instanceof ElementInformation) {
					ElementInformation info = (ElementInformation) element.getContent().getUserPointer();
					setTextNumber(0, (int) info.getPrice(true));
				}
			}
		};
		if (weWantToBuy) {
			blockdrop.getInputPanel().getDescriptionText().setTextSimple(Lng.str("Set price to buy this block at."));
		} else {
			blockdrop.getInputPanel().getDescriptionText().setTextSimple(Lng.str("Set price to sell this block at."));
		}
		blockdrop.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(707);
	}

	private void sendPriceRequest(TradePriceInterface f, int price) {
		ElementInformation info = ElementKeyMap.getInfoFast(f.getType());
		ShopInterface currentClosestShop = ((GameClientState) getState()).getCurrentClosestShop();
		assert (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId());
		if (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId()) {
			assert (f.isBuy() == weWantToBuy);
			TradePrice s = ShoppingAddOn.getPriceInstance(currentClosestShop, f.getType(), weWantToBuy);
			s.setPrice(price);
			currentClosestShop.getShoppingAddOn().clientRequestSetPrice(((GameClientState) getState()).getPlayer(), s);
		}
	}

	private void sendLimitRequest(TradePriceInterface f, int limit) {
		ElementInformation info = ElementKeyMap.getInfoFast(f.getType());
		ShopInterface currentClosestShop = ((GameClientState) getState()).getCurrentClosestShop();
		assert (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId());
		if (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId()) {
			assert (f.isBuy() == weWantToBuy);
			TradePrice s = ShoppingAddOn.getPriceInstance(currentClosestShop, f.getType(), weWantToBuy);
			s.setLimit(limit);
			currentClosestShop.getShoppingAddOn().clientRequestSetPrice(((GameClientState) getState()).getPlayer(), s);
		}
	}

	public void sendPriceRequest(ElementInformation info, int price) {
		ShopInterface currentClosestShop = ((GameClientState) getState()).getCurrentClosestShop();
		assert (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId());
		if (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId()) {
			TradePrice s = ShoppingAddOn.getPriceInstance(currentClosestShop, info.getId(), weWantToBuy);
			s.setPrice(price);
			currentClosestShop.getShoppingAddOn().clientRequestSetPrice(((GameClientState) getState()).getPlayer(), s);
		}
	}

	public void sendLimitRequest(ElementInformation info, int limit) {
		ShopInterface currentClosestShop = ((GameClientState) getState()).getCurrentClosestShop();
		assert (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId());
		if (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId()) {
			TradePrice s = ShoppingAddOn.getPriceInstance(currentClosestShop, info.getId(), weWantToBuy);
			s.setLimit(limit);
			currentClosestShop.getShoppingAddOn().clientRequestSetPrice(((GameClientState) getState()).getPlayer(), s);
		}
	}

	public ShopControllerManager getShopControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Block"), 3f, (o1, o2) -> (o1.getInfo().getName()).compareTo(o2.getInfo().getName()));
		addFixedWidthColumnScaledUI(Lng.str("Basic Price"), 140, (o1, o2) -> CompareTools.compare(o1.getPrice(), o2.getPrice()));
		addFixedWidthColumnScaledUI(weWantToBuy ? Lng.str("Max Stock") : Lng.str("Min Stock"), 140, (o1, o2) -> CompareTools.compare(o1.getLimit(), o2.getLimit()));
		addFixedWidthColumnScaledUI(Lng.str("Stock"), 100, (o1, o2) -> CompareTools.compare(o1.getAmount(), o2.getAmount()));
		addFixedWidthColumnScaledUI(Lng.str(""), 40, (o1, o2) -> 0);
		addButton(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIPricesScrollableList.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					addOfferDialog();
				}
			}
		}, weWantToBuy ? Lng.str("ADD PURCHASE PRICE") : Lng.str("ADD SALE PRICE"), FilterRowStyle.LEFT, FilterPos.TOP);
		addButton(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIPricesScrollableList.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					addOfferCategoryDialog();
				}
			}
		}, weWantToBuy ? Lng.str("ADD PURCHASE CATEGORY") : Lng.str("ADD SALE CATEGORY"), FilterRowStyle.RIGHT, FilterPos.TOP);
		addTextFilter(new GUIListFilterText<TradePriceInterface>() {

			@Override
			public boolean isOk(String input, TradePriceInterface listElement) {
				return listElement.getInfo().getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.LEFT, FilterPos.TOP);
		addButton(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIPricesScrollableList.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					clearOfferDialog();
				}
			}
		}, Lng.str("CLEAR"), HButtonType.BUTTON_RED_MEDIUM, FilterRowStyle.RIGHT, FilterPos.TOP);
		activeSortColumnIndex = 1;
	}

	@Override
	protected Collection<TradePriceInterface> getElementList() {
		List<TradePriceInterface> tradePricesClient = node.getTradePricesClient();
		System.err.println("[CLIENT] CHANGED PRICES IN PRICES LIST: " + tradePricesClient.size());
		return tradePricesClient;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<TradePriceInterface> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final TradePriceInterface f : collection) {
			GUITextOverlayTable blockText = new GUITextOverlayTable(getState()) {

				@Override
				public void draw() {
					if (weWantToBuy) {
						if (f.getLimit() >= 0 && f.getAmount() > f.getLimit()) {
							setColor(red);
						} else {
							setColor(white);
						}
					} else {
						if (f.getLimit() >= 0 && f.getAmount() < f.getLimit()) {
							setColor(red);
						} else {
							setColor(white);
						}
					}
					super.draw();
				}
			};
			GUITextOverlayTable priceText = new GUITextOverlayTable(getState());
			GUITextOverlayTable limitText = new GUITextOverlayTable(getState());
			GUITextOverlayTable stockText = new GUITextOverlayTable(getState()) {

				@Override
				public void draw() {
					if (weWantToBuy) {
						if (f.getLimit() >= 0 && f.getAmount() > f.getLimit()) {
							setColor(red);
						} else {
							setColor(white);
						}
					} else {
						if (f.getLimit() >= 0 && f.getAmount() < f.getLimit()) {
							setColor(red);
						} else {
							setColor(white);
						}
					}
					super.draw();
				}
			};
			blockText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getInfo().getName();
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
					return f.getLimit() < 0 ? Lng.str("unlimited") : StringTools.formatSeperated(f.getLimit());
				}
			});
			stockText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return StringTools.formatSeperated(f.getAmount());
				}
			});
			GUIClippedRow sysAnchorP = new GUIClippedRow(getState());
			sysAnchorP.attach(blockText);
			GUIClippedRow priceAnchorP = new GUIClippedRow(getState());
			GUIActivatableTextBar priceInput = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, priceAnchorP, new TextCallback() {

				@Override
				public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
					try {
						String text = entry;
						int val = Integer.parseInt(text);
						if (val >= -1) {
							sendPriceRequest(f, val);
						}
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
			}, t -> t) {

				@Override
				protected void onBecomingInactive() {
					try {
						String text = getText();
						int val = Integer.parseInt(text);
						if (val >= -1) {
							sendPriceRequest(f, val);
						} else {
							setText(String.valueOf(f.getPrice()));
						}
					} catch (NumberFormatException e) {
						setText(String.valueOf(f.getPrice()));
					}
				}
			};
			priceInput.rightDependentHalf = true;
			priceInput.setText(String.valueOf(f.getPrice()));
			priceAnchorP.attach(priceText);
			priceAnchorP.attach(priceInput);
			GUIClippedRow limitAnchorP = new GUIClippedRow(getState());
			GUIActivatableTextBar limitInput = new GUIActivatableTextBar(getState(), FontSize.MEDIUM_15, priceAnchorP, new TextCallback() {

				@Override
				public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
					try {
						String text = entry;
						int val = Integer.parseInt(text);
						if (val >= -1) {
							sendLimitRequest(f, val);
						}
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
			}, t -> t) {

				@Override
				protected void onBecomingInactive() {
					try {
						String text = getText();
						int val = Integer.parseInt(text);
						if (val >= -1) {
							sendLimitRequest(f, val);
						} else {
							setText(String.valueOf(f.getLimit()));
						}
					} catch (NumberFormatException e) {
						setText(String.valueOf(f.getLimit()));
					}
				}
			};
			limitInput.rightDependentHalf = true;
			limitInput.setText(String.valueOf(f.getLimit()));
			limitAnchorP.attach(limitInput);
			limitAnchorP.attach(limitText);
			GUIClippedRow ownerAnchorP = new GUIClippedRow(getState());
			ownerAnchorP.attach(stockText);
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
					return !GUIPricesScrollableList.this.isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(708);
						removePrice(f);
					}
				}
			});
			cross.onInit();
			cross.setUserPointer("X");
			cross.getSprite().setTint(new Vector4f(1, 1, 1, 1));
			blockText.getPos().y = 5;
			priceText.getPos().y = 5;
			limitText.getPos().y = 5;
			stockText.getPos().y = 5;
			cross.getPos().x = 2;
			cross.getPos().y = 2;
			final TradePriceInterfaceRow r = new TradePriceInterfaceRow(getState(), f, sysAnchorP, priceAnchorP, limitAnchorP, ownerAnchorP, cross);
			// r.expanded = new GUIElementList(getState());
			// 
			// 
			// 
			// GUISizeSettingSelectorScroll priceSetting = new GUISizeSettingSelectorScroll(getState(), new AbstractSizeSetting() {
			// 
			// @Override
			// public int getMin() {
			// return 1;
			// }
			// 
			// @Override
			// public int getMax() {
			// return (int) (f.getInfo().getPrice(true) * 10);
			// }
			// }){
			// 
			// @Override
			// public void draw() {
			// dep = r.l.getInnerTextbox();
			// super.draw();
			// }
			// 
			// };
			// 
			// priceSetting.manualXDistance = 100;
			// priceSetting.manualXWidthMod = -80;
			// 
			// GUITextButton b = new GUITextButton(getState(), 30, 30, Lng.str("SET"), new GUICallback() {
			// @Override
			// public boolean isOccluded() {
			// return false;
			// }
			// 
			// @Override
			// public void callback(GUIElement callingGuiElement, MouseEvent event) {
			// if(event.pressedLeftMouse()){
			// 
			// }
			// }
			// });
			// 
			// r.expanded.add(new GUIListElement(priceSetting, priceSetting, getState()));
			// 
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private void removePrice(TradePriceInterface f) {
		sendPriceRequest(f, -1);
	}

	private class TradePriceInterfaceRow extends Row {

		public TradePriceInterfaceRow(InputState state, TradePriceInterface f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
