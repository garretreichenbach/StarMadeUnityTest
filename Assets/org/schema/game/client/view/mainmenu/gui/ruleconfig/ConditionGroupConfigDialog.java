package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.seg.ConditionGroup;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class ConditionGroupConfigDialog extends DialogInput{

	private final GUIConditionGroupConfigPanel p;
	private GUIRuleStat stat;
	private Condition<?> cond;
	public ConditionGroupConfigDialog(InputState state, GUIRuleStat stat, ConditionGroup conditionGroup) {
		super(state);
		p = new GUIConditionGroupConfigPanel(state, stat, conditionGroup, this);
		p.onInit();
		this.stat = stat;
		this.cond = (Condition<?>) conditionGroup;
	}


	@Override
	public GUIElement getInputPanel() {
		return p;
	}

	@Override
	public void onDeactivate() {
		p.cleanUp();
		//select the original condition
		stat.selectedCondition = cond;
		stat.change();
	}


	@Override
	public void update(Timer timer) {
		super.update(timer);
		stat.updateLocal(timer);
	}

}