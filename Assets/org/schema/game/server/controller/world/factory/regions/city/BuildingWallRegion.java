package org.schema.game.server.controller.world.factory.regions.city;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.regions.Region;

public class BuildingWallRegion extends Region {

	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();

	public BuildingWallRegion(BuildingWorld world, Region[] regions, Vector3i min, Vector3i max,
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
		return ElementKeyMap.HULL_ID;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {

		getRelativePos(pos, relPos, true);
		getRelativePos(max, relMax, true);
		getRelativePos(min, relMin, true);

		if (relPos.x < 1 || relPos.z < 1 || relPos.x > relMax.x - 2 || relPos.z > relMax.z - 2 || relPos.y == relMax.y - 1) {

			if (pos.y % 8 > 3 && max.y - pos.y > 5) {
				if (pos.x % 8 < 3 || pos.z % 8 < 3) {
					return ElementKeyMap.GLASS_ID;
				}
			}

		}
		if (pos.y % 8 == 0) {
			return ElementKeyMap.TERRAIN_ICEPLANET_CRYSTAL;
		}
		//		if(pos.x == 0 || pos.z == 0 || pos.x == max.x || pos.z == max.z || pos.y == max.y){
		return getWallBlockId();
		//		}

		//		return Element.TYPE_ALL;
	}

}
