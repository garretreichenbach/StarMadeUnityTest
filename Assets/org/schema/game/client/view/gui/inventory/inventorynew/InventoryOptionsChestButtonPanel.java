package org.schema.game.client.view.gui.inventory.inventorynew;

import java.util.List;

import org.schema.game.client.controller.*;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventoryToolInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.network.objects.LongStringPair;
import org.schema.game.network.objects.ShortIntPair;
import org.schema.game.network.objects.remote.RemoteLongString;
import org.schema.game.network.objects.remote.RemoteShortIntPair;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InventoryOptionsChestButtonPanel extends GUIAnchor implements InventoryToolInterface {

	private String inventoryFilterText = "";

	private SecondaryInventoryPanelNew panel;

	private InventoryPanelNew mainPanel;

	private StashInventory inventory;

	private boolean inventoryActive;

	private SegmentController segmentController;

	private SegmentPiece pointUnsave;

	private InventoryFilterBar searchBar;

	public InventoryOptionsChestButtonPanel(InputState state, SecondaryInventoryPanelNew panel, InventoryPanelNew mainPanel, StashInventory inventory) {
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
		GUIHorizontalButtonTablePane ac = new GUIHorizontalButtonTablePane(getState(), 2, 1, this);
		ac.onInit();
		pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(inventory.getParameter());
		ac.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				pointUnsave.refresh();
				inventoryActive = pointUnsave.isActive();
				return inventoryActive ? Lng.str("Deactivate Storage Auto-Pull") : Lng.str("Activate Storage Auto-Pull");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(511);
					pointUnsave.refresh();
					long index = ElementCollection.getEncodeActivation(pointUnsave, true, !pointUnsave.isActive(), false);
					pointUnsave.getSegment().getSegmentController().sendBlockActivation(index);
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
				return inventoryActive;
			}
		});
		ac.addButton(1, 0, Lng.str("Change Items to Auto-Pull"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					popupProductionDialog(getState(), segmentController, inventory, pointUnsave);
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
		ac.getPos().y = 26;
		attach(ac);
		GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), Lng.str("Pull Tick"), this) {

			@Override
			public float getValue() {
				if (inventoryActive) {
					text = Lng.str("Pull Tick");
					double serverRunningTime = InventoryOptionsChestButtonPanel.this.getState().getController().getServerRunningTime() % ManagerContainer.TIME_STEP_STASH_PULL;
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
		progress.getPos().y = 2 + 24 + 24;
		attach(progress);
		GUIHorizontalButtonTablePane pc = new GUIHorizontalButtonTablePane(getState(), 2, 1, this);
		pc.onInit();
		pc.addButton(0, 0, Lng.str("Set Inventory Name"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					final PlayerGameTextInput playerTextInput = new PlayerGameTextInput("RENAME_INVENTORY", getState(), 10, Lng.str("Rename %s", inventory.getCustomName()), Lng.str("Enter a new name for this inventory (10 characters max)")) {

						@Override
						public void onDeactivate() {
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
							if (((ManagedSegmentController<?>) segmentController).getManagerContainer().getNamedInventoriesClient().size() < 16) {
								((ManagedSegmentController<?>) segmentController).getManagerContainer().getInventoryNetworkObject().getInventoryCustomNameModBuffer().add(new RemoteLongString(new LongStringPair(inventory.getParameter(), entry.trim()), false));
							} else {
								getState().getController().popupAlertTextMessage(Lng.str("Can only name up to 16\ninventories!"), 0);
							}
							return true;
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
					AudioController.fireAudioEventID(512);
				}
			}
		}, null);
		pc.addButton(1, 0, Lng.str("Use as personal Cargo"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(513);
					// calbacks are synched
					if (!getState().getPlayer().isInventoryPersonalCargo(inventory)) {
						if (inventory.getInventoryHolder() instanceof ManagerContainer<?>) {
							ManagerContainer<?> c = (ManagerContainer<?>) inventory.getInventoryHolder();
							SegmentPiece p = c.getSegmentController().getSegmentBuffer().getPointUnsave(inventory.getParameter());
							getState().getPlayer().requestCargoInventoryChange(p);
						} else {
							assert (false);
						}
					} else {
						getState().getPlayer().requestCargoInventoryChange(null);
					}
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return panel.isActive();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				synchronized (getState()) {
					getState().setSynched();
					try {
						return getState().getPlayer().isInventoryPersonalCargo(inventory);
					} finally {
						getState().setUnsynched();
					}
				}
			}
		});
		pc.getPos().x = 1;
		pc.getPos().y = 2 + 3 + 24 + 24 + 24;
		attach(pc);
		attach(switchInvDrop);
		GUIHorizontalArea f = new GUIHorizontalArea(getState(), HButtonType.TEXT_FIELD, 10) {

			@Override
			public void draw() {
				setWidth(InventoryOptionsChestButtonPanel.this.getWidth());
				super.draw();
			}
		};
		f.getPos().x = 1;
		f.getPos().y = 2 + 3 + 24 + 24 + 24 + 24 + 1;
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
		l.setTextSimple(new Object() {

			@Override
			public String toString() {
				return inventory.getVolumeString();
			}
		});
		l.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		f.attach(l);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#isActive()
	 */
	@Override
	public boolean isActive() {
		return panel.isActive();
	}

	@Override
	public String getText() {
		return inventoryFilterText;
	}

	@Override
	public boolean isActiveInventory(InventoryIconsNew inventoryIcons) {
		return mainPanel.isInventoryActive(inventoryIcons);
	}

	public static void popupProductionDialog(final GameClientState state, final SegmentController segmentController, final StashInventory inventory, final SegmentPiece pointUnsave) {
		PlayerGameOkCancelInput main = new PlayerGameOkCancelInput("PRODUCTION_POPUP_MAIN", state, UIScale.getUIScale().scale(500), UIScale.getUIScale().scale(400), Lng.str("Filter"), Lng.str("Every block added here will be taken out of every inventory block,\nlike chests/factories/etc, that is connected to it per tick")) {

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
		main.getInputPanel().setCancelButton(false);
		main.getInputPanel().onInit();
		main.getInputPanel().getContent().setHeight(80);
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(80));
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(100));
		final InventoryStashProductionScrollableListNew c = new InventoryStashProductionScrollableListNew(state, ((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().getContent(1), inventory, pointUnsave);
		c.onInit();
		((GUIDialogWindow) main.getInputPanel().getBackground()).getMainContentPane().getContent(1).attach(c);
		GUITextButton addEdit = new GUITextButton(state, 80, 24, ColorPalette.OK, Lng.str("ADD SINGLE"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayerInputs().size() == 1) {
					PlayerBlockTypeDropdownInputNew g = new PlayerBlockTypeDropdownInputNew("PRODUCTION_POPUP_PICK", state, Lng.str("Pick"), 2, 0, true) {

						@Override
						public void onAdditionalElementOk(Object userPointer) {
						}

						@Override
						public void onOk(ElementInformation info) {
							int amount = Math.max(1, getNumberValue(0));
							int fillUpTo = Math.max(0, getNumberValue(1));
							inventory.getFilter().filter.put(info.getId(), amount);
							inventory.getFilter().fillUpTo.put(info.getId(), fillUpTo);
							((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), info.getId(), amount), segmentController.isOnServer()));
							((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), info.getId(), fillUpTo), segmentController.isOnServer()));
							c.flagDirty();
						}

						@Override
						public void onOkMeta(MetaObject object) {
							short type = object.getObjectBlockID();
							if (MetaObjectManager.subIdTypes.contains(type)) {
								type -= Math.abs(type * 256);
								type -= object.getSubObjectId();
							}
							int amount = Math.max(1, getNumberValue(0));
							int fillUpTo = Math.max(0, getNumberValue(1));
							inventory.getFilter().filter.put(type, amount);
							inventory.getFilter().fillUpTo.put(type, fillUpTo);
							((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, amount), segmentController.isOnServer()));
							((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, fillUpTo), segmentController.isOnServer()));
							c.flagDirty();
						}

						@Override
						public boolean includeInfo(ElementInformation info) {
							return info.getSourceReference() <= 0;
						}
					};
					g.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(514);
				}
			}
		});
		GUITextButton addCategory = new GUITextButton(state, 100, 24, ColorPalette.OK, Lng.str("ADD CATEGORY"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayerInputs().size() == 1) {
					PlayerBlockCategoryDropdownInputNew g = new PlayerBlockCategoryDropdownInputNew("PRODUCTION_POPUP_PICK_CAT", state, Lng.str("Pick"), 2, 0, true) {

						@Override
						public void onAdditionalElementOk(Object userPointer) {
						}

						@Override
						public void onOk(ElementCategory cat) {
							List<ElementInformation> infoElementsRecursive = cat.getInfoElementsRecursive(new ObjectArrayList<ElementInformation>());
							for(ElementInformation info : infoElementsRecursive){
								if (info.getSourceReference() > 0) continue; // not in uu?>
								int amount = Math.max(1, getNumberValue(0));
								int fillUpTo = Math.max(0, getNumberValue(1));
								inventory.getFilter().filter.put(info.getId(), amount);
								inventory.getFilter().fillUpTo.put(info.getId(), fillUpTo);
								((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), info.getId(), amount), segmentController.isOnServer()));
								((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), info.getId(), fillUpTo), segmentController.isOnServer()));
							}
							c.flagDirty();
						}

						@Override
						public void onOkMeta() {
							for (MetaObjectType t : MetaObjectType.values()) {
								short type = t.type;
								if (MetaObjectManager.subIdTypes.contains(type)) {
									short[] sTypes = MetaObjectManager.getSubTypes(t);
									for (short sType : sTypes) {
										type = t.type;
										type -= Math.abs(type * 256);
										type -= sType;
										int amount = Math.max(1, getNumberValue(0));
										int fillUpTo = Math.max(0, getNumberValue(1));
										inventory.getFilter().filter.put(type, amount);
										inventory.getFilter().fillUpTo.put(type, fillUpTo);
										((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, amount), segmentController.isOnServer()));
										((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, fillUpTo), segmentController.isOnServer()));
									}
								} else {
									int amount = Math.max(1, getNumberValue(0));
									int fillUpTo = Math.max(0, getNumberValue(1));
									inventory.getFilter().filter.put(type, amount);
									inventory.getFilter().fillUpTo.put(type, fillUpTo);
									((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, amount), segmentController.isOnServer()));
									((NetworkInventoryInterface) segmentController.getNetworkObject()).getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(pointUnsave.getAbsoluteIndex(), type, fillUpTo), segmentController.isOnServer()));
								}
							}
							c.flagDirty();
						}
					};
					g.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(515);
				}
			}
		});
		GUITextButton clearAll = new GUITextButton(state, 120, 24, ColorPalette.CANCEL, Lng.str("CLEAR ALL PULLS"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayerInputs().size() == 1) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(516);
					c.apply((short) 0, 0, 0, true);
					c.flagDirty();
				}
			}
		});
		GUITextButton addAll = new GUITextButton(state, 60, 24, ColorPalette.OK, Lng.str("ADD ALL"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && state.getPlayerInputs().size() == 1) {
					PlayerGameTextInput p = new PlayerGameTextInput("PRODUCTION_POPUP_COUNT_CHA", state, 8, Lng.str("Count Taken Per Tick"), Lng.str("How many blocks of everything should be pulled per tick?"), "1") {

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
								final int amount = Integer.parseInt(entry);
								if (amount >= 0) {
									PlayerGameTextInput p = new PlayerGameTextInput("PRODUCTION_POPUP_COUNT_CHA", state, 8, Lng.str("Maximum Count Taken"), Lng.str("Up to how many blocks in storage should be pulled (0 for unlimited)?"), "1") {

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
												final int limit = Integer.parseInt(entry);
												if (amount >= 0) {
													c.apply((short) 0, amount, Math.max(0, limit), true);
													c.flagDirty();
													return true;
												} else {
													setErrorMessage(Lng.str("Must be positive!"));
												}
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
									AudioController.fireAudioEventID(517);
									return true;
								} else {
									setErrorMessage(Lng.str("Must be positive!"));
								}
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
					AudioController.fireAudioEventID(518);
				}
			}
		});
		addEdit.setPos(2, 40, 0);
		addCategory.setPos(90, 40, 0);
		addAll.setPos(220, 40, 0);
		clearAll.setPos(320, 40, 0);
		main.getInputPanel().getContent().attach(addEdit);
		main.getInputPanel().getContent().attach(addCategory);
		main.getInputPanel().getContent().attach(clearAll);
		main.getInputPanel().getContent().attach(addAll);
		main.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(519);
	}

	@Override
	public void clearFilter() {
		searchBar.reset();
	}
}
