package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerChamberExplosiveAction extends SegmentControllerAction {
	
	

	public SegmentControllerChamberExplosiveAction() {
		super();
	}

	public String getExplName() {
		return Lng.str("Reactor Chamber Modules");
	}


	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_REACTOR_CHAMBER_EXPLOSIVE;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("Makes %s explode if it gets hit (block destroyed)", getExplName());
	}


	@Override
	public void onTrigger(SegmentController se) {
		if(se instanceof ManagedUsableSegmentController<?>) {
			ManagedUsableSegmentController<?> s = (ManagedUsableSegmentController<?>)se;
			
			for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> e : s.getManagerContainer().getChambers()) {
				e.getElementManager().setExplosiveStructure(true);
			}
			
		}
	}

	@Override
	public void onUntrigger(SegmentController se) {
		if(se instanceof ManagedUsableSegmentController<?>) {
			ManagedUsableSegmentController<?> s = (ManagedUsableSegmentController<?>)se;
			for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> e : s.getManagerContainer().getChambers()) {
				e.getElementManager().setExplosiveStructure(false);
			}
		}
	}
	
	
}
