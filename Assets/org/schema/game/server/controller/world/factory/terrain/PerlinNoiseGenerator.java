package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.game.common.data.world.Universe;

public class PerlinNoiseGenerator {
	public double xCoord;
	public double yCoord;
	public double zCoord;
	private int permutations[];
	public PerlinNoiseGenerator() {
		this(Universe.getRandom());
	}
	public PerlinNoiseGenerator(Random r) {
		permutations = new int[512];
		xCoord = r.nextDouble() * 256D;
		yCoord = r.nextDouble() * 256D;
		zCoord = r.nextDouble() * 256D;

		shuffle(r);

	}

	public static final double gradN(int a, double b, double c, double d) {
		int i = a & 0xf;
		double t0 = i >= 8 ? c : b;
		double t1 = i >= 4 ? i != 12 && i != 14 ? d : b : c;
		return ((i & 1) != 0 ? -t0 : t0) + ((i & 2) != 0 ? -t1 : t1);
	}

	public static final double gradAssert(int hash, double x, double y, double z) {
		double d1 = grad(hash, x, y, z);

		double d2 = gradN(hash, x, y, z);

		assert (d1 == d2);

		return d1;
	}

	public static final double grad(int hash, double x, double y, double z) {
		// float u = (h < 8) ? x : y;
		// float v = (h < 4) ? y : ((h == 12 || h == 14) ? x : z);
		// return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
		return switch(hash & 0xF) {
			case 0x0 -> x + y;
			case 0x1 -> -x + y;
			case 0x2 -> x - y;
			case 0x3 -> -x - y;
			case 0x4 -> x + x;
			case 0x5 -> -x + x;
			case 0x6 -> x - x;
			case 0x7 -> -x - x;
			case 0x8 -> y + x;
			case 0x9 -> -y + x;
			case 0xA -> y - x;
			case 0xB -> -y - x;
			case 0xC -> y + z;
			case 0xD -> -y + x;
			case 0xE -> y - x;
			case 0xF -> -y - z;
			default -> 0; // never happens
		};
	}

	public void shuffle(Random r) {
		for (int i = 0; i < 256; i++) {
			permutations[i] = i;
		}

		for (int j = 0; j < 256; j++) {
			int k = r.nextInt(256 - j) + j;
			int l = permutations[j];
			permutations[j] = permutations[k];
			permutations[k] = l;
			permutations[j + 256] = permutations[j];
		}
	}

	public final double lerp(double a, double b, double c) {
		return b + a * (c - b);
	}

	public final double grad2(int a, double b, double c) {
		int i = a & 0xf;
		double d = (1 - ((i & 8) >> 3)) * b;
		double d1 = i >= 4 ? i != 12 && i != 14 ? c : b : 0.0D;
		return ((i & 1) != 0 ? -d : d) + ((i & 2) != 0 ? -d1 : d1);
	}

	public void perlinNoise(double output[], double x, double y, double z,
	                        int width, int height, int depth, double sizeXHalf,
	                        double sizeyHalf, double sizeZHalf, double half) {
		if (height == 1) {
			perlinNoise2D(output, x, z, width, depth, sizeXHalf, sizeZHalf,
					half);
		} else {
			perlinNoise3D(output, x, y, z, width, height, depth, sizeXHalf,
					sizeyHalf, sizeZHalf, half);
		}

	}

	private void perlinNoise2D(double output[], double x, double z, int width,
	                           int depth, double sizeXHalf, double sizeZHalf, double half) {
		int counter = 0;
		double halfInv = 1.0D / half;

		for (int xVal = 0; xVal < width; xVal++) {
			double xDelim = x + xVal * sizeXHalf + xCoord;
			int xIndex = (int) xDelim;

			if (xDelim < xIndex) {
				xIndex--;
			}

			int xMod = xIndex & 0xff;
			xDelim -= xIndex;
			double d10 = xDelim * xDelim * xDelim
					* (xDelim * (xDelim * 6D - 15D) + 10D);

			for (int zVal = 0; zVal < depth; zVal++) {
				double zDelim = z + zVal * sizeZHalf + zCoord;
				int zIndex = (int) zDelim;

				if (zDelim < zIndex) {
					zIndex--;
				}

				int zMod = zIndex & 0xff;
				zDelim -= zIndex;
				double d14 = zDelim * zDelim * zDelim
						* (zDelim * (zDelim * 6D - 15D) + 10D);
				int i = permutations[xMod] + 0;
				int k = permutations[i] + zMod;
				int l = permutations[xMod + 1] + 0;
				int i1 = permutations[l] + zMod;
				double d2 = lerp(d10, grad2(permutations[k], xDelim, zDelim),
						gradN(permutations[i1], xDelim - 1.0D, 0.0D, zDelim));
				double d4 = lerp(
						d10,
						gradN(permutations[k + 1], xDelim, 0.0D, zDelim - 1.0D),
						gradN(permutations[i1 + 1], xDelim - 1.0D, 0.0D,
								zDelim - 1.0D));
				double d16 = lerp(d14, d2, d4);
				output[counter++] += d16 * halfInv;
			}
		}
	}

