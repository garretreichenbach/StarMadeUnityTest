package org.schema.game.common.controller.elements.effectblock.em;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.effectblock.EffectUnit;

public class EmEffectUnit extends EffectUnit<EmEffectUnit, EmEffectCollectionManager, EmEffectElementManager> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state,
	                                          ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                          ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

}
