package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentDataInterface;
import org.schema.game.common.data.world.SegmentDataWriteException;

public class TerrainDecoTrees extends TerrainDeco {
	private final int minTrunkHeight;

	public TerrainDecoTrees() {
		this(4);
	}

	public TerrainDecoTrees(int minTrunkHeight) {
		this.minTrunkHeight = minTrunkHeight;
	}

	@Override
	public boolean generate(SegmentDataInterface data, final int xMax, final int yMax,
	                        final int zMax, Random randomContext) throws SegmentDataWriteException {

		int trunkHeight = randomContext.nextInt(3) + minTrunkHeight;

		if (yMax < 1 || yMax + trunkHeight + 1 > 256) {
			return false;
		}
		int k = data.getType(norm(xMax), norm(yMax - 1), norm(zMax));

		if (k != ElementKeyMap.TERRAIN_EARTH_TOP_DIRT
				&& k != ElementKeyMap.TERRAIN_DIRT_ID
				|| yMax >= 256 - trunkHeight - 1) {
			return false;
		}

		for (int y = yMax; y <= yMax + 1 + trunkHeight; y++) {
			int range = 1;

			if (y == yMax) {
				range = 0;
			}

			if (y >= (yMax + 1 + trunkHeight) - 2) {
				range = 2;
			}

			for (int x = xMax - range; x <= xMax + range; x++) {
				for (int z = zMax - range; z <= zMax + range; z++) {

					if (y >= 0 && y < 64) {
						int type = data.getType(norm(x), norm(y), norm(z));

						if (type != 0
								&& type != ElementKeyMap.TERRAIN_TREE_LEAF_ID
								&& type != ElementKeyMap.TERRAIN_EARTH_TOP_DIRT
								&& type != ElementKeyMap.TERRAIN_DIRT_ID
								&& type != ElementKeyMap.TERRAIN_TREE_TRUNK_ID) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}

		data.setInfoElementForcedAddUnsynched(norm(xMax), norm(yMax - 1),
				norm(zMax), ElementKeyMap.TERRAIN_DIRT_ID, false);
		byte leaveBushHeight = 3;
		int i = 0;

		for (int y = (yMax - leaveBushHeight) + trunkHeight; y <= yMax
				+ trunkHeight; y++) {
			int yDist = y - (yMax + trunkHeight);
			int leaveRange = (i + 1) - yDist / 2;

			for (int x = xMax - leaveRange; x <= xMax + leaveRange; x++) {
				int xDist = x - xMax;
				int xDistAbs = (Math.abs(xDist));

				for (int z = zMax - leaveRange; z <= zMax + leaveRange; z++) {
					int zDist = z - zMax;

					if ((xDistAbs != leaveRange
							|| Math.abs(zDist) != leaveRange || randomContext
							.nextInt(2) != 0 && yDist != 0)) {

						data.setInfoElementForcedAddUnsynched(norm(x), norm(y),
								norm(z), ElementKeyMap.TERRAIN_TREE_LEAF_ID,
								false);
					}
				}
			}
		}

		for (int trunkY = 0; trunkY < trunkHeight; trunkY++) {
			short type = data.getType(norm(xMax), norm(yMax + trunkY),
					norm(zMax));

			if (!(type != 0 && type != ElementKeyMap.TERRAIN_TREE_LEAF_ID)) {
				data.setInfoElementForcedAddUnsynched(norm(xMax), norm(yMax
								+ trunkY), norm(zMax),
						ElementKeyMap.TERRAIN_TREE_TRUNK_ID, false);
			}

		}

		return true;
	}

	@Override
	public int getRangeX(Random rand) {
		return 4 + rand.nextInt(8);
	}

	@Override
	public int getRangeZ(Random rand) {
		return 4 + rand.nextInt(8);
	}

	byte norm(int input) {
		return (byte) (Math.abs(input) % 16);
	}
}
