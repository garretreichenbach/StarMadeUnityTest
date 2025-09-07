package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;

public class SegmentControllerMassCondition extends SegmentControllerMoreLessCondition{

	
	@RuleValue(tag = "Mass")
	public float mass;
	
	public SegmentControllerMassCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_MASS_UPDATE;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_MASS_CONDITION;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return moreThan ? (a.getMass() > mass) : (a.getMass() <= mass);
	}
	@Override
	public String getQuantifierString() {
		return "Mass";
	}

	@Override
	public String getCountString() {
		return String.valueOf(mass);
	}
}
