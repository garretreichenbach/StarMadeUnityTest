package org.schema.game.common.data.world.planet.old.texgen.palette;

import org.schema.game.common.data.world.planet.old.texgen.palette.TerrainPalette.Colors;

public interface Palette {

	public Colors getPointColor(ColorMixer mixer, int x, int y, int height, int slope,
	                            float temp);

}
