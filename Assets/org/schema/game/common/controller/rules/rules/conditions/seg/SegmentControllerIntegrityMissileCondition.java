package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.List;

import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerIntegrityMissileCondition extends SegmentControllerAbstractIntegrityCondition{

	@Override
	public double getSmallestIntegrity(ManagedSegmentController<?> a) {
		double lowest = Double.POSITIVE_INFINITY;
		if(a.getManagerContainer() instanceof WeaponManagerInterface) {
			List<DumbMissileCollectionManager> collectionManagers = ((WeaponManagerInterface)a.getManagerContainer()).getMissile().getCollectionManagers();
			for(DumbMissileCollectionManager e : collectionManagers) {
				if(e.getLowestIntegrity() < lowest) {
					lowest = e.getLowestIntegrity();
				}
			}
		}
		return lowest;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_INTEGRITY_MISSILE_CONDITION;
	}

	@Override
	public String getQuantifierString() {
		return "Missile Integrity";
	}
}
