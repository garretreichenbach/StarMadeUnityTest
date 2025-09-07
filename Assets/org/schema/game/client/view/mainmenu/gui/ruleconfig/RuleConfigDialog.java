package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class RuleConfigDialog extends DialogInput{

	private final GUIRuleConfigPanel p;
	private GUIRuleStat stat;
	public RuleConfigDialog(InputState state, GUIRuleStat stat) {
		super(state);
		p = new GUIRuleConfigPanel(state, stat, this);
		p.onInit();
		this.stat = stat;
		
	}


	@Override
	public GUIElement getInputPanel() {
		return p;
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