package org.schema.game.client.controller.manager.ingame.map;

import api.common.GameClient;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.gamemap.entry.GasPlanetEntityMapEntry;
import org.schema.game.client.data.gamemap.entry.PlanetEntityMapEntry;
import org.schema.game.client.data.gamemap.entry.SelectableMapEntry;
import org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gamemap.StarPosition;
import org.schema.game.client.view.gui.RadialMenu;
import org.schema.game.client.view.gui.RadialMenuDialog;
import org.schema.game.client.view.gui.navigation.navigationnew.SaveCoordinateDialog;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.player.SavedCoordinate;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector3f;

public class MapEntryOptionsMenu extends RadialMenuDialog {

	private final SelectableMapEntry entry;
	private final String name;

	public MapEntryOptionsMenu(SelectableMapEntry entry, String name) {
		super(GameClient.getClientState());
		this.entry = entry;
		this.name = name;
	}

	@Override
	public RadialMenu createMenu(RadialMenuDialog radialMenuDialog) {
		RadialMenu menu = new RadialMenu(getState(), "MapOptionsRadialMenu", radialMenuDialog, 500, 500, 100, FontLibrary.FontSize.SMALL_14, getEntryType() + "\n" + getEntityName() + "\n" + getEntryPos().toString());
		menu.center.setCallback(new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(entry == null) getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().navigateTo(getEntryPos());
					else getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().navigateTo(entry);
					deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		menu.setForceBackButton(true);

		if(!(entry instanceof SavedCoordinate)) {
			menu.addItem(Lng.str("Set Waypoint"), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						getState().getController().getClientGameData().setWaypoint(getEntryPos());
						deactivate();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
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
		} else {
			menu.addItem(Lng.str("Remove Waypoint"), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						SavedCoordinate savedCoordinate = getState().getController().getClientGameData().getSavedCoordinates().stream().filter(c -> c.getSector().equals(getEntryPos())).findFirst().orElse(null);
						if(savedCoordinate != null) {
							savedCoordinate.setRemoveFlag(true);
							getState().getController().getClientGameData().removeSavedCoordinate(savedCoordinate);
						}
						deactivate();
					}
				}

				@Override
				public boolean isOccluded() {
					return !getState().getController().getClientGameData().hasWaypointAt(getEntryPos());
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return getState().getController().getClientGameData().hasWaypointAt(getEntryPos());
				}
			});
		}

		if(!getState().getController().getClientGameData().hasWaypointAt(getEntryPos())) {
			menu.addItem(Lng.str("Save Waypoint"), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						(new SaveCoordinateDialog(getState(), getEntryPos())).activate();
						deactivate();
					}
				}

