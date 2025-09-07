package org.schema.game.client.view.gui.inventory.inventorynew;

import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerInventoryInput;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.inventory.InventoryCallBack;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.player.inventory.NPCFactionInventory;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.player.inventory.PersonalFactoryInventory;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.network.objects.DragDrop;
import org.schema.game.network.objects.remote.RemoteDragDrop;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.Draggable;
import org.schema.schine.graphicsengine.forms.gui.DropTarget;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIExpandableButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InventoryPanelNew extends GUIElement implements GUIActiveInterface, GUIChangeListener, InventoryCallBack {

	public static final Vector4f PROGRESS_COLOR = new Vector4f(0.1f, 0.5f, 0.1f, 1.0f);

	private static int windowIdGen = 0;

	public GUIMainWindow inventoryPanel;

	private ObjectArrayList<InvWindow> otherInventories = new ObjectArrayList<InvWindow>();

	private InventoryIconsNew inventoryIcons;

	// private GUIContentPane personalTab;
	private boolean init;

	private boolean flagInventoryTabRecreate;

	private GUIContentPane availableTab;

	private boolean updateRequested;

	private InvWindow currentlyDrawing;

	private DropToSpaceAnchor dropToSpaceAnchor;

	private GUIScrollablePanel invScroll;

	// private GUIAncor inventoryAnchor;
	public InventoryPanelNew(InputState state) {
		super(state);
		getInventoryControlManager().addObserver(this);
	}

	@Override
	public void cleanUp() {
		getInventoryControlManager().deleteObserver(this);
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		// 
		// if(mainInventory.getInventory() != getState().getPlayer().getInventory()){
		// mainInventory.initialize(getState(), getState().getPlayer().getInventory());
		// //			inventoryAnchor.setHeight(getOwnPlayer().getInventory().getSlotsMax() <= 45 ? 500 : ((getOwnPlayer().getInventory().getSlotsMax()/7-1)*InventoryIcons.spacing));
		// }
		if (updateRequested) {
			if (getInventoryControlManager().getSecondInventory() == null) {
			} else {
				Object title;
				String invId;
				final Inventory inventory = getInventoryControlManager().getSecondInventory();
				// assert(false):inventory+"; "+(inventory instanceof StashInventory && !(inventory instanceof NPCFactionInventory));
				if (inventory instanceof StashInventory && !(inventory instanceof NPCFactionInventory)) {
					SegmentController s = ((ManagerContainer<?>) inventory.getInventoryHolder()).getSegmentController();
					final SegmentPiece pointUnsave = s.getSegmentBuffer().getPointUnsave(inventory.getParameter());
					if (pointUnsave == null) {
						flagInventoryTabRecreate = true;
						return;
					} else {
						final Vector3i absolutePos = pointUnsave.getAbsolutePos(new Vector3i());
						if (pointUnsave.getType() == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID) {
							title = new Object() {

								@Override
								public String toString() {
									if (inventory.getCustomName() != null && inventory.getCustomName().length() > 0) {
										return Lng.str("Micro Assembler: %s", inventory.getCustomName());
									} else {
										return Lng.str("Micro Assembler at %s", absolutePos.toStringPure());
									}
								}
							};
							invId = "STASHANDFACTORY";
						} else if (pointUnsave.getType() == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID) {
							title = new Object() {

								@Override
								public String toString() {
									if (inventory.getCustomName() != null && inventory.getCustomName().length() > 0) {
										return Lng.str("Capsule Refinery: %s", inventory.getCustomName());
									} else {
										return Lng.str("Capsule Refinery at %s", absolutePos.toStringPure());
									}
								}
							};
							invId = "STASHANDFACTORY";
						} else if (pointUnsave.getType() == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ADV_ID) {
							title = new Object() {

								@Override
								public String toString() {
									if (inventory.getCustomName() != null && inventory.getCustomName().length() > 0) {
										return Lng.str("Advanced Capsule Refinery: %s", inventory.getCustomName());
									} else {
										return Lng.str("Advanced Capsule Refinery at %s", absolutePos.toStringPure());
									}
								}
							};
							invId = "STASHANDFACTORY";
						}	else if (pointUnsave.getType() == ElementKeyMap.FACTORY_BLOCK_RECYCLER_ID) {
								title = new Object() {

									@Override
									public String toString() {
										if (inventory.getCustomName() != null && inventory.getCustomName().length() > 0) {
											return Lng.str("Block Recycler: %s", inventory.getCustomName());
										} else {
											return Lng.str("Block Recycler at %s", absolutePos.toStringPure());
										}
									}
								};
								invId = "STASHANDFACTORY";
						} else if (ElementKeyMap.getFactorykeyset().contains(pointUnsave.getType())) {
							title = new Object() {

								@Override
								public String toString() {
									if (inventory.getCustomName() != null && inventory.getCustomName().length() > 0) {
										return Lng.str("%s: %s", ElementKeyMap.getNameSave(pointUnsave.getType()), inventory.getCustomName());
									} else {
										return Lng.str("%s at %s", ElementKeyMap.getNameSave(pointUnsave.getType()), absolutePos.toStringPure());
									}
								}
							};
							invId = "STASHANDFACTORY";
						} else {
							title = new Object() {

								@Override
								public String toString() {
									if (inventory.getCustomName() != null && inventory.getCustomName().length() > 0) {
										return Lng.str("Stash: %s", inventory.getCustomName());
									} else {
										return Lng.str("Stash at at %s", absolutePos.toStringPure());
									}
								}
							};
							invId = "STASHANDFACTORY";
						}
					}
				} else {
					invId = "unknown element";
					title = "Unknown Inventory";
				}
				PlayerInventoryInput kk = new PlayerInventoryInput(invId, getState(), title, this, this, getInventoryControlManager().getSecondInventory(), this);
				kk.getInputPanel().onInit();
				otherInventories.add(new InvWindow(kk.getInputPanel(), kk.getInputPanel().getInventoryIcons()));
			}
			arrangeWindows();
			updateRequested = false;
		}
		if (flagInventoryTabRecreate) {
			recreateTabs();
			flagInventoryTabRecreate = false;
		}
		// inventoryPanel.draw();
		boolean found = false;
		for (int i = otherInventories.size() - 1; i >= 0; i--) {
			if (!found) {
				otherInventories.get(i).drawAnchor();
				if (otherInventories.get(i).anchorInside()) {
					for (MouseEvent e : getState().getController().getInputController().getMouseEvents()) {
						if (e.pressedLeftMouse()) {
							selectedWindow(otherInventories.get(i));
						}
					}
					// we cant select a lower window
					found = true;
				}
			} else {
				otherInventories.get(i).setAnchorInside(false);
			}
		}
		if (!found) {
			dropToSpaceAnchor.setInside(false);
			dropToSpaceAnchor.draw();
		} else {
		}
		for (int i = 0; i < otherInventories.size(); i++) {
			currentlyDrawing = otherInventories.get(i);
			otherInventories.get(i).draw();
		}
		currentlyDrawing = null;
	}

	@Override
	public void onInit() {
		if (inventoryPanel != null) {
			inventoryPanel.cleanUp();
		}
		inventoryPanel = new GUIMainWindow(getState(), 590, 550, "InventoryPanelNew");
		inventoryPanel.onInit();
		inventoryPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(529);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		this.dropToSpaceAnchor = new DropToSpaceAnchor(getState());
		recreateTabs();
		otherInventories.add(new InvWindow(inventoryPanel, inventoryIcons));
		init = true;
	}

	private void arrangeWindows() {
		if (otherInventories.size() == 1) {
			// inventoryPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
			boolean newPanel = inventoryPanel.savedSizeAndPosition.newPanel;
			inventoryPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
			if (newPanel) {
				inventoryPanel.getPos().x -= (int) (inventoryPanel.getWidth() / 2);
			}
		} else {
			int s = 24;
			// for(int i = 0; i < otherInventories.size(); i++){
			// if(otherInventories.get(i).drawable != inventoryPanel && !(otherInventories.get(i).inventoryIcons.getInventory() instanceof PersonalFactoryInventory)){
			// //at least one stash here now
			// break;
			// }
			// }
			for (int i = 0; i < otherInventories.size(); i++) {
				int dist = (int) (inventoryPanel.getPos().x + inventoryPanel.getWidth() + 10);
				boolean newPanel = otherInventories.get(i).isNewPanel();
				if (newPanel) {
					if (otherInventories.get(i).inventoryIcons == inventoryIcons) {
					// already aligned
					} else {
						otherInventories.get(i).getPos().x = dist;
						otherInventories.get(i).getPos().y = inventoryPanel.getPos().y;
						if (otherInventories.get(i).inventoryIcons.getInventory() instanceof PersonalFactoryInventory) {
							otherInventories.get(i).getPos().y += s;
							s += otherInventories.get(i).drawable.getHeight() + 20;
						}
					}
				} else {
					otherInventories.get(i).orientate(0);
				}
			}
			if (isCapsuleRefineryOpen() || isMacroFactioryBlockFactoryOpen() || isMicroFactoryOpen()) {
			} else {
				boolean newPanel = otherInventories.get(1).isNewPanel();
				otherInventories.get(1).orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
				if (newPanel) {
					otherInventories.get(1).getPos().x += otherInventories.get(0).drawable.getWidth() / 2;
				}
			}
		}
	}

	private void selectedWindow(InvWindow invWindow) {
		int iSel = otherInventories.indexOf(invWindow);
		if (iSel >= 0) {
			InvWindow last = otherInventories.get(otherInventories.size() - 1);
			otherInventories.set(otherInventories.size() - 1, invWindow);
			otherInventories.set(iSel, last);
		} else {
		// window may have been already remove from the closeAncore in InvInventory
		// it deligates to the close callback for input panels so the x doesnt have to be pressed twice
		}
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (inventoryPanel.getSelectedTab() < inventoryPanel.getTabs().size()) {
			beforeTab = inventoryPanel.getTabs().get(inventoryPanel.getSelectedTab()).getTabName();
		}
		inventoryPanel.clearTabs();
		// personalTab = inventoryPanel.addTab(Lng.str("OWN");
		availableTab = inventoryPanel.addTab(Lng.str("INVENTORY"));
		// createPersonalInventoryPane();
		createMainInventoryPane();
		inventoryPanel.activeInterface = this;
		if (beforeTab != null) {
			for (int i = 0; i < inventoryPanel.getTabs().size(); i++) {
				if (inventoryPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					inventoryPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	@Override
	public void update(Timer timer) {
	}

	public void createMainInventoryPane() {
		if (inventoryIcons != null) {
			inventoryIcons.cleanUp();
		}
		InventoryOptionsMainButtonPanel tools = new InventoryOptionsMainButtonPanel(getState(), this, this, true);
		tools.onInit();
		availableTab.setContent(0, tools);
		availableTab.setTextBoxHeightLast(UIScale.getUIScale().scale(28 + 24 + 24 + 24));
		// dynamic
		availableTab.addNewTextBox(UIScale.getUIScale().scale(1));
		invScroll = new GUIScrollablePanel(10, 10, availableTab.getContent(1), getState());
		invScroll.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		invScroll.setContent(inventoryIcons = new InventoryIconsNew(getState(), invScroll, () -> getOwnPlayer().getInventory(), this, tools));
		invScroll.onInit();
		availableTab.setListDetailMode(availableTab.getTextboxes().get(1));
		availableTab.getContent(1).attach(invScroll);
		availableTab.addNewTextBox(UIScale.getUIScale().scale(41));
		final GUIScrollablePanel expanded = new GUIScrollablePanel(10, UIScale.getUIScale().scale(100), getState());
		GUITextOverlay blockName = new GUITextOverlay(FontSize.BIG_20, getState());
		blockName.setTextSimple(new Object() {

			String lastCache;

			InventorySlot lastSel;

			@Override
			public String toString() {
				if (lastSel != inventoryIcons.getLastSelected() || lastCache == null) {
					if (inventoryIcons.getLastSelected() != null) {
						if (ElementKeyMap.isValidType(inventoryIcons.getLastSelected().getType())) {
							lastCache = ElementKeyMap.getInfo(inventoryIcons.getLastSelected().getType()).getName();
						} else if (inventoryIcons.getLastSelected().getType() == InventorySlot.MULTI_SLOT) {
							lastCache = Lng.str("Multi-Slot");
						} else {
							lastCache = "";
						}
					} else {
						lastCache = "";
					}
					lastSel = inventoryIcons.getLastSelected();
				}
				return lastCache;
			}
		});
		blockName.setPos(UIScale.getUIScale().scale(36), UIScale.getUIScale().scale(8), 0);
		GUIBlockSprite b = new GUIBlockSprite(getState(), (short) 0) {

			@Override
			public void draw() {
				if (inventoryIcons.getLastSelected() != null && ElementKeyMap.isValidType(inventoryIcons.getLastSelected().getType())) {
					type = inventoryIcons.getLastSelected().getType();
					super.draw();
				}
			}
		};
		b.setScale(0.5f, 0.5f, 0f);
		b.setPos(1, 1, 0);
		final GUITextOverlay desc = new GUITextOverlay(FontSize.SMALL_14, getState()) {

			@Override
			public void draw() {
				if (inventoryIcons.getLastSelected() != null && (ElementKeyMap.isValidType(inventoryIcons.getLastSelected().getType()) || inventoryIcons.getLastSelected().getType() == InventorySlot.MULTI_SLOT)) {
					setPos(4, 36, 0);
				} else {
					setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
				}
				super.draw();
			}
		};
		desc.wrapSimple = false;
		desc.setTextSimple(new Object() {

			String lastCache;

			InventorySlot lastSel;

			@Override
			public String toString() {
				if (inventoryIcons.getLastSelected() != null) {
					if (lastSel != inventoryIcons.getLastSelected() || lastCache == null) {
						if (ElementKeyMap.isValidType(inventoryIcons.getLastSelected().getType())) {
							String[] parseDescription = ElementKeyMap.getInfo(inventoryIcons.getLastSelected().getType()).parseDescription(getState());
							StringBuffer f = new StringBuffer();
							for (int i = 0; i < parseDescription.length; i++) {
								f.append(parseDescription[i] + "\n");
							}
							lastCache = f.toString();
						} else if (inventoryIcons.getLastSelected().getType() == InventorySlot.MULTI_SLOT) {
							List<InventorySlot> subSlots = inventoryIcons.getLastSelected().getSubSlots();
							StringBuffer f = new StringBuffer();
							for (InventorySlot subSlot : subSlots) {
								ElementInformation info = ElementKeyMap.getInfo(subSlot.getType());
								String[] parseDescription = info.parseDescription(getState());
								f.append(info.getName() + "\n\n");
								for (int i = 0; i < parseDescription.length; i++) {
									f.append(parseDescription[i] + "\n");
								}
								f.append("-------------------------------\n");
							}
							lastCache = f.toString();
						} else {
							lastCache = Lng.str("Click on an item to view its description.");
						}
						lastSel = inventoryIcons.getLastSelected();
					}
				} else {
					return "";
				}
				return lastCache;
			}
		});
		GUIAnchor a = new GUIAnchor(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().scale(200)) {

			@Override
			public void draw() {
				setHeight(Math.max(0, desc.getPos().y + desc.getTextHeight()));
				setWidth(Math.max(0, expanded.getClipWidth()));
				super.draw();
			}
		};
		expanded.setContent(a);
		desc.autoWrapOn = a;
		a.attach(b);
		a.attach(blockName);
		a.attach(desc);
		GUIExpandableButton detailsButton = new GUIExpandableButton(getState(), availableTab.getTextboxes().get(2), Lng.str("Show Block Information"), Lng.str("Hide Block Information"), new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		}, expanded, true) {

			@Override
			public boolean isOccluded() {
				return !InventoryPanelNew.this.isActive() || super.isOccluded();
			}
		};
		detailsButton.buttonWidthAdd = -4;
		detailsButton.onInit();
		availableTab.getContent(2).attach(detailsButton);
	}

	public void resetInvScroll() {
		invScroll.reset();
	}

	public PlayerState getOwnPlayer() {
		return InventoryPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return InventoryPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
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
	public float getHeight() {
		return inventoryPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return inventoryPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		// System.err.println("CUDRA: "+currentlyDrawing+"; "+getState().getController().getPlayerInputs().size());
		return getState().getController().getPlayerInputs().isEmpty() && (currentlyDrawing == null || (currentlyDrawing.anchorInside() || otherInventories.get(otherInventories.size() - 1) == currentlyDrawing));
	}

	public void drawToolTip() {
		for (InvWindow w : otherInventories) {
			if (w.inventoryIcons.isActive()) {
				w.drawToolTip();
			}
		}
	}

	public void drawDragging(InventorySlotOverlayElement e) {
		for (InvWindow w : otherInventories) {
			w.drawDragging(e);
		}
	}

	@Override
	public void onChange(boolean updateListDim) {
		this.updateRequested = true;
	}

	public void deactivate(PlayerInventoryInput playerInventoryInput) {
		for (int i = 0; i < otherInventories.size(); i++) {
			if (otherInventories.get(i).drawable == playerInventoryInput.getInputPanel()) {
				otherInventories.remove(i);
				return;
			}
		}
	// can happen on double click with closedAnchor of InvWindow
	// assert(false):"Couldnt deactivate "+playerInventoryInput+": "+playerInventoryInput.getInputPanel()+": "+otherInventories;
	}

	public boolean isInventoryActive(InventoryIconsNew inventoryIcons) {
		if (!this.isActive()) {
			return false;
		}
		for (int i = 0; i < otherInventories.size(); i++) {
			assert (otherInventories.get(i).inventoryIcons != null);
			if (otherInventories.get(i).inventoryIcons == inventoryIcons) {
				// System.err.println("CHEKCING ACTIVE: "+otherInventories.get(i)+": "+otherInventories.get(i).anchorInside());
				return otherInventories.get(i).anchorInside() && otherInventories.get(i).scrollPanelInside();
			}
		}
		assert (false) : inventoryIcons;
		return false;
	}

	public void resetOthers() {
		if (otherInventories.size() > 1) {
			for (int i = 0; i < otherInventories.size(); i++) {
				if (otherInventories.get(i).drawable != inventoryPanel) {
					otherInventories.remove(i);
					i--;
				}
			}
		}
	}

	public boolean isCapsuleRefineryOpen() {
		for (int i = 0; i < otherInventories.size(); i++) {
			if (otherInventories.get(i).inventoryIcons.getInventory() == getOwnPlayer().getPersonalFactoryInventoryCapsule()) {
				return true;
			}
		}
		return false;
	}

	public void openCapsuleRefineryOpen() {
		PlayerInventoryInput kk = new PlayerInventoryInput("perseCapsulea", getState(), Lng.str("Refine Raw Materials"), this, this, getOwnPlayer().getPersonalFactoryInventoryCapsule(), this);
		kk.getInputPanel().onInit();
		otherInventories.add(new InvWindow(kk.getInputPanel(), kk.getInputPanel().getInventoryIcons()));
		arrangeWindows();
	}

	public boolean isMicroFactoryOpen() {
		for (int i = 0; i < otherInventories.size(); i++) {
			if (otherInventories.get(i).inventoryIcons.getInventory() == getOwnPlayer().getPersonalFactoryInventoryMicro()) {
				return true;
			}
		}
		return false;
	}

	public void openMicroFactoryOpen() {
		PlayerInventoryInput kk = new PlayerInventoryInput("perseMicroa", getState(), Lng.str("Craft Generic Materials"), this, this, getOwnPlayer().getPersonalFactoryInventoryMicro(), this);
		kk.getInputPanel().onInit();
		otherInventories.add(new InvWindow(kk.getInputPanel(), kk.getInputPanel().getInventoryIcons()));
		arrangeWindows();
	}

	public boolean isMacroFactioryBlockFactoryOpen() {
		for (int i = 0; i < otherInventories.size(); i++) {
			if (otherInventories.get(i).inventoryIcons.getInventory() == getOwnPlayer().getPersonalFactoryInventoryMacroBlock()) {
				return true;
			}
		}
		return false;
	}

	public void openMacroFactioryBlockFactoryOpen() {
		PlayerInventoryInput kk = new PlayerInventoryInput("perseMacroa", getState(), Lng.str("Craft Basic Factory Block"), this, this, getOwnPlayer().getPersonalFactoryInventoryMacroBlock(), this);
		kk.getInputPanel().onInit();
		otherInventories.add(new InvWindow(kk.getInputPanel(), kk.getInputPanel().getInventoryIcons()));
		arrangeWindows();
	}

	public void reset() {
		inventoryPanel.reset();
	}

	@Override
	public void onFastSwitch(InventorySlotOverlayElement element, InventoryIconsNew icons) {
		InventoryIconsNew switchTo;
		/*
		 * this is called from the main inventory icons
		 */
		if (otherInventories.size() == 1 && otherInventories.get(0).inventoryIcons == inventoryIcons) {
			// only main panel open
			switchTo = inventoryIcons;
			assert (icons == switchTo);
			int freeSlot = switchTo.getInventory().getFirstActiveSlot(element.getType(), false);
			try {
				if (freeSlot < 0 || freeSlot >= switchTo.getInventory().getActiveSlotsMax()) {
					System.err.println("[CLIENT][INVENTORY] FastSwitch: No exisitng slot found: " + freeSlot + "; checking free");
					freeSlot = switchTo.getInventory().getFreeSlot();
				}
				if (freeSlot >= 0 && freeSlot < switchTo.getInventory().getActiveSlotsMax()) {
					System.err.println("[CLIENT][INVENTORY] FastSwitch: slot found: " + element.getSlot() + " -> " + freeSlot);
					switchTo.getInventory().switchSlotsOrCombineClient(freeSlot, element.getSlot(), switchTo.getInventory(), -1);
				} else {
					System.err.println("[CLIENT][INVENTORY] FastSwitch: No free slot found: " + freeSlot);
				}
			} catch (NoSlotFreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''' " + otherInventories.size());
			// multiple inventories open
			switchTo = inventoryIcons;
			if (otherInventories.size() > 1) {
				for (int i = otherInventories.size() - 1; i >= 0; i--) {
					if (otherInventories.get(i).inventoryIcons != inventoryIcons) {
						switchInventories(element, inventoryIcons, otherInventories.get(i).inventoryIcons);
						break;
					}
				}
			}
		}
	}

	private boolean switchInventories(InventorySlotOverlayElement element, InventoryIconsNew from, InventoryIconsNew to) {
		int otherSlot;
		if (element.getType() == InventorySlot.MULTI_SLOT) {
			if (from.getInventory().getSlot(element.getSlot()) == null || from.getInventory().getSlot(element.getSlot()).getSubSlots().isEmpty()) {
				return false;
			}
			otherSlot = to.getInventory().getFirstSlotMulti(element.getType(), from.getInventory().getSubSlots(element.getSlot()).get(0).getType(), false);
		} else {
			otherSlot = to.getInventory().getFirstSlot(element.getType(), false);
		}
		boolean foundStack = true;
		if (otherSlot < 0) {
			try {
				foundStack = false;
				otherSlot = to.getInventory().getFreeSlot();
			} catch (NoSlotFreeException e) {
				e.printStackTrace();
				getState().getController().popupAlertTextMessage(Lng.str("No more room in inventory!"), 0);
			}
		}
		if (otherSlot >= 0) {
			if (foundStack) {
				to.getInventory().switchSlotsOrCombineClient(otherSlot, element.getSlot(), from.getInventory(), -1);
			} else {
				from.getInventory().switchSlotsOrCombineClient(element.getSlot(), otherSlot, to.getInventory(), -1);
			}
			return true;
		} else {
			return false;
		}
	}

	public void onFastSwitch(InventorySlotOverlayElement element, InventoryIconsNew icons, SecondaryInventoryPanelNew secondaryInventoryPanelNew) {
		switchInventories(element, icons, inventoryIcons);
	}

	public void deactivateSecondary(SecondaryInventoryPanelNew panel) {
		for (int i = 0; i < otherInventories.size(); i++) {
			if (otherInventories.get(i).inventoryIcons == panel.getInventoryIcons()) {
				otherInventories.remove(i);
				return;
			}
		}
	}

	public boolean switchFromBottomBar(InventorySlotOverlayElement element) {
		if (otherInventories.size() == 1 && otherInventories.get(0).inventoryIcons == inventoryIcons) {
			return switchToInv(inventoryIcons.getInventory(), element);
		} else {
			for (int i = otherInventories.size() - 1; i >= 0; i--) {
				if (otherInventories.get(i).inventoryIcons != inventoryIcons) {
					return switchInto(inventoryIcons.getInventory(), otherInventories.get(i).inventoryIcons.getInventory(), element);
				}
			}
		}
		return false;
	}

	private boolean switchInto(Inventory from, Inventory to, InventorySlotOverlayElement element) {
		int otherSlot = to.getFirstSlot(element.getType(), false);
		boolean foundStack = true;
		if (otherSlot < 0) {
			try {
				foundStack = false;
				otherSlot = to.getFreeSlot();
			} catch (NoSlotFreeException e) {
				e.printStackTrace();
				getState().getController().popupAlertTextMessage(Lng.str("No more room in inventory!"), 0);
			}
		}
		if (otherSlot >= 0) {
			if (foundStack) {
				to.switchSlotsOrCombineClient(otherSlot, element.getSlot(), from, -1);
			} else {
				from.switchSlotsOrCombineClient(element.getSlot(), otherSlot, to, -1);
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean switchToInv(Inventory ownInv, InventorySlotOverlayElement element) {
		try {
			int freeSlot = ownInv.getFirstNonActiveSlot(element.getType(), false);
			if (freeSlot < ownInv.getActiveSlotsMax()) {
				System.err.println("[CLIENT][INVENTORY] FastSwitch: No exisitng slot found: " + freeSlot + "; checking free");
				freeSlot = ownInv.getFreeNonActiveSlot();
			}
			if (freeSlot >= ownInv.getActiveSlotsMax()) {
				System.err.println("[CLIENT][INVENTORY] FastSwitch: slot found: " + element.getSlot() + " -> " + freeSlot);
				ownInv.switchSlotsOrCombineClient(freeSlot, element.getSlot(), ownInv, -1);
			} else {
				System.err.println("[CLIENT][INVENTORY] FastSwitch: No free slot found: " + freeSlot);
			}
		} catch (NoSlotFreeException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @return the mainInventory
	 */
	public InventoryIconsNew getMainInventory() {
		return inventoryIcons;
	}

	/**
	 * @param mainInventory the mainInventory to set
	 */
	public void setMainInventory(InventoryIconsNew mainInventory) {
		this.inventoryIcons = mainInventory;
	}

	public boolean isChestOrFacActive() {
		if (otherInventories.size() == 1) {
			return false;
		}
		for (int i = 0; i < otherInventories.size(); i++) {
			if (otherInventories.get(i).inventoryIcons.getInventory() instanceof StashInventory) {
				return true;
			}
		}
		return false;
	}

	public void deactivateAllOther() {
		for (int i = 0; i < otherInventories.size(); i++) {
			if (otherInventories.get(i).drawable instanceof SecondaryInventoryPanelNew) {
				otherInventories.remove(i);
				i--;
			}
		}
		assert (otherInventories.size() == 1);
	}

	private class DropToSpaceAnchor extends GUIAnchor implements DropTarget<InventorySlotOverlayElement>, GUICallback {

		public DropToSpaceAnchor(InputState state) {
			super(state);
			setMouseUpdateEnabled(true);
			setCallback(this);
		}

		/* (non-Javadoc)
		 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
		 */
		@Override
		public void draw() {
			setWidth(GLFrame.getWidth());
			setHeight(GLFrame.getHeight());
			super.draw();
			if (isDraggingOK()) {
				Draggable dragging = getState().getController().getInputController().getDragging();
				((InventorySlotOverlayElement) dragging).flagHoverDropeZone();
			} else {
			}
		}

		private boolean isDraggingOK() {
			return getState().getController().getInputController().getDragging() != null && getState().getController().getInputController().getDragging() instanceof InventorySlotOverlayElement && isInside() && !((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getBuildSideBar().isInside();
		}

		@Override
		public void checkTarget(MouseEvent e) {
			if (isDraggingOK()) {
				Draggable dragging = getState().getController().getInputController().getDragging();
				((InventorySlotOverlayElement) dragging).flagHoverDropeZone();
				if (dragging.checkDragReleasedMouseEvent(e)) {
					System.err.println("CHECKING " + this + " " + this.hashCode() + " MOUSE NO MORE GRABBED");
					boolean remove = false;
					if (isTarget(dragging) && (dragging != this)) {
						if ((System.currentTimeMillis() - dragging.getTimeDragStarted()) > Draggable.MIN_DRAG_TIME) {
							System.err.println("NOW DROPPING " + dragging);
							onDrop((InventorySlotOverlayElement) dragging);
						} else {
							System.err.println("NO DROP: time dragged to short");
						}
						remove = true;
					}
					if ((!isTarget(dragging))) {
						System.err.println("NO DROP: not a target: " + this);
					}
					if ((dragging == this)) {
						if (dragging.isStickyDrag()) {
							dragging.setStickyDrag(false);
							dragging.reset();
							getState().getController().getInputController().setDragging(null);
						}
					}
					if (remove) {
						dragging.reset();
						getState().getController().getInputController().setDragging(null);
					}
				}
			}
		}

		@Override
		public boolean isTarget(Draggable draggable) {
			return draggable instanceof InventorySlotOverlayElement;
		}

		@Override
		public void onDrop(InventorySlotOverlayElement draggable) {
			System.err.println("[INVENTORYGUI] ITEM slot " + draggable.getSlot() + "; count " + draggable.getCount(true) + "; type " + draggable.getType() + " thrown into space");
			DragDrop d = new DragDrop();
			d.slot = draggable.getSlot();
			d.count = draggable.getCount(true);
			d.type = draggable.getType();
			d.subType = draggable.getSubSlotType();
			d.invId = draggable.getDraggingInventory().getInventoryHolder().getId();
			d.parameter = draggable.getDraggingInventory().getParameter();
			((GameClientState) getState()).getPlayer().getNetworkObject().dropOrPickupSlots.add(new RemoteDragDrop(d, false));
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			checkTarget(event);
		}

		@Override
		public boolean isOccluded() {
			return false;
		}
	}

	private class InvWindow implements GUICallback {

		public final InventoryIconsNew inventoryIcons;

		private final int id;

		GUIElement drawable;

		GUIAnchor windowAnchor;

		GUIAnchor windowAnchorClose;

		public InvWindow(GUIElement drawable, InventoryIconsNew inventoryIcons) {
			this.id = windowIdGen++;
			this.drawable = drawable;
			this.windowAnchor = new GUIAnchor(getState());
			this.windowAnchor.setMouseUpdateEnabled(true);
			this.windowAnchor.setCallback(this);
			this.windowAnchorClose = new GUIAnchor(getState());
			this.windowAnchorClose.setMouseUpdateEnabled(true);
			this.windowAnchorClose.setCallback(this);
			this.windowAnchorClose.setUserPointer("X");
			this.windowAnchor.attach(windowAnchorClose);
			this.inventoryIcons = inventoryIcons;
			assert (inventoryIcons != null);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "INV:" + drawable;
		}

		public Vector3f getPos() {
			if (drawable instanceof SecondaryInventoryPanelNew) {
				return ((SecondaryInventoryPanelNew) drawable).background.getPos();
			} else {
				return inventoryPanel.getPos();
			}
		}

		public void orientate(int o) {
			if (drawable instanceof SecondaryInventoryPanelNew) {
				((SecondaryInventoryPanelNew) drawable).background.orientate(o);
				;
			} else {
				inventoryPanel.orientate(o);
			}
		}

		public boolean isNewPanel() {
			if (drawable instanceof SecondaryInventoryPanelNew) {
				return ((SecondaryInventoryPanelNew) drawable).background.savedSizeAndPosition.newPanel;
			} else {
				return inventoryPanel.savedSizeAndPosition.newPanel;
			}
		}

		public void setAnchorInside(boolean b) {
			windowAnchor.setInside(b);
			windowAnchorClose.setInside(b);
		}

		public boolean anchorInside() {
			return windowAnchor.isInside() || windowAnchorClose.isInside();
		}

		public void drawAnchor() {
			if (drawable instanceof GUIInputPanel) {
				windowAnchor.setWidth(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getWidth());
				windowAnchor.setHeight(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getHeight());
				windowAnchor.getPos().set(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getPos());
				windowAnchorClose.setWidth(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getCloseCross().getWidth());
				windowAnchorClose.setHeight(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getCloseCross().getHeight());
				windowAnchorClose.getPos().set(((GUIDialogWindow) ((GUIInputPanel) drawable).getBackground()).getCloseCross().getPos());
			} else {
				windowAnchor.setWidth(((GUIMainWindow) drawable).getWidth());
				windowAnchor.setHeight(((GUIMainWindow) drawable).getHeight());
				windowAnchor.getPos().set(((GUIMainWindow) drawable).getPos());
				windowAnchorClose.setWidth(((GUIMainWindow) drawable).getCloseCross().getWidth());
				windowAnchorClose.setHeight(((GUIMainWindow) drawable).getCloseCross().getHeight());
				windowAnchorClose.getPos().set(((GUIMainWindow) drawable).getCloseCross().getPos());
			}
			// System.err.println("ANCHOR FOR "+drawable+": "+windowAnchor.getWidth()+"x"+windowAnchor.getHeight()+": "+windowAnchor.getPos());
			windowAnchor.draw();
		}

		public void draw() {
			drawable.draw();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return id;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof InvWindow && id == ((InvWindow) obj).id;
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse() && callingGuiElement == windowAnchorClose){
		// if(drawable instanceof GUIInputPanel){
		// ((GUIInputPanel)drawable).getCallback().callback(callingGuiElement, event);
		// }
		// }
		// if(event.pressedLeftMouse()){
		// selectedWindow(this);
		// }
		// selection is done immediately in draw() of InventoryPanelNew
		}

		public boolean scrollPanelInside() {
			return inventoryIcons.isInside();
		}

		@Override
		public boolean isOccluded() {
			return !getState().getController().getPlayerInputs().isEmpty();
		}

		public void drawToolTip() {
			inventoryIcons.drawToolTip();
		// mainInventory.drawToolTip();
		// mainInventory.drawDragging(e);
		}

		public void drawDragging(InventorySlotOverlayElement e) {
			inventoryIcons.drawDragging(e);
		}
	}

	public void clearFilter(boolean clearOthers) {
		inventoryIcons.clearFilter();
		if (clearOthers) {
			for (InvWindow o : otherInventories) {
				o.inventoryIcons.clearFilter();
			}
		}
	}
}
