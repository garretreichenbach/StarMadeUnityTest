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

import java.util.Random;

import org.schema.common.system.thread.ForRunnable;
import org.schema.common.system.thread.MultiThreadUtil;
import org.schema.game.common.data.world.planet.old.PlanetInformations;
import org.schema.game.common.data.world.planet.old.texgen.PerlinNoise;
import org.schema.game.common.data.world.planet.old.texgen.VoronoiDiagram;

/**
 * Provide Perlin noise modulated by Vorono� diagram for make Continental-like heighmap.
 *
 * @author Yacine Petitprez
 */
public class StandartSpaceGenerator extends SpaceGenerator {

	private final static float[] m_voronoi_vectors = new float[]{
			0.5f,
			-3,
			1.1680948f,
			//		-0.2082833f,
			//		0.40441322f,
			//		-0.878508f,
			//		0.7664678f
	};
	private Random m_random;

	public StandartSpaceGenerator(int texture_width, int texture_height,
	                              PlanetInformations info, SpacePalette color_palette) {
		super(texture_width, texture_height, info, color_palette);

		/* Cr�ation de l'objet hazard */
		m_random = new Random(info.getSeed());
	}

	@Override
	protected int[] generateHeightmap() {

		final int[] heightmap = new int[getWidth() * getHeight()];

		/* Preparation du bruit de perlin */
		final PerlinNoise perlin = new PerlinNoise(6, getWidth(), getHeight(), getInformations().getSeed());

		final VoronoiDiagram diagram = new VoronoiDiagram(getWidth(), getHeight(), 0.1f);

		for (int i = 0; i < 9; i++) {
			int x = m_random.nextInt(getWidth());
			int y = m_random.nextInt(getHeight());

			diagram.addPoint(x, y);
		}

		for (float vector : m_voronoi_vectors) {
			diagram.addVector(vector);
		}

		/* Permet de determiner o� s'arrette l'eau */
		int[] tab = new int[256];
		float seuil = heightmap.length * getInformations().getWaterInPercent();

		//double angle = Math.PI/2 - 0.001;

		//System.out.println( 0.5 * Math.log((1 + Math.sin(angle) )/(1 - Math.sin(angle))));

		//Utilisation de la methode multithread:
		if (MultiThreadUtil.PROCESSORS_COUNT > 1) {

			int loopCount = getWidth() * getHeight();

			int shift = 0;
			int width = getWidth();

			while (width != 1) {
				shift++;
				width >>= 1;
			}

			MultiThreadUtil.multiFor(0, loopCount,
					new ForHeightMap(perlin, shift, diagram, heightmap)
			);

			for (int i = 0; i < loopCount; i++) {
				tab[heightmap[i]]++;
			}

		} else {
			for (int x = 0; x < getWidth(); ++x) {
				for (int y = 0; y < getHeight(); ++y) {
					int result = (int) (perlin.getPerlinPointAt(x, y) * 255 * (1.0f - 0.8f * diagram.getValueAt(x, y)));
					//result += m_random.nextInt(2); //Ajout d'un pixel 1 � la valeur du pixel 1 fois sur 2
					tab[result]++;
					heightmap[y * getWidth() + x] = result;

				}
			}
		}

		int sum = 0;

		int x = 0;
		while (sum < seuil) {
			sum += tab[x++];
		}

		/* On lie le pixel seuillant l'eau au pourcentage de terre immerg�es */
		getInformations().setWaterLevel(x);

		m_heightMap = heightmap;

		//		for(int i = 0; i < (getHeight()/5) ; i ++) {
		//			addSource(m_random.nextInt(getWidth()), m_random.nextInt(getHeight()), null, 128 );
		//		}

		return heightmap;
	}

	/**
	 * Class used for multi-threading heightmap generation task
	 */
	private final class ForHeightMap implements ForRunnable {

		private PerlinNoise perlin;
		private int shift;
		private VoronoiDiagram diagram;
		private int[] heightmap;

		private ForHeightMap() {
		}

		public ForHeightMap(PerlinNoise perlin, int shift,
		                    VoronoiDiagram diagram, int[] heightmap) {
			this.perlin = perlin;
			this.shift = shift;
			this.diagram = diagram.clone();
			this.heightmap = heightmap;
		}

		//@Override
		@Override
		public ForRunnable copy() {
			ForHeightMap copie = new ForHeightMap();

			copie.perlin = perlin;
			copie.shift = shift;
			copie.diagram = diagram.clone();
			copie.heightmap = heightmap;

			return copie;
		}

		@Override
		public void run(int index) {

			int x = index & m_widthm1;
			int y = index >> shift;

			int result = (int) (perlin.getPerlinPointAt(x, y) * 255 * (1.0f - 0.8f * diagram.getValueAt(x, y)));

			heightmap[index] = result;
		}

	}

}
