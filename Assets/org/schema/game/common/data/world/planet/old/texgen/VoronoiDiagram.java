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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VoronoiDiagram implements Cloneable {

	private List<Point> voronoiPoints;
	private List<Float> coefs;
	private int width, height;
	private float m_power;
	private PointComparator m_comparator = new PointComparator();
	private Point pointTemp = new Point(0, 0);

	public VoronoiDiagram(int width, int height, float power) {
		this.width = width;
		this.height = height;
		m_power = power;
		voronoiPoints = new ArrayList<Point>();
		coefs = new ArrayList<Float>();
	}

	public void addPoint(int x, int y) {
		voronoiPoints.add(new Point(x, y));
	}

	public void addVector(float cx) {
		coefs.add(cx);
	}

	@Override
	public VoronoiDiagram clone() {
		VoronoiDiagram vd = new VoronoiDiagram(this.width, this.height, this.m_power);
		vd.coefs = new ArrayList<Float>(this.coefs);

		vd.voronoiPoints = new ArrayList<Point>(this.voronoiPoints);

		return vd;
	}

	/**
	 * We compute all array in one pass
	 *
	 * @return
	 */
	public int[] computeVoronoi() {

		int[] ret = new int[width * height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				Point p = new Point(x, y);

				m_comparator.setPoint(p);

				Collections.sort(voronoiPoints, m_comparator);

				for (int k = 0; k < coefs.size(); ++k) {
					Point vP = voronoiPoints.get(k);
					ret[(y * width) + x] += (coefs.get(k) * 255.f * vP.distanceSquared(p));
				}

				ret[(y * width) + x] = Math.max(0, Math.min(255, ret[y * width + x]));
			}
		}

		return ret;
	}

	/**
	 * Compute result at each point
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public float getValueAt(int x, int y) {
		pointTemp.x = ((float) x) / width;
		pointTemp.y = ((float) y) / height;

		m_comparator.setPoint(pointTemp);

		Collections.sort(voronoiPoints, m_comparator);

		float ret = 0;
		for (int k = 0; k < coefs.size(); ++k) {
			Point vP = voronoiPoints.get(k);
			ret += (coefs.get(k) * m_power * vP.distanceSquared(pointTemp));
		}

		return MathUtil.clamp(ret, 0, 1.0f);
	}

	private final class Point {
		float x, y;

		Point(float x, float y) {
			this.x = (x / width);
			this.y = (y / height);
		}

		/**
		 * We compute the distance to a point with texture rotation
		 */
		float distanceSquared(Point p) {
			int hw = width >> 1;
			int hh = height >> 1;

			//test rotation x
			float d1 = (x - p.x) * (x - p.x) + (y - p.y - hw) * (y - p.y - hw);
			float d2 = (x - p.x) * (x - p.x) + (y - p.y) * (y - p.y);
			float d3 = (x - p.x) * (x - p.x) + (y - p.y + hw) * (y - p.y + hw);

			//test rotation x +
			float d4 = (x - (p.x + 1.f)) * (x - (p.x + 1.f)) + (y - p.y - hw) * (y - p.y - hw);
			float d5 = (x - (p.x + 1.f)) * (x - (p.x + 1.f)) + (y - p.y) * (y - p.y);
			float d6 = (x - (p.x + 1.f)) * (x - (p.x + 1.f)) + (y - p.y + hw) * (y - p.y + hw);

			//test rotation x -
			float d7 = (x - (p.x - 1.f)) * (x - (p.x - 1.f)) + (y - p.y - hw) * (y - p.y - hw);
			float d8 = (x - (p.x - 1.f)) * (x - (p.x - 1.f)) + (y - p.y) * (y - p.y);
			float d9 = (x - (p.x - 1.f)) * (x - (p.x - 1.f)) + (y - p.y + hw) * (y - p.y + hw);

			//			//test rotation y
			//			float d10 =  (x-multiTexturePathPattern.x-hh)*(x-multiTexturePathPattern.x-hh) + 	(y-multiTexturePathPattern.y)*(y-multiTexturePathPattern.y);
			//			float d11 =  (x-multiTexturePathPattern.x)*(x-multiTexturePathPattern.x) + 			(y-multiTexturePathPattern.y)*(y-multiTexturePathPattern.y);
			//			float d12 =  (x-multiTexturePathPattern.x+hh)*(x-multiTexturePathPattern.x+hh) + 	(y-multiTexturePathPattern.y)*(y-multiTexturePathPattern.y);

			//test rotation y +
			float d13 = (x - p.x - hh) * (x - p.x - hh) + (y - p.y + 1.f) * (y - p.y + 1.f);
			float d14 = (x - p.x) * (x - p.x) + (y - p.y + 1.f) * (y - p.y + 1.f);
			float d15 = (x - p.x + hh) * (x - p.x + hh) + (y - p.y + 1.f) * (y - p.y + 1.f);

			//test rotation y -
			float d16 = (x - p.x - hh) * (x - p.x - hh) + (y - p.y - 1.f) * (y - p.y - 1.f);
			float d17 = (x - p.x) * (x - p.x) + (y - p.y - 1.f) * (y - p.y - 1.f);
			float d18 = (x - p.x + hh) * (x - p.x + hh) + (y - p.y - 1.f) * (y - p.y - 1.f);

			//
			//			float d1 =  (x-multiTexturePathPattern.x)*(x-multiTexturePathPattern.x) + (y-multiTexturePathPattern.y-hw)*(y-multiTexturePathPattern.y-hw);
			//			float d2 = (x-multiTexturePathPattern.x)*(x-multiTexturePathPattern.x) + (y-multiTexturePathPattern.y)*(y-multiTexturePathPattern.y);
			//			float d3 =  (x-multiTexturePathPattern.x)*(x-multiTexturePathPattern.x) + (y-multiTexturePathPattern.y+hw)*(y-multiTexturePathPattern.y+hw);
			//
			//			float d4 = (x-(multiTexturePathPattern.x+1.f))*(x-(multiTexturePathPattern.x+1.f)) + (y-multiTexturePathPattern.y-hw)*(y-multiTexturePathPattern.y-hw);
			//			float d5 = (x-(multiTexturePathPattern.x+1.f))*(x-(multiTexturePathPattern.x+1.f)) + (y-multiTexturePathPattern.y)*(y-multiTexturePathPattern.y);
			//			float d6 = (x-(multiTexturePathPattern.x+1.f))*(x-(multiTexturePathPattern.x+1.f)) + (y-multiTexturePathPattern.y+hw)*(y-multiTexturePathPattern.y+hw);
			//
			//			float d7 = (x-(multiTexturePathPattern.x-1.f))*(x-(multiTexturePathPattern.x-1.f)) + (y-multiTexturePathPattern.y-hw)*(y-multiTexturePathPattern.y-hw);
			//			float d8 = (x-(multiTexturePathPattern.x-1.f))*(x-(multiTexturePathPattern.x-1.f)) + (y-multiTexturePathPattern.y)*(y-multiTexturePathPattern.y);
			//			float d9 = (x-(multiTexturePathPattern.x-1.f))*(x-(multiTexturePathPattern.x-1.f)) + (y-multiTexturePathPattern.y+hw)*(y-multiTexturePathPattern.y+hw);

			return MathUtil.min(d1, d2, d3, d4, d5, d6, d7, d8, d9/*, d10, d11, d12*/, d13, d14, d15, d16, d17, d18);
		}

		@Override
		public String toString() {
			return "x= " + x + ", y = " + y;
		}
	}

	/**
	 * Classe de comparaison entre de distance entre deux points vis a vis d'un point central � comparer
	 */
	private class PointComparator implements Comparator<Point> {
		private Point toCompare;

		public PointComparator() {

		}

		//@Override
		@Override
		public int compare(Point o1, Point o2) {
			float d1 = o1.distanceSquared(toCompare);
			float d2 = o2.distanceSquared(toCompare);

			return d1 > d2 ? 1 : -1;
		}

		public void setPoint(Point p) {
			toCompare = p;
		}

	}

}
