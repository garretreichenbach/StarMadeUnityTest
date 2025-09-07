package org.schema.game.client.view.gui;

import java.awt.Font;
import java.util.Map.Entry;

import org.schema.game.client.controller.PlayerTradeDialogIndependentInput;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFaction.NPCFactionControlCommandType;
import org.schema.game.server.data.simulation.npc.NPCFaction.TurnType;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacy.NPCDipleExecType;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class RadialMenuDialogDebug extends RadialMenuDialog implements GUIActivationCallback {

	public RadialMenuDialogDebug(GameClientState state) {
		super(state);
	}

	@Override
	public RadialMenu createMenu(RadialMenuDialog radialMenuDialog) {
		final GameClientState s = getState();
		RadialMenu m = new RadialMenu(s, "DebugRadial", radialMenuDialog, 800, 600, 50, FontSize.BIG_20.getStyle(Font.BOLD));
		m.addItem("Trade Menu", new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !RadialMenuDialogDebug.this.isActive(s);
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(628);
					PlayerTradeDialogIndependentInput i = new PlayerTradeDialogIndependentInput(getState());
					i.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(629);
					deactivate();
				}
			}
		}, null);
		// RadialMenu subMenuEntityI = m.addItemAsSubMenu("NPC INV", new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		// 
		// @Override
		// public boolean isActive(InputState state) {
		// return true;
		// }
		// });
		// for(final Inventory inv : s.getGameState().getInventories().inventoriesList){
		// 
		// subMenuEntityI.addItem("Inventory "+inv, new GUICallback() {
		// 
		// @Override
		// public boolean isOccluded() {
		// return !RadialMenuDialogDebug.this.isActive(s);
		// }
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// getPGCM().inventoryAction(inv);
		// deactivate();
		// }
		// }
		// }, null);
		// }
		// 
		RadialMenu subMenuEntityTOP = m.addItemAsSubMenu("NPC CONTROL ", new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		for (final Faction fac : s.getFactionManager().getFactionCollection()) {
			if (fac.isNPC()) {
				final NPCFaction f = ((NPCFaction) fac);
				RadialMenu subMenu = subMenuEntityTOP.addItemAsSubMenu(f.getName() + "(" + f.getIdFaction() + ")", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				subMenu.addItem("Inventory", new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !RadialMenuDialogDebug.this.isActive(s);
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(630);
							getPGCM().inventoryAction(s.getGameState().getInventory(f.getIdFaction()));
							deactivate();
						}
					}
				}, null);
				RadialMenu subMenuTurns = subMenu.addItemAsSubMenu("Turn Options", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				for (final TurnType t : TurnType.values()) {
					subMenuTurns.addItem(new Object() {

						@Override
						public String toString() {
							return t.name() + " " + (f.getTurn(t).active ? "ON" : "OFF");
						}
					}, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !RadialMenuDialogDebug.this.isActive(s);
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(631);
								f.sendCommand(NPCFactionControlCommandType.TURN_MOD, t.ordinal(), !f.getTurn(t).active);
							}
						}
					}, null);
				}
				subMenu.addItem(new Object() {

					@Override
					public String toString() {
						return "Trigger Turn";
					}
				}, new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !RadialMenuDialogDebug.this.isActive(s);
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(632);
							f.sendCommand(NPCFactionControlCommandType.TURN_TRIGGER);
						}
					}
				}, null);
				RadialMenu subMenuDipl = subMenu.addItemAsSubMenu("Diplomacy", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				RadialMenu subMenuDiplTrigger = subMenuDipl.addItemAsSubMenu("Trigger Turn", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				for (final NPCDipleExecType t : NPCDipleExecType.values()) {
					subMenuDiplTrigger.addItem(new Object() {

						@Override
						public String toString() {
							return "Trigger " + t.name();
						}
					}, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !RadialMenuDialogDebug.this.isActive(s);
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(633);
								f.sendCommand(NPCFactionControlCommandType.DIPLOMACY_TRIGGER, t.ordinal());
							}
						}
					}, null);
				}
				RadialMenu subMenuDiplReactionTriggerPlayer = subMenuDipl.addItemAsSubMenu("Trigger Reaction (Player)", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				for (final Entry<Integer, String> t : f.getClientReactions().entrySet()) {
					subMenuDiplReactionTriggerPlayer.addItem(new Object() {

						@Override
						public String toString() {
							return t.getValue();
						}
					}, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !RadialMenuDialogDebug.this.isActive(s);
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(634);
								f.sendCommand(NPCFactionControlCommandType.DIPLOMACY_REACTION, t.getKey().intValue(), getState().getPlayer().getDbId());
							}
						}
					}, null);
				}
				RadialMenu subMenuDiplReactionTriggerFaction = subMenuDipl.addItemAsSubMenu("Trigger Reaction (Faction)", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				for (final Entry<Integer, String> t : f.getClientReactions().entrySet()) {
					subMenuDiplReactionTriggerFaction.addItem(new Object() {

						@Override
						public String toString() {
							return t.getValue();
						}
					}, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !RadialMenuDialogDebug.this.isActive(s);
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(635);
								f.sendCommand(NPCFactionControlCommandType.DIPLOMACY_REACTION, (long) t.getKey().intValue(), getState().getPlayer().getFactionId());
							}
						}
					}, null);
				}
				RadialMenu subMenuDiplActionSelf = subMenuDipl.addItemAsSubMenu("Trigger Player Action", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				for (final DiplActionType t : DiplActionType.values()) {
					subMenuDiplActionSelf.addItem(new Object() {

						@Override
						public String toString() {
							return t.name();
						}
					}, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !RadialMenuDialogDebug.this.isActive(s);
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(636);
								f.sendCommand(NPCFactionControlCommandType.DIPLOMACY_ACTION, t.ordinal(), getState().getPlayer().getDbId());
							}
						}
					}, null);
				}
				RadialMenu subMenuDiplPoints = subMenuDipl.addItemAsSubMenu("Modify Points", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				int[] p = new int[] { 1, 5, 25, 100, 500 };
				modPoints(f, s, subMenuDiplPoints, p, 1, "Player", getState().getPlayer().getDbId());
				modPoints(f, s, subMenuDiplPoints, p, -1, "Player", getState().getPlayer().getDbId());
				modPoints(f, s, subMenuDiplPoints, p, 1, "Faction", getState().getPlayer().getFactionId());
				modPoints(f, s, subMenuDiplPoints, p, -1, "Faction", getState().getPlayer().getFactionId());
				RadialMenu subMenuDiplActionFac = subMenuDipl.addItemAsSubMenu("Trigger Faction Action", new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				for (final DiplActionType t : DiplActionType.values()) {
					subMenuDiplActionFac.addItem(new Object() {

						@Override
						public String toString() {
							return t.name();
						}
					}, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !RadialMenuDialogDebug.this.isActive(s);
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(637);
								if (getState().getFaction() != null) {
									f.sendCommand(NPCFactionControlCommandType.DIPLOMACY_ACTION, t.ordinal(), (long) getState().getFaction().getIdFaction());
								} else {
									getState().getController().popupAlertTextMessage("Not in a faction", 0);
								}
							}
						}
					}, null);
				}
				subMenu.addItem(new Object() {

					@Override
					public String toString() {
						return "Log Credits";
					}
				}, new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !RadialMenuDialogDebug.this.isActive(s);
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(638);
							f.sendCommand(NPCFactionControlCommandType.LOG_CREDIT_STATUS);
						}
					}
				}, null);
			}
		}
		return m;
	}

	private void modPoints(final NPCFaction f, final GameClientState s, RadialMenu subMenuDiplPoints, int[] p, final int dir, final String name, final long entId) {
		for (final int pointModRaw : p) {
			final int pointMod = pointModRaw * dir;
			subMenuDiplPoints.addItem(new Object() {

				@Override
				public String toString() {
					return name + " " + pointMod;
				}
			}, new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !RadialMenuDialogDebug.this.isActive(s);
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(639);
						if (entId >= Integer.MAX_VALUE || getState().getFaction() != null) {
							f.sendCommand(NPCFactionControlCommandType.MOD_POINTS, pointMod, entId);
						} else {
							getState().getController().popupAlertTextMessage("Not in a faction", 0);
						}
					}
				}
			}, null);
		}
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
