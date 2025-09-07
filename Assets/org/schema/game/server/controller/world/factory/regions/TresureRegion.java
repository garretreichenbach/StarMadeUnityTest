package org.schema.game.server.controller.world.factory.regions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.world.factory.regions.hooks.FillChestHook;
import org.schema.game.server.controller.world.factory.regions.hooks.RegionHook;

public class TresureRegion extends UsableRegion {

	private boolean needsHook;

	public TresureRegion(Vector3i controllerPosition, Region[] regions, Vector3i min, Vector3i max,
	                     int priority, int orientation, byte controlBlockOrientation) {
		super(controllerPosition, regions, min, max, priority, orientation, controlBlockOrientation);

	}

	@Override
	public RegionHook<?> createHook(Segment lastCreated) {
		FillChestHook hook = new FillChestHook();
		hook.initialize(this, lastCreated);
		needsHook = false;

		return hook;
	}

	@Override
	public boolean hasHook() {
		return needsHook;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		if (pos.equals(controllerPosition)) {
			needsHook = true;
			return ElementKeyMap.STASH_ELEMENT;
		}
		return Element.TYPE_ALL;
	}
}
