package org.schema.game.client.data.terrain.geimipmap.lodcalc;

import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.terrain.geimipmap.TerrainPatch;
import org.schema.game.client.data.terrain.geimipmap.UpdatedTerrainPatch;

/**
 * Calculate the Level of Detail of a terrain patch based on the
 * cameras, or other locations.
 *
 * @author Brent Owens
 */
public interface LodCalculator {

	public boolean calculateLod(List<Vector3f> locations, HashMap<String, UpdatedTerrainPatch> updates);

	public void setTerrainPatch(TerrainPatch terrainPatch);
}
