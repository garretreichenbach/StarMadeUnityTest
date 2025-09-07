package org.schema.game.common.controller.elements.dockingBlock.fixed;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.EmptyValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockUnit;

public class FixedDockingBlockUnit extends DockingBlockUnit<FixedDockingBlockUnit, FixedDockingBlockCollectionManager, FixedDockingBlockElementManager> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state,
	                                          ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                          ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, "Fixed Docking Module", this, new EmptyValueEntry());
		//				col.getElementManager().getGUIUnitValues(this, col, supportCol, effectCol);
	}

}
