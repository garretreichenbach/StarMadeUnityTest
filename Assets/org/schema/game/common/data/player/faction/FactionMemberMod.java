package org.schema.game.common.data.player.faction;

public class FactionMemberMod {
	public long failTime;
	public boolean failed;
	public long lastActiveTime;
	int id;
	String initiator = null;
	String playerState;
	boolean addOrMod = false;
	byte permissions;

	public FactionMemberMod() {
	}

	public FactionMemberMod(int id, String playerState, boolean add) {
		super();
		this.id = id;
		this.playerState = playerState;
	}

}
