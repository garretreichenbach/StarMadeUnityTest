package org.schema.game.common.controller.elements.powercap;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.EmptyValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;

public class PowerCapacityUnit extends ElementCollection<PowerCapacityUnit, PowerCapacityCollectionManager, VoidElementManager<PowerCapacityUnit, PowerCapacityCollectionManager>> {

	

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, Lng.str("Power Capacity Module"), this, new EmptyValueEntry());
	}
	@Override
	public boolean hasMesh(){
		return false;
	}
}