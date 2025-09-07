package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerAlwaysTrueCondition extends SegmentControllerCondition{
	@RuleValue(tag = "AlwaysTrue")
	public boolean alwaysTrue = true;
	
	public SegmentControllerAlwaysTrueCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_RULE_CHANGE;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_ALWAYS_TRUE_CONDITION;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return alwaysTrue;
	}

	@Override
	public String getDescriptionShort() {
		return alwaysTrue ? Lng.str("This condition is always true") : Lng.str("This condition is always false");
	}
	
	
}
