package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityThrusterCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		if(a.getManagerContainer() instanceof ShipManagerContainer) {
			return ((ShipManagerContainer)a.getManagerContainer()).getThrusterElementManager().lowestIntegrity;
		}
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_THRUSTER_CONDITION;
	}
	@Override
	public String getQuantifierString() {
		return "Thruster Integrity";
	}
	
}
