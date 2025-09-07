package org.schema.game.client.controller;

import java.io.IOException;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.options.newoptions.OptionsPanelNew;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class OptionsMenu extends DialogInput implements GUIActiveInterface {

	private static OptionsPanelNew optionPanel;

	public OptionsMenu(GameClientState state) {
		super(state);
		if (optionPanel == null || optionPanel.getState() != state) {
			System.err.println("[GUI] New Options Panel");
			optionPanel = new OptionsPanelNew(state, this) {

				@Override
				public void pressedOk() {
					applyGameSettings(((OptionsMenu) activeInterface), true);
				// ((OptionsMenu)activeInterface).deactivate();
				}

				@Override
				public void pressedCancel() {
					try {
						EngineSettings.read();
						((OptionsMenu) activeInterface).deactivate();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}

				@Override
				public void pressedApply() {
					applyGameSettings(((OptionsMenu) activeInterface), false);
				}
			};
		}
		optionPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(199);
					applyGameSettings(OptionsMenu.this, true);
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		});
		optionPanel.reset();
		optionPanel.setCallback(this);
		optionPanel.activeInterface = this;
		try {
			getState().getController().getInputController().getJoystick().read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(200);
		}
		if (event.pressedLeftMouse() && callingGuiElement.getUserPointer() != null) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(203);
				deactivate();
				getState().getGlobalGameControlManager().getOptionsControlManager().setActive(false);
				getState().getGlobalGameControlManager().getMainMenuManager().setDelayedActive(true);
				getState().getGlobalGameControlManager().getMainMenuManager().hinderInteraction(200);
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(202);
				deactivate();
				getState().getGlobalGameControlManager().getOptionsControlManager().setActive(false);
				getState().getGlobalGameControlManager().getMainMenuManager().setDelayedActive(true);
				getState().getGlobalGameControlManager().getMainMenuManager().hinderInteraction(200);
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(201);
				deactivate();
				getState().getGlobalGameControlManager().getOptionsControlManager().setActive(false);
				getState().getGlobalGameControlManager().getMainMenuManager().setDelayedActive(true);
				getState().getGlobalGameControlManager().getMainMenuManager().hinderInteraction(200);
			} else {
				assert (false) : "not known command: '" + callingGuiElement.getUserPointer() + "'";
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	@Override
	public boolean isActive() {
		return !MainMenuGUI.runningSwingDialog && (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == optionPanel);
	}

	@Override
	public GUIElement getInputPanel() {
		return optionPanel;
	}

	@Override
	public void onDeactivate() {
		getState().getGlobalGameControlManager().getOptionsControlManager().setActive(false);
		getState().getGlobalGameControlManager().getMainMenuManager().setDelayedActive(true);
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}
}
