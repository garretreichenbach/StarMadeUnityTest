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

import org.schema.common.FastMath;

/**
 * This class add some functions like multi maxThis/minThis.
 *
 * @author Yacine Petitprez
 */
public final class MathUtil {
	private MathUtil() {
	}

	/**
	 * Clamp a value between minThis and maxThis bounds.
	 *
	 * @param val
	 * @param minThis
	 * @param maxThis
	 * @return val if val>=minThis and val<=maxThis. minThis if val<minThis and maxThis if val>maxThis
	 */
	public final static float clamp(float val, float min, float max) {
		return Math.max(min, Math.min(max, val));
	}

	/**
	 * Clamp a value between minThis and maxThis bounds.
	 *
	 * @param val
	 * @param minThis
	 * @param maxThis
	 * @return val if val>=minThis and val<=maxThis. minThis if val<minThis and maxThis if val>maxThis
	 */
	public final static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	/**
	 * Sinuso�dal interpolation between two scalar a and b
	 *
	 * @param a
	 * @param b
	 * @param alpha
	 * @return Le mix des deux valeur par la valeur alpha
	 */
	public final static float cosMix(float a, float b, float alpha) {
		alpha = (1 - FastMath.cos(alpha * FastMath.PI)) * 0.5f;

		return a * (1 - alpha) + b * alpha;
	}

	/**
	 * Linear interpolation between to scalar a and b
	 *
	 * @param a
	 * @param b
	 * @param alpha
	 * @return Le mix des deux valeur
	 */
	public final static float linMix(float a, float b, float alpha) {

		return a * (1 - alpha) + b * alpha;

	}

	public final static float max(float... fs) {
		float ret = Float.MIN_VALUE;
		for (float f : fs) {
			ret = Math.max(ret, f);
		}
		return ret;
	}

	public final static float min(float... fs) {
		float ret = Float.MAX_VALUE;
		for (float f : fs) {
			ret = Math.min(ret, f);
		}
		return ret;
	}

}
