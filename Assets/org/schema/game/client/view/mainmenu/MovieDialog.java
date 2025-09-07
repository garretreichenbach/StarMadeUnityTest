package org.schema.game.client.view.mainmenu;

import java.io.File;
import java.io.IOException;

import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.AddTextBoxInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMoviePlayer;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMovieWindow;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class MovieDialog extends DialogInput {

	private final GUIMovieWindow inputPanel;

	public MovieDialog(InputState state, String winId, int initialWidth, int initialHeight, int initalPosX, int initialPosY, File movie) throws IOException {
		super(state);
		this.inputPanel = new GUIMovieWindow(state, initialWidth, initialHeight, winId, movie, initalPosX, initialPosY, null, this);
		inputPanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !MovieDialog.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(862);
					deactivate();
				}
			}
		});
	}

	public GUIMoviePlayer getPlayer() {
		return inputPanel.getPlayer();
	}

	public void setLooping(boolean b) {
		inputPanel.setLooping(b);
	}

	@Override
	public GUIElement getInputPanel() {
		return inputPanel;
	}

	@Override
	public void onDeactivate() {
		if (!isInBackground()) {
			inputPanel.cleanUp();
		}
	}

	public void setTitle(String title) {
		this.inputPanel.setTitle(title);
	}

	public void setExtraPanel(AddTextBoxInterface x) {
		this.inputPanel.setExtraPanel(x);
	}
}
