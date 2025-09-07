package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementKeyMap;

public class AdditionalModifierSlimColumn extends AdditionalModifierAbstract {
	private short topBlock;
	private short main;
	private short lava;

	public AdditionalModifierSlimColumn(short cover, short main, short lava) {
		this.topBlock = cover;
		this.main = main;
		this.lava = lava;
	}

	protected void generateBiggerSlimColumn(long seed, int width, int depth,
	                                        short typesArray[], double randX, double randY, double randZ,
	                                        Random rand) {
		generateSlimColumn(seed, width, depth, typesArray, randX, randY, randZ,
				1.0F + rand.nextFloat() * 6F, 0.0F, 0.0F, -1, -1, 0.5D, rand);
	}

	protected void generateSlimColumn(long seed, int width, int depth,
	                                  short typesArray[], double randX, double randY, double randZ,
	                                  float dist0, float dist1, float dist2, int dir0, int dir1,
	                                  double dir2, Random rand) {
		double xNorm = width * 16 + 8;
		double zNorm = depth * 16 + 8;
		float start = 0.0F;
		float end = 0.0F;
		Random random = new Random(seed);

		if (dir1 <= 0) {
			int i = range * 16 - 16;
			dir1 = i - random.nextInt(i / 4);
		}

		boolean dirNeg = false;

		if (dir0 == -1) {
			dir0 = dir1 / 2;
			dirNeg = true;
		}

		int j = random.nextInt(dir1 / 2) + dir1 / 4;
		boolean extraH = random.nextInt(6) == 0;

		for (; dir0 < dir1; dir0++) {
			double dirL = 1.5D + (FastMath.sinFast((dir0 * FastMath.PI) / dir1)
					* dist0 * 1.0F);
			double xE = dirL * dir2;
			float f2 = FastMath.cosFast(dist2);
			float f3 = FastMath.sinFast(dist2);
			randX += FastMath.cosFast(dist1) * f2;
			randY += f3;
			randZ += FastMath.sinFast(dist1) * f2;

			if (extraH) {
				dist2 *= 0.92F;
			} else {
				dist2 *= 0.7F;
			}

			dist2 += end * 0.1F;
			dist1 += start * 0.1F;
			end *= 0.9F;
			start *= 0.75F;
			end += (random.nextFloat() - random.nextFloat())
					* random.nextFloat() * 2.0F;
			start += (random.nextFloat() - random.nextFloat())
					* random.nextFloat() * 4F;

			if (!dirNeg && dir0 == j && dist0 > 1.0F && dir1 > 0) {
				generateSlimColumn(random.nextLong(), width, depth, typesArray,
						randX, randY, randZ, random.nextFloat() * 0.5F + 0.5F,
						dist1 - (FastMath.HALF_PI), dist2 / 3F, dir0, dir1,
						1.0D, rand);
				generateSlimColumn(random.nextLong(), width, depth, typesArray,
						randX, randY, randZ, random.nextFloat() * 0.5F + 0.5F,
						dist1 + (FastMath.HALF_PI), dist2 / 3F, dir0, dir1,
						1.0D, rand);
				return;
			}

			if (!dirNeg && random.nextInt(4) == 0) {
				continue;
			}

			double minX = randX - xNorm;
			double minY = randZ - zNorm;
			double minZ = dir1 - dir0;
			double d7 = dist0 + 2.0F + 16F;

			if ((minX * minX + minY * minY) - minZ * minZ > d7 * d7) {
				return;
			}

			if (randX < xNorm - 16D - dirL * 2D
					|| randZ < zNorm - 16D - dirL * 2D
					|| randX > xNorm + 16D + dirL * 2D
					|| randZ > zNorm + 16D + dirL * 2D) {
				continue;
			}

			minX = (int) (randX - dirL) - width * 16 - 1;
			int maxX = ((int) (randX + dirL) - width * 16) + 1;
			minY = (int) (randY - xE) - 1;
			int maxY = (int) (randY + xE) + 1;
			minZ = (int) (randZ - dirL) - depth * 16 - 1;
			int maxZ = ((int) (randZ + dirL) - depth * 16) + 1;

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
						int j2 = (xx * 16 + zz) * SEGMENT_HEIGHT + yy;

						if (yy < 0 || yy >= SEGMENT_HEIGHT) {
							continue;
						}

						if (typesArray[j2] == ElementKeyMap.WATER) {
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
				double xD = (((xx + width * 16) + 0.5D) - randX) / dirL;

				label0:

				for (int zz = (int) minZ; zz < maxZ; zz++) {
					double zD = (((zz + depth * 16) + 0.5D) - randZ) / dirL;
					int index = (xx * 16 + zz) * SEGMENT_HEIGHT + maxY;
					boolean isCover = false;

					if (xD * xD + zD * zD >= 1.0D) {
						continue;
					}

					int yyD = maxY - 1;

					do {
						if (yyD < minY) {
							continue label0;
						}

						double yD = ((yyD + 0.5D) - randY) / xE;

						if (yD > MARGIN && xD * xD + yD * yD + zD * zD < 1.0D) {
							short type = typesArray[index];

							if (type == topBlock) {
								isCover = true;
							}
							if (isGroundType(type)) {
								if (yyD < 10) {
									typesArray[index] = lava;
								} else {
									typesArray[index] = main;

									if (isCover
											&& typesArray[index - 1] == main) {
										typesArray[index - 1] = main;
									}
								}
							}
						}

						index--;
						yyD--;
					} while (true);
				}
			}

			if (dirNeg) {
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
				generateBiggerSlimColumn(rand.nextLong(), width, depth,
						typeArray, randX, randY, randZ, rand);
				points += getAdditionalPoints(rand);
			}

			for (int l = 0; l < points; l++) {

				generateSlimColumn(rand.nextLong(), width, depth, typeArray,
						randX, randY, randZ, getDist0(rand), getDist1(rand),
						getDist2(rand), 0, 0, 1.0D, rand);
			}
		}
	}
}
