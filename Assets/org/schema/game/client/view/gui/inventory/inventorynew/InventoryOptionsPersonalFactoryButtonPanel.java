package org.schema.game.client.view.gui.inventory.inventorynew;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockConsistenceGraph;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventoryToolInterface;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FixedRecipe;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.PersonalFactoryInventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class InventoryOptionsPersonalFactoryButtonPanel extends GUIAnchor implements InventoryToolInterface {

	private String inventoryFilterText = "";

	private SecondaryInventoryPanelNew panel;

	private InventoryPanelNew mainPanel;

	private PersonalFactoryInventory inventory;

	private boolean inventoryActive = true;

	private FixedRecipe recipe;

	public InventoryOptionsPersonalFactoryButtonPanel(InputState state, SecondaryInventoryPanelNew panel, InventoryPanelNew mainPanel, PersonalFactoryInventory inventory) {
		super(state);
		this.panel = panel;
		this.mainPanel = mainPanel;
		this.inventory = inventory;
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

	@Override
	public void onInit() {
		GUIHorizontalButtonTablePane pc = new GUIHorizontalButtonTablePane(getState(), 1, 1, this);
		pc.onInit();
		short production = inventory.getProduction();
		short type = inventory.getFactoryType();
		String name;
		if (type == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID) {
			name = "Refine Raw Materials";
		} else if (type == ElementKeyMap.FACTORY_COMPONENT_FAB_ID) {
			name = "Craft Basic Components";
		} else if (type == ElementKeyMap.FACTORY_BLOCK_ASSEMBLER_ID) {
			name = "Craft Macro Factories";
		} else {
			throw new NullPointerException("Personal factory type must be assigned correctly in code.");
		}
		if (type == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID) {
			recipe = ElementKeyMap.personalMeshAndCompositeRecipe;
		} else if (type == ElementKeyMap.FACTORY_COMPONENT_FAB_ID) {
			recipe = ElementKeyMap.personalComponentRecipe;
		} else if (type == ElementKeyMap.FACTORY_BLOCK_ASSEMBLER_ID) {
			recipe = ElementKeyMap.macroBlockRecipe;
		} else {
			throw new NullPointerException("Personal factory type must be assigned correctly in code.");
		}
		pc.addButton(0, 0, Lng.str("View Graph to %s", name), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					short production = inventory.getProduction();
					if (getState().getPlayerInputs().isEmpty()) {
						String name;
						if (inventory.getFactoryType() == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID) {
							name = Lng.str("Refine Raw Materials");
						} else if (inventory.getFactoryType() == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID) {
							name = Lng.str("Craft Generic Materials");
						} else if (ElementKeyMap.isMacroFactory(inventory.getFactoryType())) {
							name = Lng.str("Craft Macro Factory");
						} else {
							throw new NullPointerException("Must be assigned");
						}
						PlayerGameOkCancelInput g = new PlayerGameOkCancelInput("InventoryOptionsPersonalFactoryButtonPanel_" + name, getState(), 824, 400, name, "") {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
							}
						};
						g.getInputPanel().setCancelButton(false);
						g.getInputPanel().onInit();
						if (recipe != null) {
							GUIScrollablePanel sc = new GUIScrollablePanel(820, 420, ((GUIDialogWindow) g.getInputPanel().getBackground()).getMainContentPane().getContent(0), getState());
							sc.setContent(recipe.getGUI(getState()));
							g.getInputPanel().getContent().attach(sc);
						} else {
							assert (production != 0);
							g.getInputPanel().getContent().attach(new GUIBlockConsistenceGraph(getState(), ElementKeyMap.getInfo(production), ((GUIDialogWindow) g.getInputPanel().getBackground()).getMainContentPane().getContent(0)));
						}
						g.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(528);
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
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
		pc.getPos().x = UIScale.getUIScale().scale(1);
		pc.getPos().y = UIScale.getUIScale().scale(1);
		attach(pc);
		GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), "Pull Tick", this) {
			@Override
			public float getValue() {
				if (inventoryActive) {
					short type = inventory.getFactoryType();
					if (type == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID) {
						text = Lng.str("Refining Generic Materials");
					} else if (type == ElementKeyMap.FACTORY_COMPONENT_FAB_ID) {
						text = Lng.str("Crafting Basic Components");
					} else if (type == ElementKeyMap.FACTORY_BLOCK_ASSEMBLER_ID) {
						text = Lng.str("Crafting Factories");
					} else {
						throw new NullPointerException("Personal factory type must be assigned correctly in code.");
					}
					double serverRunningTime = InventoryOptionsPersonalFactoryButtonPanel.this.getState().getController().getServerRunningTime() % AbstractOwnerState.FACTORY_TIME;
					float t = (float) (serverRunningTime / AbstractOwnerState.FACTORY_TIME);
					return (t);
				} else {
					text = "Production Inactive";
					return 0;
				}
			}
		};
		progress.getColor().set(InventoryPanelNew.PROGRESS_COLOR);
		progress.onInit();
		progress.getPos().x = UIScale.getUIScale().scale(1);
		progress.getPos().y = UIScale.getUIScale().scale(1 + 24);
		attach(progress);
		GUIHorizontalArea f = new GUIHorizontalArea(getState(), HButtonType.TEXT_FIELD, 10) {

			@Override
			public void draw() {
				setWidth(InventoryOptionsPersonalFactoryButtonPanel.this.getWidth());
				super.draw();
			}
		};
		f.getPos().x = UIScale.getUIScale().scale(1);
		f.getPos().y = UIScale.getUIScale().scale(1 + 24 + 25);
		attach(f);
		GUIScrollablePanel guiScrollablePanel = new GUIScrollablePanel(24, 24, this, getState());
		guiScrollablePanel.setScrollable(GUIScrollablePanel.SCROLLABLE_NONE);
		guiScrollablePanel.setLeftRightClipOnly = true;
		guiScrollablePanel.dependent = f;
		GUITextOverlay l = new GUITextOverlay(FontSize.MEDIUM_15, getState()) {

			@Override
			public void draw() {
				if (inventory.isOverCapacity()) {
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
				return inventory.getVolumeString();
			}
		});
		l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		f.attach(l);
	}

	@Override
	public String getText() {
		return inventoryFilterText;
	}

	@Override
	public boolean isActiveInventory(InventoryIconsNew inventoryIcons) {
		return mainPanel.isInventoryActive(inventoryIcons);
	}

	@Override
	public void clearFilter() {
	}
}
