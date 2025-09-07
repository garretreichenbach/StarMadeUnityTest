package org.schema.game.common.data.factory;

import java.util.HashMap;

import org.schema.common.util.linAlg.Vector3i;

public class FactoryState {
	private final HashMap<Vector3i, FactoryUnit> units = new HashMap<Vector3i, FactoryUnit>();

	/**
	 * @return the units
	 */
	public HashMap<Vector3i, FactoryUnit> getUnits() {
		return units;
	}

}
