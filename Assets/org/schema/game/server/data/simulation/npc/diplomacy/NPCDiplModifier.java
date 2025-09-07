package org.schema.game.server.data.simulation.npc.diplomacy;

public abstract class NPCDiplModifier {
	
	public abstract String getName();
	public abstract boolean isStatic();
	public abstract int getValue();
	public int getFrequency() {
		return 0;
	}
	
}
