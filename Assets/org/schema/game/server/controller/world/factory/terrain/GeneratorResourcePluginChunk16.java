package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentDataInterface;
import org.schema.game.common.data.world.SegmentDataWriteException;

public class GeneratorResourcePluginChunk16 extends TerrainDeco {
	private final short convertableBlock;
	protected short minableBlockId;
	private int numberOfBlocks;

	public GeneratorResourcePluginChunk16(int count, short type, short convertableBlock) {
		this.minableBlockId = type;
		this.numberOfBlocks = count;
		this.convertableBlock = convertableBlock;

		assert (convertableBlock > 0 && ElementKeyMap.getInfo(convertableBlock).resourceInjection != ResourceInjectionType.OFF) : ElementKeyMap.toString(convertableBlock);
	}

	@Override
	public boolean generate(SegmentDataInterface segmentData, int xStart, int yStart,
	                        int zStart, Random randomContext) throws SegmentDataWriteException {
		float randomAngle = randomContext.nextFloat() * FastMath.PI;

		double sinF = FastMath.sinTable(randomAngle);
		double cosF = FastMath.cosTable(randomAngle);

		double xA = (xStart + 8) + (sinF * numberOfBlocks) * 0.125d;
		double xB = (xStart + 8) - (sinF * numberOfBlocks) * 0.125d;

		double yA = (yStart + randomContext.nextInt(3)) - 2;
		double yB = (yStart + randomContext.nextInt(3)) - 2;

		double zA = (zStart + 8) + (cosF * numberOfBlocks) * 0.125d;
		double zB = (zStart + 8) - (cosF * numberOfBlocks) * 0.125d;

		double invNumberOfBlocks = 1d / numberOfBlocks;

		for (int i = 0; i <= numberOfBlocks; i++) {
			double xRes = xA + ((xB - xA) * i) * invNumberOfBlocks;
			double yRes = yA + ((yB - yA) * i) * invNumberOfBlocks;
			double zRes = zA + ((zB - zA) * i) * invNumberOfBlocks;

			double rand = (randomContext.nextDouble() * numberOfBlocks) * 0.0625d;

			create(segmentData, i, randomContext, invNumberOfBlocks, xRes,
					yRes, zRes, rand);

		}

		return true;
	}

	private void create(SegmentDataInterface segmentData, int i, Random randomContext,
	                    double invNumberOfBlocks, double xRes, double yRes, double zRes,
	                    double rand) throws SegmentDataWriteException {

		double sinA = (FastMath.sinTable((i * FastMath.PI)
				* (float) invNumberOfBlocks) + 1.0f)
				* rand + 1.0d;
		double sinB = sinA;

		int fromX = (int) (xRes - sinA * 0.5d);
		int fromY = (int) (yRes - sinB * 0.5d);
		int fromZ = (int) (zRes - sinA * 0.5d);

		int toX = (int) (xRes + sinA * 0.5d);
		int toY = (int) (yRes + sinB * 0.5d);
		int toZ = (int) (zRes + sinA * 0.5d);

		for (int xRun = fromX; xRun <= toX; xRun++) {
			double xProbe = ((xRun + 0.5d) - xRes) / (sinA * 0.5d);
			double xProbe2 = xProbe * xProbe;

			if (xProbe2 < 1.0d) {
				for (int yRun = fromY; yRun <= toY; yRun++) {
					double yProbe = ((yRun + 0.5d) - yRes) / (sinB * 0.5d);
					double yProbe2 = yProbe * yProbe;

					if (xProbe2 + yProbe2 < 1.0d) {

						for (int zRun = fromZ; zRun <= toZ; zRun++) {
							double zProbe = ((zRun + 0.5d) - zRes)
									/ (sinA * 0.5d);

							if (xProbe2 + yProbe2 + zProbe * zProbe < 1.0d
									&& segmentData.getType(
									(byte) Math.abs(xRun % 16),
									(byte) Math.abs(yRun % 16),
									(byte) Math.abs(zRun % 16)) == convertableBlock) {

								setBlock(segmentData, xRun, yRun, zRun);
							}
						}
					}
				}
			}
		}
	}

	public void setBlock(SegmentDataInterface data, int x, int y, int z) throws SegmentDataWriteException {

		byte orientation = ElementKeyMap.resIDToOrientationMapping[minableBlockId];

		orientation = (byte) Math.min(Math.max(1, orientation),  15);
		assert (orientation > 0 && orientation < 16);
		data.setInfoElementForcedAddUnsynched((byte) Math.abs(x % 16),
				(byte) Math.abs(y % 16), (byte) Math.abs(z % 16),
				convertableBlock, orientation, (byte) 0,
				false);
	}
}