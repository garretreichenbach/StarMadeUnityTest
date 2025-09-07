package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.List;

import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityBeamCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		double lowest = Double.POSITIVE_INFINITY;
		if(a.getManagerContainer() instanceof WeaponManagerInterface) {
			List<DamageBeamCollectionManager> collectionManagers = ((WeaponManagerInterface)a.getManagerContainer()).getBeam().getCollectionManagers();
			for(DamageBeamCollectionManager e : collectionManagers) {
				if(e.getLowestIntegrity() < lowest) {
					lowest = e.getLowestIntegrity();
				}
			}
		}
		return lowest;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_BEAM_CONDITION;
	}

	@Override
	public String getQuantifierString() {
		return "Beam Integrity";
	}

	
}
