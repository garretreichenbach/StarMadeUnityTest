package org.schema.game.server.controller.world.factory.regions.city;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.regions.Region;

public class CityBaseRegion extends Region {

	public CityBaseRegion(BuildingWorld world, Region[] regions, Vector3i min, Vector3i max,
	                      int priority, int orientation) {
		super(regions, min, max, priority, orientation);

		min.y += BuildingRegion.HEIGTH;
		max.y += BuildingRegion.HEIGTH;

		min.x -= world.WORLD_HALF;
		min.z -= world.WORLD_HALF;
		max.x -= world.WORLD_HALF;
		max.z -= world.WORLD_HALF;
	}

	private short getWallBlockId() {
		return ElementKeyMap.HULL_COLOR_BLACK_ID;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {

		//		getRelativePos(pos, relPos, true);
		//		getRelativePos(max, relMax, true);
		//		getRelativePos(min, relMin, true);
		//
		if (pos.y - min.y == 0) {
			return getWallBlockId();
		}
		//
		return Element.TYPE_NONE;
	}

}
