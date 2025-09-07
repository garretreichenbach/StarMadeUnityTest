package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.List;

import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityReactorChamberCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> chambers = a.getManagerContainer().getPowerInterface().getChambers();
		double integrity = Double.POSITIVE_INFINITY;
		for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> e : chambers) {
			integrity = Math.min(integrity, e.getCollectionManager().getLowestIntegrity());
		}
		return integrity;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_CHAMBER_CONDITION;
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_COLLECTION_UPDATE | TRIGGER_ON_REACTOR_ACTIVITY_CHANGE;
	}
	@Override
	public String getQuantifierString() {
		return "Reactor Chamber Integrity";
	}
}
