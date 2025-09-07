package org.schema.game.server.ai.program.common.states;

import org.schema.game.server.ai.program.simpirates.states.SimulationGroupState;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class WaitingTimed extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	
	private long startTime;
	private int seconds;

	public WaitingTimed(AiEntityStateInterface gObj, int seconds) {
		super(gObj);
		this.seconds = seconds;
	}

	@Override
	public boolean onEnter() {
		this.startTime = System.currentTimeMillis();
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		if (getEntityState() instanceof SimulationGroup) {
			((SimulationGroup) getEntityState()).onWait();
		}

		if (System.currentTimeMillis() - startTime > seconds * 1000) {
			stateTransition(Transition.WAIT_COMPLETED);
		}

		return false;
	}
}