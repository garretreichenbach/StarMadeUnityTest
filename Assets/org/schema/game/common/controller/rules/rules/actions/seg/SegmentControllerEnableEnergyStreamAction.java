package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerEnableEnergyStreamAction extends SegmentControllerAction {
	
	

	public SegmentControllerEnableEnergyStreamAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_ENABLE_ENERGY_STREAM;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Enables energy stream for entity");
	}

	@Override
	public void onTrigger(SegmentController se) {
		if(se instanceof ManagedUsableSegmentController<?>) {
			ManagedUsableSegmentController<?> s = (ManagedUsableSegmentController<?>)se;
			s.getManagerContainer().getPowerInterface().setEnergyStreamEnabled(true);
		}
	}

	@Override
	public void onUntrigger(SegmentController se) {
		if(se instanceof ManagedUsableSegmentController<?>) {
			ManagedUsableSegmentController<?> s = (ManagedUsableSegmentController<?>)se;
			s.getManagerContainer().getPowerInterface().setEnergyStreamEnabled(false);
		}
	}

	
	
}
