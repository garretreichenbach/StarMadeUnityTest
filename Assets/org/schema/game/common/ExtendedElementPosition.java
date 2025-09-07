package org.schema.game.common;

import org.schema.common.util.linAlg.Vector3i;

public class ExtendedElementPosition {
	public int x;
	public int y;
	public int z;
	public int tx;
	public int ty;
	public int tz;
	public short type;

	public ExtendedElementPosition() {
	}

	;

	public ExtendedElementPosition(Vector3i from, Vector3i pos, short type) {
		setPos(pos);
		setFrom(from);
		this.type = type;
	}

	/**
	 * Returns a hash code value based on the data values in this
	 * object.  Two different Tuple4f objects with identical data values
	 * (i.e., Tuple4f.equals returns true) will return the same hash
	 * code value.  Two objects with different data members may return the
	 * same hash value, although this is not likely.
	 *
	 * @return the integer hash code value
	 */
	@Override
	public int hashCode() {

		long bits = 1L;
		bits = 31L * bits + x;
		bits = 31L * bits + y;
		bits = 31L * bits + z;
		bits = 31L * bits + tx;
		bits = 31L * bits + ty;
		bits = 31L * bits + tz;
		bits = 31L * bits + type;
		return (int) (bits ^ (bits >> 32));
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof ExtendedElementPosition) {
			return type == ((ExtendedElementPosition) o).type
					&& x == (((ExtendedElementPosition) o).x)
					&& y == (((ExtendedElementPosition) o).y)
					&& z == (((ExtendedElementPosition) o).z)
					&& tx == (((ExtendedElementPosition) o).x)
					&& ty == (((ExtendedElementPosition) o).y)
					&& tz == (((ExtendedElementPosition) o).z);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ElementPosition [x=" + x + ", y=" + y + ", z=" + z + ", type="
				+ type + "]";
	}

	public void set(Vector3i from, Vector3i pos, short type) {
		setPos(pos);
		setFrom(from);
		this.type = type;
	}

	private void setFrom(Vector3i from) {
		this.tx = from.x;
		this.ty = from.y;
		this.tz = from.z;
	}

	public void setPos(Vector3i pos) {
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
	}

}
