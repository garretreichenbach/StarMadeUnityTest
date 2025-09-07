package org.schema.game.common.controller.elements.factorymanager;

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
public class FactoryManagerUnit extends ElementCollection<FactoryManagerUnit, FactoryManagerCollection, VoidElementManager<FactoryManagerUnit, FactoryManagerCollection>> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}
}