package org.schema.game.client.view.gui.rules;

import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.GUIRuleSetConfigPanel;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.GUIRuleSetStat;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class RuleSetConfigDialogGame extends DialogInput {

	private final GUIRuleSetConfigPanel p;

	private GUIRuleSetStat stat;

	public RuleSetConfigDialogGame(InputState state, GUIRuleSetStat stat) {
		super(state);
		p = new GUIRuleSetConfigPanel(state, stat, this);
		p.onInit();
		this.stat = stat;
	}

	@Override
	public GUIElement getInputPanel() {
		return p;
	}

	@Override
	public void deactivate() {
		(new PlayerOkCancelInput("CONFIRM", getState(), 300, 140, Lng.str("Confirm"), Lng.str("All unsaved work will be lost. Proceeed?")) {

			@Override
			public void pressedOK() {
				RuleSetConfigDialogGame.super.deactivate();
				deactivate();
			}

			@Override
			public void onDeactivate() {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(678);
	}

	@Override
	public void onDeactivate() {
		p.cleanUp();
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		stat.updateLocal(timer);
	}
}
