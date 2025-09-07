package org.schema.game.client.controller.manager;

import org.schema.game.client.controller.OptionsMenu;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class OptionGameControlManager extends AbstractControlManager {

	private OptionsMenu gameMenu;

	public OptionGameControlManager(GameClientState state) {
		super(state);
		initialize();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	@Override
	public void onSwitch(boolean active) {
		boolean contains = false;
		if (active) {
			synchronized (getState().getController().getPlayerInputs()) {
				for (DialogInterface p : getState().getController().getPlayerInputs()) {
					if (p instanceof OptionsMenu) {
						contains = true;
					}
				}
			}
			if (!contains) {
				gameMenu = new OptionsMenu(getState());
				gameMenu.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(198);
			}
		} else {
			synchronized (getState().getController().getPlayerInputs()) {
				for (int i = 0; i < getState().getController().getPlayerInputs().size(); i++) {
					DialogInterface p = getState().getController().getPlayerInputs().get(i);
					if (p instanceof OptionsMenu) {
						getState().getController().getPlayerInputs().get(i).deactivate();
						break;
					}
				}
			}
		}
		super.onSwitch(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(false);
		super.update(timer);
	}

	public void initialize() {
	}

	public void deactivateMenu() {
		if (gameMenu != null && getState().getController().getPlayerInputs().contains(gameMenu)) {
			gameMenu.deactivate();
		}
	}
}
