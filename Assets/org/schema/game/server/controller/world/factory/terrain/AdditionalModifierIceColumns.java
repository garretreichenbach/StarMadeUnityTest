package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementKeyMap;

public class AdditionalModifierIceColumns extends AdditionalModifierAbstract {
	private short coverBlock;
	private short extraBlock;
	private short fluid;

	private Random random;

	public AdditionalModifierIceColumns(short topBlock, short filler, short lava) {
		this.coverBlock = topBlock;
		this.extraBlock = filler;
		this.fluid = lava;
		random = new Random();
	}

	protected void generateBiggerColumn(long seed, int width, int depth,
	                                    short valueArray[], double randX, double randY, double randZ,
	                                    Random rand) {
		generateColumn(seed, width, depth, valueArray, randX, randY, randZ,
				1.0F + rand.nextFloat() * 6F, 0.0F, 0.0F, -1, -1, 0.5D);
	}

	protected void generateColumn(long seed, int width, int depth,
	                              short typeArray[], double randX, double randY, double randZ,
	                              float distX, float distY, float distZ, int dir0, int dir1,
	                              double dirz) {
		double xNorm = width * 16 + 8;
		double zNorm = depth * 16 + 8;
		float start = 0.0F;
		float end = 0.0F;
		random.setSeed(seed);

		if (dir1 <= 0) {
			int i = range * 16 - 16;
			dir1 = i - random.nextInt(i / 4);
		}

		boolean negDir0 = false;

		if (dir0 == -1) {
			dir0 = dir1 / 2;
			negDir0 = true;
		}

		int j = random.nextInt(dir1 / 2) + dir1 / 4;
		boolean flag1 = random.nextInt(6) == 0;

		for (; dir0 < dir1; dir0++) {
			double dir0N = 1.5D + (FastMath
					.sinFast((dir0 * FastMath.PI) / dir1) * distX * 1.0F);
			double n = dir0N * dirz;
			randX += random.nextInt(3) - 1;
			randY += 1;
			randZ += random.nextInt(3) - 1;

			if (flag1) {
				distZ *= 0.92F;
			} else {
				distZ *= 0.7F;
			}

			distZ += end * 0.1F;
			distY += start * 0.1F;
			end *= 0.9F;
			start *= 0.75F;
			end += (random.nextFloat() - random.nextFloat())
					* random.nextFloat() * 2.0F;
			start += (random.nextFloat() - random.nextFloat())
					* random.nextFloat() * 4F;

			if (!negDir0 && dir0 == j && distX > 1.0F && dir1 > 0) {
				generateColumn(random.nextLong(), width, depth, typeArray,
						randX, randY, randZ, random.nextFloat() * 0.5F + 0.5F,
						distY - (FastMath.PI / 2F), distZ / 3F, dir0, dir1,
						1.0D);
				generateColumn(random.nextLong(), width, depth, typeArray,
						randX, randY, randZ, random.nextFloat() * 0.5F + 0.5F,
						distY + (FastMath.PI / 2F), distZ / 3F, dir0, dir1,
						1.0D);
				return;
			}

			double xMin = randX - xNorm;
			double yMin = randZ - zNorm;
			double zMin = dir1 - dir0;
			double dirDistMar = distX + 2.0 + 16.0;

			if ((xMin * xMin + yMin * yMin) - zMin * zMin > dirDistMar
					* dirDistMar) {
				return;
			}

			if (randX < xNorm - 16D - dir0N * 2D
					|| randZ < zNorm - 16D - dir0N * 2D
					|| randX > xNorm + 16D + dir0N * 2D
					|| randZ > zNorm + 16D + dir0N * 2D) {
				continue;
			}

			xMin = (int) (randX - dir0N) - width * 16 - 1;
			int xMax = ((int) (randX + dir0N) - width * 16) + 1;
			yMin = (int) (randY - n) - 1;
			int yMax = (int) (randY + n) + 1;
			zMin = (int) (randZ - dir0N) - depth * 16 - 1;
			int zMax = ((int) (randZ + dir0N) - depth * 16) + 1;

			xMin = Math.max(0, xMin);
			xMax = Math.min(16, xMax);

			yMin = Math.max(1, yMin);
			yMax = Math.min(60, yMax);

			zMin = Math.max(0, zMin);
			zMax = Math.min(16, zMax);

			boolean hasWater = false;

			for (int xx = (int) xMin; !hasWater && xx < xMax; xx++) {
				for (int zz = (int) zMin; !hasWater && zz < zMax; zz++) {
					for (int yy = yMax + 1; !hasWater && yy >= yMin - 1; yy--) {
						int index = (xx * 16 + zz) * SEGMENT_HEIGHT + yy;

						if (yy < 0 || yy >= SEGMENT_HEIGHT) {
							continue;
						}

						if (typeArray[index] == ElementKeyMap.WATER) {
							hasWater = true;
						}

						if (yy != yMin - 1 && xx != xMin && xx != xMax - 1
								&& zz != zMin && zz != zMax - 1) {
							yy = (int) yMin;
						}
					}
				}
			}

			if (hasWater) {
				continue;
			}

			for (int xx = (int) xMin; xx < xMax; xx++) {
				double xD = (((xx + width * 16) + 0.5D) - randX) / dir0N;

				label0:

				for (int zz = (int) zMin; zz < zMax; zz++) {
					double zD = (((zz + depth * 16) + 0.5D) - randZ) / dir0N;
					int index = (xx * 16 + zz) * SEGMENT_HEIGHT + yMax;
					boolean isCover = false;

					if (xD * xD + zD * zD >= 1.0D) {
						continue;
					}

					int yDist = yMax - 1;

					do {
						if (yDist < yMin) {
							continue label0;
						}

						double yD = ((yDist + 0.5D) - randY) / n;

						if (yD > MARGIN && xD * xD + yD * yD + zD * zD < 1.0D) {
							short type = typeArray[index];

							if (type == coverBlock) {
								isCover = true;
							}
							if (isGroundType(type)) {
								if (yDist < 10) {
									typeArray[index] = fluid;
								} else {
									typeArray[index] = extraBlock;

									if (isCover
											&& typeArray[index - 1] == extraBlock) {
										typeArray[index - 1] = extraBlock;
									}
								}
							}

						}

						index--;
						yDist--;
					} while (true);
				}
			}

			if (negDir0) {
				break;
			}
		}
	}

	@Override
	protected boolean isGroundType(short type) {
		return true;
	}

	@Override
	protected void generate(int xLocal, int zLocal, int width,
	                        int depth, short typeArray[], Random rand) {

		int height = genHeight(rand);
		height = isZeroHeight(rand) ? 0 : height;

		for (int j = 0; j < height; j++) {
			double randX = getRandX(xLocal, rand);
			double randY = getRandY(rand);
			double randZ = getRandZ(zLocal, rand);
			int points = 1;

			if (isBigger(rand)) {
				generateBiggerColumn(rand.nextLong(), width, depth, typeArray,
						randX, randY, randZ, rand);
				points += getAdditionalPoints(rand);
			}

			for (int l = 0; l < points; l++) {

				generateColumn(rand.nextLong(), width, depth, typeArray, randX,
						randY, randZ, getDist0(rand), getDist1(rand),
						getDist2(rand), 0, 0, 1.0D);
			}
		}
	}
}
