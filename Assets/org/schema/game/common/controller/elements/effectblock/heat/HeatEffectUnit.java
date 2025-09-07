package org.schema.game.common.controller.elements.effectblock.heat;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.effectblock.EffectUnit;

public class HeatEffectUnit extends EffectUnit<HeatEffectUnit, HeatEffectCollectionManager, HeatEffectElementManager> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state,
	                                          ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                          ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

}
