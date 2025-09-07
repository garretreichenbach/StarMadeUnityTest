package org.schema.game.server.ai.program.simpirates.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.game.server.data.simulation.groups.TargetSectorSimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class MakingMoveDesicion extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	
	private TargetSectorSimulationGroup targetGroup;

	public MakingMoveDesicion(AiEntityStateInterface gObj, TargetSectorSimulationGroup targetSectorSimulationGroup) {
		super(gObj);
		this.targetGroup = targetSectorSimulationGroup;
		assert (this.targetGroup != null);
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
		assert (this.targetGroup.targetSector != null);
		((TargetProgram<?>) getEntityState().getCurrentProgram()).setSectorTarget(new Vector3i(this.targetGroup.targetSector));
		stateTransition(Transition.MOVE_TO_SECTOR);
		return false;
	}

}
