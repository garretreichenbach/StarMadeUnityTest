package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.sound.controller.AudioController;

public abstract class ButtonResult extends AdvResult<ButtonCallback> {

	public ButtonResult() {
	}

	// public abstract HButtonType getType();
	public void rightClick() {
		if (callback != null) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
			AudioController.fireAudioEventID(305);
			callback.pressedRightMouse();
		}
	}

	public void leftClick() {
		if (callback != null) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
			AudioController.fireAudioEventID(306);
			callback.pressedLeftMouse();
		}
	}

	@Override
	protected void initDefault() {
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	public abstract HButtonColor getColor();
}
