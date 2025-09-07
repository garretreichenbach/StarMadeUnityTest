package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerStabilizerExplosiveAction extends SegmentControllerExplosiveAction {
	
	

	public SegmentControllerStabilizerExplosiveAction() {
		super();
	}

	@Override
	public String getExplName() {
		return Lng.str("Reactor Stabilizer Modules");
	}

	@Override
	public UsableElementManager<?, ?, ?> getElementManager(ManagedUsableSegmentController<?> se) {
		return se.getManagerContainer().getStabilizer().getElementManager();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_STABILIZER_EXPLOSIVE;
	}
	
	
}
