package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.util.Collection;

import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;


public abstract class GUIRuleCollection extends GUIObservable{
	public abstract Collection<Rule> getRules();

	public abstract void setSelectedRule(Rule f);

	public abstract void setSelectedAction(Action<?> object);

	public abstract void setSelectedCondition(Condition<?> object);

	public abstract void change();

	public abstract boolean canRulesBeIgnored();
	

	
	
}
