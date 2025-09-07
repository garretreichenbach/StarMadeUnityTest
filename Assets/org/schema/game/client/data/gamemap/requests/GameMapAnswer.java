package org.schema.game.client.data.gamemap.requests;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.gamemap.entry.MapEntryInterface;

public class GameMapAnswer {
	public Vector3i pos;
	public byte type;

	public MapEntryInterface[] data;

	public GameMapAnswer() {
	}

	public GameMapAnswer(byte type, Vector3i pos) {
		this.type = type;
		this.pos = pos;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return type * 90000 + pos.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return type == ((GameMapAnswer) obj).type && pos.equals(((GameMapAnswer) obj).pos);
	}

}
