package org.schema.game.common.data.player.faction;

public class FactionKick {
	public final String initiator;
	public final int faction;
	public final String player;

	public FactionKick(String initiator, int faction, String player) {
		super();
		this.initiator = initiator;
		this.faction = faction;
		this.player = player;
	}

}