	private void perlinNoise3D(double output[], double x, double y, double z,
	                           int width, int height, int depth, double sizeXHalf,
	                           double sizeyHalf, double sizeZHalf, double half) {
		int iterA = 0;
		int iterB = -1;
		double invHalf = 1.0D / half;
		double lerp0 = 0.0D;
		double lerp1 = 0.0D;
		double lerp2 = 0.0D;
		double lerp3 = 0.0D;

		for (int xVal = 0; xVal < width; xVal++) {
			double xDelim = x + xVal * sizeXHalf + xCoord;
			int xx = (int) xDelim;

			if (xDelim < xx) {
				xx--;
			}

			int xMod = xx & 0xff;
			xDelim -= xx;
			double xNoiseS = xDelim * xDelim * xDelim
					* (xDelim * (xDelim * 6D - 15D) + 10D);

			for (int zVal = 0; zVal < depth; zVal++) {
				double zDelim = z + zVal * sizeZHalf + zCoord;
				int zz = (int) zDelim;

				if (zDelim < zz) {
					zz--;
				}

				int zMod = zz & 0xff;
				zDelim -= zz;
				double zNoiseS = zDelim * zDelim * zDelim
						* (zDelim * (zDelim * 6D - 15D) + 10D);

				for (int yVal = 0; yVal < height; yVal++) {
					double yDelim = y + yVal * sizeyHalf + yCoord;
					int yy = (int) yDelim;

					if (yDelim < yy) {
						yy--;
					}

					int yMod = yy & 0xff;
					yDelim -= yy;
					double yNoiseS = yDelim * yDelim * yDelim
							* (yDelim * (yDelim * 6D - 15D) + 10D);

					if (yVal == 0 || yMod != iterB) {
						iterB = yMod;
						int index0 = permutations[xMod] + yMod;
						int index1 = permutations[xMod + 1] + yMod;

						int index2 = permutations[index0] + zMod;
						int index3 = permutations[index0 + 1] + zMod;

						int index4 = permutations[index1] + zMod;
						int index5 = permutations[index1 + 1] + zMod;
						lerp0 = lerp(
								xNoiseS,
								gradN(permutations[index2], xDelim, yDelim,
										zDelim),
								gradN(permutations[index4], xDelim - 1.0D,
										yDelim, zDelim));
						lerp1 = lerp(
								xNoiseS,
								gradN(permutations[index3], xDelim,
										yDelim - 1.0D, zDelim),
								gradN(permutations[index5], xDelim - 1.0D,
										yDelim - 1.0D, zDelim));
						lerp2 = lerp(
								xNoiseS,
								gradN(permutations[index2 + 1], xDelim, yDelim,
										zDelim - 1.0D),
								gradN(permutations[index4 + 1], xDelim - 1.0D,
										yDelim, zDelim - 1.0D));
						lerp3 = lerp(
								xNoiseS,
								gradN(permutations[index3 + 1], xDelim,
										yDelim - 1.0D, zDelim - 1.0D),
								gradN(permutations[index5 + 1], xDelim - 1.0D,
										yDelim - 1.0D, zDelim - 1.0D));
					}

					double a = lerp(yNoiseS, lerp0, lerp1);
					double b = lerp(yNoiseS, lerp2, lerp3);
					double ab = lerp(zNoiseS, a, b);
					//for LoD: this is a factor  (remove + from += for deterministic values)
					output[iterA++] += ab * invHalf;
				}
			}
		}

	}
}
