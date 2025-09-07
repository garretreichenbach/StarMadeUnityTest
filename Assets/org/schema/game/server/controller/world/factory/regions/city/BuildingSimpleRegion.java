package org.schema.game.server.controller.world.factory.regions.city;

import java.util.Random;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.regions.Region;

public class BuildingSimpleRegion extends Region {

	public static int HEIGTH = 32;

	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();

	private Random random;

	public BuildingSimpleRegion(BuildingWorld world, Region[] regions, Vector3i min, Vector3i max,
	                            int priority, int orientation, Random random) {
		super(regions, min, max, priority, orientation);

		min.y += HEIGTH;
		max.y += HEIGTH;

		min.x -= world.WORLD_HALF;
		min.z -= world.WORLD_HALF;
		max.x -= world.WORLD_HALF;
		max.z -= world.WORLD_HALF;
		this.random = random;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {

		getRelativePos(pos, relPos, true);
		getRelativePos(max, relMax, true);
		getRelativePos(min, relMin, true);
		//
		//		if(pos.x == 0 || pos.z == 0 || pos.x == max.x || pos.z == max.z || pos.y == max.y){

		if (pos.y % 8 == 0) {
			int i = random.nextInt(48);
			if (max.y - pos.y > 5 && i < 9) {
				if (!(relPos.x < 1 || relPos.z < 1 || pos.x > relMax.x - 2 || relPos.z > relMax.z - 2 || relPos.y == relMax.y - 1)) {
					switch (i) {
						case (0):
							return ElementKeyMap.LIGHT_YELLOW;
						case (1):
							return ElementKeyMap.LIGHT_ID;
						case (2):
							return ElementKeyMap.LIGHT_RED;
						case (3):
							return ElementKeyMap.LIGHT_YELLOW;
						case (4):
							return ElementKeyMap.LIGHT_YELLOW;
						case (5):
							return ElementKeyMap.LIGHT_RED;
						case (6):
							return ElementKeyMap.LIGHT_YELLOW;
						case (7):
							return ElementKeyMap.LIGHT_BLUE;
						case (8):
							return ElementKeyMap.LIGHT_YELLOW;
					}
				}
			}
			return ElementKeyMap.HULL_ID;
		} else {
			if (relPos.x < 1 || relPos.z < 1 || relPos.x > relMax.x - 2 || relPos.z > relMax.z - 2 || relPos.y == relMax.y - 1) {

				if (pos.y % 8 > 3 && max.y - pos.y > 5) {
					if (pos.x % 8 > 2 || pos.z % 8 > 2) {
						return ElementKeyMap.GLASS_ID;
					}
				}

				return ElementKeyMap.HULL_COLOR_BLACK_ID;
			}
		}
		//		}
		//
		return Element.TYPE_ALL;
	}

}
