package org.schema.game.common.controller.elements.fleetmanager;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementCollection;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class FleetManagerUnit extends ElementCollection<FleetManagerUnit, FleetManagerCollectionManager, VoidElementManager<FleetManagerUnit, FleetManagerCollectionManager>> {
	
	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}
}
