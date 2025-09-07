package org.schema.game.server.controller.world.factory.regions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.world.factory.regions.hooks.RegionHook;

public class CoreRegion extends UsableRegion {

	public CoreRegion(Vector3i controllerPosition, Region[] regions, Vector3i min, Vector3i max,
	                  int priority, int orientation, byte controlBlockOrientation) {
		super(controllerPosition, regions, min, max, priority, orientation, controlBlockOrientation);

	}

	@Override
	public RegionHook<?> createHook(Segment lastCreated) {
		return null;
	}

	@Override
	public boolean hasHook() {
		return false;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		if (pos.equals(controllerPosition)) {
			return ElementKeyMap.CORE_ID;
		}
		return Element.TYPE_ALL;
	}
}
