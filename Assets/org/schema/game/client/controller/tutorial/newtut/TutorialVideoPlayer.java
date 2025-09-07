package org.schema.game.client.controller.tutorial.newtut;

import org.schema.game.client.view.gui.tutorial.tutorialnewvideo.GUITutorialVideoSelector;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class TutorialVideoPlayer extends DialogInput {

	private GUITutorialVideoSelector inputPanel;

	public TutorialVideoPlayer(InputState input) {
		super(input);
		int width = 850;
		int height = 300;
		inputPanel = new GUITutorialVideoSelector(input, width, height, GLFrame.getWidth() / 2 - width / 2, GLFrame.getHeight() / 2 - height / 2, this, this);
		inputPanel.onInit();
		inputPanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !TutorialVideoPlayer.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(291);
					deactivate();
				}
			}
		});
	}

	@Override
	public GUIElement getInputPanel() {
		return inputPanel;
	}

	@Override
	public void onDeactivate() {
	}

	public void playIntroVideo() {
		inputPanel.playIntroVideo();
	}
}
