package org.schema.game.client.view.gui.newgui;

import api.listener.events.gui.GUITopBarCreateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerCrewMenu;
import org.schema.game.client.controller.PlayerRaceInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.faction.FactionBlockDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.reactor.ReactorTreeDialog;
import org.schema.game.common.controller.*;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import java.util.ArrayList;

public class GUITopBar extends GUIElement implements DialogInterface {

	GUIHorizontalButtonTablePane taskPane;

	PlayerCrewMenu crew;

	private final ObjectArrayList<ExpandedButton> expandableButtons = new ObjectArrayList<ExpandedButton>();

	private GUIHorizontalArea tutButton;

	public GUITopBar(InputState state) {
		super(state);
	}
	//INSERTED CODE @54
	private ArrayList<GUIHorizontalArea> buttons = new ArrayList<>();
	//
	@Override
	public void cleanUp() {
		if (taskPane != null) {
			taskPane.cleanUp();
		}
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();
		taskPane.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		taskPane = new GUIHorizontalButtonTablePane(getState(), 9, 1, this);
		taskPane.onInit();
		int i = 0;
		taskPane.addButton(i++, 0, Lng.str("INVENTORY[%s]", KeyboardMappings.INVENTORY_PANEL.getKeyChar()), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(567);
					getPlayerGameControlManager().inventoryAction(null);
				}
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
				return getPlayerGameControlManager().getInventoryControlManager().isTreeActive();
			}
		});
		tutButton = taskPane.addButton(i++, 0, Lng.str("TUTORIAL[%s]", KeyboardMappings.TUTORIAL.getKeyChar()), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(568);
					if (!getState().getController().getTutorialController().isTutorialSelectorActive()) {
						getState().getController().getTutorialController().onActivateFromTopTaskBar();
						EngineSettings.TUTORIAL_BUTTON_BLINKING.setOn(false);
						tutButton.setBlinking(EngineSettings.TUTORIAL_BUTTON_BLINKING.isOn());
					} else {
						getState().getController().getTutorialController().onDeactivateFromTopTaskBar();
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
				return true;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getState().getController().getTutorialController().isTutorialSelectorActive();
			}
		});
		tutButton.setBlinking(EngineSettings.TUTORIAL_BUTTON_BLINKING.isOn());
		taskPane.addButton(i++, 0, new Object() {

			@Override
			public String toString() {
				if (getState().getCurrentClosestShop() == null) {
					return Lng.str("NO SHOP IN RANGE");
				} else {
					return Lng.str("SHOP");
				}
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(569);
					getPlayerGameControlManager().shopAction();
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getState().getCurrentClosestShop() != null;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getPlayerGameControlManager().getShopControlManager().isTreeActive();
			}
		});
		taskPane.addButton(i++, 0, Lng.str("NAVIGATION[%s]", KeyboardMappings.NAVIGATION_PANEL.getKeyChar()), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(570);
					getPlayerGameControlManager().navigationAction();
				}
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
				return getPlayerGameControlManager().getNavigationControlManager().isTreeActive();
			}
		});
		addShipExpander(i++);
		taskPane.addButton(i++, 0, Lng.str("CATALOG[%s]", KeyboardMappings.CATALOG_PANEL.getKeyChar()), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(571);
					getPlayerGameControlManager().catalogAction();
				}
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
				return getPlayerGameControlManager().getCatalogControlManager().isTreeActive();
			}
		});
		taskPane.addButton(i++, 0, Lng.str("FACTION[%s]", KeyboardMappings.FACTION_MENU.getKeyChar()), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(572);
					getPlayerGameControlManager().factionAction();
				}
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
				return getPlayerGameControlManager().getFactionControlManager().isTreeActive();
			}
		});
		addFleetCrewExpander(i++);
		addMessageExpander(i++);
		//INSERTED CODE @269
		GUITopBarCreateEvent guiTopBarCreateEvent = new GUITopBarCreateEvent(this, this.buttons, this.expandableButtons, this.taskPane);
		StarLoader.fireEvent(guiTopBarCreateEvent, false);
		//
	}

	private void addFleetCrewExpander(int index) {
		final ExpandedButton e = new ExpandedButton(getState(), 2);
		e.onInit();
		e.setMain(index, new Object() {

			@Override
			public String toString() {
				return Lng.str("CREW & FLEETS");
			}
		}, taskPane, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return false;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		e.addExpandedButton(Lng.str("CREW[%s]", KeyboardMappings.CREW_CONTROL.getKeyChar()), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(573);
					if (crew != null && getState().getController().getPlayerInputs().contains(crew)) {
						crew.deactivate();
						crew.getInputPanel().cleanUp();
						crew = null;
					} else {
						crew = new PlayerCrewMenu(getState());
						crew.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(574);
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
				return true;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return crew != null && getState().getController().getPlayerInputs().contains(crew);
			}
		});
		e.addExpandedButton(Lng.str("FLEETS[%s]", KeyboardMappings.FLEET_PANEL.getKeyChar()), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(575);
					getPlayerGameControlManager().fleetAction();
				}
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
				return getPlayerGameControlManager().getFleetControlManager().isTreeActive();
			}
		});
	}

	private void addShipExpander(int index) {
		ExpandedButton shipExpander = new ExpandedButton(getState(), 7);
		shipExpander.onInit();
		shipExpander.setMain(index, new Object() {

			@Override
			public String toString() {
				SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
				if (currentPlayerObject != null) {
					if (currentPlayerObject instanceof Ship) {
						return Lng.str("SHIP");
					} else if (currentPlayerObject instanceof Planet || currentPlayerObject instanceof PlanetIco) {
						return Lng.str("PLANET");
					} else if (currentPlayerObject instanceof SpaceStation) {
						return Lng.str("STATION");
					}
				}
				return Lng.str("STRUCTURE N/A");
			}
		}, taskPane, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return false;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		shipExpander.addExpandedButton(Lng.str("REACTOR[%s]", KeyboardMappings.REACTOR_KEY.getKeyChar()), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(576);
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject().hasAnyReactors()) {
						if (getState().getPlayerInputs().isEmpty()) {
							final ReactorTreeDialog d = new ReactorTreeDialog(getState(), ((ManagedSegmentController<?>) getState().getCurrentPlayerObject()));
							d.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(577);
						} else if (getState().getPlayerInputs().get(0) instanceof ReactorTreeDialog) {
							getState().getPlayerInputs().get(0).deactivate();
						}
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
				return getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject().hasAnyReactors();
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return !getState().getPlayerInputs().isEmpty() && getState().getPlayerInputs().get(0) instanceof ReactorTreeDialog;
			}
		});
		shipExpander.addExpandedButton(Lng.str("WEAPON[%s]", KeyboardMappings.WEAPON_PANEL.getKeyChar()), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(578);
					getPlayerGameControlManager().weaponAction();
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof Ship;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getPlayerGameControlManager().getWeaponControlManager().isTreeActive();
			}
		});
		shipExpander.addExpandedButton(Lng.str("ENTITY STRUCTURE[%s]", KeyboardMappings.STRUCTURE_PANEL.getKeyChar()), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(579);
					getPlayerGameControlManager().structureAction();
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getPlayerGameControlManager().getStructureControlManager().isTreeActive();
			}
		});
		shipExpander.addExpandedButton(Lng.str("AI[%s]", KeyboardMappings.AI_CONFIG_PANEL.getKeyChar()), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(580);
					getPlayerGameControlManager().aiConfigurationAction(null);
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getPlayerGameControlManager().getAiConfigurationManager().isTreeActive();
			}
		});
		shipExpander.addExpandedButton(Lng.str("CARGO"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(581);
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?>) {
						Long2ObjectOpenHashMap<StashInventory> dd = ((ManagedSegmentController<?>) getState().getCurrentPlayerObject()).getManagerContainer().getNamedInventoriesClient();
						if (dd.size() > 0) {
							getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getInventoryPanel().deactivateAllOther();
							getPlayerGameControlManager().inventoryAction(dd.values().iterator().next(), true, true);
						} else {
							getState().getController().popupAlertTextMessage(Lng.str("This structure has no\nnamed Stashes."), 0);
						}
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
				return getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?>;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getPlayerGameControlManager().getInventoryControlManager().isTreeActive() && getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getInventoryPanel().isChestOrFacActive();
			}
		});
		shipExpander.addExpandedButton(Lng.str("THRUST"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(582);
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof Ship) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().activateThrustManager(getState().getShip());
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
				return getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof Ship;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getThrustManager().isTreeActiveAndNotSuspended();
			}
		});
		shipExpander.addExpandedButton(new Object() {

			@Override
			public String toString() {
				if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
					SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
					if (c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0) {
						return Lng.str("FACTION[%s]", KeyboardMappings.FACTION_MENU.getKeyChar());
					} else {
						return Lng.str("NO FACTION BLOCK");
					}
				}
				return Lng.str("FACTION");
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(583);
					if (!getState().getController().getPlayerInputs().isEmpty() && getState().getController().getPlayerInputs().get(0) instanceof FactionBlockDialog) {
						getState().getController().getPlayerInputs().get(0).deactivate();
					} else if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
						SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
						if (c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0) {
							if (getState().getPlayer().getFactionId() != 0) {
								getPlayerGameControlManager().getPlayerIntercationManager().activateFactionDiag(c);
							} else if (c.getFactionId() != 0) {
								getPlayerGameControlManager().getPlayerIntercationManager().activateResetFactionIfOwner(c);
							} else {
							}
						}
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
				if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
					SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
					if (c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return !getState().getController().getPlayerInputs().isEmpty() && getState().getController().getPlayerInputs().get(0) instanceof FactionBlockDialog;
			}
		});
	}

	private void addMessageExpander(int index) {
		ExpandedButton messageExpander = new ExpandedButton(getState(), 3);
		messageExpander.onInit();
		messageExpander.setMain(index, Lng.str("PLAYER"), taskPane, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return false;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		// messageExpander.addExpandedButton(Lng.str("TRADE"), new GUICallback() {
		// @Override
		// public boolean isOccluded() {
		// return false;
		// }
		//
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if (event.pressedLeftMouse()) {
		// for(int i = 0; i < getState().getPlayerInputs().size(); i++){
		// if(getState().getPlayerInputs().get(i) instanceof PlayerManualTradeOverviewInput){
		// getState().getPlayerInputs().get(i).deactivate();
		// return;
		// }
		// }
		// PlayerManualTradeOverviewInput o = new PlayerManualTradeOverviewInput(getState());
		// o.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
		// }
		// }
		// }, new GUIActivationHighlightCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		//
		// @Override
		// public boolean isActive(InputState state) {
		// return true;
		// }
		//
		// @Override
		// public boolean isHighlighted(InputState state) {
		// for(int i = 0; i < getState().getPlayerInputs().size(); i++){
		// if(getState().getPlayerInputs().get(i) instanceof PlayerManualTradeOverviewInput){
		// return true;
		// }
		// }
		// return false;
		// }
		// });
		messageExpander.addExpandedButton(Lng.str("MAIL"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(584);
					getState().getGlobalGameControlManager().getIngameControlManager().activatePlayerMesssages();
				}
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
				return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerMailManager().isTreeActive();
			}
		});
		messageExpander.addExpandedButton(Lng.str("MESSAGE LOG"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(585);
					getState().getGlobalGameControlManager().getIngameControlManager().activateMesssageLog();
				}
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
				return getState().getGlobalGameControlManager().getIngameControlManager().getMessageLogManager().isTreeActive();
			}
		});
		messageExpander.addExpandedButton(Lng.str("RACES"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(586);
					boolean cc = true;
					for (DialogInterface x : getState().getController().getPlayerInputs()) {
						if (x instanceof PlayerRaceInput) {
							cc = false;
						}
					}
					if (cc) {
						PlayerRaceInput ri = new PlayerRaceInput(getState(), null);
						ri.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(587);
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
				return true;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return false;
			}
		});

		messageExpander.addExpandedButton(Lng.str("MUSIC"), new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					AudioController.fireAudioEventID(587);
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().openMusicManager();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationHighlightCallback() {
			@Override
			public boolean isHighlighted(InputState state) {
				return AudioController.instance.isMusicPlaying();
			}

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
	}

	public PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	@Override
	public float getHeight() {
		return 24;
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	@Override
	public float getWidth() {
		return GLFrame.getWidth();
	}

	private void addExpander(ExpandedButton expandedButton) {
		expandableButtons.add(expandedButton);
	}

	int exp = 0;

	private void handleExp(boolean expanded) {
		if (expanded) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.EXPAND)*/
			AudioController.fireAudioEventID(589);
			exp++;
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
			AudioController.fireAudioEventID(588);
			exp--;
		}
	}

	public class ExpandedButton extends GUIHorizontalButtonTablePane {
		int index = 0;

		private boolean expanded;

		public ExpandedButton(InputState state, int rows) {
			super(state, 1, rows, null);
			addExpander(this);
		}

		private class CB implements GUICallbackBlocking {

			private final GUICallback callback;

			public CB(GUICallback callback) {
				this.callback = callback;
			}

			@Override
			public boolean isOccluded() {
				return callback.isOccluded();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				callback.callback(callingGuiElement, event);
			}

			@Override
			public boolean isBlocking() {
				// only checked when inside the area
				return true;
			}

			@Override
			public void onBlockedCallbackExecuted() {
			}

			@Override
			public void onBlockedCallbackNotExecuted(boolean anyOtherBlockedCallbacksExecuted) {
			// if(!anyOtherBlockedCallbacksExecuted){
			// expanded = !expanded;
			// }
			}
		}

		public void addExpandedButton(Object text, final GUICallback callback, final GUIActivationHighlightCallback actCallback) {
			final CB blockingCallback = new CB(callback);
			final GUIHorizontalArea addButton = super.addButton(0, index, text, HButtonType.BUTTON_BLUE_MEDIUM, blockingCallback, new GUIActivationHighlightCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return actCallback.isVisible(state) && expanded;
				}

				@Override
				public boolean isHighlighted(InputState state) {
					return actCallback.isHighlighted(state);
				}

				@Override
				public boolean isActive(InputState state) {
					return actCallback.isActive(state);
				}
			});
			index++;
		}

		public void setMain(int index, Object name, GUIHorizontalButtonTablePane mother, final GUIActivationCallback c) {
			mother.addButton(index, 0, name, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						expanded = !expanded;
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(590);
						handleExp(expanded);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationHighlightCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return c.isActive(state);
				}

				@Override
				public boolean isHighlighted(InputState state) {
					return expanded;
				}
			});
			this.dependend = mother.getButtons()[0][index];
			this.setPos(0, mother.getHeight(), 0);
			mother.getButtons()[0][index].attach(this);
		}
	}

	@Override
	public GUIElement getInputPanel() {
		return this;
	}

	@Override
	public boolean allowChat() {
		return false;
	}

	@Override
	public void deactivate() {
	}

	@Override
	public boolean checkDeactivated() {
		return false;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	}

	@Override
	public void updateDeacivated() {
	}

	@Override
	public long getDeactivationTime() {
		return 0;
	}

	public ObjectArrayList<ExpandedButton> getExpandableButtons() {
		return expandableButtons;
	}

	public boolean isAnyExtended() {
		return exp > 0;
	}

	public boolean isAnyCallback(ObjectArrayList<GUICallback> guiCallbacks) {
		for (GUICallback g : guiCallbacks) {
			for (ExpandedButton b : expandableButtons) {
				if (b == g) {
					return true;
				}
			}
		}
		return false;
	}

	public void deextendAll() {
		for (ExpandedButton b : expandableButtons) {
			b.expanded = false;
			handleExp(b.expanded);
		}
	}

	@Override
	public void handleCharEvent(KeyEventInterface e) {
	}
}
