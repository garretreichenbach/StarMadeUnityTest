package org.schema.game.client.view.gui.navigation.navigationnew;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerScanDialog;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationFilter;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationFilterEditDialog;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.PlayerSectorInput;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public class NavigationOptionsButtonPanel extends GUIAnchor {

	private NavigationPanelNew panel;

	public NavigationOptionsButtonPanel(InputState state, NavigationPanelNew panel) {
		super(state);
		this.panel = panel;
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
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 2, 3, Lng.str("Navigation Options"), this);
		p.onInit();
		p.activeInterface = panel;
		p.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				Vector3i waypoint = getState().getController().getClientGameData().getWaypoint();
				return waypoint != null ? Lng.str("WP: %s (set)", waypoint.toStringPure()) : Lng.str("Set or Load Waypoint");
			}
		}, HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					popupWaypointDialog();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
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
		p.addButton(0, 1, Lng.str("Save current coordinates"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					popupSaveCoordinatesDialog();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return !getState().getPlayer().isInTutorial() && !getState().getPlayer().isInTestSector() && !getState().getPlayer().isInPersonalSector();
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		p.addButton(1, 0, Lng.str("Set navigation filter"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					popupNavigationFilterDialog();
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
		p.addButton(1, 1, Lng.str("Open Galaxy Map (%s)", KeyboardMappings.MAP_PANEL.getKeyChar()), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(559);
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().galaxyMapAction();
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
		p.addButton(0, 2, Lng.str("Search last entered ship"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			private long lastSearchPressed;

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(System.currentTimeMillis() - lastSearchPressed > 5000) {
						getState().getController().popupGameTextMessage(Lng.str("Searching for last entered...!"), 0);
						getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.SEARCH_LAST_ENTERED_SHIP);
						lastSearchPressed = System.currentTimeMillis();
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("Spam Protection:\nplease wait a moment!"), 0);
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
				return true;
			}
		});
		p.addButton(1, 2, Lng.str("Scan History"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					PlayerScanDialog s = new PlayerScanDialog((getState()), getState().getPlayer().getScanHistory());
					s.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(560);
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
		setPos(1, 0, 0);
		attach(p);
	}

	public InShipControlManager getInShipControlManager() {
		return getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
	}

	private NavigationControllerManager getNavigationControlManager() {
		return getPlayerGameControlManager().getNavigationControlManager();
	}

	private PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	private void popupNavigationFilterDialog() {
		final NavigationFilter filterClone = getNavigationControlManager().getFilterClone();
		final long filterbefore = getNavigationControlManager().getFilter().getFilter();
		NavigationFilterEditDialog p = new NavigationFilterEditDialog(getState(), filterClone, false) {

			/* (non-Javadoc)
			 * @see org.schema.game.client.controller.manager.ingame.navigation.NavigationFilterEditDialog#onDeactivate()
			 */
			@Override
			public void onDeactivate() {
				if(filterClone.getFilter() != filterbefore) {
					panel.flagDirty();
				}
				super.onDeactivate();
			}
		};
		p.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(561);
	}

	private void popupSaveCoordinatesDialog() {
		if((getState().getController().getClientGameData().getWaypoint() != null && !getState().getController().getClientGameData().hasWaypointAt(getState().getController().getClientGameData().getWaypoint())) || getState().getController().getClientGameData().getWaypoint() == null) {
			(new SaveCoordinateDialog(getState(), getState().getController().getClientGameData().getWaypoint())).activate();
		}
		/*
		Vector3i waypoint = getState().getController().getClientGameData().getWaypoint();
		PlayerGameTextInput p = new PlayerGameTextInput("NavigationOptionsButtonPanel_popupSaveCoordinatesDialog", getState(), 32, Lng.str("Save Coordinates"), Lng.str("Enter a name for the coordinates %s", getState().getPlayer().getCurrentSector().toStringPure()), "") {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean onInput(String entry) {
				try {
					getState().getController().getClientChannel().sendSavedCoordinateToServer(entry, getState().getPlayer().getCurrentSector());
					return true;
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
				return false;
			}
		};
		p.activate();
		 */
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(562);
	}

	private void popupWaypointDialog() {
		Vector3i waypoint = getState().getController().getClientGameData().getWaypoint();

		PlayerSectorInput p = new PlayerSectorInput(getState(), Lng.str("Enter waypoint"), Lng.str("Enter waypoint (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100]).\nUse ~ for relative coordinates (e.g. ~, ~-1, ~) would be the sector directly below you."), (waypoint != null ? (waypoint.x + ", " + waypoint.y + ", " + waypoint.z) : "")) {

			@Override
			public void handleEnteredEmpty() {
				getState().getController().getClientGameData().setWaypoint(null);
			}

			@Override
			public void handleEntered(Vector3i p) {
				getState().getController().getClientGameData().setWaypoint(p);
			}

			@Override
			public Object getSelectCoordinateButtonText() {
				return Lng.str("PLOT");
			}
		};
		p.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(563);
	}
}
