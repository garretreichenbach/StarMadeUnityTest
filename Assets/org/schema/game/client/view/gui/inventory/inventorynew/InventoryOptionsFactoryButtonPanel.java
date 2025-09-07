package org.schema.game.client.view.gui.inventory.inventorynew;

import api.config.BlockConfig;
import api.element.recipe.CustomModRefinery;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerBlockTypeDropdownInputNew;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockConsistenceGraph;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventoryToolInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.factory.FactoryCollectionManager;
import org.schema.game.common.controller.elements.factory.FactoryElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FixedRecipe;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.LongStringPair;
import org.schema.game.network.objects.remote.RemoteLongString;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.remote.LongIntPair;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.sound.controller.AudioController;

import java.util.Locale;

import static org.schema.game.common.data.element.ElementKeyMap.*;

public class InventoryOptionsFactoryButtonPanel extends GUIAnchor implements InventoryToolInterface {

	private String inventoryFilterText = "";

	private SecondaryInventoryPanelNew panel;

	private InventoryPanelNew mainPanel;

	private StashInventory inventory;

	private boolean inventoryActive;

	private SegmentController segmentController;

	private SegmentPiece pointUnsave;

	private InventoryFilterBar searchBar;

	public InventoryOptionsFactoryButtonPanel(InputState state, SecondaryInventoryPanelNew panel, InventoryPanelNew mainPanel, StashInventory inventory) {
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
		searchBar = new InventoryFilterBar(getState(), Lng.str("FILTER BY BLOCK NAME"), this, new TextCallback() {

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
		}, t -> {
			inventoryFilterText = t;
			return t;
		});
		searchBar.getPos().x = 1;
		searchBar.getPos().y = 2;
		searchBar.leftDependentHalf = true;
		searchBar.onInit();
		attach(searchBar);
		segmentController = ((ManagerContainer<?>) inventory.getInventoryHolder()).getSegmentController();
		GUIInventoryOtherDropDown switchInvDrop = new GUIInventoryOtherDropDown(getState(), this, segmentController, element -> {
			if (element.getContent().getUserPointer() != null && element.getContent().getUserPointer() instanceof StashInventory) {
				StashInventory v = (StashInventory) element.getContent().getUserPointer();
				panel.getPlayerInput().deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setSecondInventory(v);
				mainPanel.onChange(false);
			}
		});
		switchInvDrop.onInit();
		switchInvDrop.rightDependentHalf = true;
		switchInvDrop.getPos().y = 2;

		pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(inventory.getParameter());
		 boolean canChangeProduction =
				 !(
						 pointUnsave.getType() == FACTORY_MICRO_ASSEMBLER_ID ||
						 pointUnsave.getType() == FACTORY_CAPSULE_REFINERY_ID ||
						 pointUnsave.getType() == FACTORY_CAPSULE_REFINERY_ADV_ID ||
						 pointUnsave.getType() == FACTORY_CORE_EXTRACTOR||
						 pointUnsave.getType() == FACTORY_BLOCK_RECYCLER_ID ||
						 pointUnsave.getType() == FACTORY_GAS_EXTRACTOR
				 );
	    //INSERTED CODE
		//If the block is a custom mod refinery, canChangeProduction should be false
		if(BlockConfig.isCustomModRefinery(pointUnsave.getType())){
			canChangeProduction = false;
		}
		final boolean finalCanChangeProduction = canChangeProduction;
		///

		final boolean activationToggleDisabledByHB = !(inventoryActive || //deactivation always possible
				(FactoryElementManager.ENABLE_EXTRACTORS_ON_HOMEBASE || !ElementKeyMap.isResourceExtractor(pointUnsave.getType()))
				|| !segmentController.isHomeBase());
		//can't activate extractors if homebase and config-disabled; can still deactivate them if for some reason they're still on though.

		int productionColumns = canChangeProduction? 2 : 1;
		GUIHorizontalButtonTablePane ac = new GUIHorizontalButtonTablePane(getState(), productionColumns, 1, this);
		ac.onInit();
		ac.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				pointUnsave.refresh();
				inventoryActive = pointUnsave.isActive();
                if (inventoryActive) return Lng.str("Deactivate Production");
                else if (!activationToggleDisabledByHB) return Lng.str("Activate Production");
				else return Lng.str("Cannot Activate (Disabled By Homebase Protection)");
            }
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(520);
					pointUnsave.refresh();
					System.err.println("FACTORY SETTING ACTIVE: "+pointUnsave.isActive()+" -> "+!pointUnsave.isActive());
					long index = ElementCollection.getEncodeActivation(pointUnsave, true, !pointUnsave.isActive(), false);
					pointUnsave.getSegment().getSegmentController().sendBlockActivation(index);
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
				return !activationToggleDisabledByHB;
			}
		});

		if(canChangeProduction) ac.addButton(1, 0, Lng.str("Change Production"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					popupMacroProductionDialog();
				}
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
		ac.getPos().x = 1;
		ac.getPos().y = 2 + 24;
		attach(ac);
		GUIHorizontalButtonTablePane pc = new GUIHorizontalButtonTablePane(getState(), 3, 1, this);
		pc.onInit();
		pc.addButton(0, 0, Lng.str("View Recipe"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					short production = inventory.getProduction();
					final FixedRecipe recipe;
					//INSERTED CODE
					CustomModRefinery customModRefinery = BlockConfig.customModRefineries.get(pointUnsave.getType());
					if(customModRefinery != null){
						// If the block is a mod refinery, use its custom recipe list
						recipe = customModRefinery.getRecipe();
					} else recipe = switch(pointUnsave.getType()) {
						case FACTORY_CAPSULE_REFINERY_ID -> capsuleRecipe;
						case FACTORY_CAPSULE_REFINERY_ADV_ID -> advCapsuleRecipe;
						case FACTORY_MICRO_ASSEMBLER_ID -> microAssemblerRecipe;
						case FACTORY_BLOCK_RECYCLER_ID -> recyclerRecipe;
						default -> null;
					};

					if (getState().getPlayerInputs().isEmpty()) {
						String name;
						//INSERTED CODE
						if(customModRefinery != null){
							// If the block is a mod refinery, use its custom name
							name = customModRefinery.getName();
						} else name = switch(pointUnsave.getType()){
							case FACTORY_CAPSULE_REFINERY_ID, FACTORY_CAPSULE_REFINERY_ADV_ID -> Lng.str("Capsule Refining");
							case FACTORY_MICRO_ASSEMBLER_ID -> Lng.str("Micro Assembly");
							case FACTORY_BLOCK_RECYCLER_ID -> Lng.str("Recycling");
							default -> ElementKeyMap.getInfo(inventory.getProduction()).getName(); //can throw a breaking nullpointer. is this desired?
						};

						PlayerGameOkCancelInput g = new PlayerGameOkCancelInput("PRODUCTION_POPUP_GRAPHM", getState(), 824, 400, Lng.str("Recipe Graph for %s", name), "") {

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
						AudioController.fireAudioEventID(521);
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
				return !finalCanChangeProduction || ElementKeyMap.isValidType(inventory.getProduction());
			}
		});
		pc.addButton(1, 0, Lng.str("Set Name"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					final PlayerGameTextInput playerTextInput = new PlayerGameTextInput("RENAME_INVENTORY", getState(), 10, Lng.str("Rename %s", inventory.getCustomName()), Lng.str("Enter a new name for this stash (10 characters max)")) {

						@Override
						public void onDeactivate() {
						}

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public boolean onInput(String entry) {
							if (((ManagedSegmentController<?>) segmentController).getManagerContainer().getNamedInventoriesClient().size() < 16) {
								((ManagedSegmentController<?>) segmentController).getManagerContainer().getInventoryNetworkObject().getInventoryCustomNameModBuffer().add(new RemoteLongString(new LongStringPair(inventory.getParameter(), entry.trim()), false));
							} else {
								getState().getController().popupAlertTextMessage(Lng.str("Can only name up to 16\ninventories!"), 0);
							}
							return true;
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public String[] getCommandPrefixes() {
							return null;
						}
					};
					playerTextInput.getInputPanel().onInit();
					GUITextButton removeName = new GUITextButton(getState(), 100, 20, ColorPalette.CANCEL, Lng.str("CLEAR NAME"), new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !playerTextInput.isActive();
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								((ManagedSegmentController<?>) segmentController).getManagerContainer().getInventoryNetworkObject().getInventoryCustomNameModBuffer().add(new RemoteLongString(new LongStringPair(inventory.getParameter(), ""), false));
								playerTextInput.deactivate();
							}
						}
					}) {

						/* (non-Javadoc)
						 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
						 */
						@Override
						public void draw() {
							setPos(playerTextInput.getInputPanel().getButtonCancel().getPos().x + playerTextInput.getInputPanel().getButtonCancel().getWidth() + 15, playerTextInput.getInputPanel().getButtonCancel().getPos().y, 0);
							super.draw();
						}
					};
					playerTextInput.getInputPanel().getBackground().attach(removeName);
					playerTextInput.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(522);
				}
			}
		}, null);
		pc.addButton(2, 0, new Object() {

			@Override
			public String toString() {
				String capStr = inventory.getProductionLimit() > 0 ? String.valueOf(inventory.getProductionLimit()) : Lng.str("none");
				return Lng.str("Cap: %s", capStr);
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameTextInput p = new PlayerGameTextInput("PRODUCTION_POPUP_COUNT_CHA", getState(), 8, Lng.str("Maximum Production"), Lng.str("Up to how many blocks should be produced (0 for unlimited)?"), "1") {

						@Override
						public boolean isOccluded() {
							return false;
						}

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
								final int limit = Math.max(0, Integer.parseInt(entry));
								inventory.setProductionLimit(limit);
								((ManagedSegmentController<?>) segmentController).getManagerContainer().getInventoryNetworkObject().getInventoryProductionLimitBuffer().add(new RemoteLongIntPair(new LongIntPair(inventory.getParameter(), limit), false));
								return true;
							} catch (NumberFormatException e) {
								setErrorMessage(Lng.str("Must be a number!"));
							}
							return false;
						}

						@Override
						public void onDeactivate() {
						}
					};
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(523);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !finalCanChangeProduction || ElementKeyMap.isValidType(inventory.getProduction());
			}
		});
		pc.getPos().x = 1;
		pc.getPos().y = 2 + 24 + 24;
		attach(pc);
		GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), Lng.str("Pull Tick"), this) {

			@Override
			public float getValue() {
				if (inventoryActive) {
					if (pointUnsave.getSegmentController() instanceof ManagedSegmentController<?>) {
						ManagerContainer<?> mm = ((ManagedSegmentController<?>) pointUnsave.getSegmentController()).getManagerContainer();
						ManagerModuleCollection<?, ?, ?> managerModuleCollection = mm.getModulesControllerMap().get(pointUnsave.getType());
						if (managerModuleCollection != null && managerModuleCollection.getElementManager() instanceof FactoryElementManager) {
							FactoryElementManager fac = (FactoryElementManager) managerModuleCollection.getElementManager();
							FactoryCollectionManager facCol = fac.getCollectionManagersMap().get(pointUnsave.getAbsoluteIndex());
							if (facCol != null) {
								if (facCol.getPowered() < 1.0f) {
									getColor().set(1, 0, 0, 1);
									text = Lng.str("Unpowered (%s%%). Needs %s power/sec", StringTools.formatPointZero(facCol.getPowered() * 100f), StringTools.formatPointZero(facCol.getPowerConsumedPerSecondCharging()));
									return 1;
								}
							}
						}
					}
					getColor().set(0, 1, 0, 1);
					//INSERTED CODE
					// Custom text for mod refineries
					boolean applyBakeTime = false;
					ConfigEntityManager configManager = null;
					if(pointUnsave.getSegmentController() instanceof ManagedSegmentController<?>) {
						ManagerContainer<?> container = ((ManagedSegmentController<?>) pointUnsave.getSegmentController()).getManagerContainer();
						configManager = container.getMainReactor().getConfigManager();
						applyBakeTime = true;
					}
					if(BlockConfig.isCustomModRefinery(pointUnsave.getType())){
						CustomModRefinery modRefinery = BlockConfig.customModRefineries.get(pointUnsave.getType());
						long bakeTime = modRefinery.getBakeTime();
						// Factor in bake time chambers
						if(applyBakeTime){
							bakeTime = (long) configManager.apply(StatusEffectType.FACTORY_BAKE_TIME_MULT, ((float) bakeTime));
						}
						double serverRunningTime = InventoryOptionsFactoryButtonPanel.this.getState().getController().getServerRunningTime() % bakeTime;
						float t = (float) (serverRunningTime / bakeTime);
						text = modRefinery.getProductionText();


						return t;
					///
					} else if (pointUnsave.getType() == FACTORY_MICRO_ASSEMBLER_ID) {
						long bakeTime = FactoryCollectionManager.MICRO_BAKE_TIME;
						if(applyBakeTime){
							bakeTime = (long) configManager.apply(StatusEffectType.FACTORY_BAKE_TIME_MULT, ((float) bakeTime));
						}
						double serverRunningTime = InventoryOptionsFactoryButtonPanel.this.getState().getController().getServerRunningTime() % bakeTime;
						float t = (float) (serverRunningTime / bakeTime);
						text = Lng.str("Refining Materials");
						return (t);
					} else if (pointUnsave.getType() == FACTORY_CAPSULE_REFINERY_ID || pointUnsave.getType() == FACTORY_CAPSULE_REFINERY_ADV_ID) {
						long bakeTime = FactoryCollectionManager.CAPSULE_BAKE_TIME;
						if (applyBakeTime) {
							bakeTime = (long) configManager.apply(StatusEffectType.FACTORY_BAKE_TIME_MULT, ((float) bakeTime));
						}
						double serverRunningTime = InventoryOptionsFactoryButtonPanel.this.getState().getController().getServerRunningTime() % bakeTime;
						float t = (float) (serverRunningTime / bakeTime);
						text = Lng.str("Producing Capsules");
						return (t);
					} else if (pointUnsave.getType() == FACTORY_BLOCK_RECYCLER_ID) {
						long bakeTime = FactoryCollectionManager.DEFAULT_BAKE_TIME;
						if(applyBakeTime){
							bakeTime = (long) configManager.apply(StatusEffectType.FACTORY_BAKE_TIME_MULT, ((float) bakeTime));
						}
						double serverRunningTime = InventoryOptionsFactoryButtonPanel.this.getState().getController().getServerRunningTime() % bakeTime;
						float t = (float) (serverRunningTime / bakeTime);
						text = Lng.str("Disassembling Blocks Into Components");
						return (t);
					} else if (ElementKeyMap.isResourceExtractor(pointUnsave.getType())) {
						long bakeTime = FactoryCollectionManager.EXTRACTOR_BAKE_TIME;
						if (applyBakeTime) {
							bakeTime = (long) configManager.apply(StatusEffectType.FACTORY_BAKE_TIME_MULT, ((float) bakeTime));
						}
						double serverRunningTime = InventoryOptionsFactoryButtonPanel.this.getState().getController().getServerRunningTime() % bakeTime;
						float t = (float) (serverRunningTime / bakeTime);
						if(pointUnsave.getType() == FACTORY_CORE_EXTRACTOR) {
							if(segmentController.getType() == SimpleTransformableSendableObject.EntityType.PLANET_ICO){
								text = Lng.str("Extracting Planet Resources");
							}
							else {
								text = Lng.str("No action: Planet core extractor is not on a planet!");
							}
						} else text = Lng.str("Extracting Resources");
						return (t);
					} else if (ElementKeyMap.isValidType(inventory.getProduction()) && ElementKeyMap.getFactorykeyset().contains(pointUnsave.getType())) {
						long bakeTime = (long) (ElementKeyMap.getInfo(inventory.getProduction()).getFactoryBakeTime() * 1000f);
						if(applyBakeTime){
							bakeTime = (long) configManager.apply(StatusEffectType.FACTORY_BAKE_TIME_MULT, ((float) bakeTime));
						}
						double serverRunningTime = InventoryOptionsFactoryButtonPanel.this.getState().getController().getServerRunningTime() % bakeTime;
						float t = (float) (serverRunningTime / bakeTime);
						text = Lng.str("Producing %s", (ElementKeyMap.getInfo(inventory.getProduction()).getName()));
						return (t);
					} else {
						double serverRunningTime = InventoryOptionsFactoryButtonPanel.this.getState().getController().getServerRunningTime() % ManagerContainer.TIME_STEP_STASH_PULL;
						float t = (float) (serverRunningTime / ManagerContainer.TIME_STEP_STASH_PULL);
						text = Lng.str("Nothing in Production");
						return (t);
					}
				} else {
					if (ElementKeyMap.isValidType(inventory.getProduction()) && ElementKeyMap.getFactorykeyset().contains(pointUnsave.getType())) {
						text = Lng.str("%s (Inactive)", (ElementKeyMap.getInfo(inventory.getProduction()).getName()));
					} else {
						text = Lng.str("Production Inactive");
					}
					return 0;
				}
			}
		};
		progress.getColor().set(InventoryPanelNew.PROGRESS_COLOR);
		progress.onInit();
		progress.getPos().x = 1;
		progress.getPos().y = 2 + 24 + 24 + 24;
		attach(progress);
		attach(switchInvDrop);
		GUIHorizontalArea f = new GUIHorizontalArea(getState(), HButtonType.TEXT_FIELD, 10) {

			@Override
			public void draw() {
				setWidth(InventoryOptionsFactoryButtonPanel.this.getWidth());
				super.draw();
			}
		};
		f.getPos().x = 1;
		f.getPos().y = 25 + 24 + 25 + 25;
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
		l.setTextSimple(new Object(){

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

	public void popupMicroOrMacroProductionDialog() {
		final FixedRecipe recipe;
		if (pointUnsave.getType() == FACTORY_CAPSULE_REFINERY_ID) {
			recipe = capsuleRecipe;
		} else if (pointUnsave.getType() == FACTORY_CAPSULE_REFINERY_ADV_ID) {
			recipe = advCapsuleRecipe;
		} else if (pointUnsave.getType() == FACTORY_MICRO_ASSEMBLER_ID) {
			recipe = microAssemblerRecipe;
		} else {
			throw new NullPointerException("Must be assigned");
		}
		if (getState().getPlayerInputs().isEmpty()) {
			PlayerOkCancelInput g = new PlayerOkCancelInput("KSKJDK", getState(), Lng.str("Production info"), "") {

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
					deactivate();
				}
			};
			g.getInputPanel().onInit();
			GUIScrollablePanel sc = new GUIScrollablePanel(820, 420, getState());
			sc.setContent(recipe.getGUI(getState()));
			g.getInputPanel().setCancelButton(false);
			g.getInputPanel().getContent().attach(sc);
			sc.dependent = g.getInputPanel().getContent();
			g.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(524);
		}
	}

	public void popupMacroProductionDialog() {
		ObjectArrayList<GUIElement> additional = new ObjectArrayList<GUIElement>();
		GUIAnchor nonA = new GUIAnchor(getState(), 600, 32);
		GUITextOverlay none = new GUITextOverlay(FontSize.SMALL_14, getState());
		none.getPos().y = 7;
		nonA.attach(none);
		none.setTextSimple(Lng.str("Use slot Recipe"));
		nonA.setUserPointer(new ElementInformation((short) 0, "Use Slot Recipe", null, new short[6]));
		additional.add(nonA);
		PlayerBlockTypeDropdownInputNew g = new PlayerBlockTypeDropdownInputNew("PRODUCTION_POPUP_MACRO", getState(), "Pick", additional, 0, 0, false) {

			@Override
			public ObjectArrayList<GUIElement> getElements(GameClientState state, String contain, ObjectArrayList<GUIElement> additionalElements) {
				ObjectArrayList<GUIElement> g = new ObjectArrayList<GUIElement>();
				if (additionalElements != null) {
					g.addAll(additionalElements);
				}
				int i = 0;
				for (ElementInformation info : ElementKeyMap.sortedByName) {
					if (info.isProducedIn(pointUnsave.getType()) && (contain.trim().length() == 0 || info.getName().toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH)))) {
						GUIAnchor guiAnchor = new GUIAnchor(state, 300, 32);
						g.add(guiAnchor);
						GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state);
						t.setTextSimple(info.getName());
						guiAnchor.setUserPointer(info);
						GUIBlockSprite b = new GUIBlockSprite(state, info.getId());
						b.getScale().set(0.5f, 0.5f, 0.5f);
						guiAnchor.attach(b);
						t.getPos().x = 50;
						t.getPos().y = 7;
						guiAnchor.attach(t);
						i++;
					}
				}
				return g;
			}

			@Override
			public void onAdditionalElementOk(Object userPointer) {
			}

			@Override
			public void onOk(ElementInformation info) {
				((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryProductionBuffer().add(ElementCollection.getIndex4((short) inventory.getParameterX(), (short) inventory.getParameterY(), (short) inventory.getParameterZ(), info.getId()));
			}

			@Override
			public void onOkMeta(MetaObject object) {
			}
		};
		g.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(525);
	}

	@Override
	public void clearFilter() {
		searchBar.reset();
	}
}
