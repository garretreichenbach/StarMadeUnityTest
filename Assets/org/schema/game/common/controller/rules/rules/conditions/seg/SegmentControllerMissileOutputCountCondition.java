package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.List;

import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerMissileOutputCountCondition extends SegmentControllerAbstractOutputCountCondition{


	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_OUTPUTS_PER_MISSILE;
	}

	@Override
	public double getOutputCount(ManagedSegmentController<?> a) {
		int count = 0;
		if(a.getManagerContainer() instanceof WeaponManagerInterface) {
			List<DumbMissileCollectionManager> collectionManagers = ((WeaponManagerInterface)a.getManagerContainer()).getMissile().getCollectionManagers();
			for(DumbMissileCollectionManager e : collectionManagers) {
				count = Math.max(e.getElementCollections().size(), count);
			}
		}
		return count;
	}

	@Override
	public String getQuantifierString() {
		return "Missile Output(s)";
	}

	
}
