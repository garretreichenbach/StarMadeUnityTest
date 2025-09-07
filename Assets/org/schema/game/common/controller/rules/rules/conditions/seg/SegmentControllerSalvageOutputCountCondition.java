package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.List;

import org.schema.game.common.controller.elements.SalvageManagerContainer;
import org.schema.game.common.controller.elements.beam.harvest.SalvageBeamCollectionManager;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.common.language.Lng;

public class SegmentControllerSalvageOutputCountCondition extends SegmentControllerAbstractOutputCountCondition{


	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_OUTPUTS_PER_SALVAGE;
	}

	@Override
	public double getOutputCount(ManagedSegmentController<?> a) {
		int count = 0;
		if(a.getManagerContainer() instanceof SalvageManagerContainer) {
			List<SalvageBeamCollectionManager> collectionManagers = ((SalvageManagerContainer)a.getManagerContainer()).getSalvage().getCollectionManagers();
			for(SalvageBeamCollectionManager e : collectionManagers) {
				count = Math.max(e.getElementCollections().size(), count);
			}
		}
		return count;
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("Salvage Output(s)");
	}

	
}
