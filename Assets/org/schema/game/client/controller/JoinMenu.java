package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.gamemenus.gamemenusnew.JoinPanelNew;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class JoinMenu extends PlayerInput implements GUIActiveInterface {

	private GUIElement menuPanel;

	public JoinMenu(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(65);
		}
		if (event.pressedLeftMouse() && !getState().getGlobalGameControlManager().getMainMenuManager().isActive() && !isDelayedFromMainMenuDeactivation()) {
			try {
				if (callingGuiElement.getUserPointer().equals("JOIN")) {
					if (!getState().isWaitingForPlayerActivate()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(70);
						getState().getController().spawnAndActivatePlayerCharacter();
						deactivate();
					}
				} else // else if (callingGuiElement.getUserPointer().equals("GREEN_TEAM")) {
				// if(!getState().isWaitingForPlayerActivate()){
				// getState().getController().queueUIAudio("0022_menu_ui - enter");
				// getState().getPlayer().setTeamId(Team.GREEN, true);
				// getState().getController().spawnAndActivatePlayerCharacter();
				// deactivate();
				// }
				// }else if (callingGuiElement.getUserPointer().equals("BLUE_TEAM")) {
				// if(!getState().isWaitingForPlayerActivate()){
				// getState().getController().queueUIAudio("0022_menu_ui - enter");
				// getState().getPlayer().setTeamId(Team.BLUE, true);
				// getState().getController().spawnAndActivatePlayerCharacter();
				// deactivate();
				// }
				// }
				if (callingGuiElement.getUserPointer().equals("EXIT")) {
					System.err.println("EXIT: WAS: " + callingGuiElement.wasInside() + " -> " + callingGuiElement.isInside() + ": " + callingGuiElement.getUserPointer());
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(67);
					System.err.println("[GAMEMENU] MENU EXIT: SYSTEM WILL NOW EXIT");
					String desc = "Exit Game to Desktop?";
					PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), "Exit Game", desc) {

						@Override
						public void onDeactivate() {
							getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().hinderInteraction(400);
						}

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void pressedOK() {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(68);
							GLFrame.setFinished(true);
							deactivate();
						}
					};
					check.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(69);
				} else if (callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(66);
					getState().getController().getInputController().setLastDeactivatedMenu(System.currentTimeMillis());
					getState().getGlobalGameControlManager().activateMainMenu();
				} else {
					assert (false) : "not known command: " + callingGuiElement.getUserPointer();
				}
			} catch (PlayerNotYetInitializedException e) {
				e.printStackTrace();
				getState().getController().popupAlertTextMessage(Lng.str("Login Procedure not finished!\nPlease try again."), 0);
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	// nothing to do. just consume the key
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().indexOf(this) == getState().getController().getPlayerInputs().size() - 1;
	}

	@Override
	public GUIElement getInputPanel() {
		if (menuPanel == null) {
			// do it here so we dont call ress loader too early
			this.menuPanel = new JoinPanelNew(getState(), this);
			((JoinPanelNew) menuPanel).setCloseCallback(new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						pressedExit();
					}
				}
			});
			((JoinPanelNew) menuPanel).activeInterface = this;
		}
		return menuPanel;
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public boolean isOccluded() {
		return !isActive();
	}

	public void pressedJoin() {
		if (!getState().isWaitingForPlayerActivate()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
			AudioController.fireAudioEventID(71);
			try {
				getState().getController().spawnAndActivatePlayerCharacter();
				deactivate();
			} catch (PlayerNotYetInitializedException e) {
				e.printStackTrace();
				getState().getController().popupAlertTextMessage(Lng.str("Login Procedure not finished!\nPlease try again."), 0);
			}
		}
	}

	public void pressedExit() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
		AudioController.fireAudioEventID(72);
		String desc = Lng.str("Exit Game...");
		PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Exit Game"), desc) {

			@Override
			public void onDeactivate() {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().hinderInteraction(400);
			}

			@Override
			public void pressedOK() {
				GameMainMenuController.currentMainMenu.switchFrom(getState());
				deactivate();
			}

			@Override
			public void pressedSecondOption() {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(73);
				GLFrame.setFinished(true);
				deactivate();
			}
		};
		check.getInputPanel().setOkButtonText(Lng.str("Main Menu"));
		check.getInputPanel().setSecondOptionButton(true);
		check.getInputPanel().setSecondOptionButtonText(Lng.str("Desktop"));
		check.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(74);
	}
}
