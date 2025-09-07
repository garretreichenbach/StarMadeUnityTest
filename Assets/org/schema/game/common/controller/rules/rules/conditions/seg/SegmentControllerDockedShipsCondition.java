package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerDockedShipsCondition extends SegmentControllerMoreLessCondition{

	

	
	@RuleValue(tag = "Entities")
	public int entities;
	
	public SegmentControllerDockedShipsCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_DOCKING_CHANGED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_DOCKED_SHIPS;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return moreThan ? (a.railController.getNormalDockCount() > entities) : (a.railController.getNormalDockCount() <= entities);
	}
	@Override
	public String getCountString() {
		return String.valueOf(entities);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("docked ships");
	}
}
