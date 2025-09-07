package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.common.controller.rules.RuleManagerProvider;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionList;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;

import java.util.Collection;
import java.util.List;

public class GUIRuleStat extends GUIRuleCollection implements RuleManagerProvider, ConditionProvider, ActionProvider{
	
	public RuleSetManager manager;

	
	public final RuleSet ruleSet;
	public Rule selectedRule;
	public Condition<?> selectedCondition;
	public Action<?> selectedAction;

	private String path;


	private GUIRuleSetStat rStat;


	public RuleListUpdater rsc;
	
	public GUIRuleStat(InputState state, RuleSetManager manager, GUIRuleSetStat rStat, RuleSet ruleSet){
		this.manager = manager;
		this.ruleSet = ruleSet;
		this.rStat = rStat;
	}
	
	public void updateLocal(Timer timer){
	}
	public void change(){
		rsc.updateDetailPanel(selectedRule, selectedCondition, selectedAction);
		notifyObservers();
		rStat.change();
	}
	public String getLoadedPath(){
		return path;
	}
	

	
	@Override
	public RuleSetManager getRuleSetManager() {
		return manager;
	}

	@Override
	public ConditionList getConditions() {
		return selectedRule.getConditions();
	}
	@Override
	public void getAllConditions(ConditionList all) {
		selectedRule.getAllConditions(all);		
	}
	@Override
	public boolean isConditionsAvailable() {
		return selectedRule != null;
	}

	@Override
	public Collection<Rule> getRules() {
		return ruleSet;
	}

	@Override
	public void setSelectedRule(Rule f) {
		selectedRule = f;
	}

	@Override
	public void setSelectedAction(Action<?> object) {
		selectedAction = object;
	}

	@Override
	public void setSelectedCondition(Condition<?> object) {
		selectedCondition = object;
	}

	@Override
	public boolean canRulesBeIgnored() {
		return false;
	}

	@Override
	public List<Action<?>> getActions() {
		return selectedRule.getActions();
	}

	@Override
	public boolean isActionAvailable() {
		return selectedRule != null;
	}



}
