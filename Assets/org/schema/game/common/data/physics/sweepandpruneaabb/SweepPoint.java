package org.schema.game.common.data.physics.sweepandpruneaabb;

import javax.vecmath.Vector3f;

public class SweepPoint<E> {
	public E seg;
	public float minX;
	public float minY;
	public float minZ;

	public float maxX;
	public float maxY;
	public float maxZ;

	public int axis;
	public boolean hasXPair;
	public boolean hasYPair;
	public boolean hasZPair;
	private int hash;

	public void set(E s, Vector3f min, Vector3f max, int hash) {
		minX = min.x;
		minY = min.y;
		minZ = min.z;

		maxX = max.x;
		maxY = max.y;
		maxZ = max.z;

		this.seg = s;
		this.hash = hash;
		hasXPair = false;
		hasYPair = false;
		hasZPair = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return hash == (((SweepPoint<?>) obj).hash);
	}

	@Override
	public String toString() {
		return "X(" + minX + ", " + maxX + ")";
	}

	public String toFullString() {
		return "SweepPoint [seg=" + seg + ", (" + minX + ", " + minY
				+ ", " + minZ + "), (" + maxX + ", " + maxY
				+ ", " + maxZ + "), hash=" + hash
				+ "]";
	}

	public float getSort() {
		return minX;
	}

	public boolean equalsPos(SweepPoint<?> a) {
		return minX == a.minX && minY == a.minY && minZ == a.minZ;
	}

}
