package org.schema.game.server.ai.program.simpirates.states;

import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class Disbanding extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	
	private long started;

	public Disbanding(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {
		started = System.currentTimeMillis();
		return false;
	}

	@Override
	public boolean onExit() {
				return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		boolean allUnloaded = true;
		String notUnloaded = "";
		for (int i = 0; i < getSimGroup().getMembers().size(); i++) {
			if (getSimGroup().isLoaded(i)) {
				allUnloaded = false;
				notUnloaded += getSimGroup().getMembers().get(i) + "; ";
			}
		}

		if (allUnloaded) {
			getSimGroup().getState().getSimulationManager().disband(getSimGroup());
			//end state machine
			getEntityState().setCurrentProgram(null);
		} else {
			System.err.println("[AI] DISBANDING: Waiting for all to unload. Still loaded: " + notUnloaded);
			if (System.currentTimeMillis() - started > 240000) {
				stateTransition(Transition.WAIT_COMPLETED);
			}
		}

		return false;
	}

}
