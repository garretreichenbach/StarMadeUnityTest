package org.schema.game.server.ai.program.simpirates.states;

import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class Starting extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	

	public Starting(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		stateTransition(Transition.PLAN);
		return false;
	}

}