				@Override
				public boolean isOccluded() {
					return getState().getController().getClientGameData().hasWaypointAt(getEntryPos());
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return !getState().getController().getClientGameData().hasWaypointAt(getEntryPos());
				}
			});
		}

		menu.addItem(Lng.str("Fleet Options"), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFleetControlManager().setActive(true);
					deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return getState().getFleetManager().getAvailableFleets(getState().getPlayerName()).isEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !getState().getFleetManager().getAvailableFleets(getState().getPlayerName()).isEmpty();
			}
		});

		menu.addItem(Lng.str("Copy to Clipboard"), new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection("(" + getEntryPos().x + ", " + getEntryPos().y + ", " + getEntryPos().z + ")"), null);
					deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
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

		if(entry instanceof FleetMember.FleetMemberMapIndication) {
			FleetMember member = ((FleetMember.FleetMemberMapIndication) entry).getMember();
			if(getState().getFleetManager().getAvailableFleetsClient().contains(member.getFleetByOwner(getState().getPlayer().getName()))) {
				menu.addItem(new Object() {
					@Override
					public String toString() {
						if(MapControllerManager.selectedFleets.contains(entry)) return Lng.str("Unselect");
						else return Lng.str("Select");
					}
				}.toString(), new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							if(MapControllerManager.selectedFleets.contains(entry)) {
								MapControllerManager.selectedFleets.remove(entry);
								((FleetMember.FleetMemberMapIndication) entry).s = false;
							} else {
								MapControllerManager.selectedFleets.add((FleetMember.FleetMemberMapIndication) entry);
								((FleetMember.FleetMemberMapIndication) entry).s = true;
							}
							deactivate();
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
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
			}
		}
		if(!MapControllerManager.selectedFleets.isEmpty()) {
			menu.addItem(Lng.str("Move To"), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						for(FleetMember.FleetMemberMapIndication m : MapControllerManager.selectedFleets) {
							if(m.getMember().getFactionId() == getState().getPlayer().getFactionId()) m.getMember().getFleetByOwner(getState().getPlayerName()).sendFleetCommand(FleetCommandTypes.MOVE_FLEET, getEntryPos());
						}
						MapControllerManager.resetFleets();
						deactivate();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
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
			menu.addItem(Lng.str("Attack"), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						for(FleetMember.FleetMemberMapIndication m : MapControllerManager.selectedFleets) {
							m.getMember().getFleetByOwner(getState().getPlayerName()).sendFleetCommand(FleetCommandTypes.FLEET_ATTACK, getEntryPos());
						}
						MapControllerManager.resetFleets();
						deactivate();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
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
			menu.addItem(Lng.str("Defend"), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						for(FleetMember.FleetMemberMapIndication m : MapControllerManager.selectedFleets) {
							m.getMember().getFleetByOwner(getState().getPlayerName()).sendFleetCommand(FleetCommandTypes.FLEET_DEFEND, getEntryPos());
						}
						MapControllerManager.resetFleets();
						deactivate();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
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
			if(entry != null) {
				for(FleetMember.FleetMemberMapIndication indication : MapControllerManager.selectedFleets) {
					if(indication.equals(entry)) {
						Fleet fleet = indication.getMember().getFleetByOwner(getState().getPlayerName());
						if(fleet != null) {
							if(fleet.getMembers().size() > 1) {
								menu.addItem(Lng.str("Escort"), new GUICallback() {
									@Override
									public void callback(GUIElement callingGuiElement, MouseEvent event) {
										if(event.pressedLeftMouse()) {
											for(FleetMember.FleetMemberMapIndication m : MapControllerManager.selectedFleets) {
												if(m.getMember().getFactionId() == getState().getPlayer().getFactionId()) m.getMember().getFleetByOwner(getState().getPlayerName()).sendFleetCommand(FleetCommandTypes.ESCORT);
											}
											MapControllerManager.resetFleets();
											deactivate();
										}
									}

									@Override
									public boolean isOccluded() {
										return false;
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
							}
							if(fleet.canStealth()) {
								menu.addItem(new Object() {
									@Override
									public String toString() {
										if(fleet.isStealth()) {
											return Lng.str("Toggle Stealth (ON)");
										} else {
											return Lng.str("Toggle Stealth (OFF)");
										}
									}
								}.toString(), new GUICallback() {
									@Override
									public void callback(GUIElement callingGuiElement, MouseEvent event) {
										if(event.pressedLeftMouse()) {
											if(fleet.isStealth()) {
												fleet.sendFleetCommand(FleetCommandTypes.UNJAM);
												fleet.sendFleetCommand(FleetCommandTypes.UNCLOAK);
											} else {
												fleet.sendFleetCommand(FleetCommandTypes.JAM);
												fleet.sendFleetCommand(FleetCommandTypes.CLOAK);
											}
											MapControllerManager.resetFleets();
											deactivate();
										}
									}

									@Override
									public boolean isOccluded() {
										return false;
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
							}
							if(fleet.canInterdict()) {
								menu.addItem(new Object() {
									@Override
									public String toString() {
										if(fleet.isInterdict()) return Lng.str("Toggle Interdiction (ON)");
										else return Lng.str("Toggle Interdiction (OFF)");
									}
								}.toString(), new GUICallback() {
									@Override
									public void callback(GUIElement callingGuiElement, MouseEvent event) {
										if(event.pressedLeftMouse()) {
											if(fleet.isInterdict()) fleet.sendFleetCommand(FleetCommandTypes.STOP_INTERDICT);
											else fleet.sendFleetCommand(FleetCommandTypes.INTERDICT);
											MapControllerManager.resetFleets();
											deactivate();
										}
									}

									@Override
									public boolean isOccluded() {
										return false;
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
							}
						}
						break;
					}
				}
			}
		}
		return menu;
	}

	private String getEntityName() {
		String n = name;
		if(n.contains("(")) n = n.substring(0, n.indexOf('(') - 1);
		if(n.contains(":")) n = n.substring(0, n.indexOf(':') - 1);
		return n;
	}

	private String getEntryType() {
		return switch(entry) {
			case SavedCoordinate savedCoordinate -> "[Waypoint] ";
			case PlanetEntityMapEntry transformableEntityMapEntry -> "[Planet] ";
			case GasPlanetEntityMapEntry gasPlanetEntityMapEntry -> "[Gas Giant] ";
			case StarPosition starPosition -> "[Star] ";
			case FleetMember.FleetMemberMapIndication fleetMemberMapIndication -> "[Fleet] ";
			case null, default -> "";
		};
	}

	private Vector3i getEntryPos() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().getEntryPos(entry);
	}
}
