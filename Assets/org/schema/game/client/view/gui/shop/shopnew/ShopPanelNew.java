package org.schema.game.client.view.gui.shop.shopnew;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerBlockTypeDropdownInputNew;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.ingame.shop.BuyQuantityDialog;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement;
import org.schema.game.client.view.gui.inventory.inventorynew.InventoryOptionsChestButtonPanel;
import org.schema.game.client.view.gui.inventory.inventorynew.InventoryPanelNew;
import org.schema.game.client.view.gui.trade.GUIPricesScrollableList;
import org.schema.game.client.view.gui.trade.GUITradeActiveScrollableList;
import org.schema.game.client.view.gui.trade.GUITradeNodeScrollableList;
import org.schema.game.client.view.gui.trade.OrderDialog;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.trade.TradeManager;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.TradePrice;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.List;
import java.util.Locale;


public class ShopPanelNew extends GUIElement implements GUIActiveInterface, DropTarget<InventorySlotOverlayElement> {
	public GUIMainWindow shopPanel;
	private boolean isOwnShopBefore;
	private boolean isTradeShopBefore;
	// private GUIContentPane personalTab;
	private boolean init;
	private boolean flagShopTabRecreate;
	private GUIContentPane shopBuySellTab;
	private GUIContentPane shopBlueprintsTab;
	private GUIContentPane shopOptionsTab;
	private ShopScrollableCategoryList shopList;
	private GUIContentPane shopRepairTab;
	private GUIContentPane tradeTab;
	private GUIContentPane pricesTab;
	private ShopInterface currentShop;
	private GUITabbedContent blueprintsTabs;
	private boolean lastWasTrade;
	private boolean inventoryActive;

	public ShopPanelNew(InputState state) {
		super(state);
		currentShop = getState().getCurrentClosestShop();
	}

	@Override
	public void cleanUp() {
	}

	public boolean isTradeNode() {
		return currentShop != null && currentShop.isTradeNode() && !currentShop.getSegmentController().getType().equals(SimpleTransformableSendableObject.EntityType.SHIP) && (TradeNodeClient) getState().getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().get(currentShop.getSegmentController().dbId) != null;
	}

	@Override
	public void draw() {
		if(!init) {
			currentShop = getState().getCurrentClosestShop();
			lastWasTrade = isTradeNode();
			isOwnShopBefore = isOwnShop();
			isTradeShopBefore = isShopTrader();
			if(currentShop == null) {
				return;
			}
			onInit();
		}
		if(currentShop != getState().getCurrentClosestShop() || isTradeNode() != lastWasTrade || isOwnShopBefore != isOwnShop() || isTradeShopBefore != isShopTrader()) {
			flagShopTabRecreate = true;
			currentShop = getState().getCurrentClosestShop();
			lastWasTrade = isTradeNode();
			isOwnShopBefore = isOwnShop();
			isTradeShopBefore = isShopTrader();
			if(!lastWasTrade || !isTradeShopBefore) {
				List<DialogInterface> c = getState().getPlayerInputs();
				if(!c.isEmpty() && c.get(c.size() - 1) instanceof OrderDialog) {
					getState().getController().popupAlertTextMessage(Lng.str("Cannot order! Shop no longer in trade network\nor permissions changed"), 0);
					c.get(c.size() - 1).deactivate();
				}
			}
		}
		if(currentShop == null) {
			return;
		}
		if(flagShopTabRecreate) {
			recreateTabs();
			flagShopTabRecreate = false;
		}
		if(isOwnShop()) {
			shopBuySellTab.setTextBoxHeight(1, 1, UIScale.getUIScale().scale(28 + 24 + 25));
		} else {
			shopBuySellTab.setTextBoxHeight(1, 1, UIScale.getUIScale().scale(28 + 24));
		}
		shopPanel.draw();
	}

