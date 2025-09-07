package org.schema.game.client.data.terrain.geimipmap.lodcalc;

/**
 * Just multiplies the terrain patch size by 2. So every two
 * patches away the camera is, the LOD changes.
 * <p/>
 * Set it higher to have the LOD change less frequently.
 *
 * @author bowens
 */
public class SimpleLodThreshold implements LodThreshold {

	private int size; // size of a terrain patch
	private float lodMultiplier = 2;

	public SimpleLodThreshold(int patchSize, float lodMultiplier) {
		this.size = patchSize;
		this.lodMultiplier = lodMultiplier;
	}

	@Override
	public float getLodDistanceThreshold() {
		return size * lodMultiplier;
	}

}
