package org.schema.game.common.controller.elements.dockingBlock;

import org.schema.game.common.data.element.ElementCollection;

public abstract class DockingBlockUnit<
		E extends DockingBlockUnit<E, EC, EM>,
		EC extends DockingBlockCollectionManager<E, EC, EM>,
		EM extends DockingBlockElementManager<E, EC, EM>>
		extends ElementCollection<E, EC, EM> {

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#significatorUpdate(int, int, int, int, int, int, int, int, int, long)
	 */
	@Override
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin,
	                                  int zMin, int xMax, int yMax, int zMax, long index) {

		int sx = xMax;
		int sy = yMax - (yMax - yMin) / 2;
		int sz = zMax - (zMax - zMin) / 2;

		significator = ElementCollection.getIndex(sx, sy, sz);
	}

	//	@Override
	//	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
	//		return ControllerManagerGUI.create(state, "Docking Module", this, new EmptyValueEntry());
	//	}

}