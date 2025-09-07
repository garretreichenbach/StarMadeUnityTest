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
import java.util.LinkedList;
import java.util.List;

import org.schema.game.common.data.world.planet.old.PlanetInformations;

/**
 * Gestion d'une palette de terrain
 *
 * @author Yacine Petitprez
 */
public abstract class TerrainPalette implements Cloneable, Palette {

	private List<ColorRange> colorRanges;
	private ColorMixer mixer;
	private PlanetInformations infos;
	/**
	 * Cr�ation de la palette
	 *
	 * @param informations
	 */
	public TerrainPalette(PlanetInformations informations) {
		colorRanges = new LinkedList<ColorRange>();
		mixer = new ColorMixer();
		infos = informations;
	}

	public void attachTerrainRange(ColorRange tr) {
		colorRanges.add(tr);
	}

	public PlanetInformations getInformations() {
		return infos;
	}

	/**
	 * Use of personnal mixer. Useful for multithreading
	 *
	 * @param mixer
	 * @param x
	 * @param y
	 * @param height
	 * @param slope
	 * @param temp
	 * @return
	 */
	@Override
	public Colors getPointColor(ColorMixer mixer, int x, int y, int height, int slope, float temp) {

		for (ColorRange range : colorRanges) {
			mixer.attachColor(range.getTerrainColor(), range.getFactor(x, y, height, slope, temp));
		}

		Color terrain = mixer.getMixedColor();
		mixer.clear();

		for (ColorRange range : colorRanges) {
			mixer.attachColor(range.getTerrainSpecular(), range.getFactor(x, y, height, slope, temp));
		}

		Color specular = mixer.getMixedColor();

		mixer.clear();

		return new Colors(terrain, specular);
	}

	public Colors getPointColor(int x, int y, int height, int slope, float temp) {
		return getPointColor(mixer, x, y, height, slope, temp);
	}

	public abstract void initPalette();

	public final static class Colors {
		Color m_terrain;
		Color m_specular;

		public Colors() {
		}

		public Colors(Color terrain, Color specular) {
			this.m_terrain = terrain;
			this.m_specular = specular;
		}

		public Color getSpecular() {
			return m_specular;
		}

		public Color getTerrain() {
			return m_terrain;
		}

	}
}
