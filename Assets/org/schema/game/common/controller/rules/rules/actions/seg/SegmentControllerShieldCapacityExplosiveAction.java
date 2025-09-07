package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerShieldCapacityExplosiveAction extends SegmentControllerExplosiveAction {
	
	

	public SegmentControllerShieldCapacityExplosiveAction() {
		super();
	}

	@Override
	public String getExplName() {
		return Lng.str("Shield Capacity Modules");
	}

	@Override
	public UsableElementManager<?, ?, ?> getElementManager(ManagedUsableSegmentController<?> se) {
		if(se.getManagerContainer() instanceof ShieldContainerInterface) {
			return ((ShieldContainerInterface)se.getManagerContainer()).getShieldCapacityManager().getElementManager();
		}
		return null;
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_SHIELD_CAPACITY_EXPLOSIVE;
	}
	
	
}
