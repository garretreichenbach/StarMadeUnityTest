package org.schema.game.client.view.gui.inventory.inventorynew;

import java.util.List;

import org.schema.game.client.controller.PlayerInventoryInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.inventory.InventoryCallBack;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement;
import org.schema.game.client.view.gui.inventory.InventoryToolInterface;
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
import org.schema.game.common.data.player.inventory.PersonalFactoryInventory;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIExpandableButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;

public class SecondaryInventoryPanelNew extends GUIInputPanel implements GUIActiveInterface, InventoryCallBack {

	final PlayerInventoryInput playerInput;
	private GUIActiveInterface actInterface;
	private InventoryPanelNew mainPanel;
	private InventoryIconsNew inventoryIcons;
	//	private GUIContentPane personalTab;
	private boolean init;
	private Inventory inventory;
	public SecondaryInventoryPanelNew(String typeId, ClientState state, Object title, int initialWidth, int initialHeight, Inventory inventory, PlayerInventoryInput callback, GUIActiveInterface actInterface, InventoryPanelNew mainPanel) {
		super("SecondaryInventoryPanelNew" + typeId, state, initialWidth, initialHeight, callback, title, "");
		this.mainPanel = mainPanel;
		this.inventory = inventory;
		this.actInterface = actInterface;
		this.autoOrientate = false;
		this.playerInput = callback;
		
		((GUIDialogWindow) this.getBackground()).innerHeightSubstraction = UIScale.getUIScale().W_innerHeightSubstraction_noPadding;
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public boolean isActive() {
		return actInterface.isActive();
	}

	@Override
	public void update(Timer timer) {
	}

	public void createMainInventoryPane() {
		if (inventoryIcons != null) {
			inventoryIcons.cleanUp();
		}
		InventoryToolInterface tools;
		GUIAnchor c;

		if (inventory instanceof PersonalFactoryInventory) {
			c = new InventoryOptionsPersonalFactoryButtonPanel(getState(), this, mainPanel, (PersonalFactoryInventory) inventory);
			((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(50+25);
			((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1)); //dynamic
			tools = (InventoryToolInterface) c;
		} else if (inventory instanceof StashInventory && !(inventory instanceof NPCFactionInventory)) {
			SegmentController s = ((ManagerContainer<?>) inventory.getInventoryHolder()).getSegmentController();
			SegmentPiece pointUnsave = s.getSegmentBuffer().getPointUnsave(inventory.getParameter());
			final int hhSize = 76 + 24 + 25 + 2;
			if (pointUnsave == null) {
				assert (false);
				return;
			} else if (pointUnsave.getType() == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID || pointUnsave.getType() == ElementKeyMap.FACTORY_BLOCK_RECYCLER_ID) {
				c = new InventoryOptionsFactoryButtonPanel(getState(), this, mainPanel, (StashInventory) inventory);
				((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(hhSize);
				((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1)); //dynamic
			} else if (pointUnsave.getType() == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID || pointUnsave.getType() == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ADV_ID) {
				c = new InventoryOptionsFactoryButtonPanel(getState(), this, mainPanel, (StashInventory) inventory);
				((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(hhSize);
				((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1)); //dynamic
			} else if (ElementKeyMap.getFactorykeyset().contains(pointUnsave.getType())) {
				c = new InventoryOptionsFactoryButtonPanel(getState(), this, mainPanel, (StashInventory) inventory);
				((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(hhSize);
				((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1)); //dynamic
			}  else if (pointUnsave.getType() == ElementKeyMap.SHIPYARD_COMPUTER) {
				c = new ShipyardOptionsChestButtonPanel(getState(), this, mainPanel, (StashInventory) inventory);
				((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(hhSize+24+ 24 + 24 + 2 + 2 + 3);
				((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1)); //dynamic
			} else {
				c = new InventoryOptionsChestButtonPanel(getState(), this, mainPanel, (StashInventory) inventory);
				((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(hhSize);
				((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1)); //dynamic
			}
			tools = (InventoryToolInterface) c;

		} else {
			c = new InventoryOptionsMainButtonPanel(getState(), this, mainPanel, false);
			tools = (InventoryToolInterface) c;
			((GUIDialogWindow) background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(26));
			((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(1)); //dynamic
		}

		c.onInit();
		((GUIDialogWindow) background).getMainContentPane().setContent(0, c);

		GUIScrollablePanel invScroll = new GUIScrollablePanel(10, 10, ((GUIDialogWindow) background).getMainContentPane().getContent(1), getState());
		invScroll.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		invScroll.setContent(inventoryIcons = new InventoryIconsNew(getState(), invScroll, () -> SecondaryInventoryPanelNew.this.inventory, this, tools));
		invScroll.onInit();


		((GUIDialogWindow) background).getMainContentPane().setListDetailMode(((GUIDialogWindow) background).getMainContentPane().getTextboxes().get(1));

		((GUIDialogWindow) background).getMainContentPane().getContent(1).attach(invScroll);

		if (hasBlockInformation()) {
			((GUIDialogWindow) background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(44));

			final GUIScrollablePanel expanded = new GUIScrollablePanel(10, 100, getState());

			GUITextOverlay blockName = new GUITextOverlay(FontSize.BIG_20, getState());
			blockName.setTextSimple(new Object() {
				String lastCache;
				InventorySlot lastSel;

				@Override
				public String toString() {
					if (lastSel != inventoryIcons.getLastSelected() || lastCache == null) {
						if(inventoryIcons.getLastSelected() != null){
							if (ElementKeyMap.isValidType(inventoryIcons.getLastSelected().getType())) {
								lastCache = ElementKeyMap.getInfo(inventoryIcons.getLastSelected().getType()).getName();
							} else if (inventoryIcons.getLastSelected().getType() == InventorySlot.MULTI_SLOT) {
								lastCache = Lng.str("Multi-Slot");
							} else {
								lastCache = "";
							}
						}else{
							lastCache = "";
						}
						lastSel = inventoryIcons.getLastSelected();
					}

					return lastCache;
				}

			});
			blockName.setPos(36, 8, 0);

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
					if (inventoryIcons.getLastSelected() != null && 
							(ElementKeyMap.isValidType(inventoryIcons.getLastSelected().getType()) || inventoryIcons.getLastSelected().getType() == InventorySlot.MULTI_SLOT)) {
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
					if(inventoryIcons.getLastSelected() != null){
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
								for(InventorySlot subSlot : subSlots){
									ElementInformation info = ElementKeyMap.getInfo(subSlot.getType());
									String[] parseDescription = info.parseDescription(getState());
									f.append(info.getName()+"\n\n");
									for (int i = 0; i < parseDescription.length; i++) {
										f.append(parseDescription[i] + "\n");
									}
									f.append("-------------------------------");
								}
								lastCache = f.toString();
							} else {
								lastCache = Lng.str("Click on an item to view its description.");
							}
							lastSel = inventoryIcons.getLastSelected();
						}
					}else{
						return "";
					}
					return lastCache;
				}

			});
			GUIAnchor a = new GUIAnchor(getState(), 300, 200) {

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

			GUIExpandableButton detailsButton = new GUIExpandableButton(getState(), ((GUIDialogWindow) background).getMainContentPane().getTextboxes().get(2), Lng.str("Show Block Information"), Lng.str("Hide Block Information"), new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			}, expanded, false) {
				@Override
				public boolean isOccluded() {
					return !mainPanel.isActive() || super.isOccluded();
				}

			};
			detailsButton.buttonWidthAdd = -4;
			detailsButton.onInit();

			((GUIDialogWindow) background).getMainContentPane().getContent(2).attach(detailsButton);
		}

	}

	private boolean hasBlockInformation() {
		return !(inventory instanceof PersonalFactoryInventory);
	}

	public PlayerState getOwnPlayer() {
		return SecondaryInventoryPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return SecondaryInventoryPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return ((GUIDialogWindow) background).getHeight();
	}

	@Override
	public float getWidth() {
		return ((GUIDialogWindow) background).getWidth();
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();

		}
		((GUIDialogWindow) background).draw();
	}

	@Override
	public void onInit() {
		super.onInit();

		createMainInventoryPane();

		((GUIDialogWindow) background).activeInterface = actInterface;

		init = true;
	}

	public void drawToolTip() {
		inventoryIcons.drawToolTip();
	}

	public void drawDragging(InventorySlotOverlayElement e) {
		inventoryIcons.drawDragging(e);
	}

	public InventoryIconsNew getInventoryIcons() {
		return inventoryIcons;
	}

	@Override
	public void onFastSwitch(InventorySlotOverlayElement element,
				InventoryIconsNew icons) {
		mainPanel.onFastSwitch(element, icons, this);
	}

	public PlayerInventoryInput getPlayerInput() {
		return playerInput;
	}

}
