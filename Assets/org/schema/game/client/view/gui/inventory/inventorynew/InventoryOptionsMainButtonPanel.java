package org.schema.game.client.view.gui.inventory.inventorynew;

import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement;
import org.schema.game.client.view.gui.inventory.InventoryToolInterface;
import org.schema.game.client.view.gui.shiphud.newhud.BottomBarBuild;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.Draggable;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonExpandable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class InventoryOptionsMainButtonPanel extends GUIAnchor implements InventoryToolInterface {

	private String inventoryFilterText = "";

	private GUIElement panel;

	private InventoryPanelNew mainPanel;

	private boolean hasDropPanel;

	private GUIHorizontalButtonTablePane acNormal;

	private GUIHorizontalButtonTablePane acCreative;

	private InventoryFilterBar searchBar;

	public InventoryOptionsMainButtonPanel(InputState state, GUIElement panel, InventoryPanelNew mainPanel, boolean hasDropPanel) {
		super(state);
		this.panel = panel;
		this.mainPanel = mainPanel;
		this.hasDropPanel = hasDropPanel;
	}

	public PlayerState getOwnPlayer() {
		return this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return this.getState().getFaction();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	private boolean dropActive(boolean precondition) {
		Draggable dragging = getState().getController().getInputController().getDragging();
		if (!(dragging instanceof InventorySlotOverlayElement)) {
			return false;
		} else {
			final InventorySlotOverlayElement draggable = (InventorySlotOverlayElement) getState().getController().getInputController().getDragging();
			GUIElement superior = draggable.superior;
			if (superior != null && (superior instanceof BottomBarBuild || superior == mainPanel.getMainInventory())) {
				return precondition;
			}
		}
		return false;
	}

	public GUIHorizontalButtonTablePane getMainButtons(boolean creative) {
		GUIHorizontalButtonTablePane ac = new GUIHorizontalButtonTablePane(getState(), creative ? 3 : 2, 1, this);
		ac.onInit();
		GUIHorizontalButtonExpandable craft = new GUIHorizontalButtonExpandable(getState(), HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("Craft"), ac.activeInterface);
		ac.addButton(craft, 0, 0);
		craft.addButton(Lng.str("Refine Raw Materials"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					mainPanel.openCapsuleRefineryOpen();
				}
			}

			@Override
			public boolean isOccluded() {
				return !panel.isActive() || mainPanel.isCapsuleRefineryOpen();
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !mainPanel.isCapsuleRefineryOpen();
			}
		});
		craft.addButton(Lng.str("Craft Generic Resources"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive() || mainPanel.isMicroFactoryOpen();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					mainPanel.openMicroFactoryOpen();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !mainPanel.isMicroFactoryOpen();
			}
		});
		craft.addButton(Lng.str("Craft Basic Factory"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive() || mainPanel.isMacroFactioryBlockFactoryOpen();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					mainPanel.openMacroFactioryBlockFactoryOpen();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !mainPanel.isMacroFactioryBlockFactoryOpen();
			}
		});
		ac.addButton(1, 0, Lng.str("Cargo"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(526);
					getState().getPlayer().requestUseCargoInventory(!getState().getPlayer().isUseCargoInventory());
				}
			}

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getState().getPlayer().isUseCargoInventory();
			}
		});
		if (creative) {
			ac.addButton(2, 0, Lng.str("Creative Mode"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(527);
						getState().getPlayer().setUseCreativeMode(!getState().getPlayer().isUseCreativeMode());
					}
				}

				@Override
				public boolean isOccluded() {
					return !panel.isActive();
				}
			}, new GUIActivationHighlightCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}

				@Override
				public boolean isHighlighted(InputState state) {
					return getState().getPlayer().isCreativeModeEnabled();
				}
			});
		}
		ac.getPos().x = UIScale.getUIScale().scale(1);
		ac.getPos().y = UIScale.getUIScale().scale(1);
		return ac;
	}

	@Override
	public void draw() {
		detach(acNormal);
		detach(acCreative);
		if (getState().getPlayer().isHasCreativeMode()) {
			attach(acCreative);
		} else {
			attach(acNormal);
		}
		super.draw();
	}

	@Override
	public void onInit() {
		acNormal = getMainButtons(false);
		acCreative = getMainButtons(true);
		attach(acNormal);
		this.searchBar = new InventoryFilterBar(getState(), Lng.str("FILTER BY BLOCK NAME"), this, new TextCallback() {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
			}

			@Override
			public void newLine() {
			}
		}, t -> {
			// Only reset scroll if you changed the search text
			if (!inventoryFilterText.equals(t)) {
				mainPanel.resetInvScroll();
			}
			inventoryFilterText = t;
			return t;
		});
		this.searchBar.getPos().x = UIScale.getUIScale().scale(1);
		this.searchBar.getPos().y = UIScale.getUIScale().scale(25);
		this.searchBar.leftDependentHalf = true;
		this.searchBar.onInit();
		attach(this.searchBar);
		CreditsPanel p = new CreditsPanel(getState(), this);
		p.rightDependentHalf = true;
		p.onInit();
		p.getPos().y = UIScale.getUIScale().scale(25);
		attach(p);
		if (hasDropPanel) {
			GUIHorizontalButtonTablePane dropButtons = new GUIHorizontalButtonTablePane(getState(), 2, 1, this);
			dropButtons.onInit();
			final GUIActivationCallback sellActive = new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return dropActive(getState().getCurrentClosestShop() != null);
				}
			};
			final GUIActivationCallback trashActive = new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return dropActive(true);
				}
			};
			dropButtons.addButton(0, 0, new Object() {

				@Override
				public String toString() {
					if (getState().getCurrentClosestShop() != null) {
						return Lng.str("DROP TO SELL");
					} else {
						return Lng.str("Drop to sell (no shop)");
					}
				}
			}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				private void onDrop(MouseEvent e) {
					// System.err.println("HHHH "+getState().getGuiCallbackController().blockingCount());
					Draggable dragging = getState().getController().getInputController().getDragging();
					if (dragging != null && dragging.checkDragReleasedMouseEvent(e)) {
						final InventorySlotOverlayElement draggable = (InventorySlotOverlayElement) getState().getController().getInputController().getDragging();
						if (!draggable.getDraggingInventory().isInfinite()) {
							ShopInterface currentClosestShop = getState().getCurrentClosestShop();
							if (currentClosestShop != null) {
								if (draggable.getType() > 0) {
									getShopControlManager().openSellDialog(draggable.getType(), draggable.getCount(true), currentClosestShop);
								} else {
									if (draggable.getType() == InventorySlot.MULTI_SLOT) {
										if (draggable.getSubSlotType() != Element.TYPE_NONE) {
											getShopControlManager().openSellDialog(draggable.getSubSlotType(), draggable.getCount(true), currentClosestShop);
										} else {
											getState().getController().popupAlertTextMessage(Lng.str("Cannot sell a whole stack at once (yet)\nRight click and split it first."), 0);
										}
									} else {
										getState().getController().popupInfoTextMessage(Lng.str("Item cannot be\nsold (yet)!"), 0);
									}
								}
							} else {
								getState().getController().popupAlertTextMessage(Lng.str("ERROR: Not in shop range!"), 0);
							}
						} else {
							getState().getController().popupAlertTextMessage(Lng.str("ERROR: Can't sell from creative mode!"), 0);
						}
						dragging.setStickyDrag(false);
						dragging.reset();
						getState().getController().getInputController().setDragging(null);
					}
				}

				@Override
				public boolean isOccluded() {
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					return currentClosestShop == null || !sellActive.isActive(getState());
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					onDrop(event);
				}
			}, sellActive);
			dropButtons.addButton(1, 0, new Object() {

				@Override
				public String toString() {
					return Lng.str("DROP TO TRASH");
				}
			}, HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

				private void onDrop(MouseEvent e) {
					Draggable dragging = getState().getController().getInputController().getDragging();
					if (dragging != null && dragging.checkDragReleasedMouseEvent(e)) {
						final InventorySlotOverlayElement draggable = (InventorySlotOverlayElement) getState().getController().getInputController().getDragging();
						if (draggable.getType() > 0) {
							getShopControlManager().openDeleteDialog(draggable.getSlot(), draggable.getType(), draggable.getCount(true), draggable.getDraggingInventory());
						} else {
							if (draggable.getType() == InventorySlot.MULTI_SLOT) {
								// if(draggable.getSubSlotType() != Element.TYPE_NONE){
								// getShopControlManager().openDeleteDialog(draggable.getSlot(), draggable.getType(), draggable.getCount(true));
								// }else{
								getState().getController().popupAlertTextMessage(Lng.str("Cannot delete a whole stack at once (yet)\nRight click and split it first."), 0);
							// }
							}
						}
						dragging.setStickyDrag(false);
						dragging.reset();
						getState().getController().getInputController().setDragging(null);
					}
				}

				@Override
				public boolean isOccluded() {
					return !trashActive.isActive(getState());
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					onDrop(event);
				}
			}, trashActive);
			dropButtons.getPos().x = UIScale.getUIScale().scale(1);
			dropButtons.getPos().y = UIScale.getUIScale().h + UIScale.getUIScale().h + UIScale.getUIScale().scale(1);
			attach(dropButtons);
			GUIHorizontalArea f = new GUIHorizontalArea(getState(), HButtonType.TEXT_FIELD, 10) {

				@Override
				public void draw() {
					setWidth(InventoryOptionsMainButtonPanel.this.getWidth());
					super.draw();
				}
			};
			f.getPos().x = UIScale.getUIScale().scale(1);
			f.getPos().y = UIScale.getUIScale().h + UIScale.getUIScale().h + UIScale.getUIScale().h + UIScale.getUIScale().scale(1);
			attach(f);
			GUIScrollablePanel guiScrollablePanel = new GUIScrollablePanel(UIScale.getUIScale().h, UIScale.getUIScale().h, this, getState());
			guiScrollablePanel.setScrollable(GUIScrollablePanel.SCROLLABLE_NONE);
			guiScrollablePanel.setLeftRightClipOnly = true;
			guiScrollablePanel.dependent = f;
			GUITextOverlay l = new GUITextOverlay(FontSize.MEDIUM_15, getState()) {

				@Override
				public void draw() {
					if (mainPanel.getMainInventory().getInventory().isOverCapacity()) {
						setColor(1, 0.3f, 0.3f, 1);
					} else {
						setColor(1, 1, 1, 1);
					}
					super.draw();
				}
			};
			;
			l.setTextSimple(new Object() {

				@Override
				public String toString() {
					if (mainPanel != null && mainPanel.getMainInventory() != null && mainPanel.getMainInventory().getInventory() != null) {
						return mainPanel.getMainInventory().getInventory().getVolumeString();
					} else {
						return Lng.str("n/a");
					}
				}
			});
			l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset);
			f.attach(l);
		}
	}

	@Override
	public String getText() {
		return inventoryFilterText;
	}

	@Override
	public boolean isActiveInventory(InventoryIconsNew inventoryIcons) {
		return mainPanel.isInventoryActive(inventoryIcons);
	}

	public PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	public InventoryControllerManager getInventoryControlManager() {
		return getPlayerGameControlManager().getInventoryControlManager();
	}

	public ShopControllerManager getShopControlManager() {
		return getPlayerGameControlManager().getShopControlManager();
	}

	@Override
	public void clearFilter() {
		searchBar.reset();
	}
}
