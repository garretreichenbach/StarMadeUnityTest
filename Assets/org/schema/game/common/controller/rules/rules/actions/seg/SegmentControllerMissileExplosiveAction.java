package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerMissileExplosiveAction extends SegmentControllerExplosiveAction {
	
	

	public SegmentControllerMissileExplosiveAction() {
		super();
	}

	@Override
	public String getExplName() {
		return Lng.str("Missile Modules");
	}

	@Override
	public UsableElementManager<?, ?, ?> getElementManager(ManagedUsableSegmentController<?> se) {
		if(se.getManagerContainer() instanceof WeaponManagerInterface) {
			return ((WeaponManagerInterface)se.getManagerContainer()).getMissile().getElementManager();
		}
		return null;
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_MISSILE_EXPLOSIVE;
	}
	
	
}
