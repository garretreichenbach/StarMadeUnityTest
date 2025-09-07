package org.schema.game.client.view.gui;

import java.awt.Font;

import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerRaceInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.faction.FactionBlockDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.reactor.ReactorTreeDialog;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentController.PullPermission;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class RadialMenuDialogMain extends RadialMenuDialog implements GUIActivationCallback {

	public RadialMenuDialogMain(GameClientState state) {
		super(state);
	}

	@Override
	public RadialMenu createMenu(RadialMenuDialog radialMenuDialog) {
		final GameClientState s = getState();
		RadialMenu m = new RadialMenu(s, "MainRadial", radialMenuDialog, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(600), UIScale.getUIScale().scale(50), FontSize.BIG_20.getStyle(Font.BOLD));
		m.addItem(Lng.str("Inventory"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(640);
					getPGCM().inventoryAction(null);
					deactivate();
				}
			}
		}, this);
		m.addItem(Lng.str("Faction"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(641);
					getPGCM().factionAction();
					deactivate();
				}
			}
		}, this);
		m.addItem(Lng.str("Navigation"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(642);
					getPGCM().navigationAction();
					deactivate();
				}
			}
		}, this);
		m.addItem(Lng.str("Galaxy Map"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(643);
					getPGCM().galaxyMapAction();
					deactivate();
				}
			}
		}, this);
		m.addItem(Lng.str("Catalog"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(644);
					getPGCM().catalogAction();
					deactivate();
				}
			}
		}, this);
		m.addItem(Lng.str("Shop"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(645);
					getPGCM().shopAction();
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return RadialMenuDialogMain.this.isActive(s) && s.isInShopDistance();
			}
		});
		m.addItem(Lng.str("Fleet"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(646);
					getPGCM().fleetAction();
					deactivate();
				}
			}
		}, this);
		final GUIActivationCallback entiyAct = new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return RadialMenuDialogMain.this.isActive(s) && getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?>;
			}
		};
		RadialMenu subMenuEntity = m.addItemAsSubMenu(getOwnEntityName(), entiyAct);
		RadialMenu mines = subMenuEntity.addItemAsSubMenu(Lng.str("Mines"), entiyAct);
		mines.addItem(Lng.str("Arm mines in sector"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !entiyAct.isActive(s) || !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(647);
					deactivate();
					getState().getController().getMineController().requestArmedClient(getState().getPlayer().getId(), getState().getPlayer().getCurrentSector());
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return entiyAct.isVisible(state);
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state);
			}
		});
		mines.addItem(Lng.str("Arm all mines"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !entiyAct.isActive(s) || !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(648);
					deactivate();
					getState().getController().getMineController().requestArmedAllClient(getState().getPlayer().getId());
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return entiyAct.isVisible(state);
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state);
			}
		});
		String acti = getState().getPlayer().getMineAutoArmSeconds() >= 0 ? StringTools.formatCountdown(getState().getPlayer().getMineAutoArmSeconds()) : Lng.str("manual");
		mines.addItem(Lng.str("Set auto arm timer (%s)", acti), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !entiyAct.isActive(s) || !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(649);
					deactivate();
					PlayerTextInput secsInput = new PlayerTextInput("INPOUTMINESEC", getState(), 8, Lng.str("Mine Timer"), Lng.str("Enter mine arm timer in seconds. Use -1 for manual arming")) {

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
								int secs = Math.max(-1, Integer.parseInt(entry));
								((GameClientState) getState()).getPlayer().requestMineArmTimerChange(secs);
								return true;
							} catch (NumberFormatException e) {
								getState().getController().popupAlertTextMessage(Lng.str("Must be a number"));
							}
							return false;
						}

						@Override
						public void onDeactivate() {
						}
					};
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return entiyAct.isVisible(state);
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state);
			}
		});
		subMenuEntity.addItem(Lng.str("Reactor"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !entiyAct.isActive(s) || !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(650);
					deactivate();
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?>) {
						ReactorTreeDialog d = new ReactorTreeDialog(getState(), (ManagedSegmentController<?>) getState().getCurrentPlayerObject());
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(651);
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return entiyAct.isVisible(state);
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state) && ((SegmentController) getState().getCurrentPlayerObject()).hasAnyReactors();
			}
		});
		subMenuEntity.addItem(Lng.str("Structure"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !entiyAct.isActive(s) || !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(652);
					getPGCM().structureAction();
					deactivate();
				}
			}
		}, entiyAct);
		subMenuEntity.addItem(Lng.str("Weapon"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(653);
					getPGCM().weaponAction();
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state) && s.getCurrentPlayerObject() instanceof Ship;
			}
		});
		subMenuEntity.addItem(Lng.str("AI Config"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(654);
					getPGCM().aiConfigurationAction(null);
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state) && s.getCurrentPlayerObject() instanceof Ship;
			}
		});
		subMenuEntity.addItem(Lng.str("Cargo"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(655);
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController && getState().getCurrentPlayerObject() instanceof ManagedSegmentController<?>) {
						Long2ObjectOpenHashMap<StashInventory> dd = ((ManagedSegmentController<?>) getState().getCurrentPlayerObject()).getManagerContainer().getNamedInventoriesClient();
						if (dd.size() > 0) {
							getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().getInventoryPanel().deactivateAllOther();
							getPGCM().inventoryAction(dd.values().iterator().next(), true, true);
						} else {
							getState().getController().popupAlertTextMessage(Lng.str("This structure has no\nnamed Stashes."), 0);
						}
					}
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state) && s.getCurrentPlayerObject() instanceof ManagedSegmentController<?>;
			}
		});
		subMenuEntity.addItem(Lng.str("Thrusters"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(656);
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof Ship) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().activateThrustManager(getState().getShip());
					}
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return entiyAct.isActive(state) && s.getCurrentPlayerObject() instanceof Ship;
			}
		});
		subMenuEntity.addItem(new Object() {

			@Override
			public String toString() {
				if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
					SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
					return c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0 ? Lng.str("Faction") : Lng.str("Faction\n(No Faction Block)");
				} else {
					return Lng.str("n/a");
				}
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(657);
					if (!getState().getController().getPlayerInputs().isEmpty() && getState().getController().getPlayerInputs().get(0) instanceof FactionBlockDialog) {
						getState().getController().getPlayerInputs().get(0).deactivate();
					} else if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
						SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
						if (c.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) > 0) {
							if (getState().getPlayer().getFactionId() != 0) {
								getPGCM().getPlayerIntercationManager().activateFactionDiag(c);
							} else if (c.getFactionId() != 0) {
								getPGCM().getPlayerIntercationManager().activateResetFactionIfOwner(c);
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
		RadialMenu loadUnloadSettings = subMenuEntity.addItemAsSubMenu(Lng.str("Load/Unload\nRail Setting"), this);
		addLoadUnloadSettings(loadUnloadSettings, s);
		RadialMenu messages = m.addItemAsSubMenu(Lng.str("Data Pad"), this);
		messages.addItem(Lng.str("Mail"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(658);
					getState().getGlobalGameControlManager().getIngameControlManager().activatePlayerMesssages();
					deactivate();
				}
			}
		}, this);
		messages.addItem(Lng.str("Message Log"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(659);
					getState().getGlobalGameControlManager().getIngameControlManager().activateMesssageLog();
					deactivate();
				}
			}
		}, this);
		messages.addItem(Lng.str("Racing"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(660);
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
						AudioController.fireAudioEventID(661);
					}
					deactivate();
				}
			}
		}, this);
		return m;
	}

	private void addLoadUnloadSettings(RadialMenu loadUnloadSettings, final GameClientState s) {
		for (final PullPermission p : PullPermission.values()) {
			loadUnloadSettings.addItem(new Object() {

				@Override
				public String toString() {
					return p.desc;
				}
			}, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !RadialMenuDialogMain.this.isActive(s);
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(662);
						if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
							SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
							c.getNetworkObject().pullPermissionChangeBuffer.add((byte) p.ordinal());
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
						return true;
					}
					return false;
				}

				@Override
				public boolean isHighlighted(InputState state) {
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
						SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
						return c.pullPermission == p;
					}
					return false;
				}
			}, false);
		}
		loadUnloadSettings.addItem(new Object() {

			@Override
			public String toString() {
				if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
					SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
					if (c.railController.isDocked()) {
						if (c.lastAllowed != c.railController.previous.rail.getSegmentController().dbId) {
							return Lng.str("Allow current\nDock to pull");
						} else {
							return Lng.str("Disable current\nDock pulling");
						}
					} else {
						return Lng.str("Allow current\n(not docked)");
					}
				}
				return "-";
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogMain.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(663);
					if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
						SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
						if (c.railController.isDocked()) {
							if (c.lastAllowed != c.railController.previous.rail.getSegmentController().dbId) {
								c.getNetworkObject().pullPermissionAskAnswerBuffer.add(c.railController.previous.rail.getSegmentController().dbId);
							} else {
								c.getNetworkObject().pullPermissionAskAnswerBuffer.add(0);
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
					return c.railController.isDocked();
				}
				return false;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				if (getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
					SegmentController c = (SegmentController) getState().getCurrentPlayerObject();
					return c.railController.isDocked() && c.lastAllowed == c.railController.previous.rail.getSegmentController().dbId;
				}
				return false;
			}
		}, false);
	}

	private Object getOwnEntityName() {
		if (getState().getCurrentPlayerObject() != null) {
			return getState().getCurrentPlayerObject().getType().getName();
		}
		return Lng.str("unknown entity");
	}

	public PlayerGameControlManager getPGCM() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	@Override
	public boolean isVisible(InputState state) {
		return true;
	}

	@Override
	public boolean isActive(InputState state) {
		return super.isActive();
	}
}
