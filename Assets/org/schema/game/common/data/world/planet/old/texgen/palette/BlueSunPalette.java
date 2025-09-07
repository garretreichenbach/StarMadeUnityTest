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

public class BlueSunPalette extends TerrainPalette {

	public BlueSunPalette(PlanetInformations informations) {
		super(informations);
	}

	@Override
	public void initPalette() {

		/*TerrainRange tundra = new GaussianTerrainRange(
				getInformations().getWater_level(), -1, getInformations().getWater_level(), 4,
				-1, -1, 0, 2.5f,
				-258,80,5, 1.0f,
				new Color(0x413a28), Color.black
		);*/

		ColorRange upstairs = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel(), 4,
				-1, -1, 0, 0.8f,
				-258, 200, 130, 5.5f,
				new Color(0x0079e8), Color.white
		);

		ColorRange upupstairs = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 1.1f, 3,
				-1, -1, 5, 6,
				-15, 200, 150, 2,
				new Color(0x0078f4), Color.white
		);

		ColorRange very_deep_water = new GaussianTerrainRange(
				-1, getInformations().getWaterLevel(), 0, 50,
				-1, -1, 0, 5,
				-258, 200, 50, 5,
				new Color(0x02006d), Color.white
		);

		ColorRange deep_water = new GaussianTerrainRange(
				-1, getInformations().getWaterLevel(), getInformations().getWaterLevel() * .5f, 50,
				-1, -1, 0, 5,
				-258, 200, 50, 5,
				new Color(0x000587), Color.white
		);

		ColorRange light_water = new GaussianTerrainRange(
				-1, getInformations().getWaterLevel(), getInformations().getWaterLevel(), 5,
				-1, -1, 0, 1.0f,
				-258, 200, 100, 5,
				new Color(0x003fb5), Color.white
		);

		ColorRange arctic = new GaussianTerrainRange(
				-1, -1, 0, -1f,
				-1, -1, 0, -1f,
				-258, 5, -100, 3.0f,
				new Color(0xFFFFFF), Color.white
		);

		ColorRange desert = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 1.2f, 4,
				-1, -1, 0, 2f,
				-258, 200, 160, 3.0f,
				new Color(0x0078e6), Color.white
		);

		ColorRange desert_dune = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 1.3f, 4,
				-1, -1, 0, 2f,
				-1, 200, 180, 3.0f,
				new Color(0x0078e6), Color.white
		);

		ColorRange moutain = new GaussianTerrainRange(
				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel() * 2f, 4,
				-1, -1, 10, 1.5f,
				-258, 200, 200, 3,
				new Color(0xFFFFFF), Color.white
		);

		//attachTerrainRange(tundra);
		attachTerrainRange(upstairs);
		attachTerrainRange(upupstairs);
		attachTerrainRange(deep_water);
		attachTerrainRange(light_water);
		attachTerrainRange(arctic);
		attachTerrainRange(very_deep_water);
		attachTerrainRange(desert);
		attachTerrainRange(desert_dune);
		attachTerrainRange(moutain);
	}

}
