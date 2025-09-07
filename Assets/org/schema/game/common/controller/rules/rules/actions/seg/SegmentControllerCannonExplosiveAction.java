package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.elements.WeaponManagerInterface;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerCannonExplosiveAction extends SegmentControllerExplosiveAction {
	
	

	public SegmentControllerCannonExplosiveAction() {
		super();
	}

	@Override
	public String getExplName() {
		return Lng.str("Cannon Modules");
	}

	@Override
	public UsableElementManager<?, ?, ?> getElementManager(ManagedUsableSegmentController<?> se) {
		if(se.getManagerContainer() instanceof WeaponManagerInterface) {
			return ((WeaponManagerInterface)se.getManagerContainer()).getWeapon().getElementManager();
		}
		return null;
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_CANNON_EXPLOSIVE;
	}
	
	
}
