package org.schema.game.server.controller.world.factory.regions.city;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.regions.Region;

public class RadioTowerRegion extends Region {

	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();

	public RadioTowerRegion(BuildingWorld world, Region[] regions, Vector3i min, Vector3i max,
	                        int priority, int orientation) {
		super(regions, min, max, priority, orientation);

		min.y += BuildingRegion.HEIGTH;
		max.y += BuildingRegion.HEIGTH;
		min.x -= world.WORLD_HALF;
		min.z -= world.WORLD_HALF;
		max.x -= world.WORLD_HALF;
		max.z -= world.WORLD_HALF;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {

		//		getRelativePos(pos, relPos, true);
		//		getRelativePos(max, relMax, true);
		//		getRelativePos(min, relMin, true);
		//
//		if (pos.y == max.y - 1) {
//
//			return ElementKeyMap.LIGHT_BEACON_ID;
//		}

		return ElementKeyMap.HULL_COLOR_WHITE_ID;
	}

}
