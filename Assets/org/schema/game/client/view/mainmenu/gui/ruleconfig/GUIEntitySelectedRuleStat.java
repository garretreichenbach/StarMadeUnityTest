package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.conditions.ConditionList;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;

public class GUIEntitySelectedRuleStat extends GUIObservable implements ConditionProvider{

	private Rule rule;

	public void selectRule(Rule r) {
		System.err.println("ENTITY SELECTED RULE FOR CONDITION LIST "+rule);
		this.rule = r;
		notifyObservers();
	}
	
	@Override
	public ConditionList getConditions() {
		return rule.getConditions();
	}

	@Override
	public boolean isConditionsAvailable() {
		return rule != null;
	}

	@Override
	public void getAllConditions(ConditionList all) {
		rule.getAllConditions(all);		
	}

}
