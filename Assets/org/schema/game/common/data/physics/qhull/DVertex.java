package org.schema.game.common.data.physics.qhull;

/**
 * Represents vertices of the hull, as well as the points from
 * which it is formed.
 *
 * @author John E. Lloyd, Fall 2004
 */
class DVertex {
	/**
	 * Spatial point associated with this vertex.
	 */
	DPoint3d pnt;

	/**
	 * Back index into an array.
	 */
	int index;

	/**
	 * List forward link.
	 */
	DVertex prev;

	/**
	 * List backward link.
	 */
	DVertex next;

	/**
	 * Current face that this vertex is outside of.
	 */
	Face face;

	/**
	 * Constructs a vertex and sets its coordinates to 0.
	 */
	public DVertex() {
		pnt = new DPoint3d();
	}

	/**
	 * Constructs a vertex with the specified coordinates
	 * and index.
	 */
	public DVertex(double x, double y, double z, int idx) {
		pnt = new DPoint3d(x, y, z);
		index = idx;
	}

}
