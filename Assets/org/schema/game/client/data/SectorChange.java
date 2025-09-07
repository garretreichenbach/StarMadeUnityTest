package org.schema.game.client.data;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;

public class SectorChange {
	public final int from;
	public final int to;
	public final SimpleTransformableSendableObject what;

	public SectorChange(SimpleTransformableSendableObject what, int from, int to) {
		super();
		this.from = from;
		this.to = to;
		this.what = what;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (what).hashCode() + (from) + (to) * 10000;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return ((SectorChange) o).what == (what) && ((SectorChange) o).from == (from) && ((SectorChange) o).to == (to);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SectorChange[" + from + " -> " + to + "]";
	}

}
