package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerReactorExplosiveAction extends SegmentControllerExplosiveAction {
	
	

	public SegmentControllerReactorExplosiveAction() {
		super();
	}

	@Override
	public String getExplName() {
		return Lng.str("Main Reactor Modules");
	}

	@Override
	public UsableElementManager<?, ?, ?> getElementManager(ManagedUsableSegmentController<?> se) {
		return se.getManagerContainer().getMainReactor().getElementManager();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_REACTOR_EXPLOSIVE;
	}
	
	
}
