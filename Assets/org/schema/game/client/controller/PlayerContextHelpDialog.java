package org.schema.game.client.controller;

import java.io.IOException;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUICheckBoxTextPairNew;
import org.schema.schine.sound.controller.AudioController;

public class PlayerContextHelpDialog {

	private GameClientState state;

	private EngineSettings setting;

	private String desc;

	private boolean ignore;

	private FontInterface descText = FontSize.MEDIUM_15;

	private FontInterface checkBoxText = FontSize.MEDIUM_15;

	boolean checkBoxChecked;

	public boolean ignoreToggle = true;

	public PlayerContextHelpDialog(GameClientState state, EngineSettings setting, String desc, boolean ignore) {
		this.state = state;
		this.setting = setting;
		this.desc = desc;
		this.ignore = ignore;
		this.checkBoxChecked = !setting.isOn();
	}

	public void activate() {
		if (!state.getPlayerInputs().isEmpty()) {
			return;
		}
		final PlayerOkCancelInput c = new PlayerOkCancelInput("CONTEXT_HLP", state, 400, 200, Lng.str("Help"), desc, new FontStyle(descText)) {

			@Override
			public void pressedOK() {
				deactivate();
			}

			@Override
			public void onDeactivate() {
				setting.setOn(!checkBoxChecked);
				try {
					EngineSettings.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		c.getInputPanel().setCancelButton(false);
		c.getInputPanel().onInit();
		GUITextOverlay descriptionText = c.getInputPanel().getDescriptionText();
		String cText;
		if (ignore) {
			cText = Lng.str("Ignore and don't show again");
		} else {
			cText = Lng.str("Don't show again");
		}
		GUICheckBoxTextPairNew pair = new GUICheckBoxTextPairNew(state, cText, checkBoxText) {

			@Override
			public boolean isChecked() {
				return checkBoxChecked;
			}

			@Override
			public void deactivate() {
				checkBoxChecked = false;
			}

			@Override
			public void activate() {
				checkBoxChecked = true;
			}

			@Override
			public void draw() {
				if (ignoreToggle) {
					setPos(c.getInputPanel().getButtonOK().getWidth() + 6, 0, 0);
					super.draw();
				}
			}
		};
		descriptionText.updateTextSize();
		pair.onInit();
		c.getInputPanel().getButtonOK().attach(pair);
		c.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(222);
	}
}
