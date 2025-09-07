package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerLastCheckedCondition extends SegmentControllerCondition{

	

	
	
	public SegmentControllerLastCheckedCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_ADMIN_FLAG_CHANGED | TRIGGER_ON_BUILD_BLOCK | TRIGGER_ON_REMOVE_BLOCK;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_LAST_CHECKED;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return a.lastAdminCheckFlag > a.lastEditBlocks;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Was entity checked after it was last modified");
	}
}
