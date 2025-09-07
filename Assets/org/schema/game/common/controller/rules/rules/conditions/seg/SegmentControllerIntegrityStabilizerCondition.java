package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityStabilizerCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		return a.getManagerContainer().getStabilizer().getIntegrity();
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_STABILIZER_CONDITION;
	}

	@Override
	public String getQuantifierString() {
		return "Stabilizer Integrity";
	}
}
