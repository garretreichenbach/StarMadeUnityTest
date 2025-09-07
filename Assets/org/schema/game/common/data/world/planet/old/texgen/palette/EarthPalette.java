/*
 	This file is part of jME Planet Demo.

    jME Planet Demo is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation.

    jME Planet Demo is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jME Planet Demo.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.schema.game.common.data.world.planet.old.texgen.palette;

import java.awt.Color;

import org.schema.game.common.data.world.planet.old.PlanetInformations;

public class EarthPalette extends TerrainPalette {

	public EarthPalette(PlanetInformations informations) {
		super(informations);
	}

	@Override
	public void initPalette() {

		ColorRange tundra = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel(), 4,
				-1, -1, 0, 2.5f,
				-258, 80, 5, 1.0f,
				new Color(0x413a28), Color.black
		);

		ColorRange herbe = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel(), 3,
				-1, -1, 0, 2,
				0, 80, 18, 3.5f,
				new Color(0x474D30), Color.black
		);

		ColorRange eq_forest = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 1.1f, 3,
				-1, -1, 5, 6,
				-15, 50, 25, 2,
				new Color(0x272f16), Color.black
		);

		ColorRange very_deep_water = new GaussianTerrainRange(
				-1, getInformations().getWaterLevel(), 0, 50,
				-1, -1, 0, 5,
				-258, 100, 50, 5,
				new Color(0x152430), Color.white
		);

		ColorRange deep_water = new GaussianTerrainRange(
				-1, getInformations().getWaterLevel(), getInformations().getWaterLevel() * .5f, 50,
				-1, -1, 0, 5,
				-258, 100, 50, 5,
				new Color(0x203849), Color.white
		);

		ColorRange light_water = new GaussianTerrainRange(
				-1, getInformations().getWaterLevel(), getInformations().getWaterLevel(), 5,
				-1, -1, 0, 1.0f,
				-258, 100, 50, 5,
				new Color(0x4FAAA8), Color.white
		);

		ColorRange arctic = new GaussianTerrainRange(
				-1, -1, 0, -1f,
				-1, -1, 0, -1f,
				-258, 5, -50, 3.5f,
				new Color(0xFFFFFF), Color.white
		);

		ColorRange desert = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 1.2f, 4,
				-1, -1, 0, 2f,
				-1, 100, 50, 3.0f,
				new Color(0xe5dfad), Color.black
		);

		ColorRange desert_dune = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 1.3f, 4,
				-1, -1, 0, 2f,
				-1, 100, 50, 3.0f,
				new Color(0xe5dfbd), Color.black
		);

		ColorRange red_moutain = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 1.6f, 2,
				-1, -1, 20, 1.0f,
				-258, -258, 10, 2,
				new Color(0x78644e), Color.black
		);

		ColorRange moutain = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 2f, 4,
				-1, -1, 10, 1.5f,
				-258, -258, 5, 3,
				new Color(0x87644e), Color.black
		);

		attachTerrainRange(tundra);
		attachTerrainRange(herbe);
		attachTerrainRange(eq_forest);
		attachTerrainRange(deep_water);
		attachTerrainRange(light_water);
		attachTerrainRange(arctic);
		attachTerrainRange(very_deep_water);
		attachTerrainRange(desert);
		attachTerrainRange(desert_dune);
		attachTerrainRange(moutain);

	}

}
