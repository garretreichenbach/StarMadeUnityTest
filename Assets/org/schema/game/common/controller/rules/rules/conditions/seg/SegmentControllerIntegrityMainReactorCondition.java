package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityMainReactorCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		return a.getManagerContainer().getPowerInterface().getActiveReactorIntegrity();
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_MAIN_REACTOR_CONDITION;
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_COLLECTION_UPDATE | TRIGGER_ON_REACTOR_ACTIVITY_CHANGE;
	}
	@Override
	public String getQuantifierString() {
		return "Main Reactor Integrity";
	}
}
