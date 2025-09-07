package org.schema.game.client.data.gamemap.requests;

import org.schema.common.util.linAlg.Vector3i;

public class GameMapRequest {
	public Vector3i pos;
	public byte type;

	public GameMapRequest() {
	}

	public GameMapRequest(byte type, Vector3i pos) {
		this.type = type;
		this.pos = pos;
	}

	public GameMapRequest(GameMapRequest gameMapRequest) {
		this(gameMapRequest.type, new Vector3i(gameMapRequest.pos));

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
		return type == ((GameMapRequest) obj).type && pos.equals(((GameMapRequest) obj).pos);
	}

}
