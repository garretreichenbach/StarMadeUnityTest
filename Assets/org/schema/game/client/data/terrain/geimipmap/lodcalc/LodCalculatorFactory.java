package org.schema.game.client.data.terrain.geimipmap.lodcalc;

import org.schema.game.client.data.terrain.geimipmap.TerrainPatch;

/**
 * Creates LOD Calculator objects for the terrain patches.
 *
 * @author Brent Owens
 */
public interface LodCalculatorFactory {

	public LodCalculator createCalculator();

	public LodCalculator createCalculator(TerrainPatch terrainPatch);
}
