package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementKeyMap;

public class AdditionalModifierCave extends AdditionalModifierAbstract {

	private final short typeCover;
	private final short typeRock;
	private final short typeExtra;
	private final short typeFluid;

	public AdditionalModifierCave(short topBlock, short rock, short extra,
	                              short fluid, long seed) {
		this.typeCover = topBlock;
		this.typeExtra = extra;
		this.typeRock = rock;
		this.typeFluid = fluid;
	}

	protected void generateBigPoint(long sed, int x, int z, short values[],
	                                double randX, double randY, double randZ, Random random) {
		generatePoint(sed, x, z, values, randX, randY, randZ,
				1.0f + random.nextFloat() * 6f, 0, 0, -1, -1, 0.5, random);
	}

	protected void generatePoint(long rand, int x, int z, short typeArray[],
	                             double rX, double rY, double rZ, float dX, float dY, float dZ,
	                             int dirX, int dirY, double dirZ, Random random) {
		double xNorm = x * 16 + 8;
		double zNorm = z * 16 + 8;
		float start = 0;
		float end = 0;

		random.setSeed(rand);

		if (dirY <= 0) {
			int i = range * 16 - 16;
			dirY = i - random.nextInt(i / 4);
		}

		boolean negDir = false;

		if (dirX == -1) {
			dirX = dirY / 2;
			negDir = true;
		}

		int j = random.nextInt(dirY / 2) + dirY / 4;

		boolean isMore = random.nextInt(6) == 0;

		for (; dirX < dirY; dirX++) {
			double ddA = 1.5D + (FastMath.sinFast((dirX * FastMath.PI) / dirY)
					* dX * 1.0F);
			double d3 = ddA * dirZ;
			float f2 = FastMath.cosFast(dZ);
			float f3 = FastMath.sinFast(dZ);
			rX += FastMath.cosFast(dY) * f2;
			rY += f3;
			rZ += FastMath.sinFast(dY) * f2;

			if (isMore) {
				dZ *= 0.92F;
			} else {
				dZ *= 0.7F;
			}

			dZ += end * 0.1F;
			dY += start * 0.1F;
			end *= 0.9F;
			start *= 0.75F;
			end += (random.nextFloat() - random.nextFloat())
					* random.nextFloat() * 2.0F;
			start += (random.nextFloat() - random.nextFloat())
					* random.nextFloat() * 4F;

			if (!negDir && dirX == j && dX > 1.0F && dirY > 0) {
				generatePoint(random.nextLong(), x, z, typeArray, rX, rY, rZ,
						random.nextFloat() * 0.5F + 0.5F, dY
								- (FastMath.PI / 2F), dZ / 3F, dirX, dirY,
						1.0D, random);
				generatePoint(random.nextLong(), x, z, typeArray, rX, rY, rZ,
						random.nextFloat() * 0.5F + 0.5F, dY
								+ (FastMath.PI / 2F), dZ / 3F, dirX, dirY,
						1.0D, random);
				return;
			}

			if (!negDir && random.nextInt(4) == 0) {
				continue;
			}

			double minX = rX - xNorm;
			double minY = rZ - zNorm;
			double minZ = dirY - dirX;
			double d7 = dX + 2.0F + 16f;

			if ((minX * minX + minY * minY) - minZ * minZ > d7 * d7) {
				return;
			}

			if (rX < xNorm - 16D - ddA * 2D || rZ < zNorm - 16D - ddA * 2D
					|| rX > xNorm + 16D + ddA * 2D
					|| rZ > zNorm + 16D + ddA * 2D) {
				continue;
			}

			minX = (int) (rX - ddA) - x * 16 - 1;
			int maxX = ((int) (rX + ddA) - x * 16) + 1;
			minY = (int) (rY - d3) - 1;
			int maxY = (int) (rY + d3) + 1;
			minZ = (int) (rZ - ddA) - z * 16 - 1;
			int maxZ = ((int) (rZ + ddA) - z * 16) + 1;

			minX = Math.max(0, minX);
			maxX = Math.min(16, maxX);

			minY = Math.max(1, minY);
			maxY = Math.min(60, maxY);

			minZ = Math.max(0, minZ);
			maxZ = Math.min(16, maxZ);

			boolean hasWater = false;

			for (int xx = (int) minX; !hasWater && xx < maxX; xx++) {
				for (int zz = (int) minZ; !hasWater && zz < maxZ; zz++) {
					for (int yy = maxY + 1; !hasWater && yy >= minY - 1; yy--) {
						int index = (xx * 16 + zz) * SEGMENT_HEIGHT + yy;

						if (yy < 0 || yy >= SEGMENT_HEIGHT) {
							continue;
						}

						if (typeArray[index] == ElementKeyMap.WATER) {
							hasWater = true;
						}

						if (yy != minY - 1 && xx != minX && xx != maxX - 1
								&& zz != minZ && zz != maxZ - 1) {
							yy = (int) minY;
						}
					}
				}
			}

			if (hasWater) {
				continue;
			}

			for (int xx = (int) minX; xx < maxX; xx++) {
				double xD = (((xx + x * 16) + 0.5D) - rX) / ddA;
				label0:

				for (int zz = (int) minZ; zz < maxZ; zz++) {
					double zD = (((zz + z * 16) + 0.5D) - rZ) / ddA;
					int index = (xx * 16 + zz) * SEGMENT_HEIGHT + maxY;
					boolean isCover = false;

					if (xD * xD + zD * zD >= 1.0D) {
						continue;
					}

					int yDist = maxY - 1;

					do {
						if (yDist < minY) {
							continue label0;
						}

						double yD = ((yDist + 0.5D) - rY) / d3;

						if (yD > MARGIN && xD * xD + yD * yD + zD * zD < 1.0D) {
							short type = typeArray[index];

							if (type == typeCover) {
								isCover = true;
							}

							if (isGroundType(type)) {
								if (yDist < 10) {
									typeArray[index] = typeFluid;
								} else {
									typeArray[index] = 0;

									if (isCover
											&& typeArray[index - 1] == typeExtra) {
										typeArray[index - 1] = typeCover;
									}
								}
							}
						}

						index--;
						yDist--;
					} while (true);
				}
			}

			if (negDir) {
				break;
			}
		}
	}

	@Override
	protected boolean isGroundType(short type) {
		return type == typeRock || type == typeExtra || type == typeCover;
	}

	@Override
	protected void generate(int xLocal, int zLocal,
	                        int widthLocal, int depthLocal, short typeArray[], Random rand) {
		int height = genHeight(rand);
		height = isZeroHeight(rand) ? 0 : height;

		for (int j = 0; j < height; j++) {
			double randX = getRandX(xLocal, rand);
			double randY = getRandY(rand);
			double randZ = getRandZ(zLocal, rand);
			int cavePoints = 1;
			if (isBigger(rand)) {
				generateBigPoint(rand.nextLong(), widthLocal, depthLocal,
						typeArray, randX, randY, randZ, rand);
				cavePoints += getAdditionalPoints(rand);
			}

			for (int l = 0; l < cavePoints; l++) {
				generatePoint(rand.nextLong(), widthLocal, depthLocal,
						typeArray, randX, randY, randZ, getDist0(rand),
						getDist1(rand), getDist2(rand), 0, 0, 1d, rand);
			}
		}
	}

}
