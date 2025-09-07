package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.client.view.mainmenu.gui.ruleconfig.ConditionProvider;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionList;

public interface ConditionGroup extends ConditionProvider{
	public ConditionList getConditions();
	public boolean isAllTrue();
	public void setAllTrue(boolean b);
	public void addCondition(Condition<?> c);
	public void removeCondition(Condition<?> c);
}
