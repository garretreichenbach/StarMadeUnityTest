package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerDockedTurretsCondition extends SegmentControllerMoreLessCondition{

	

	
	@RuleValue(tag = "Entities")
	public int entities;
	
	public SegmentControllerDockedTurretsCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_DOCKING_CHANGED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_DOCKED_TURRETS;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return moreThan ? (a.railController.getTurretCount() > entities) : (a.railController.getTurretCount() <= entities);
	}
	@Override
	public String getCountString() {
		return String.valueOf(entities);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("docked turrets");
	}
}
