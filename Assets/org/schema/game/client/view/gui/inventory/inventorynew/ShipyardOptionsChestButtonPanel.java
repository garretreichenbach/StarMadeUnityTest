package org.schema.game.client.view.gui.inventory.inventorynew;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerButtonTilesInput;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerShipyardInfoDialog;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.character.PlayerExternalController;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.inventory.InventoryIconsNew;
import org.schema.game.client.view.gui.inventory.InventoryToolInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipyardManagerContainerInterface;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardCommandType;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardRequestType;
import org.schema.game.common.controller.elements.shipyard.orders.states.Constructing;
import org.schema.game.common.controller.elements.shipyard.orders.states.ShipyardState;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.LongStringPair;
import org.schema.game.network.objects.remote.RemoteLongString;
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
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class ShipyardOptionsChestButtonPanel extends GUIAnchor implements InventoryToolInterface {

	private String inventoryFilterText = "";

	private SecondaryInventoryPanelNew panel;

	private InventoryPanelNew mainPanel;

	private StashInventory inventory;

	private boolean inventoryActive;

	private SegmentController segmentController;

	private SegmentPiece pointUnsave;

	private InventoryFilterBar searchbar;

	public ShipyardOptionsChestButtonPanel(InputState state, SecondaryInventoryPanelNew panel, InventoryPanelNew mainPanel, StashInventory inventory) {
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

	public ShipyardCollectionManager getShipyard() {
		pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(inventory.getParameter());
		if (pointUnsave != null && segmentController instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) segmentController).getManagerContainer() instanceof ShipyardManagerContainerInterface) {
			ShipyardManagerContainerInterface sm = (ShipyardManagerContainerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer();
			ShipyardCollectionManager shipyardCollectionManager = sm.getShipyard().getCollectionManagersMap().get(pointUnsave.getAbsoluteIndex());
			return shipyardCollectionManager;
		}
		return null;
	}

	@Override
	public void onInit() {
		searchbar = new InventoryFilterBar(getState(), Lng.str("FILTER BY BLOCK NAME"), this, new TextCallback() {

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
		searchbar.getPos().x = UIScale.getUIScale().scale(1);
		searchbar.getPos().y = UIScale.getUIScale().scale(2);
		searchbar.leftDependentHalf = true;
		searchbar.onInit();
		attach(searchbar);
		segmentController = ((ManagerContainer<?>) inventory.getInventoryHolder()).getSegmentController();
		GUIInventoryOtherDropDown switchInvDrop = new GUIInventoryOtherDropDown(getState(), this, segmentController, element -> {
			if (element.getContent().getUserPointer() != null && element.getContent().getUserPointer() instanceof StashInventory) {
				StashInventory v = (StashInventory) element.getContent().getUserPointer();
				panel.playerInput.deactivate();
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager().setSecondInventory(v);
				mainPanel.onChange(false);
			}
		});
		switchInvDrop.onInit();
		switchInvDrop.rightDependentHalf = true;
		switchInvDrop.getPos().y = UIScale.getUIScale().smallinset;
		GUIHorizontalButtonTablePane ac = new GUIHorizontalButtonTablePane(getState(), 2, 2, this);
		final ShipyardCollectionManager yard = getShipyard();
		ac.onInit();
		pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(inventory.getParameter());
		ac.addButton(0, 0, Lng.str("Place Order"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					popupNewOrderDialog(yard);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				boolean active = false;
				for (ShipyardCommandType c : ShipyardCommandType.values()) {
					if (yard.isCommandUsable(c)) {
						active = true;
						break;
					}
				}
				return active;
			}
		});
		ac.addButton(1, 0, Lng.str("Undock"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					final SegmentController currentDocked = yard.getCurrentDocked();
					if (currentDocked != null && currentDocked.railController.isDockedAndExecuted() && !currentDocked.isVirtualBlueprint()) {
						PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Undock"), Lng.str("Do you really want to undock\nthe current ship '%s' from shipyard?", currentDocked.getRealName())) {

							@Override
							public void onDeactivate() {
							}

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void pressedOK() {
								final SegmentController currentDocked = yard.getCurrentDocked();
								if (currentDocked != null && currentDocked.railController.isDockedAndExecuted() && !currentDocked.isVirtualBlueprint()) {
									yard.sendStateRequestToServer(ShipyardRequestType.UNDOCK);
								}
								deactivate();
							}
						};
						c.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(531);
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
				return yard.isCurrentStateUndockable();
			}
		});
		ac.addButton(0, 1, Lng.str("Resource Status"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerShipyardInfoDialog p = new PlayerShipyardInfoDialog(getState(), yard);
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(532);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return yard.isCurrentStateWithGoalListClient();
			}
		});
		ac.addButton(1, 1, Lng.str("Cancel current Order"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					Class<? extends ShipyardState> s = yard.getCurrentClientStateClass();
					PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Cancel Order"), Lng.str("Do you really want to cancel the current order?\n") + (s == Constructing.class ? Lng.str("Blocks collected for construction will be refunded.") : "")) {

						@Override
						public void onDeactivate() {
						}

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void pressedOK() {
							yard.sendStateRequestToServer(ShipyardRequestType.CANCEL);
							deactivate();
						}
					};
					c.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(533);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return yard.isCurrentStateCancelClient();
			}
		});
		ac.getPos().x = UIScale.getUIScale().scale(1);
		ac.getPos().y = UIScale.getUIScale().scale(26);
		attach(ac);
		GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), Lng.str("Pull Tick"), this) {

			@Override
			public float getValue() {
				if (inventoryActive) {
					text = Lng.str("Pull Tick");
					double serverRunningTime = ShipyardOptionsChestButtonPanel.this.getState().getController().getServerRunningTime() % ManagerContainer.TIME_STEP_STASH_PULL;
					float t = (float) (serverRunningTime / ManagerContainer.TIME_STEP_STASH_PULL);
					return t;
				} else {
					text = Lng.str("Item Auto-Pull Inactive");
					return 0;
				}
			}
		};
		GUIHorizontalProgressBar progressOrder = new GUIHorizontalProgressBar(getState(), Lng.str("Current Order"), this) {

			private long wasUnpowered;

			@Override
			public float getValue() {
				getColor().set(InventoryPanelNew.PROGRESS_COLOR);
				if (!yard.isPowered() || (wasUnpowered > 0 && System.currentTimeMillis() - wasUnpowered < 500)) {
					if (wasUnpowered == 0) {
						wasUnpowered = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - wasUnpowered >= 500) {
						wasUnpowered = 0;
					}
					String powerCon;
					if (yard.getSegmentController().isUsingPowerReactors()) {
						powerCon = StringTools.formatPointZero(yard.getPowerConsumedPerSecondResting());
					} else {
						powerCon = StringTools.formatPointZero(yard.getPowerConsumption());
					}
					text = Lng.str("Unpowered! Needs %s e/sec", powerCon);
					getColor().set(0.7f, 0.0f, 0.0f, 1.0f);
					return 1;
				} else if (yard.getCurrentDocked() != null && !yard.isDockingValid()) {
					text = Lng.str("current dock/design too large for this shipyard");
					getColor().set(0.7f, 0.0f, 0.0f, 1.0f);
					return 1;
				} else {
					text = Lng.str("--- %s ---", yard.getStateDescription());
				}
				// System.err.println("CC: "+text);
				if (yard.isWorkingOnOrder()) {
					return (float) yard.getCompletionOrderPercent();
				} else {
					return 0;
				}
			}
		};
		progressOrder.getColor().set(InventoryPanelNew.PROGRESS_COLOR);
		progressOrder.onInit();
		progressOrder.getPos().x = UIScale.getUIScale().scale(1);
		progressOrder.getPos().y = UIScale.getUIScale().scale(3 + 3 + 24 + 24 + 24);
		attach(progressOrder);
		GUIHorizontalProgressBar progressLoaded = new GUIHorizontalProgressBar(getState(), Lng.str("Loaded Entity"), this) {

			@Override
			public float getValue() {
				text = Lng.str("no design loaded / no ship docked");
				VirtualBlueprintMetaItem currentDesign = yard.getCurrentDesignObject();
				if (currentDesign != null && yard.getCurrentDocked() != null) {
					text = Lng.str("Virtual: %s", yard.getCurrentDocked().getRealName());
					return 1;
				} else if (yard.getCurrentDocked() != null) {
					text = Lng.str("Real: %s", yard.getCurrentDocked().getRealName());
					return 1;
				}
				return 0;
			}
		};
		progressLoaded.getColor().set(new Vector4f(0.3f, 0.3f, 0.3f, 1.0f));
		progressLoaded.onInit();
		progressLoaded.getPos().x = UIScale.getUIScale().scale(1);
		progressLoaded.getPos().y = UIScale.getUIScale().scale(3 + 3 + 24 + 24 + 24 + 24);
		attach(progressLoaded);
		GUIHorizontalButtonTablePane pc = new GUIHorizontalButtonTablePane(getState(), 3, 1, this);
		pc.onInit();
		pc.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				pointUnsave.refresh();
				inventoryActive = pointUnsave.isActive();
				return inventoryActive ? Lng.str("Pulling on Construction") : Lng.str("No Block Pulling");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(534);
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
				pointUnsave.refresh();
				inventoryActive = pointUnsave.isActive();
				return inventoryActive;
			}
		});
		pc.addButton(1, 0, Lng.str("Set Inventory Name"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

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
					AudioController.fireAudioEventID(535);
				}
			}
		}, null);
		pc.addButton(2, 0, Lng.str("Edit Design"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !panel.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					SimpleTransformableSendableObject cc = getState().getPlayer().getFirstControlledTransformableWOExc();
					if (cc != null) {
						PlayerGameControlManager pp = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
						PlayerInteractionControlManager pi = pp.getPlayerIntercationManager();
						// Vector3i compPos = getShipyard().getControllerElement().getAbsolutePos(new Vector3i());
						Vector3i anc = getShipyard().getCurrentDocked().railController.previous.rail.getAbsolutePos(new Vector3i());
						anc.sub(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
						// compPos.sub(anc);
						// anc.negate();
						Vector3i extPos = anc;
						if (cc instanceof PlayerCharacter) {
							PlayerExternalController ch = pi.getPlayerCharacterManager();
							if (cc != null && cc instanceof PlayerCharacter && getShipyard().getCurrentDocked() != null) {
								pp.inventoryAction(inventory);
								SegmentPiece p = getShipyard().getCurrentDocked().getSegmentBuffer().getPointUnsave(Ship.core);
								if (p != null && ElementKeyMap.isValidType(p.getType())) {
									System.err.println("[CLIENT] Shipyard: Entering Design " + getShipyard().getCurrentDocked());
									boolean enter = ch.checkEnterAndEnterIfPossible(p);
									if (enter) {
										pi.getInShipControlManager().enteredFromShipyard(extPos);
										panel.playerInput.deactivate();
									} else {
										getState().getController().popupAlertTextMessage(Lng.str("Can't enter ship design. Core access denied."), 0);
									}
								} else {
									getState().getController().popupAlertTextMessage(Lng.str("Can't enter ship design. No core found.\n(Please report at help.star-made.org! This might be a bug)"), 0);
								}
							}
						} else if (cc instanceof SegmentController) {
							SegmentPiece p = getShipyard().getCurrentDocked().getSegmentBuffer().getPointUnsave(Ship.core);
							if (p != null) {
								pi.getInShipControlManager();
								InShipControlManager.switchEntered(p.getSegmentController());
								pi.getInShipControlManager().enteredFromShipyard(extPos);
								panel.playerInput.deactivate();
							}
						}
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
				return getShipyard().isLoadedDesignValid();
			}
		});
		pc.getPos().x = UIScale.getUIScale().scale(1);
		pc.getPos().y = UIScale.getUIScale().scale(2 + 3 + 3 + 24 + 24 + 24 + 24 + 24);
		attach(pc);
		progress.getColor().set(InventoryPanelNew.PROGRESS_COLOR);
		progress.onInit();
		progress.getPos().x = UIScale.getUIScale().scale(1);
		progress.getPos().y = UIScale.getUIScale().scale(2 + 3 + 3 + 24 + 24 + 24 + 24 + 24 + 24);
		attach(progress);
		attach(switchInvDrop);
		GUIHorizontalArea f = new GUIHorizontalArea(getState(), HButtonType.TEXT_FIELD, 10) {

			@Override
			public void draw() {
				setWidth(ShipyardOptionsChestButtonPanel.this.getWidth());
				super.draw();
			}
		};
		f.getPos().x = UIScale.getUIScale().scale(1);
		f.getPos().y = UIScale.getUIScale().scale(2 + 3 + 3 + 24 + 24 + 24 + 24 + 24 + 24 + 25);
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

	private void popupNewOrderDialog(ShipyardCollectionManager yard) {
		PlayerButtonTilesInput a = new PlayerButtonTilesInput("SHIPYARDORDER", getState(), 800, 640, Lng.str("Order"), 260, 165) {

			@Override
			public void onDeactivate() {
			}
		};
		for (ShipyardCommandType c : ShipyardCommandType.values()) {
			c.addTile(a, yard);
		}
		a.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(536);
	}

	@Override
	public void clearFilter() {
		searchbar.reset();
	}
}
