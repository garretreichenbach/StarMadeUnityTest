package org.schema.game.server.data.simulation.npc.resources.fsm.states;

import org.schema.game.server.data.simulation.npc.resources.NPCFactionFSM;
import org.schema.game.server.data.simulation.npc.resources.fsm.NPCState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class NPCSAbort extends NPCState{

	public NPCSAbort(NPCFactionFSM gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {
		return true;
	}

	@Override
	public boolean onExit() {
		return true;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		stateTransition(Transition.NPCS_ABORT);
		return true;
	}

}
