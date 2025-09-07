package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.List;

import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityCannonCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		double lowest = Double.POSITIVE_INFINITY;
		if(a.getManagerContainer() instanceof WeaponManagerInterface) {
			List<CannonCollectionManager> collectionManagers = ((WeaponManagerInterface)a.getManagerContainer()).getWeapon().getCollectionManagers();
			for(CannonCollectionManager e : collectionManagers) {
				if(e.getLowestIntegrity() < lowest) {
					lowest = e.getLowestIntegrity();
				}
			}
		}
		return lowest;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_CANNON_CONDITION;
	}

	@Override
	public String getQuantifierString() {
		return "Cannon Integrity";
	}
}
