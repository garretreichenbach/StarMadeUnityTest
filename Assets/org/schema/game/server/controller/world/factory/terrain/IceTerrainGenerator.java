package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.game.server.controller.RequestDataPlanet;

public class IceTerrainGenerator extends TerrainGenerator {

	public IceTerrainGenerator(long seed) {
		super(seed);
		this.setFlatness(0.652342742342441661d);

		halfHeightFactor = 0.4;
		heighNormValue = 48;//default 128;

		planetEdgeHeight = 36; //default 24
		polyMargin = 44f; //default 28

		hasC = true;
	}

	@Override
	protected void initOctaves(int xPos, int yPos, int zPos, int width, int height, int depth, RequestDataPlanet requestData) {
		double d = 393.12345431233d;
		double d1 = 621.5127634345d;

		double smooth2d = 100;

		requestData.getR().noise2DMid16 = noiseGen6Oct16for2D.make2DOctaves(xPos, zPos, width, depth, smooth2d, smooth2d, requestData.getR().noise2DMid16);

		double smoothXZ = 40;
		double smoothY = 180;

		requestData.getR().noiseSmall8 = noiseGen3Oct8.make3DOctaves(xPos, yPos, zPos, width, height, depth, d / smoothXZ, d1 / smoothY, d / smoothXZ, requestData.getR().noiseSmall8);
		requestData.getR().noise1Big16 = noiseGen1Oct16.make3DOctaves(xPos, yPos, zPos, width, height, depth, d * 3f, d1 / 1.6d, d * 3f, requestData.getR().noise1Big16);
		requestData.getR().noise2Big16 = noiseGen2Oct16.make3DOctaves(xPos, yPos, zPos, width, height, depth, d * 3f, d1 / 1.9d, d * 3f, requestData.getR().noise2Big16);
	}

	@Override
	protected void initNoises(Random rand) {
		noiseGen1Oct16 = new OctavesGenerator(rand, 20);
		noiseGen2Oct16 = new OctavesGenerator(rand, 19);
		noiseGen3Oct8 = new OctavesGenerator(rand, 7);
		noiseGen4Oct4 = new OctavesGenerator(rand, 5);
		//        noiseGen5Oct10for2D = new NoiseGeneratorOctaves(rand, 10);
		noiseGen6Oct16for2D = new OctavesGenerator(rand, 8);
	}

}
