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
package org.schema.game.common.data.world.planet.old.texgen;

/**
 * Random generator based on linear congruence algorithm
 *
 * @author Yacine Petitprez
 */
public class Randomizer3D {
	private final static float INV_0X10000 = 1.f / 0x10000;
	/*
	 * Générateur à congruence linéaire de Knuth & Lewis
	 */
	private final static int a = 1664525;
	private final static int b = 1013904223;
	private int seed;

	/**
	 * Constructeur avec la graine en parametre
	 *
	 * @param seed
	 */
	public Randomizer3D(int seed) {
		this.seed = seed;
	}

	public float getPointRandom(int x, int y) {
		return getPointRandom(x, y, 0);
	}

	public float getPointRandom(int x, int y, int z) {
		/* Return rand value on [0..65535[ */
		float ret = (turn(x) * turn(seed * y) * turn((seed * seed * z))) & 0xFFFF;

		/* Return rand value on [0..1[ */
		return (ret * INV_0X10000);
	}

	/**
	 * Fonction de congruence lineaire sur x
	 *
	 * @param x La graine de la fonction de congruence.
	 * @return
	 */
	public int turn(int x) {
		return (a * x) + b;
	}

}
