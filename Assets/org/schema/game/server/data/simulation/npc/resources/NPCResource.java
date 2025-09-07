package org.schema.game.server.data.simulation.npc.resources;

import org.schema.game.server.data.simulation.npc.NPCFaction;

public interface NPCResource {
	public int getId();
	public String getName();
	public int getAvailable(NPCFaction f);
	public void aquesitionProcedure(NPCFaction f, int needed);
}
