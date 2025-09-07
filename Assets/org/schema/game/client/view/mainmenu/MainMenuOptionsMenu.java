package org.schema.game.client.view.mainmenu;

import java.io.IOException;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.gui.options.newoptions.OptionsPanelNew;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public class MainMenuOptionsMenu extends MainMenuInputDialog implements GUIActiveInterface {

	private OptionsPanelNew optionPanel;

	public MainMenuOptionsMenu(GameMainMenuController state) {
		super(state);
		optionPanel = new OptionsPanelNew(state, this) {

			private int sWidth = GLFrame.getWidth();

			private int sHeight = GLFrame.getHeight();

			@Override
			public void pressedOk() {
				applyGameSettings(((MainMenuOptionsMenu) activeInterface), true);
			}

			@Override
			public void pressedCancel() {
				try {
				EngineSettings.read();
				KeyboardMappings.read();
				}catch(IOException e) {
					e.printStackTrace();
				}
			// ((MainMenuOptionsMenu)activeInterface).deactivate();
			}

			@Override
			public void pressedApply() {
				applyGameSettings(((MainMenuOptionsMenu) activeInterface), false);
			}

			@Override
			public void draw() {
				if (sWidth != GLFrame.getWidth() || sHeight != GLFrame.getHeight()) {
					sWidth = GLFrame.getWidth();
					sHeight = GLFrame.getHeight();
					setPos(435, 35, 0);
					setWidth(GLFrame.getWidth() - 470);
					setHeight(GLFrame.getHeight() - 70);
				}
				super.draw();
			}
		};
		optionPanel.onInit();
		optionPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					applyGameSettings(MainMenuOptionsMenu.this, true);
					deactivate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(857);
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		});
		optionPanel.setPos(435, 35, 0);
		optionPanel.setWidth(GLFrame.getWidth() - 470);
		optionPanel.setHeight(GLFrame.getHeight() - 70);
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
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(858);
		}
		if (event.pressedLeftMouse() && callingGuiElement.getUserPointer() != null) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(861);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(860);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(859);
				deactivate();
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
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}

	@Override
	public boolean isInside() {
		return getInputPanel().isInside();
	}
}
