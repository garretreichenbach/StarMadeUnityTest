package org.schema.game.client.view.gui.fleet;

import org.schema.game.client.controller.PlayerButtonTilesInput;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.FleetStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class FleetOptionButtons extends GUIAnchor {

	private final FleetPanel panel;

	public FleetOptionButtons(InputState state, FleetPanel panel) {
		super(state);
		this.panel = panel;
	}

	public PlayerState getOwnPlayer() {
		return getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return getState().getFaction();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	public void openCreateFleetDialog() {
		PlayerGameTextInput pp = new PlayerGameTextInput("INFLEET", getState(), 64, Lng.str("New Fleet"), Lng.str("Please choose a name for the new fleet!"), Lng.str("my new fleet")) {

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
				if(entry.length() < 1) {
					super.getState().getController().popupAlertTextMessage(Lng.str("Must be at least one letter"), 0);
					return false;
				}
				super.getState().getFleetManager().requestCreateFleet(entry, super.getState().getPlayer().getName());
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(494);
				return true;
			}

			@Override
			public void onDeactivate() {
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if(EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck(Lng.str("May only contain letters, numbers, spaces, _, -"));
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
				AudioController.fireAudioEventID(495);
				return false;
			}
		});

		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(496);
	}

	public FleetManager getFleetManager() {
		return getState().getFleetManager();
	}

	@Override
	public void onInit() {
		{
			GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 2, 4, Lng.str("Fleets"), this);
			p.onInit();
			p.activeInterface = panel;
			p.addButton(0, 0, Lng.str("Create New Fleet"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						if(getState().getController().getPlayerInputs().isEmpty()) {
							openCreateFleetDialog();
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive() || !getState().getController().getPlayerInputs().isEmpty();
				}

			}, null);

			p.addButton(1, 0, Lng.str("Delete Fleet"), GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						if(getState().getController().getPlayerInputs().isEmpty() && getFleetManager().getSelected() != null) {
							new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Confirm"), Lng.str("Do you really want to delete this Fleet?")) {

								@Override
								public void pressedOK() {
									getState().getFleetManager().requestFleetRemove(getFleetManager().getSelected());
									deactivate();
								}

								@Override
								public void onDeactivate() {
								}
							}.activate();
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive() || !getState().getController().getPlayerInputs().isEmpty();
				}

			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return getFleetManager().getSelected() != null;
				}

			});
			p.addButton(0, 1, Lng.str("Add Member") + (getState().getGameState().isOnlyAddFactionToFleet() ? Lng.str(" (Faction only)") : ""), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						if(getState().getController().getPlayerInputs().isEmpty() && getFleetManager().getSelected() != null) {
							AddShipToFleetPlayerInput a = new AddShipToFleetPlayerInput(getState(), getFleetManager().getSelected());
							a.activate();
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive() || !getState().getController().getPlayerInputs().isEmpty();
				}

			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return getFleetManager().getSelected() != null;
				}

			});

			p.addButton(1, 1, Lng.str("Order"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						if(getState().getController().getPlayerInputs().isEmpty() && getFleetManager().getSelected() != null) {
							popupNewOrderDialog(getFleetManager().getSelected());
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive() || !getState().getController().getPlayerInputs().isEmpty();
				}

			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return getFleetManager().getSelected() != null;
				}

			});

			p.addButton(0, 2, Lng.str("Rename Fleet"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						if(getState().getController().getPlayerInputs().isEmpty() && getFleetManager().getSelected() != null) {
							openRenameFleetDialog(getFleetManager().getSelected());
						}
					}
				}

				private void openRenameFleetDialog(Fleet selected) {
					(new PlayerGameTextInput("RenameFleet", getState(), 30, "Enter new name for fleet", selected.getName()) {
						@Override
						public void onDeactivate() {

						}

						@Override
						public boolean onInput(String entry) {
							if(!entry.isEmpty()) {
								getFleetManager().getSelected().setName(entry.trim());
								return true;
							} else return false;
						}

						@Override
						public String[] getCommandPrefixes() {
							return new String[0];
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) {
							return null;
						}

						@Override
						public void onFailedTextCheck(String msg) {

						}
					}).activate();
				}

				@Override
				public boolean isOccluded() {
					return !isActive() || !getState().getController().getPlayerInputs().isEmpty();
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

			p.addButton(1, 2, new Object() {
				@Override
				public String toString() {
					try {
						return Lng.str("Faction Access %s", (getFleetManager().getSelected().getFactionAccessString()));
					} catch(Exception e) {
						if(getState().getPlayer().getFactionId() <= 0) return "No Faction";
						if(getFleetManager().getSelected() == null) return "Fleet Empty";
						return "Faction Access PERSONAL";
					}
				}
			}, GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						Fleet selectedFleet = getFleetManager().getSelected();
						if(getState().getController().getPlayerInputs().isEmpty() && selectedFleet != null) {
							byte access = selectedFleet.getFactionAccess();
							access++;
							if(access > 5) access = 0;
							selectedFleet.setFactionAccessible(access);
							getFleetManager().requestFleetPermissionChange(selectedFleet, access);
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive() || !getState().getController().getPlayerInputs().isEmpty() || getFleetManager().getSelected() == null || getFleetManager().getSelected().isEmpty();
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return getState().getPlayer().getFactionId() > 0 && getFleetManager().getSelected() != null && !getFleetManager().getSelected().isEmpty();
				}
			});

			p.addButton(0, 3, new Object() {
				@Override
				public String toString() {
					try {
						return Lng.str("Targeting %s", getFleetManager().getSelected().isCombinedTargeting() ? "COMBINED" : "SEPARATE");
					} catch(Exception e) {
						return Lng.str("Targeting SEPARATE");
					}
				}
			}, GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse() && getState().getController().getPlayerInputs().isEmpty()) {
						if(getFleetManager().getSelected() != null) {
//							getFleetManager().getSelected().setCombinedTargeting(!getFleetManager().getSelected().isCombinedTargeting());
							getFleetManager().requestFleetTargetingChange(getFleetManager().getSelected(), getFleetManager().getSelected().isCombinedTargeting());
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive() || !getState().getController().getPlayerInputs().isEmpty() || getFleetManager().getSelected() == null || getFleetManager().getSelected().isEmpty();
				}
			}, new GUIActivationHighlightCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return getFleetManager().getSelected() != null && !getFleetManager().getSelected().isEmpty();
				}

				@Override
				public boolean isHighlighted(InputState state) {
					return getFleetManager().getSelected() != null && getFleetManager().getSelected().isCombinedTargeting();
				}
			});

			setPos(1, 0, 0);
			attach(p);
		}
	}

	private void popupNewOrderDialog(Fleet fleet) {
		PlayerButtonTilesInput a = new PlayerButtonTilesInput("FLEETORDER", getState(), 800, 640, Lng.str("Order"), 260, 165) {

			@Override
			public void onDeactivate() {
			}
		};
		for(FleetCommandTypes c : FleetCommandTypes.values()) {
			c.addTile(a, fleet);
		}
		a.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(500);
	}
}