	@Override
	public void onInit() {
		if(shopPanel != null) {
			shopPanel.cleanUp();
		}
		shopPanel = new GUIMainWindow(getState(), UIScale.getUIScale().scale(750), UIScale.getUIScale().scale(550), "ShopPanelNew");
		shopPanel.onInit();
		shopPanel.setMouseUpdateEnabled(true);
		shopPanel.setCallback(new GUICallback() {
			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(shopPanel.isInside()) {
					checkTarget(event);
				}
			}
		});
		shopPanel.setCloseCallback(new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(680);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}
		});
		shopPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		init = true;
	}

	public void recreateTabs() {
		System.err.println("[CLIENT] Shop panel recreate for shop " + currentShop);
		Object beforeTab = null;
		if(shopPanel.getSelectedTab() < shopPanel.getTabs().size()) {
			beforeTab = shopPanel.getTabs().get(shopPanel.getSelectedTab()).getTabName();
		}
		shopPanel.clearTabs();
		// personalTab = catalogPanel.addTab(Lng.str("OWN");
		shopBuySellTab = shopPanel.addTab(Lng.str("SHOP"));
		if(currentShop.getSegmentController().getType() != SimpleTransformableSendableObject.EntityType.SHIP) {
			shopBlueprintsTab = shopPanel.addTab(Lng.str("BLUEPRINTS"));
			createBlueprintsPane();
			shopRepairTab = shopPanel.addTab(Lng.str("REPAIRS"));
		}
		// createPersonalCatalogPane();
		createShopPane();
		if(isShopTrader()) {
			shopOptionsTab = shopPanel.addTab(Lng.str("OPTIONS"));
			createOptionPane();
			if(currentShop.getSegmentController().getType() != SimpleTransformableSendableObject.EntityType.SHIP) {
				tradeTab = shopPanel.addTab(Lng.str("TRADE"));
				createTradePane();
				pricesTab = shopPanel.addTab(Lng.str("SET PRICES"));
				createPricesPane();
			}
		}
		if(currentShop.getSegmentController().getType() != SimpleTransformableSendableObject.EntityType.SHIP) createRepairPane();
		shopPanel.activeInterface = this;
		if(beforeTab != null) {
			for(int i = 0; i < shopPanel.getTabs().size(); i++) {
				if(shopPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					shopPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	private void createTradeButtons(GUIAnchor attachTo) {
		if(!currentShop.getSegmentController().getType().equals(SimpleTransformableSendableObject.EntityType.SHIP)) {
			GUIHorizontalButtonTablePane optionButtons = new GUIHorizontalButtonTablePane(getState(), 1, 1, attachTo);
			optionButtons.onInit();
			optionButtons.addButton(0, 0, new Object() {
				@Override
				public String toString() {
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					return !currentClosestShop.getShoppingAddOn().isTradeNode() ? Lng.str("Add shop to trade network") : Lng.str("Remove shop from trade network");
				}
			}, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						ShopInterface currentClosestShop = getState().getCurrentClosestShop();
						currentClosestShop.getShoppingAddOn().requestTradeNodeStateOnClient(!currentClosestShop.getShoppingAddOn().isTradeNode());
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(681);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			}, new GUIActivationHighlightCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					// Maybe removing shop from trade network should be real shop owner only?
					return isShopTrader();
				}

				@Override
				public boolean isHighlighted(InputState state) {
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					return currentClosestShop.getShoppingAddOn().isTradeNode();
				}
			});
			attachTo.attach(optionButtons);
		}
	}

	private void createTradePane() {
		tradeTab.setTextBoxHeightLast(UIScale.getUIScale().scale(26));
		createTradeButtons(tradeTab.getContent(0));
		tradeTab.addNewTextBox(UIScale.getUIScale().scale(150));
		if(isTradeNode()) {
			GUITradeActiveScrollableList trAct = new GUITradeActiveScrollableList(getState(), currentShop, tradeTab.getContent(1));
			trAct.onInit();
			tradeTab.getContent(1).attach(trAct);
			tradeTab.addNewTextBox(UIScale.getUIScale().scale(76 + 21));
			GUITradeNodeScrollableList nodes = new GUITradeNodeScrollableList(getState(), currentShop, tradeTab.getContent(2));
			nodes.onInit();
			tradeTab.getContent(2).attach(nodes);
			tradeTab.setEqualazingHorizontalDividers(1);
		} else {
			GUITextOverlay notOwnShop = new GUITextOverlay(getState());
			notOwnShop.setTextSimple(Lng.str("Shops to trade with are only available at shops that are in the trade network."));
			notOwnShop.setPos(UIScale.getUIScale().scale(6), UIScale.getUIScale().scale(10), 0);
			tradeTab.getContent(1).attach(notOwnShop);
		}
	}

	public void switchAll() {
		TradeNodeClient node = (TradeNodeClient) getState().getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().get(currentShop.getSegmentController().getDbId());
		if(isTradeNode() && isShopTrader()) {
			ShopInterface currentClosestShop = getState().getCurrentClosestShop();
			assert (currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId());
			if(currentClosestShop != null && currentClosestShop.getSegmentController().dbId == node.getEntityDBId()) {
				for(short type : ElementKeyMap.typeList()) {
					ElementInformation info = ElementKeyMap.getInfo(type);
					TradePriceInterface buyPrice = currentClosestShop.getPrice(info.getId(), true);
					TradePriceInterface sellPrice = currentClosestShop.getPrice(info.getId(), false);
					if(buyPrice != null) {
						TradePrice price = ShoppingAddOn.getPriceInstanceIfExisting(currentClosestShop, type, true);
						price.setSell(!price.isSell());
						currentClosestShop.getShoppingAddOn().clientRequestSetPrice(getState().getPlayer(), price);
						if(sellPrice == null) {
							// remove previous price since there is nothing to be swapped with
							price = ShoppingAddOn.getPriceInstanceIfExisting(currentClosestShop, type, true);
							price.setPrice(-1);
							currentClosestShop.getShoppingAddOn().clientRequestSetPrice(getState().getPlayer(), price);
						}
					}
					if(sellPrice != null) {
						TradePrice price = ShoppingAddOn.getPriceInstanceIfExisting(currentClosestShop, type, false);
						price.setSell(!price.isSell());
						currentClosestShop.getShoppingAddOn().clientRequestSetPrice(getState().getPlayer(), price);
						if(buyPrice == null) {
							// remove previous price since there is nothing to be swapped with
							price = ShoppingAddOn.getPriceInstanceIfExisting(currentClosestShop, type, false);
							price.setPrice(-1);
							currentClosestShop.getShoppingAddOn().clientRequestSetPrice(getState().getPlayer(), price);
						}
					}
				}
			}
		}
	}

	private void createPricesPane() {
		TradeNodeClient n = (TradeNodeClient) getState().getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().get(currentShop.getSegmentController().getDbId());
		if(isTradeNode() && isShopTrader()) {
			int contentP = 0;
			if(getState().isAdmin()) {
				pricesTab.setTextBoxHeightLast(UIScale.getUIScale().P_SMALL_PANE_HEIGHT);
				GUIHorizontalButtonTablePane optionButtons = new GUIHorizontalButtonTablePane(getState(), 1, 1, pricesTab.getContent(contentP));
				optionButtons.onInit();
				optionButtons.addButton(0, 0, Lng.str("Switch Prices (Admin)"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							switchAll();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SWITCH)*/
							AudioController.fireAudioEventID(682);
						}
					}

					@Override
					public boolean isOccluded() {
						return !isActive();
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				pricesTab.getContent(contentP).attach(optionButtons);
				contentP++;
				pricesTab.addNewTextBox(76 + 21);
			} else {
				pricesTab.setTextBoxHeightLast(76 + 21);
			}
			GUIPricesScrollableList nodesBuy = new GUIPricesScrollableList(getState(), pricesTab.getContent(contentP), currentShop, n, true);
			nodesBuy.onInit();
			pricesTab.getContent(contentP).attach(nodesBuy);
			pricesTab.addNewTextBox(UIScale.getUIScale().scale(10));
			contentP++;
			GUIPricesScrollableList nodesSell = new GUIPricesScrollableList(getState(), pricesTab.getContent(contentP), currentShop, n, false);
			nodesSell.onInit();
			pricesTab.getContent(contentP).attach(nodesSell);
			pricesTab.setEqualazingHorizontalDividers(getState().isAdmin() ? 1 : 0);
		} else {
			pricesTab.setTextBoxHeightLast(76 + 21);
			GUITextOverlay notOwnShop = new GUITextOverlay(getState());
			notOwnShop.setTextSimple(Lng.str("Only prices of trade network shops you\nown or have permission to are editable."));
			notOwnShop.setPos(6, 10, 0);
			pricesTab.getContent(0).attach(notOwnShop);
		}
	}
	
	private boolean isBpDialogActive() {
		List<DialogInterface> c = getState().getPlayerInputs();
		for(DialogInterface d : c) {
			if(d instanceof AddBlueprintDialog) return true;
		}
		return false;
	}
	
	private void createBlueprintsPane() {
		blueprintsTabs = new GUITabbedContent(getState(), shopBlueprintsTab.getContent(0));
		blueprintsTabs.activationInterface = this;
		blueprintsTabs.setPos(0, UIScale.getUIScale().smallinset, 0);
		blueprintsTabs.onInit();
		shopBlueprintsTab.getContent(0).attach(blueprintsTabs);
		ServerTab: {
			GUIContentPane tab = blueprintsTabs.addTab(Lng.str("SERVER"));
			BlueprintMarketDataScrollableList list = new BlueprintMarketDataScrollableList(getState(), tab.getContent(0), AddBlueprintDialog.ADMIN_MODE);
			list.onInit();
			tab.getContent(0).attach(list);
			
			if(getState().isAdmin()) {
				tab.setTextBoxHeightLast(UIScale.getUIScale().scale(10));
				tab.setListDetailMode(tab.getTextboxes().get(tab.getTextboxes().size() - 1));
				tab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
				GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, tab.getContent(1));
				buttonPane.onInit();
				buttonPane.addButton(0, 0, Lng.str("ADD BLUEPRINT"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
					@Override
					public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
						if(mouseEvent.pressedLeftMouse()) {
							(new AddBlueprintDialog(getState(), AddBlueprintDialog.ADMIN_MODE)).activate();
						}
					}

					@Override
					public boolean isOccluded() {
						return !getState().isAdmin() || isBpDialogActive();
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState inputState) {
						return true;
					}

					@Override
					public boolean isActive(InputState inputState) {
						return getState().isAdmin() && !isBpDialogActive();
					}
				});
				tab.getContent(1).attach(buttonPane);
			}
		}
		
		PlayerTab: {
			GUIContentPane tab = blueprintsTabs.addTab(Lng.str("PLAYER"));
			BlueprintMarketDataScrollableList list = new BlueprintMarketDataScrollableList(getState(), tab.getContent(0), AddBlueprintDialog.PLAYER_MODE);
			list.onInit();
			tab.getContent(0).attach(list);

			tab.setTextBoxHeightLast(UIScale.getUIScale().scale(10));
			tab.setListDetailMode(tab.getTextboxes().get(tab.getTextboxes().size() - 1));
			tab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
			GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, tab.getContent(1));
			buttonPane.onInit();
			buttonPane.addButton(0, 0, Lng.str("ADD BLUEPRINT"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
				@Override
				public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
					if(mouseEvent.pressedLeftMouse()) {
						(new AddBlueprintDialog(getState(), AddBlueprintDialog.PLAYER_MODE)).activate();
					}
				}

				@Override
				public boolean isOccluded() {
					return isBpDialogActive();
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState inputState) {
					return true;
				}

				@Override
				public boolean isActive(InputState inputState) {
					return !isBpDialogActive();
				}
			});
			tab.getContent(1).attach(buttonPane);
		}
	}

	private void createShopPane() {
		shopBuySellTab.setTextBoxHeightLast(UIScale.getUIScale().scale(28));
		shopBuySellTab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		shopBuySellTab.addDivider(280);
		shopList = new ShopScrollableCategoryList(getState(), shopBuySellTab.getContent(0, 0), this);
		shopList.onInit();
		shopBuySellTab.getContent(0, 0).attach(shopList);
		shopBuySellTab.setListDetailMode(0, shopBuySellTab.getTextboxes(0).get(0));
		GUISearchBar searchBar = new GUISearchBar(getState(), shopBuySellTab.getContent(0, 1), new TextCallback() {
			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void newLine() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}
		}, shopList);
		shopBuySellTab.getContent(0, 1).attach(searchBar);
		shopBuySellTab.setTextBoxHeightLast(1, UIScale.getUIScale().scale(76 + 21));
		GUIAnchor shopInfoAncor = new GUIAnchor(getState(), UIScale.getUIScale().scale(200), UIScale.getUIScale().scale(72));
		GUITextOverlay ownCredits = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		GUITextOverlay shopCredits = new GUITextOverlay(FontSize.SMALL_15, getState());
		GUITextOverlay shopCargo = new GUITextOverlay(FontSize.SMALL_15, getState()) {
			@Override
			public void draw() {
				ShopInterface currentClosestShop = ((GameClientState) getState()).getCurrentClosestShop();
				assert (currentClosestShop == null || currentClosestShop.getShopInventory() != null) : currentClosestShop;
				if(currentClosestShop != null && currentClosestShop.getShopInventory().isOverCapacity()) {
					setColor(Color.red);
				} else {
					setColor(Color.white);
				}
				super.draw();
			}
		};
		GUITextOverlay shopOwner = new GUITextOverlay(FontSize.SMALL_15, getState());
		ownCredits.setTextSimple(new Object() {
			@Override
			public String toString() {
				return Lng.str("Own Credits: %s", StringTools.formatSeperated(getState().getPlayer().getCredits()));
			}
		});
		shopCredits.setTextSimple(new Object() {
			@Override
			public String toString() {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null) {
					return Lng.str("Shop Credits: %s", StringTools.formatSeperated(currentClosestShop.getCredits()));
				} else {
					return Lng.str("Shop Credits: n/a");
				}
			}
		});
		shopCargo.setTextSimple(new Object() {
			@Override
			public String toString() {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null) {
					return Lng.str("Shop Cargo: %s / %s", StringTools.formatPointZero(currentClosestShop.getShopInventory().getVolume()), StringTools.formatPointZero(currentClosestShop.getShopInventory().getCapacity()));
				} else {
					return Lng.str("Shop Cargo: n/a");
				}
			}
		});
		shopOwner.setTextSimple(new Object() {
			@Override
			public String toString() {
				String newS;
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null) {
					if(currentClosestShop.isAiShop()) {
						return Lng.str("Shop Owner: Trading Guild");
					} else if(currentClosestShop.getShopOwners().isEmpty()) {
						return Lng.str("nobody (take ownership in options)");
					} else {
						return Lng.str("Shop Owner%s: %s", (currentClosestShop.getShopOwners().size() > 1 ? "s" : ""), currentClosestShop.getShopOwners().toString());
					}
				}
				return Lng.str("Shop Owner: n/a");
			}
		});
		ownCredits.setColor(0.9f, 0.9f, 0.7f, 1.0f);
		ownCredits.getPos().x = UIScale.getUIScale().inset;
		ownCredits.getPos().y = UIScale.getUIScale().inset;
		shopInfoAncor.attach(ownCredits);
		shopCredits.getPos().x = UIScale.getUIScale().inset;
		shopCredits.getPos().y = UIScale.getUIScale().scale(4 + 26);
		shopInfoAncor.attach(shopCredits);
		shopOwner.getPos().x = UIScale.getUIScale().inset;
		shopOwner.getPos().y = UIScale.getUIScale().scale(4 + 26 + 21);
		shopInfoAncor.attach(shopOwner);
		shopCargo.getPos().x = UIScale.getUIScale().inset;
		shopCargo.getPos().y = UIScale.getUIScale().scale(4 + 26 + 21 + 21);
		shopInfoAncor.attach(shopCargo);
		GUIScrollablePanel pSInfo = new GUIScrollablePanel(10, 10, shopBuySellTab.getContent(1, 0), getState());
		pSInfo.setContent(shopInfoAncor);
		shopBuySellTab.getContent(1, 0).attach(pSInfo);
		// this is being updated in draw() is needed
		shopBuySellTab.addNewTextBox(1, UIScale.getUIScale().scale(28 + 24));
		createBuySellButtonPanel(shopBuySellTab.getContent(1, 1));
		// description
		shopBuySellTab.addNewTextBox(1, UIScale.getUIScale().scale(100));
		createBlockDescriptionPanel(shopBuySellTab.getContent(1, 2));
	}

	public void createRepairPane() {
		shopRepairTab.setTextBoxHeightLast(UIScale.getUIScale().scale(76));
		createShopRepairsButtonPanel(shopRepairTab.getContent(0));
	}

	public void createOptionPane() {
		shopOptionsTab.setTextBoxHeightLast(76 + 21);
		GUIAnchor shopInfoAncor = new GUIAnchor(getState(), UIScale.getUIScale().scale(200), UIScale.getUIScale().scale(72));
		GUITextOverlay ownCredits = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		GUITextOverlay shopCredits = new GUITextOverlay(FontSize.SMALL_15, getState());
		GUITextOverlay shopOwner = new GUITextOverlay(FontSize.SMALL_15, getState());
		GUITextOverlay shopCargo = new GUITextOverlay(FontSize.SMALL_15, getState()) {
			@Override
			public void draw() {
				ShopInterface currentClosestShop = ((GameClientState) getState()).getCurrentClosestShop();
				if(currentClosestShop != null && currentClosestShop.getShopInventory().isOverCapacity()) {
					setColor(Color.red);
				} else {
					setColor(Color.white);
				}
				super.draw();
			}
		};
		ownCredits.setTextSimple(new Object() {
			@Override
			public String toString() {
				return Lng.str("Own Credits: %s", StringTools.formatSeperated(getState().getPlayer().getCredits()));
			}
		});
		shopCredits.setTextSimple(new Object() {
			@Override
			public String toString() {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null) {
					return Lng.str("Shop Credits: %s", StringTools.formatSeperated(currentClosestShop.getCredits()));
				} else {
					return Lng.str("Shop Credits: n/a");
				}
			}
		});
		shopCargo.setTextSimple(new Object() {
			@Override
			public String toString() {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null) {
					return Lng.str("Shop Cargo: %s / %s", StringTools.formatPointZero(currentClosestShop.getShopInventory().getVolume()), StringTools.formatPointZero(currentClosestShop.getShopInventory().getCapacity()));
				} else {
					return Lng.str("Shop Cargo: n/a");
				}
			}
		});
		shopOwner.setTextSimple(new Object() {
			@Override
			public String toString() {
				String newS;
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null) {
					if(currentClosestShop.isAiShop()) {
						return Lng.str("Shop Owner: Trading Guild (buy a shop module to create your own shop)");
					} else if(currentClosestShop.getShopOwners().isEmpty()) {
						return Lng.str("Nobody (you can take ownership)");
					} else {
						return Lng.str("Shop Owner%s: %s", (currentClosestShop.getShopOwners().size() > 1 ? "s" : ""), currentClosestShop.getShopOwners().toString());
					}
				}
				return Lng.str("Shop Owner: n/a");
			}
		});
		ownCredits.setColor(0.9f, 0.9f, 0.7f, 1.0f);
		ownCredits.getPos().x = UIScale.getUIScale().inset;
		ownCredits.getPos().y = UIScale.getUIScale().inset;
		shopInfoAncor.attach(ownCredits);
		shopCredits.getPos().x = UIScale.getUIScale().inset;
		shopCredits.getPos().y = UIScale.getUIScale().scale(4 + 26);
		shopInfoAncor.attach(shopCredits);
		shopOwner.getPos().x = UIScale.getUIScale().inset;
		shopOwner.getPos().y = UIScale.getUIScale().scale(4 + 26 + 21);
		shopInfoAncor.attach(shopOwner);
		shopCargo.getPos().x = UIScale.getUIScale().inset;
		shopCargo.getPos().y = UIScale.getUIScale().scale(4 + 26 + 21 + 21);
		shopInfoAncor.attach(shopCargo);
		GUIScrollablePanel pSInfo = new GUIScrollablePanel(10, 10, shopOptionsTab.getContent(0), getState());
		pSInfo.setContent(shopInfoAncor);
		shopOptionsTab.getContent(0).attach(pSInfo);
		shopOptionsTab.addNewTextBox(28 + 24 + 26 + 21);
		createShopOptionsButtonPanel(shopOptionsTab.getContent(1));
		shopOptionsTab.addNewTextBox(UIScale.getUIScale().scale(100));
		GUIAnchor optAnc = new GUIAnchor(getState());
		GUIDropDownList lPermShop = getShopPermissionDropDown();
		lPermShop.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().scale(5), 0);
		GUIDropDownList lPermTrade = getShopPermissionTradeDropDown();
		lPermTrade.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().scale(5 + 28), 0);
		GUITextOverlay ooShop = new GUITextOverlay(FontSize.SMALL_15, getState());
		ooShop.setTextSimple(new Object() {
			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop.getPermissionToPurchase() == TradeManager.PERM_ALL) {
					return Lng.str("Current Permission: Everyone has permission to buy and sell");
				} else if(currentClosestShop.getPermissionToPurchase() == TradeManager.PERM_ALL_BUT_ENEMY) {
					return Lng.str("Current Permission: Only enemies don't have permission to buy and sell");
				} else if(currentClosestShop.getPermissionToPurchase() == TradeManager.PERM_ALLIES_AND_FACTION) {
					return Lng.str("Current Permission: Only allies have permission to buy and sell");
				} else if(currentClosestShop.getPermissionToPurchase() == TradeManager.PERM_FACTION) {
					return Lng.str("Current Permission: Only faction has permission to buy and sell");
				}
				return Lng.str("n/a");
			}
		});
		ooShop.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().scale(28 + 28 + 5), 0);
		GUITextOverlay ooTrade = new GUITextOverlay(FontSize.SMALL_15, getState());
		ooTrade.setTextSimple(new Object() {
			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				long permissionToTrade = currentClosestShop.getPermissionToTrade();
				if(permissionToTrade == TradeManager.NONE) {
					return Lng.str("Trade Permission: Owner can take/put blocks and can trade here.");
				} else if(permissionToTrade == TradeManager.PERM_FACTION) {
					return Lng.str("Trade Permission: Faction can take/put blocks and can trade here.");
				} else if(permissionToTrade == TradeManager.PERM_ALLIES_AND_FACTION) {
					return Lng.str("Trade Permission: Faction and Allies can take/put blocks and can trade here.");
				} else if(permissionToTrade == TradeManager.PERM_ALL) {
					return Lng.str("Trade Permission: WARNING: Everyone can trade and put/take blocks.");
				}
				return Lng.str("n/a");
			}
		});
		ooTrade.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().scale(28 + 28 + 25 + 5), 0);
		optAnc.attach(ooShop);
		optAnc.attach(ooTrade);
		optAnc.attach(lPermShop);
		optAnc.attach(lPermTrade);
		GUIScrollablePanel p = new GUIScrollablePanel(10, 10, shopOptionsTab.getContent(2), getState());
		p.setContent(optAnc);
		shopOptionsTab.getContent(2).attach(p);
	}

	private GUIDropDownList getShopPermissionTradeDropDown() {
		int permissionWidth = 400;
		int permissionHeight = 20;
		GUITextOverlay[] p = new GUITextOverlay[4];
		p[0] = new GUITextOverlay(getState());
		p[0].setTextSimple(Lng.str("OWNER ONLY"));
		p[0].setUserPointer(TradeManager.NONE);
		p[1] = new GUITextOverlay(getState());
		p[1].setTextSimple(Lng.str("FACTION OWNED SHOP"));
		p[1].setUserPointer(TradeManager.PERM_FACTION);
		p[2] = new GUITextOverlay(getState());
		p[2].setTextSimple(Lng.str("ALLIED OWNED SHOP"));
		p[2].setUserPointer(TradeManager.PERM_ALLIES_AND_FACTION);
		p[3] = new GUITextOverlay(getState());
		p[3].setTextSimple(Lng.str("OWNED BY EVERYONE"));
		p[3].setUserPointer(TradeManager.PERM_ALL);
		GUIAnchor[] pA = new GUIAnchor[p.length];
		for(int i = 0; i < pA.length; i++) {
			pA[i] = new GUIAnchor(getState(), 100, permissionHeight);
			pA[i].setUserPointer(p[i].getUserPointer());
			pA[i].attach(p[i]);
			p[i].setPos(3, 3, 0);
		}
		GUIDropDownList permissionTrade = new GUIDropDownList(getState(), permissionWidth, permissionHeight, p.length * permissionHeight + 5, new DropDownCallback() {
			private boolean first = true;

			@Override
			public void onSelectionChanged(GUIListElement element) {
				long p = (Long) element.getContent().getUserPointer();
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(!isOwnShop()) {
					// ((GameClientState)getState()).getController().popupAlertTextMessage("Permission Denied", 0);
				} else {
					if(first) {
						first = false;
					} else {
						currentClosestShop.getShoppingAddOn().clientRequestTradePermission(getState().getPlayer(), p);
					}
				}
			}
		}, pA) {
			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIDropDownList#draw()
			 */
			@Override
			public void draw() {
				if(isOwnShop()) {
					super.draw();
				}
			}
		};
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		if(!isOwnShop()) {
		} else {
			for(int i = 0; i < permissionTrade.getList().size(); i++) {
				if((Long) permissionTrade.getList().get(i).getContent().getUserPointer() == currentClosestShop.getPermissionToTrade()) {
					permissionTrade.setSelectedElement(permissionTrade.getList().get(i));
				}
			}
		}
		return permissionTrade;
	}

	private GUIDropDownList getShopPermissionDropDown() {
		int permissionWidth = 400;
		int permissionHeight = 20;
		GUITextOverlay[] p = new GUITextOverlay[4];
		p[0] = new GUITextOverlay(getState());
		p[0].setTextSimple(Lng.str("ALL CAN SHOP"));
		p[0].setUserPointer(TradeManager.PERM_ALL);
		p[1] = new GUITextOverlay(getState());
		p[1].setTextSimple(Lng.str("NO ENEMIES CAN SHOP"));
		p[1].setUserPointer(TradeManager.PERM_ALL_BUT_ENEMY);
		p[2] = new GUITextOverlay(getState());
		p[2].setTextSimple(Lng.str("ALLIES CAN SHOP"));
		p[2].setUserPointer(TradeManager.PERM_ALLIES_AND_FACTION);
		p[3] = new GUITextOverlay(getState());
		p[3].setTextSimple(Lng.str("FACTION CAN SHOP"));
		p[3].setUserPointer(TradeManager.PERM_FACTION);
		GUIAnchor[] pA = new GUIAnchor[p.length];
		for(int i = 0; i < pA.length; i++) {
			pA[i] = new GUIAnchor(getState(), 100, permissionHeight);
			pA[i].setUserPointer(p[i].getUserPointer());
			pA[i].attach(p[i]);
			p[i].setPos(3, 3, 0);
		}
		GUIDropDownList permission = new GUIDropDownList(getState(), permissionWidth, permissionHeight, p.length * permissionHeight + 5, new DropDownCallback() {
			boolean first = true;

			@Override
			public void onSelectionChanged(GUIListElement element) {
				long p = (Long) element.getContent().getUserPointer();
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(!isOwnShop()) {
					// ((GameClientState)getState()).getController().popupAlertTextMessage("Permission Denied", 0);
				} else {
					if(first) {
						first = false;
					} else {
						currentClosestShop.getShoppingAddOn().clientRequestLocalPermission(getState().getPlayer(), p);
					}
				}
			}
		}, pA) {
			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIDropDownList#draw()
			 */
			@Override
			public void draw() {
				if(isOwnShop()) {
					super.draw();
				}
			}
		};
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		if(!isOwnShop()) {
		} else {
			for(int i = 0; i < permission.getList().size(); i++) {
				if((Long) permission.getList().get(i).getContent().getUserPointer() == currentClosestShop.getPermissionToPurchase()) {
					permission.setSelectedElement(permission.getList().get(i));
				}
			}
		}
		return permission;
	}

	private void createBlockDescriptionPanel(GUIAnchor attachTo) {
		GUIScrollablePanel p = new GUIScrollablePanel(10, 10, attachTo, getState());
		final int addHeight = 32;
		GUIAnchor a = new GUIAnchor(getState(), 450, 200);
		p.setContent(a);
		GUITextOverlay blockName = new GUITextOverlay(FontSize.BIG_20, getState()) {
			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#draw()
			 */
			@Override
			public void draw() {
				super.draw();
				if(isOwnShop()) {
					setPos(36, 8 + addHeight, 0);
				} else {
					setPos(36, 8, 0);
				}
			}
		};
		blockName.setTextSimple(new Object() {
			String lastCache;
			short lastSel;

			@Override
			public String toString() {
				if(lastSel != getShopControlManager().getSelectedElementClass() || lastCache == null) {
					if(ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
						lastCache = ElementKeyMap.getInfo(getShopControlManager().getSelectedElementClass()).getName();
					} else if(getShopControlManager().getSelectedElementClass() == InventorySlot.MULTI_SLOT) {
						lastCache = Lng.str("Multi-Slot");
					} else {
						lastCache = "";
					}
					lastSel = getShopControlManager().getSelectedElementClass();
				}
				return lastCache;
			}
		});
		GUITextOverlay sellPrice = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		sellPrice.setTextSimple(new Object() {
			@Override
			public String toString() {
				if(isOwnShop()) {
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					if(currentClosestShop != null && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
						int price = currentClosestShop.getPriceString(ElementKeyMap.getInfo(getShopControlManager().getSelectedElementClass()), false);
						return price > 0 ? "Shop owner buying this block for " + StringTools.formatSmallAndBig(price) : "Can't sell";
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
		});
		GUITextOverlay buyPrice = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		buyPrice.setTextSimple(new Object() {
			@Override
			public String toString() {
				if(isOwnShop()) {
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					if(currentClosestShop != null && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
						int price = currentClosestShop.getPriceString(ElementKeyMap.getInfo(getShopControlManager().getSelectedElementClass()), true);
						return price > 0 ? "Shop owner selling this block for " + StringTools.formatSmallAndBig(price) : "Can't buy";
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
		});
		GUITextOverlay stockAmount = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		stockAmount.setTextSimple(new Object() {
			@Override
			public String toString() {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
					int stock = currentClosestShop.getShopInventory().getOverallQuantity(getShopControlManager().getSelectedElementClass());
					return stock > 0 ? "STOCK: " + StringTools.formatSeperated(stock) : "Empty";
				} else {
					return "";
				}
			}
		});
		sellPrice.setPos(1, 1, 0);
		buyPrice.setPos(1, 18, 0);
		stockAmount.setPos(300, 9, 0);
		GUIBlockSprite b = new GUIBlockSprite(getState(), (short) 0) {
			@Override
			public void draw() {
				if(ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
					type = getShopControlManager().getSelectedElementClass();
					super.draw();
					setScale(0.5f, 0.5f, 0f);
					if(isOwnShop()) {
						setPos(1, 1 + addHeight, 0);
					} else {
						setPos(1, 1, 0);
					}
				}
			}
		};
		final GUITextOverlay desc = new GUITextOverlay(FontSize.SMALL_14, getState()) {
			@Override
			public void draw() {
				if(ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass()) || getShopControlManager().getSelectedElementClass() == InventorySlot.MULTI_SLOT) {
					setPos(4, 36, 0);
				} else {
					setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				}
				if(isOwnShop()) {
					getPos().y += addHeight;
				}
				super.draw();
			}
		};
		desc.setTextSimple(new Object() {
			String lastCache;
			short lastSel;
			float lastParentSize = 0.0F;

			@Override
			public String toString() {
				try {
					if(lastSel != getShopControlManager().getSelectedElementClass() || lastCache == null || lastParentSize != ((GUIScrollablePanel) desc.getParent().getParent()).getWidth()) {
						lastParentSize = ((GUIScrollablePanel) desc.getParent().getParent()).getWidth();
						if(ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
							String[] parseDescription = ElementKeyMap.getInfo(getShopControlManager().getSelectedElementClass()).parseDescription(getState());
							StringBuilder f = new StringBuilder();
							for(int i = 0; i < parseDescription.length; i++) {
								String line = parseDescription[i];
								String[] splitLines = line.split(" ");
								String currentLine = "";
								for(String str : splitLines) {
									str += " ";
									if(desc.getFont().getWidth(currentLine + str) >= lastParentSize) {
										f.append(currentLine);
										f.append("\n");
										currentLine = str;
									} else {
										currentLine += str;
									}
								}
								f.append(currentLine);
								f.append("\n");
							}
							lastCache = f.toString();
						} else if(getShopControlManager().getSelectedElementClass() == InventorySlot.MULTI_SLOT) {
							lastCache = Lng.str("This is a Multi-Slot. It contains multiple shapes of the same item.\nYou can put it in your hotbar and select a specific shape with the mouse wheel\nor right click it and take a specific one.");
						} else {
							lastCache = Lng.str("Choose an item from the left panel.");
						}
						lastSel = getShopControlManager().getSelectedElementClass();
					}
					return lastCache;
				} catch(Exception r) {
					return "error";
				}
			}
		});
		a.attach(b);
		a.attach(blockName);
		a.attach(desc);
		a.attach(sellPrice);
		a.attach(buyPrice);
		a.attach(stockAmount);
		attachTo.attach(p);
	}

	private void rebootShip() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().popupShipRebootBuyDialog();
	}

	// private void repairArmor() {
	// getState().getGlobalGameControlManager().getIngameControlManager()
	// .getPlayerGameControlManager().getPlayerIntercationManager()
	// .getInShipControlManager().popupShipArmorRepairBuyDialog();
	// }
	private void createShopRepairsButtonPanel(GUIAnchor attachTo) {
		GUIHorizontalButtonTablePane optionButtons = new GUIHorizontalButtonTablePane(getState(), 1, 2, attachTo);
		optionButtons.onInit();
		optionButtons.addButton(0, 0, Lng.str("Repair Systems (Reboot)"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(683);
					rebootShip();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getState().getShip() != null && !getState().getShip().getHpController().isRebooting() && getState().getShip().getHpController().getHpPercent() < 1d;
			}
		});
		// optionButtons.addButton(0, 1, Lng.str("Repair Armor"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
		// @Override
		// public boolean isOccluded() {
		// return !isActive();
		// }
		//
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if (event.pressedLeftMouse()) {
		// repairArmor();
		// }
		// }
		// }, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		//
		// @Override
		// public boolean isActive(InputState state) {
		// return getState().getShip() != null && !getState().getShip().getHpController().isRebooting() && getState().getShip().getHpController().getArmorHpPercent() < 1d;
		// }
		// });
		attachTo.attach(optionButtons);
	}

	private void createShopOptionsButtonPanel(GUIAnchor attachTo) {
		GUIHorizontalButtonTablePane optionButtons = new GUIHorizontalButtonTablePane(getState(), 2, 3, attachTo);
		optionButtons.onInit();
		optionButtons.addButton(0, 0, Lng.str("DEPOSIT CREDITS (+)"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					popupDeposit(getState(), Lng.str("How much?"));
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return isShopTrader();
			}
		});
		optionButtons.addButton(1, 0, Lng.str("WITHDRAW CREDITS (-)"), GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					popupWithdrawal();
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return isShopTrader();
			}
		});
		optionButtons.addButton(0, 1, Lng.str("ADD SHOP OWNER"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					popupAddShopOwner();
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return isOwnShop();
			}
		});
		optionButtons.addButton(1, 1, Lng.str("REMOVE SHOP OWNER"), GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					popupRemoveShopOwner();
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return isOwnShop();
			}
		});
		final SegmentController segmentController = currentShop.getSegmentController();
		final StashInventory inventory = currentShop.getShopInventory();
		if(!(segmentController instanceof ManagedSegmentController<?>)) {
			return;
		}
		long shopBlockIndex = ((ManagedSegmentController<?>) segmentController).getManagerContainer().shopBlockIndex;
		if(shopBlockIndex == Long.MIN_VALUE) {
			return;
		}
		final SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(shopBlockIndex);
		if(pointUnsave == null) {
			return;
		}
		optionButtons.addButton(0, 2, new Object() {
			@Override
			public String toString() {
				pointUnsave.refresh();
				inventoryActive = pointUnsave.isActive();
				return inventoryActive ? Lng.str("Deactivate Storage Auto-Pull") : Lng.str("Activate Storage Auto-Pull");
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(684);
					pointUnsave.refresh();
					long index = ElementCollection.getEncodeActivation(pointUnsave, true, !pointUnsave.isActive(), false);
					pointUnsave.getSegment().getSegmentController().sendBlockActivation(index);
				}
			}

			@Override
			public boolean isOccluded() {
				return !ShopPanelNew.this.isActive();
			}
		}, new GUIActivationHighlightCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return isShopTrader();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return inventoryActive;
			}
		});
		optionButtons.addButton(1, 2, Lng.str("Change Items to Auto-Pull"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !ShopPanelNew.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					InventoryOptionsChestButtonPanel.popupProductionDialog(getState(), segmentController, inventory, pointUnsave);
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return isShopTrader();
			}
		});
		attachTo.attach(optionButtons);
		GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), Lng.str("Pull Tick"), attachTo) {
			@Override
			public float getValue() {
				if(inventoryActive) {
					text = Lng.str("Pull Tick");
					double serverRunningTime = ShopPanelNew.this.getState().getController().getServerRunningTime() % ManagerContainer.TIME_STEP_STASH_PULL;
					float t = (float) (serverRunningTime / ManagerContainer.TIME_STEP_STASH_PULL);
					return t;
				} else {
					text = Lng.str("Item Auto-Pull Inactive");
					return 0;
				}
			}
		};
		progress.getColor().set(InventoryPanelNew.PROGRESS_COLOR);
		progress.onInit();
		progress.getPos().x = 1;
		progress.getPos().y = 2 + 24 + 24 + 24;
		attachTo.attach(progress);
	}

	// Can player take or buy blocks from shop
	private boolean canBuyOne() {
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		return isPurchasePermission(getState()) && currentClosestShop != null && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass()) && (currentClosestShop.getShopInventory().getOverallQuantity(getShopControlManager().getSelectedElementClass()) > 0 || currentClosestShop.isInfiniteSupply()) && (isOwnPlayerOwner(currentClosestShop) || currentClosestShop.getShoppingAddOn().canAfford(getState().getPlayer(), getShopControlManager().getSelectedElementClass(), 1) > 0);
	}

	// Can player put or sell blocks to shop
	private boolean canSellOne() {
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		// System.err.println("TESTS: "
		// + isPurchasePermission(getState()) + "; "
		// + (currentClosestShop != null) + "; "
		// + ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass()) + "; "
		// + isOwnPlayerOwner(currentClosestShop) + "; "
		// + getState().getPlayer().getInventory().getOverallQuantity(getShopControlManager().getSelectedElementClass()) + "; "
		// + currentClosestShop.getShopInventory().getOverallQuantity(getShopControlManager().getSelectedElementClass()) + "; "
		// + currentClosestShop.getShoppingAddOn().canShopAfford(getShopControlManager().getSelectedElementClass(), 1) + "; "
		// + currentClosestShop.getShoppingAddOn().canAfford(getState().getPlayer(), getShopControlManager().getSelectedElementClass(), 1));
		return isPurchasePermission(getState()) && currentClosestShop != null && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass()) && getState().getPlayer().getInventory().getOverallQuantity(getShopControlManager().getSelectedElementClass()) > 0 && (isOwnPlayerOwner(currentClosestShop) || currentClosestShop.getShoppingAddOn().canShopAfford(getShopControlManager().getSelectedElementClass(), 1) > 0);
	}

	private boolean isOwnPlayerOwner(ShopInterface currentClosestShop) {
		return currentClosestShop.getShopOwners().isEmpty() || currentClosestShop.getShopOwners().contains(getOwnPlayer().getName().toLowerCase(Locale.ENGLISH));
	}

	private void createBuySellButtonPanel(GUIAnchor attachTo) {
		GUIHorizontalButtonTablePane buySellButtons = new GUIHorizontalButtonTablePane(getState(), 2, 3, attachTo);
		buySellButtons.onInit();
		buySellButtons.addButton(0, 0, new Object() {
			@Override
			public String toString() {
				if(isShopTrader()) {
					return Lng.str("TAKE AS OWNER");
				} else {
					return Lng.str("BUY");
				}
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.BUY)*/
					AudioController.fireAudioEventID(685);
					buyOne(getShopControlManager().getSelectedElementClass());
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return canBuyOne();
			}
		});
		buySellButtons.addButton(1, 0, new Object() {
			@Override
			public String toString() {
				if(isShopTrader()) {
					return Lng.str("PUT IN AS OWNER");
				} else {
					return Lng.str("SELL");
				}
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(!getState().getPlayer().getInventory().isLockedInventory()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELL)*/
						AudioController.fireAudioEventID(687);
						sellOne(getShopControlManager().getSelectedElementClass());
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
						AudioController.fireAudioEventID(686);
						getState().getController().popupAlertTextMessage(Lng.str("Can't sell from this inventory"), 0);
					}
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return canSellOne();
			}
		});
		buySellButtons.addButton(0, 1, new Object() {
			@Override
			public String toString() {
				if(isShopTrader()) {
					return Lng.str("TAKE QUANTITY AS OWNER");
				} else {
					return Lng.str("BUY AMOUNT");
				}
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					buyMore(getShopControlManager().getSelectedElementClass());
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return canBuyOne();
			}
		});
		buySellButtons.addButton(1, 1, new Object() {
			@Override
			public String toString() {
				if(isShopTrader()) {
					return Lng.str("PUT IN QUANTITY AS OWNER");
				} else {
					return Lng.str("SELL AMOUNT");
				}
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(!getState().getPlayer().getInventory().isLockedInventory()) {
						sellMore(getShopControlManager().getSelectedElementClass());
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("Can't sell from this inventory"), 0);
					}
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return canSellOne();
			}
		});
		buySellButtons.addButton(1, 2, new Object() {
			@Override
			public String toString() {
				return Lng.str("SET SALE PRICE");
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					short t = getShopControlManager().getSelectedElementClass();
					if(currentClosestShop != null && ElementKeyMap.isValidType(t)) {
						ElementInformation info = ElementKeyMap.getInfo(t);
						TradePriceInterface price = currentClosestShop.getPrice(info.getId(), false);
						editBuyPrice((int) ((price == null || price.getPrice() <= 0) ? info.getPrice(true) : price.getPrice()));
					}
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return isShopTrader();
			}

			@Override
			public boolean isActive(InputState state) {
				return isShopTrader() && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass());
			}
		});
		buySellButtons.addButton(0, 2, new Object() {
			@Override
			public String toString() {
				return Lng.str("SET PURCHASE PRICE");
			}
		}, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					short t = getShopControlManager().getSelectedElementClass();
					if(currentClosestShop != null && ElementKeyMap.isValidType(t)) {
						ElementInformation info = ElementKeyMap.getInfo(t);
						TradePriceInterface price = currentClosestShop.getPrice(info.getId(), true);
						editSellPrice((int) ((price == null || price.getPrice() <= 0) ? info.getPrice(true) : price.getPrice()));
					}
				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return isShopTrader();
			}

			@Override
			public boolean isActive(InputState state) {
				return isShopTrader() && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass());
			}
		});
		attachTo.attach(buySellButtons);
	}

	protected void editBuyPrice(int currentBuyAmount) {
		ObjectArrayList<GUIElement> extra = new ObjectArrayList<GUIElement>();
		GUIAnchor creditsAnchor = new GUIAnchor(getState(), 100, 32);
		GUITextOverlay credistText = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		credistText.setTextSimple(Lng.str("CREDITS"));
		credistText.setPos(8, 8, 0);
		creditsAnchor.attach(credistText);
		creditsAnchor.setUserPointer("credits");
		extra.add(creditsAnchor);
		(new PlayerBlockTypeDropdownInputNew("EditShopPriceDialog_AMOUNT", getState(), Lng.str("Edit Sale Price"), extra, 1, currentBuyAmount, false) {
			@Override
			public void onOk(ElementInformation info) {
				getState().getController().popupAlertTextMessage(Lng.str("It's currently now allowed to set block as price"), 0);
			}

			@Override
			public void onAdditionalElementOk(Object userPointer) {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
					TradePrice p = ShoppingAddOn.getPriceInstance(currentClosestShop, getShopControlManager().getSelectedElementClass(), false);
					p.setPrice(getNumberValue());
					currentClosestShop.getShoppingAddOn().clientRequestSetPrice(getState().getPlayer(), p);
				}
			}

			@Override
			public void onOkMeta(MetaObject object) {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(688);
	}

	protected void editSellPrice(int currentSellAmount) {
		ObjectArrayList<GUIElement> extra = new ObjectArrayList<GUIElement>();
		GUIAnchor creditsAnchor = new GUIAnchor(getState(), 100, 32);
		GUITextOverlay credistText = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		credistText.setTextSimple(Lng.str("CREDITS"));
		credistText.setPos(8, 8, 0);
		creditsAnchor.attach(credistText);
		creditsAnchor.setUserPointer("credits");
		extra.add(creditsAnchor);
		(new PlayerBlockTypeDropdownInputNew("EditShopPriceDialog_AMOUNT", getState(), Lng.str("Edit Purchase Price"), extra, 1, currentSellAmount, false) {
			@Override
			public void onOk(ElementInformation info) {
				getState().getController().popupAlertTextMessage(Lng.str("It's currently now allowed to set block as price"), 0);
			}

			@Override
			public void onAdditionalElementOk(Object userPointer) {
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(currentClosestShop != null && ElementKeyMap.isValidType(getShopControlManager().getSelectedElementClass())) {
					TradePrice p = ShoppingAddOn.getPriceInstance(currentClosestShop, getShopControlManager().getSelectedElementClass(), true);
					p.setPrice(getNumberValue());
					currentClosestShop.getShoppingAddOn().clientRequestSetPrice(getState().getPlayer(), p);
				}
			}

			@Override
			public void onOkMeta(MetaObject object) {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(689);
	}

	public boolean isOwnShop() {
		return isOwnShop(getState());
	}

	// Includes shop ownership
	private boolean isShopTrader() {
		return isShopTrader(getState());
	}

	public static boolean isShopTrader(GameClientState state) {
		return ShoppingAddOn.isTradePermShop(state, state.getCurrentClosestShop());
	}

	public static boolean isOwnShop(GameClientState state) {
		return ShoppingAddOn.isSelfOwnedShop(state, state.getCurrentClosestShop());
	}

	private static boolean isPurchasePermission(GameClientState state) {
		return ShoppingAddOn.isPurchasePermission(state.getPlayer(), state.getCurrentClosestShop());
	}

	public static void popupDeposit(final GameClientState state, String desc) {
		(new PlayerGameTextInput("ENTER_NAME", state, 10, Lng.str("Deposit Credits (+)"), desc) {
			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				if(entry.length() < 1) {
					return false;
				}
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(!isShopTrader(state)) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!"), 0);
				} else {
					try {
						long amount = Long.parseLong(entry.trim());
						int a = (int) Math.min(amount, Integer.MAX_VALUE);
						if(getState().getPlayer().getCredits() < amount) {
							state.getController().popupAlertTextMessage(Lng.str("You don't have enough credits"), 0);
							setText(String.valueOf(getState().getPlayer().getCredits()));
							return false;
						}
						currentClosestShop.getShoppingAddOn().clientRequestDeposit(getState().getPlayer(), a);
					} catch(NumberFormatException e) {
						e.printStackTrace();
						state.getController().popupAlertTextMessage(Lng.str("Please enter a valid number"), 0);
						return false;
					}
				}
				return true;
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(690);
	}

	public void popupWithdrawal() {
		(new PlayerGameTextInput("ENTER_NAME", getState(), 10, Lng.str("Withdrawal Credits (-)"), Lng.str("How much?")) {
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
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				if(entry.length() < 1) {
					return false;
				}
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(!isShopTrader()) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!"), 0);
				} else {
					try {
						long amount = Long.parseLong(entry.trim());
						int a = (int) Math.min(amount, Integer.MAX_VALUE);
						if(currentClosestShop.getShoppingAddOn().getCredits() < amount) {
							getState().getController().popupAlertTextMessage(Lng.str("Shop doesn't have enough credits"), 0);
							setText(String.valueOf(currentClosestShop.getShoppingAddOn().getCredits()));
							return false;
						}
						currentClosestShop.getShoppingAddOn().clientRequestWithdrawal(getState().getPlayer(), a);
					} catch(NumberFormatException e) {
						e.printStackTrace();
						getState().getController().popupAlertTextMessage(Lng.str("Please enter a valid number"), 0);
						return false;
					}
				}
				return true;
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(691);
	}

	public void popupAddShopOwner() {
		(new PlayerGameTextInput("ENTER_NAME", getState(), 80, Lng.str("Add Owner"), Lng.str("Type the name of the player.")) {
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
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				if(entry.length() < 3) {
					return false;
				}
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(!isOwnShop()) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!"), 0);
				} else {
					currentClosestShop.getShoppingAddOn().clientRequestPlayerAdd(getState().getPlayer(), entry.trim());
				}
				return true;
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(692);
	}

	public void popupRemoveShopOwner() {
		(new PlayerGameTextInput("ENTER_NAME", getState(), 80, Lng.str("Remove Owner"), Lng.str("Type the name of the player.")) {
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
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				if(entry.length() < 3) {
					return false;
				}
				ShopInterface currentClosestShop = getState().getCurrentClosestShop();
				if(!isOwnShop()) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!"), 0);
				} else {
					currentClosestShop.getShoppingAddOn().clientRequestPlayerRemove(getState().getPlayer(), entry.trim());
				}
				return true;
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(693);
	}

	public ShopControllerManager getShopControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}

	private void buyOne(short type) {
		if(getShopControlManager().canBuy(type)) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.SHOP, AudioTags.BUY)*/
			AudioController.fireAudioEventID(694);
			getState().getPlayer().getInventoryController().buy(type, 1);
		}
	}

	private void sellOne(short type) {
		if(getShopControlManager().canSell(type)) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.SHOP, AudioTags.SELL)*/
			AudioController.fireAudioEventID(695);
			getState().getPlayer().getInventoryController().sell(type, 1);
		}
	}

	private void buyMore(short type) {
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		if(currentClosestShop != null) {
			String title = ShoppingAddOn.isSelfOwnedShop(getState(), currentClosestShop) ? Lng.str("Take Quantity") : Lng.str("Buy Quantity");
			synchronized(getState().getController().getPlayerInputs()) {
				getState().getController().getPlayerInputs().add(new BuyQuantityDialog(getState(), title, type, 1, currentClosestShop));
			}
		}
	}

	private void sellMore(short type) {
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		if(currentClosestShop != null) {
			getShopControlManager().openSellDialog(type, 1, currentClosestShop);
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: shop no longer  in range!"), 0);
		}
	}

	public PlayerState getOwnPlayer() {
		return ShopPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return ShopPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return shopPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return shopPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void reset() {
		if(shopPanel != null) {
			shopPanel.reset();
		}
	}

	@Override
	public void checkTarget(MouseEvent e) {
		if(e.releasedLeftMouse()) {
			// System.err.println("CHECKING "+this+" MOUSE NO MORE GRABBED");
			if(getState().getController().getInputController().getDragging() != null && isTarget(getState().getController().getInputController().getDragging()) && (getState().getController().getInputController().getDragging() != this)) {
				if((System.currentTimeMillis() - getState().getController().getInputController().getDragging().getTimeDragStarted()) > Draggable.MIN_DRAG_TIME) {
					onDrop((InventorySlotOverlayElement) getState().getController().getInputController().getDragging());
				} else {
				}
				getState().getController().getInputController().setDragging(null);
			}
			if(getState().getController().getInputController().getDragging() != null && (getState().getController().getInputController().getDragging() == this)) {
			}
			if(getState().getController().getInputController().getDragging() != null && (!isTarget(getState().getController().getInputController().getDragging()))) {
				System.err.println("NO DROP: not a target: " + this);
			}
		}
	}

	@Override
	public boolean isTarget(Draggable draggable) {
		return true;
	}

	@Override
	public void onDrop(final InventorySlotOverlayElement draggable) {
		draggable.setStickyDrag(false);
		ShopInterface currentClosestShop = getState().getCurrentClosestShop();
		if(currentClosestShop != null) {
			if(draggable.getType() > 0) {
				if(!getState().getPlayer().getInventory().isLockedInventory()) {
					getShopControlManager().openSellDialog(draggable.getType(), draggable.getCount(true), currentClosestShop);
				}
			} else if(draggable.getType() < 0) {
				getState().getController().popupInfoTextMessage(Lng.str("Item cannot be\nsold (yet)!"), 0);
			}
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR: Not in shop range!"), 0);
		}
		draggable.reset();
	}
}
