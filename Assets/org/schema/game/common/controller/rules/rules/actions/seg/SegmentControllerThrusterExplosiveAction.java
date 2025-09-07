package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerThrusterExplosiveAction extends SegmentControllerExplosiveAction {
	
	

	public SegmentControllerThrusterExplosiveAction() {
		super();
	}

	@Override
	public String getExplName() {
		return Lng.str("Thruster Modules");
	}

	@Override
	public UsableElementManager<?, ?, ?> getElementManager(ManagedUsableSegmentController<?> se) {
		if(se instanceof Ship) {
			return ((Ship)se).getManagerContainer().getThrusterElementManager();
		}
		return null;
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_THRUSTER_EXPLOSIVE;
	}
	
	
}
