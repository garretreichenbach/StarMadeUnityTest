package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.gamemenus.gamemenusnew.MainMenuPanelNew;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class MainMenu extends PlayerInput {
	private GUIElement menuPanel;

	public MainMenu(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if(callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(75);
		}
		// System.err.println("CALLBACK: "+callingGuiElement.getUserPointer());
		if(event.pressedLeftMouse()) {
			if(callingGuiElement.getUserPointer().equals("TUTORIAL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(82);
				deactivate();
				getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
				getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
				getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
				if(getState().getController().getPlayerInputs().isEmpty()) {
					if(getState().getPlayer().isInTestSector()) {
						getState().getGlobalGameControlManager().openExitTestSectorPanel(null);
					}
					if(getState().getPlayer().isInTutorial()) {
						getState().getGlobalGameControlManager().openExitTutorialPanel(null);
					} else {
						getState().getGlobalGameControlManager().openTutorialPanel();
					}
				}
				stopMusic();
			} else if(callingGuiElement.getUserPointer().equals("RESUME") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(81);
				deactivate();
				getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
				getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
				getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
			} else if(callingGuiElement.getUserPointer().equals("EXIT")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(80);
				stopMusic();
				setErrorMessage(Lng.str("Sorry! There is no menu to go back to. Please choose 'exit game'."));
			} else if(callingGuiElement.getUserPointer().equals("MESSAGELOG")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(79);
				getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
				getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
				getState().getGlobalGameControlManager().getIngameControlManager().activateMesssageLog();
				getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
			} else if(callingGuiElement.getUserPointer().equals("RESPAWN")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(77);
				String desc = Lng.str("Are you sure you want to respawn?\nYou will spawn at the last activated\n\"Undeathinator\" module.");
				PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Respawn?"), desc) {
					@Override
					public void onDeactivate() {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().hinderInteraction(400);
					}

					@Override
					public void pressedOK() {
						if(getState().getController().suicide()) {
							deactivate();
							stopMusic();
							getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
							getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
							getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
						} else {
							setErrorMessage(Lng.str("Those who don't exist cannot be killed!."));
						}
						deactivate();
					}
				};
				check.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				stopMusic();
			} else if(callingGuiElement.getUserPointer().equals("EXIT_TO_WINDOWS")) {
				// getState().getController().queueUIAudio("0022_menu_ui - back");
				// 
				// String desc = "Exit Game to Desktop?";
				// 
				// PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(),
				// Lng.str("Exit Game"), desc) {
				// 
				// @Override
				// public boolean isOccluded() {
				// return false;
				// }
				// 
				// @Override
				// public void onDeactivate() {
				// getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().
				// hinderInteraction(400);
				// }
				// 
				// @Override
				// public void pressedOK() {
				// 
				// 
				// deactivate();
				// 
				// }
				// public void pressedSecondOption() {
				// getState().getController()
				// .queueUIAudio("0022_action - buttons push medium");
				// GLFrame.setFinished(true);
				// }
				// };
				// check.getInputPanel().setOkButtonText("Main Menu");
				// check.getInputPanel().setSecondOptionButton(true);
				// check.getInputPanel().setSecondOptionButtonText("Desktop");
				// check.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
				stopMusic();
				pressedExit();
			} else if(callingGuiElement.getUserPointer().equals("OPTIONS")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(76);
				getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
				getState().getGlobalGameControlManager().getOptionsControlManager().setActive(true);
			} else {
				assert (false) : "not known command: " + callingGuiElement.getUserPointer();
			}
		}
	}

	private void stopMusic() {
		if(AudioController.instance != null) AudioController.instance.stopMusic();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e); // nothing to do. just consume the key
		if(e.isEscapeKeyRaw() && isActive() && e.isPressed()) {
			deactivate(); //Fix for needing to press ESC twice to regain mouse control
			getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
			getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
		}
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().indexOf(this) == getState().getController().getPlayerInputs().size() - 1;
	}

	@Override
	public GUIElement getInputPanel() {
		if(menuPanel == null) {
			menuPanel = new MainMenuPanelNew(getState(), this);
			((MainMenuPanelNew) menuPanel).setCloseCallback(new GUICallback() {
				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						pressedResume();
					}
				}
			});
			((MainMenuPanelNew) menuPanel).activeInterface = this;
		}
		return menuPanel;
	}

	@Override
	public void onDeactivate() {
	}

	public void setErrorMessage(String msg) {
		getState().getController().popupAlertTextMessage(msg, 0);
	}

	public void pressedResume() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
		AudioController.fireAudioEventID(83);
		deactivate();
		getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
		getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
	}

	public void pressedOptions() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
		AudioController.fireAudioEventID(84);
		getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
		getState().getGlobalGameControlManager().getOptionsControlManager().setActive(true);
	}

	public String getTutorialsStringObj() {
		if(getState().getPlayer().isInTestSector()) {
			return Lng.str("EXIT TEST SECTOR");
		} else if(getState().getPlayer().isInTutorial()) {
			return Lng.str("EXIT TUTORIAL");
		} else {
			return Lng.str("TUTORIALS");
		}
	}

	public void pressedTutorials() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
		AudioController.fireAudioEventID(85);
		deactivate();
		getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
		getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
		getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
		if(getState().getPlayer().isInTestSector()) {
			getState().getGlobalGameControlManager().openExitTestSectorPanel(null);
		} else if(getState().getController().getPlayerInputs().isEmpty()) {
			getState().getController().getTutorialController().onActivateFromTopTaskBar();
			// else if (getState().getPlayer().isInTutorial()) {
			// getState().getGlobalGameControlManager().openExitTutorialPanel(null);
			// } else {
			// getState().getGlobalGameControlManager().openTutorialPanel();
			// }
		}
	}

	public void pressedSuicide() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
		AudioController.fireAudioEventID(86);
		String desc = Lng.str("Are you sure you want to respawn?\nYou will spawn at the last activated\n\"Undeathinator\" module.");
		PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Respawn?"), desc) {
			@Override
			public void onDeactivate() {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().hinderInteraction(400);
			}

			@Override
			public void pressedOK() {
				if(getState().getController().suicide()) {
					deactivate();
					getState().getGlobalGameControlManager().getMainMenuManager().setActive(false);
					getState().getGlobalGameControlManager().getIngameControlManager().setDelayedActive(true);
					getState().getGlobalGameControlManager().getIngameControlManager().hinderInteraction(500);
				} else {
					setErrorMessage("Those who don't exist cannot be killed!.");
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
					AudioController.fireAudioEventID(87);
				}
				deactivate();
			}
		};
		check.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(88);
	}

	public void pressedExit() {
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
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(89);
				GLFrame.setFinished(true);
				deactivate();
			}
		};
		check.getInputPanel().setOkButtonText(Lng.str("Main Menu"));
		check.getInputPanel().setSecondOptionButton(true);
		check.getInputPanel().setSecondOptionButtonText(Lng.str("Desktop"));
		check.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(90);
	}
}
