package org.schema.game.common.controller.elements.missile.dumb;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.missile.MissileUnit;

public class DumbMissileUnit extends MissileUnit<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> {

	@Override
	public float getDamage() {
		float damage = super.getDamage();
		damage += DumbMissileElementManager.ADDITIVE_DAMAGE;
		return damage;
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state,
	                                          ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                          ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}
	
}
