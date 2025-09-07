package org.schema.game.server.data.simulation.npc.geo;

public interface LvlIteratorCallback {
	public void handleFree(int x, int y, int z, short lvl);
	public void handleTaken( int x, int y, int z, short lvl);
}
