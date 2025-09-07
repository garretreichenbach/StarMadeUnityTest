package org.schema.game.server.ai.program.simpirates.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class ReturningHome extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	

	public ReturningHome(AiEntityStateInterface gObj) {
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

		SimulationGroup simGroup = getSimGroup();
		System.err.println("[SIM][AI] RETURNING HOME TO " + simGroup.getStartSector());
		((TargetProgram<?>) getEntityState().getCurrentProgram()).setSectorTarget(new Vector3i(simGroup.getStartSector()));

		stateTransition(Transition.MOVE_TO_SECTOR);

		return false;
	}

}
