package org.schema.game.common.controller.elements.shield.regen;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.EmptyValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.shield.CenterOfMassUnit;
import org.schema.schine.common.language.Lng;

public class ShieldRegenUnit extends CenterOfMassUnit<ShieldRegenUnit, ShieldRegenCollectionManager, VoidElementManager<ShieldRegenUnit, ShieldRegenCollectionManager>> {

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#significatorUpdate(int, int, int, int, int, int, int, int, int, long)
	 */
	@Override
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin,
	                                  int zMin, int xMax, int yMax, int zMax, long index) {
		super.significatorUpdateMin(x, y, z, xMin, yMin, zMin, xMax, yMax, zMax, index);
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, Lng.str("Shield Module"), this, new EmptyValueEntry());
	}


	
}
