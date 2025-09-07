package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityShieldCapacityCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		if(a.getManagerContainer() instanceof ShieldContainerInterface) {
			return ((ShieldContainerInterface)a.getManagerContainer()).getShieldCapacityManager().getLowestIntegrity();
		}
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_SHIELD_CAPACITY_CONDITION;
	}

	@Override
	public String getQuantifierString() {
		return "Shield Capacity Integrity";
	}
}
