package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.conditions.Condition;

public interface RuleListUpdater {

	void updateDetailPanel(Rule selectedRule, Condition<?> selectedCondition, Action<?> selectedAction);

}
