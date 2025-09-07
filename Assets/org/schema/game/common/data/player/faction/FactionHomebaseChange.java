package org.schema.game.common.data.player.faction;

import org.schema.common.util.linAlg.Vector3i;

public class FactionHomebaseChange {
	public String initiator;
	public int factionId;
	public String baseUID;
	public Vector3i homeVector;
	public String realName;
	public boolean admin;

	public FactionHomebaseChange(String initiator, int factionId, String baseUID, Vector3i homeVector, String realName) {
		super();
		this.initiator = initiator;
		this.factionId = factionId;
		this.baseUID = baseUID != null ? baseUID : "";
		this.homeVector = homeVector;
		this.realName = realName;
	}

}
