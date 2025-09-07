package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class SegmentControllerTypeCountCondition extends SegmentControllerMoreLessCondition{

	
	@RuleValue(tag = "BlockType")
	public short type;
	
	@RuleValue(tag = "Blocks")
	public int count;
	
	public SegmentControllerTypeCountCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_BUILD_BLOCK | TRIGGER_ON_REMOVE_BLOCK;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_TYPE_COUNT_CONDITION;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return moreThan ? (a.getElementClassCountMap().get(type) > count) : (a.getElementClassCountMap().get(type) <= count);
	}
	@Override
	public String getCountString() {
		return String.valueOf(count);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("%s Blocks", ElementKeyMap.toString(type));
	}
}
