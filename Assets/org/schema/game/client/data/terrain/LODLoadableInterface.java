package org.schema.game.client.data.terrain;

import java.util.Vector;

import org.schema.game.client.data.terrain.geimipmap.LODGeomap;

public interface LODLoadableInterface {
	public Vector<LODGeomap> getGeoMapsToLoad();

	public void load();
}
