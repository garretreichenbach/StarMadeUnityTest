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
package org.schema.game.common.data.world.space.textgen;

import java.awt.Color;

import org.schema.game.common.data.world.planet.old.PlanetInformations;
import org.schema.game.common.data.world.planet.old.texgen.palette.ColorRange;
import org.schema.game.common.data.world.planet.old.texgen.palette.GaussianTerrainRange;

public class StandardSpacePalette extends SpacePalette {

	public StandardSpacePalette(PlanetInformations informations) {
		super(informations);
	}

	@Override
	public void initPalette() {
		//			minThis			maxThis				medium			variance
		//		int m_minh, int m_maxh, float m_mediumh, float m_varianceh, //height
		//		int m_mins, int m_maxs, float m_mediums, float m_variances, //slope
		//		int m_mint, int m_maxt, float m_mediumt, float m_variancet, //temp
		//		Color m_color_terrain, Color m_color_specular

		ColorRange upstairs = new GaussianTerrainRange(
				(getInformations().getWaterLevel()), -1, getInformations().getWaterLevel(), 20,
				-1, -1, 2, 0.3f,
				-258, 100, 50, 5.5f,
				new Color(0xBABABA), Color.black
		);

		ColorRange upupstairs = new GaussianTerrainRange(
				(int) (getInformations().getWaterLevel() * 1.1f), -1, getInformations().getWaterLevel() * 1.1f, 20,
				-1, -1, 5, 6,
				-258, 100, 5, 5,
				new Color(0xCACACA), Color.black
		);
		ColorRange moutain = new GaussianTerrainRange(
				(int) (getInformations().getWaterLevel() * 1.2f), -1, getInformations().getWaterLevel() * 2f, 3,
				-1, -1, 10, 1.5f,
				-258, 100, 5, 3,
				new Color(0xFFFFFF), Color.black
		);
		ColorRange desert = new GaussianTerrainRange(
				(int) (getInformations().getWaterLevel() * 1.2f), -1, getInformations().getWaterLevel() * 1.2f, 3,
				-1, -1, 0, 2f,
				-258, 100, 10, 3.0f,
				new Color(0xDADADA), Color.black
		);
		ColorRange light_water = new GaussianTerrainRange(
				-1, (int) (getInformations().getWaterLevel() * 1.7f), getInformations().getWaterLevel(), 30,
				-1, -1, 0, 1.0f,
				-258, 100, 50, 5,
				new Color(0x4FAAA8), Color.white
		);
		ColorRange deep_water = new GaussianTerrainRange(
				-1, (int) (getInformations().getWaterLevel() * 1.5f), getInformations().getWaterLevel() * .5f, 50,
				-1, -1, 0, 5,
				-258, 100, 50, 5,
				Color.black, Color.white
		);
		ColorRange very_deep_water = new GaussianTerrainRange(
				-1, (int) (getInformations().getWaterLevel() * 1.7f), 0, 50,
				-1, -1, 0, 5,
				-258, 100, 50, 5,
				Color.black, Color.white
		);

		//		ColorRange arctic = new GaussianTerrainRange(
		//				-1, -1, 0, -1f,
		//				-1, -1, 0, -1f,
		//				-258, 5, -100, 3.0f,
		//				new Color(0xFFFFFF), Color.white
		//		);
		//
		//		ColorRange desert = new GaussianTerrainRange(
		//				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel()*1.2f, 4,
		//				-1, -1, 0, 2f,
		//				-258, 100, 0, 3.0f,
		//				Color.yellow, Color.black
		//		);
		//
		//		ColorRange desert_dune = new GaussianTerrainRange(
		//				getInformations().getWaterLevel(), -1, getInformations().getWaterLevel()*1.3f, 4,
		//				-1, -1, 0, 2f,
		//				-1, 100, 50, 3.0f,
		//				Color.green, Color.black
		//		);
		//

		//attachTerrainRange(tundra);
		attachTerrainRange(upstairs);
		attachTerrainRange(upupstairs);
		attachTerrainRange(deep_water);
		attachTerrainRange(light_water);

		attachTerrainRange(very_deep_water);

		//		attachTerrainRange(arctic);
		attachTerrainRange(desert);
		//		attachTerrainRange(desert_dune);
		attachTerrainRange(moutain);

	}

}
