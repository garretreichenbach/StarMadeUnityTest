package org.schema.game.common.controller.elements.shield.capacity;

import org.schema.common.util.CompareTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.EmptyValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.shield.CenterOfMassUnit;
import org.schema.schine.common.language.Lng;

public class ShieldCapacityUnit extends CenterOfMassUnit<ShieldCapacityUnit, ShieldCapacityCollectionManager, VoidElementManager<ShieldCapacityUnit, ShieldCapacityCollectionManager>> implements Comparable<ShieldCapacityUnit>{

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
		return ControllerManagerGUI.create(state, Lng.str("Shield Capacity Module"), this, new EmptyValueEntry());
	}

	@Override
	public int compareTo(ShieldCapacityUnit o) {
		return CompareTools.compare(idPos, o.idPos);
	}



	
}
