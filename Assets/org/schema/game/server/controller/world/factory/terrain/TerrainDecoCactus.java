package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentDataInterface;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.controller.world.factory.WorldCreatorPlanetFactory;

public class TerrainDecoCactus extends TerrainDeco {
	private final short trunkId;
	private final short leafId;
	private WorldCreatorPlanetFactory gen;

	public TerrainDecoCactus(WorldCreatorPlanetFactory factory,
	                         short trunkId, short leafId) {
		this.gen = factory;
		this.trunkId = trunkId;
		this.leafId = leafId;

	}

	@Override
	public boolean generate(SegmentDataInterface data, final int xMax, final int yMax,
	                        final int zMax, Random randomContext) throws SegmentDataWriteException {

		int trunkHeight = randomContext.nextInt(4) + 4;
		boolean flag = true;

		if (yMax < 1 || yMax + trunkHeight + 1 > 256) {
			return false;
		}

		for (int j = yMax; j <= yMax + 1 + trunkHeight; j++) {
			byte h = 1;

			if (j == yMax) {
				h = 0;
			}

			if (j >= (yMax + 1 + trunkHeight) - 2) {
				h = 2;
			}

			for (int l = xMax - h; l <= xMax + h && flag; l++) {
				for (int j1 = zMax - h; j1 <= zMax + h && flag; j1++) {
					if (j >= 0 && j < 64) {
						int type = data.getType(norm(l), norm(j), norm(j1));

						if (type != 0 && type != leafId && type != gen.getTop()
								&& type != gen.getFiller() && type != trunkId) {
							flag = false;
						}
					} else {
						flag = false;
					}
				}
			}
		}

		if (!flag) {
			return false;
		}

		int k = data.getType(norm(xMax), norm(yMax - 1), norm(zMax));

		if (k != ElementKeyMap.TERRAIN_EARTH_TOP_DIRT && k != gen.getFiller()
				|| yMax >= 256 - trunkHeight - 1) {
			return false;
		}

		data.setInfoElementForcedAddUnsynched(norm(xMax), norm(yMax - 1),
				norm(zMax), gen.getFiller(), false);

		boolean xAnc = false;
		boolean zAnc = false;
		for (int trunkY = 0; trunkY < trunkHeight; trunkY++) {
			int l2 = data.getType(norm(xMax), norm(yMax + trunkY), norm(zMax));
			// int l2 = par1World.getBlockId(xMax, yMax + l1, zMax);

			if (l2 != 0) {
				continue;
			}
			// System.err.println("Setting trunk at "+norm(xMax)+"; "+norm(zMax));
			data.setInfoElementForcedAddUnsynched(norm(xMax), norm(yMax
					+ trunkY), norm(zMax), leafId, false);

			if (trunkY > 0 && trunkY < trunkHeight - 1
					&& Universe.getRandom().nextFloat() > 0.7f) {
				if (Universe.getRandom().nextFloat() > 0.5) {
					byte x = norm(xMax);
					byte y = norm(yMax + trunkY);
					byte z = norm(zMax
							+ (Universe.getRandom().nextFloat() > 0.5 ? 1 : -1));

					data.setInfoElementForcedAddUnsynched(x, y, z, leafId,
							false);

					if (!zAnc && Universe.getRandom().nextFloat() > 0.8) {
						zAnc = true;
						if (zMax < z) {
							z++;
						} else {
							z--;
						}
						data.setInfoElementForcedAddUnsynched(x, y, z, leafId,
								false);
						data.setInfoElementForcedAddUnsynched(x,
								(byte) (y + 1), z, leafId, false);
					}
				} else {
					byte x = norm(xMax
							+ (Universe.getRandom().nextFloat() > 0.5 ? 1 : -1));
					byte y = norm(yMax + trunkY);
					byte z = norm(zMax);
					data.setInfoElementForcedAddUnsynched(x, y, z, leafId,
							false);
					if (!xAnc && Universe.getRandom().nextFloat() > 0.8) {
						xAnc = true;
						if (xMax < x) {
							x++;
						} else {
							x--;
						}
						data.setInfoElementForcedAddUnsynched(x, y, z, leafId,
								false);
						data.setInfoElementForcedAddUnsynched(x,
								(byte) (y + 1), z, leafId, false);
					}
				}
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
