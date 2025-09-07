package org.schema.game.common.data.player.tech;

public class Technology {
	public final short id;
	public final short[] dependsOn;
	public final String name;
	public final int tier;

	public final TechAbility[] abilities;

	public Technology(short id, int tier, short[] dependsOn, TechAbility[] abilities, String name) {
		super();
		this.id = id;
		this.dependsOn = dependsOn;
		this.name = name;
		this.tier = tier;
		this.abilities = abilities;
	}

}
