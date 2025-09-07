package org.schema.game.server.data.simulation.npc.resources.fsm;

import org.schema.game.server.data.simulation.npc.resources.NPCFactionFSM;
import org.schema.schine.ai.stateMachines.State;

public abstract class NPCState extends State{

	public NPCState(NPCFactionFSM gObj) {
		super(gObj);
		
	}

	@Override
	public NPCFactionFSM getEntityState() {
		return (NPCFactionFSM) super.getEntityState();
	}

}
