package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.view.mainmenu.MainMenuInputDialog;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

public class RuleSetConfigDialogMainMenu extends MainMenuInputDialog {

	private final GUIRuleSetConfigPanel p;

	private GUIRuleSetStat stat;

	public RuleSetConfigDialogMainMenu(GameMainMenuController state, GUIRuleSetStat stat) {
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
				RuleSetConfigDialogMainMenu.super.deactivate();
				deactivate();
			}

			@Override
			public void onDeactivate() {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(846);
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

	@Override
	public boolean isInside() {
		return p.isInside();
	}
}
