package org.schema.game.client.data.terrain.geimipmap.lodcalc;

import org.schema.game.client.data.terrain.geimipmap.TerrainPatch;

/**
 * @author bowens
 */
public class LodDistanceCalculatorFactory implements LodCalculatorFactory {

	private float lodThresholdSize = 100f;
	private LodThreshold lodThreshold = null;

	public LodDistanceCalculatorFactory() {

	}

	public LodDistanceCalculatorFactory(LodThreshold lodThreshold) {
		this.lodThreshold = lodThreshold;
	}

	@Override
	public LodCalculator createCalculator() {
		return new DistanceLodCalculator();
	}

	@Override
	public LodCalculator createCalculator(TerrainPatch terrainPatch) {
		if (lodThreshold == null) {
			lodThreshold = new SimpleLodThreshold(terrainPatch.getSize(), lodThresholdSize);
		}
		return new DistanceLodCalculator(terrainPatch, lodThreshold);
	}

}
