package org.schema.game.server.controller.world.factory.terrain;

import java.util.Arrays;
import java.util.Random;

public class OctavesGenerator {
	private static final int MAX_OCTAVES = 32;
	private static double buffer[] = new double[MAX_OCTAVES];

	static {
		buffer[0] = 1.0d;
		for (int i = 1; i < MAX_OCTAVES; i++) {
			buffer[i] = buffer[i - 1] * 0.5d;
		}
	}

	private PerlinNoiseGenerator generators[];
	private int octaves;

	public OctavesGenerator(Random r, int octs) {

		octaves = octs;
		assert (octaves <= MAX_OCTAVES);
		generators = new PerlinNoiseGenerator[octs];

		for (int i = 0; i < octs; i++) {
			generators[i] = new PerlinNoiseGenerator(r);
		}
	}

	public double[] make2DOctaves(int x, int z, int width, int depth,
	                              double sizeX, double sizeZ, double noiseArray[]) {
		return make3DOctaves(x, 10, z, width, 1, depth, sizeX, 1.0d, sizeZ,
				noiseArray);
	}

	public double[] make3DOctaves(int x, int y, int z, int width, int height,
	                              int depth, double scaleX, double scaleY, double scaleZ,
	                              double output[]) {
		if (output == null) {
			output = new double[width * height * depth];
		} else {
			Arrays.fill(output, 0);
		}

		for (int j = 0; j < octaves; j++) {
			double halfBuffer = buffer[j];

			double xPosScaled = x * halfBuffer * scaleX;
			double yPosScaled = y * halfBuffer * scaleY;
			double zPosScaled = z * halfBuffer * scaleZ;
			long xModulo = (long) xPosScaled;
			long yModulo = (long) zPosScaled;
			xPosScaled -= xModulo;
			zPosScaled -= yModulo;
			xModulo %= 16777213L;
			yModulo %= 16777219L;
			xPosScaled += xModulo;
			zPosScaled += yModulo;
			generators[j].perlinNoise(output, xPosScaled, yPosScaled,
					zPosScaled, width, height, depth, scaleX * halfBuffer, scaleY
							* halfBuffer, scaleZ * halfBuffer, halfBuffer);
			
		}

		return output;
	}

}
