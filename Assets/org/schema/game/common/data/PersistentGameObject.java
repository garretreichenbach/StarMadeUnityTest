package org.schema.game.common.data;

import org.schema.game.server.data.GameServerState;

public class PersistentGameObject {

	private String flagShipUniqueName;
	public PersistentGameObject(GameServerState state) {
	}

	/**
	 * @return the flagShipUniqueName
	 */
	public String getFlagShipUniqueName() {
		return flagShipUniqueName;
	}

	/**
	 * @param flagShipUniqueName the flagShipUniqueName to set
	 */
	public void setFlagShipUniqueName(String flagShipUniqueName) {
		this.flagShipUniqueName = flagShipUniqueName;
	}
}
