/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Path</H2>
 * <H3>org.schema.schine.ai.aStar</H3>
 * Path.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.ai.aStar;

import java.util.Vector;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3fTools;

// TODO: Auto-generated Javadoc

/**
 * The Class Path.
 */
public class Path {

	int lastMark = -1;
	//
	/**
	 * The segs.
	 */
	private Vector<PathSegment> segs = new Vector<PathSegment>();
	/**
	 * The path mark.
	 */
	private int pathMark = -1;

	/**
	 * Adds the all.
	 *
	 * @param path the path
	 */
	public void addAll(Path path) {
		for (PathSegment s : path.segs) {
			this.addSeg(s);
		}
	}

	/**
	 * Adds the seg.
	 *
	 * @param s the s
	 */
	public void addSeg(PathSegment s) {
		segs.add(s);
	}

	/**
	 * Clear.
	 */
	public void clear() {
		segs.clear();
	}

	/**
	 * Gets the nearest segment.
	 *
	 * @param pos the pos
	 * @return the nearest segment
	 */
	public PathSegment getNearestSegment(Vector3f posf, boolean loop, float tolDis) {
		Vector3f pos = new Vector3f(posf);

		float minDistance = Float.MAX_VALUE;

		if (pathMark == -1) {
			pathMark = size() - 1;
		}
		PathSegment nearest = getSeg(pathMark);
		//System.err.println("mark: "+getPathMark()+" - "+segs.get(getPathMark()
		// ));
		int mark = pathMark;
		int start = loop ? size() - 1 : mark;
		for (int i = start; i >= 0; i--) {
			PathSegment cur = getSeg(i);
			Vector3f disSeg = Vector3fTools.pointToSegmentDistance(pos,
					cur.endPixel, cur.startPixel);
			disSeg = Vector3fTools.sub(disSeg, pos);
			float dis = disSeg.length();

			// <= so the following pathSegements have an advantage
			if (dis <= minDistance) {

				minDistance = dis;
				pathMark = i;
				nearest = cur;

			}
			Vector3f endToStartDis = new Vector3f(cur.startPixel);
			endToStartDis.sub(pos);
			if (endToStartDis.length() < tolDis) {
				minDistance = dis;
				pathMark = i;
				nearest = cur;
				break;
			}
		}
		//		if(mark != lastMark){
		//			System.err.println("mark: "+getPathMark()+" - "+segs.get(mark));
		//		}
		lastMark = mark;
		return nearest;
	}

	/**
	 * Gets the path mark.
	 *
	 * @return the pathMark
	 */
	public int getPathMark() {
		return pathMark;
	}

	/**
	 * Sets the path mark.
	 *
	 * @param pathMark the pathMark to set
	 */
	public void setPathMark(int pathMark) {
		this.pathMark = pathMark;
	}

	/**
	 * Gets the seg.
	 *
	 * @param i the i
	 * @return the seg
	 */
	public PathSegment getSeg(int i) {
		return segs.get(i);
	}

	/**
	 * Gets the segs.
	 *
	 * @return the segs
	 */
	public Vector<PathSegment> getSegs() {
		return segs;
	}

	/**
	 * Sets the segs.
	 *
	 * @param segs the new segs
	 */
	public void setSegs(Vector<PathSegment> segs) {
		this.segs = segs;
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return size() <= 0;
	}

	/**
	 * Checks if is last.
	 *
	 * @param seg the seg
	 * @return true, if is last
	 */
	public boolean isLast(PathSegment seg) {
		return segs.firstElement() == seg;
	}

	/**
	 * Removes the.
	 *
	 * @param i the i
	 */
	public void remove(int i) {
		segs.remove(i);
	}

	/**
	 * Removes the seg.
	 *
	 * @param s the s
	 */
	public void removeSeg(PathSegment s) {
		segs.remove(s);
	}

	/**
	 * Reset path.
	 */
	public void resetPath() {
		pathMark = -1;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return segs.size();
	}

	/**
	 * Smooth.
	 *
	 * @param map the map
	 */
	public void smooth(Map map) {

		PathSegment smallest = segs.get(size() - 1);
		int sIndex = size() - 2;
		Vector<PathSegment> delSegs = new Vector<PathSegment>();
		for (int i = sIndex; i >= 0; i--) {

			PathSegment next = segs.get(i);
			Vector3f dir = Vector3fTools.sub(next.startPixel, smallest.startPixel);
			float len = dir.length();
			Vector3f pos = new Vector3f(smallest.startPixel);
			//			System.err.println("checking from " + pos + " to "
			//					+ Vector3f.add(pos, dir));
			dir.normalize();
			dir.scale(Map.FS / 4);

			boolean valid = true;
			for (int l = 0; l < len; l += Map.FS / 4) {
				pos.add(dir);
				// System.err.println("testing "+pos);
				int x = FastMath.round(pos.x / Map.FS);
				int z = FastMath.round(pos.z / Map.FS);
				if (!map.checkField(x, z) || !map.isWalkable(x, z) || i == 0 || map.getField(x, z).getEntities().size() > 0) {
					for (int t = sIndex; t > i; t--) {
						System.err.println("removing not needed segment " + segs.get(t));
						segs.remove(t);
					}
					valid = false;
					// smallest end == last valid start
					smallest.end = segs.get(i).start;
					smallest.endPixel = segs.get(i).startPixel;

					// new smallest
					smallest = segs.get(i);

					sIndex = --i; // next field. decrement i;

					//					 delSegs.add(segs.get(sIndex));
					//
					//					 segs.get(i).start = smallest.end;
					//					 segs.get(i).startPixel = smallest.endPixel;
					//					 smallest = segs.get(i+1);
					//					 sIndex = i;
					break;
				}
				// System.err.println("on field "+x+", "+
				// y+" valid "+map.isWalkable(x, y));

			}
			//System.err.println("checked "+sIndex+" to "+i+" len "+len+" valid="
			// +valid);

		}
		segs.removeAll(delSegs);
		for (PathSegment p : segs) {
			p.resetDirection();
		}
		System.err.println("Smoothing Path! now: " + segs);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String p = "Path: ";
		for (PathSegment seg : segs) {
			p += " -> " + seg;
		}
		return p;
	}

}
