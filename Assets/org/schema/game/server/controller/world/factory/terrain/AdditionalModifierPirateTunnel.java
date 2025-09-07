package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.common.FastMath;

public class AdditionalModifierPirateTunnel extends AdditionalModifierAbstract {

	private final double sVal = 1.0d;
	private short main;
	private short filler;
	private short light;
	private Random random;

	public AdditionalModifierPirateTunnel(short main, short filler, short light) {
		this.filler = filler;
		this.main = main;
		this.light = light;
		random = new Random();
		range = 4;
	}

	protected void generatePirateTunnel(long seed, int x, int z,
	                                    short valuesArray[], double randX, double randY, double randZ,
	                                    float dist0, float dist1, float dist2, int dir0, int dir1,
	                                    double dir2, short type, Random rand) {

		double xNorm = x * 16 + 8;
		double zNorm = z * 16 + 8;
		float start = 0.0F;
		float end = 0.0F;
		random.setSeed(seed);

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
		boolean flag1 = random.nextInt(6) == 0;

		for (; dir0 < dir1; dir0++) {
			double ddA = 1.5D + (FastMath.sinFast((dir0 * FastMath.PI) / dir1)
					* dist0 * 1.0F);
			double d3 = ddA * dir2;
			float f2 = FastMath.cosFast(dist2);
			float f3 = FastMath.sinFast(dist2);
			randX += FastMath.cosFast(dist1) * f2;
			randY += f3;
			randZ += FastMath.sinFast(dist1) * f2;

			if (flag1) {
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
				boolean r = random.nextInt(5) == 0;
				// -1, -1 -> tower
				if (random.nextInt(2) == 0) {
					generatePirateTunnel(random.nextLong(), x, z, valuesArray,
							randX, randY, randZ,
							random.nextFloat() * 0.9F + 0.5F, dist1
									- ((float) Math.PI / 2F), dist2 / 3F, dir0,
							dir1, r ? xNorm : sVal, r ? main : filler, rand);
					generatePirateTunnel(random.nextLong(), x, z, valuesArray,
							randX, randY, randZ, 1.0F + rand.nextFloat() * 3F,
							dist1 - ((float) Math.PI / 6F), dist2, dir0, dir1,
							r ? xNorm : sVal, r ? main : filler, rand);
				} else {
					generatePirateTunnel(random.nextLong(), x, z, valuesArray,
							randX, randY, randZ,
							random.nextFloat() * 0.8F + 0.8F, dist1
									+ ((float) Math.PI / 2F), dist2 / 2F, dir0,
							dir1, sVal, filler, rand);
				}

				return;
			}

			if (!dirNeg && random.nextInt(2) == 0) {
				continue;
			}

			double minX = randX - xNorm;
			double minY = randZ - zNorm;
			double minZ = dir1 - dir0;
			double d7 = dist0 + 2.0F + 16F;

			if ((minX * minX + minY * minY) - minZ * minZ > d7 * d7) {
				return;
			}

			if (randX < xNorm - 16D - ddA * 2D
					|| randZ < zNorm - 16D - ddA * 2D
					|| randX > xNorm + 16D + ddA * 2D
					|| randZ > zNorm + 16D + ddA * 2D) {
				continue;
			}

			minX = (int) (randX - ddA) - x * 16 - 1;
			int maxX = ((int) (randX + ddA) - x * 16) + 1;
			minY = (int) (randY - d3) - 1;
			int maxY = (int) (randY + d3) + 1;
			minZ = (int) (randZ - ddA) - z * 16 - 1;
			int maxZ = ((int) (randZ + ddA) - z * 16) + 1;

			minX = Math.max(0, minX);
			maxX = Math.min(16, maxX);

			minY = Math.max(1, minY);
			maxY = Math.min(60, maxY);

			minZ = Math.max(0, minZ);
			maxZ = Math.min(16, maxZ);

			for (int xLocal = (int) minX; xLocal < maxX; xLocal++) {
				double xLocalPoint = (((xLocal + x * 16) + 0.5D) - randX) / ddA;

				label0:

				for (int zLocal = (int) minZ; zLocal < maxZ; zLocal++) {
					double zLocalPoint = (((zLocal + z * 16) + 0.5D) - randZ)
							/ ddA;

					int index = (xLocal * 16 + zLocal) * SEGMENT_HEIGHT + maxY;

					short typeTaken = type;
					double dist = xLocalPoint * xLocalPoint + zLocalPoint
							* zLocalPoint;
					if (dist >= 1.0d) {
						continue;
					}

					int yDist = maxY - 1;

					do {
						if (yDist < minY) {
							continue label0;
						}

						double yD = ((yDist + 0.5D) - randY) / d3;

						if (yD > MARGIN
								&& xLocalPoint * xLocalPoint + yD * yD
								+ zLocalPoint * zLocalPoint < 1.0d) {
							if (typeTaken == main && yDist % 10 == 0) {
								valuesArray[index] = light;
							} else {
								valuesArray[index] = typeTaken;

							}

						}

						index--;
						yDist--;
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
	protected boolean isBigger(Random rand) {
		return rand.nextInt(3) == 0;
	}

	@Override
	protected void generate(int x, int z, int width,
	                        int depth, short typeArray[], Random rand) {

		int height = 0;

		if (rand.nextInt(5) == 0) {
			height = rand.nextInt(3) + 1;
		}

		for (int j = 0; j < height; j++) {
			double randX = getRandX(x, rand);
			double randY = getRandY(rand);
			double randZ = getRandZ(z, rand);
			int points = 1;

			if (isBigger(rand)) {
				generateLargerPirateTunnel(rand.nextLong(), width, depth,
						typeArray, randX, randY, randZ, rand);
				points += getAdditionalPoints(rand);
			}

			for (int l = 0; l < points; l++) {
				generatePirateTunnel(rand.nextLong(), width, depth, typeArray,
						randX, randY, randZ, getDist0(rand), getDist1(rand),
						getDist2(rand), 0, 0, 2.0, main, rand);
			}
		}
	}

	protected void generateLargerPirateTunnel(long seed, int x, int z,
	                                          short valuesArray[], double randX, double randY, double randZ,
	                                          Random rand) {
		generatePirateTunnel(seed, x, z, valuesArray, randX, randY, randZ,
				1.0F + rand.nextFloat() * 6F, 0.0F, 0.0F, -1, -1, 0.5D, filler,
				rand);
	}
}
