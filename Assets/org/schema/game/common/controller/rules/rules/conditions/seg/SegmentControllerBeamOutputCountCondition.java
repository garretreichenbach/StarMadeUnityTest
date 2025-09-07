package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.List;

import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.common.language.Lng;

public class SegmentControllerBeamOutputCountCondition extends SegmentControllerAbstractOutputCountCondition{


	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_OUTPUTS_PER_BEAM;
	}
	@Override
	public double getOutputCount(ManagedSegmentController<?> a) {
		int count = 0;
		if(a.getManagerContainer() instanceof WeaponManagerInterface) {
			List<DamageBeamCollectionManager> collectionManagers = ((WeaponManagerInterface)a.getManagerContainer()).getBeam().getCollectionManagers();
			for(DamageBeamCollectionManager e : collectionManagers) {
				count = Math.max(e.getElementCollections().size(), count);
			}
		}
		return count;
	}
	@Override
	public String getQuantifierString() {
		return Lng.str("Beam Output(s)");
	}
	
}
