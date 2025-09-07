package org.schema.game.common.data.physics.octree;

public class OctreeLevel {
	public static final short START = 0;
	public static final short END = 1;
	public static final short DIM = 2;
	public static final short HALF = 3;

	byte level;
	int index;
	int id;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return level * 8 + index + id * 100000;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return level == ((OctreeLevel) obj).level && id == ((OctreeLevel) obj).id && index == ((OctreeLevel) obj).index;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OctreeLevel [level=" + level + ", index=" + index + ", id="
				+ id + "]";
	}

}
