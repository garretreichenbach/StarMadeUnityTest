package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class SegmentControllerTypePercentageCondition extends SegmentControllerMoreLessCondition{

	
	@RuleValue(tag = "BlockType")
	public short type;
	
	@RuleValue(tag = "Percent")
	public float percent;
	
	public SegmentControllerTypePercentageCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_BUILD_BLOCK | TRIGGER_ON_REMOVE_BLOCK;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_TYPE_PERCENT_CONDITION;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		float percent = this.percent / 100.0f;
		float total = a.getTotalElements();
		if(total == 0) {
			return moreThan;
		}
		return moreThan ? (a.getElementClassCountMap().get(type)/total > percent) : (a.getElementClassCountMap().get(type)/total <= percent);
	}
	@Override
	public String getCountString() {
		return String.valueOf(percent)+"%";
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("%s Blocks", ElementKeyMap.toString(type));
	}
}
