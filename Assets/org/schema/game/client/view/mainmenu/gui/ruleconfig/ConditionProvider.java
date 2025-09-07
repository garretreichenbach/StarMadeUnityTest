package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.common.controller.rules.rules.conditions.ConditionList;

public interface ConditionProvider {
	public ConditionList getConditions();

	public boolean isConditionsAvailable();

	public void getAllConditions(ConditionList all);
}
