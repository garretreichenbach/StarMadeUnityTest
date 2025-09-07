package org.schema.game.server.ai.program.simpirates;

import java.util.HashMap;

import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.simulation.SimPrograms;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

public class PirateSimulationProgram extends TargetProgram<SimulationGroup> implements SimulationProgramInterface {

	private String PROGRAM = "PROGRAM";

	public PirateSimulationProgram(SimulationGroup entityState, boolean startSuspended) {
		super(entityState, startSuspended);
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting) {

	}

	@Override
	protected String getStartMachine() {
		return PROGRAM;
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {
		machines.put(PROGRAM, new PirateSimulationMachine(getEntityState(), this));
	}

	@Override
	public SimPrograms getProgram() {
		return SimPrograms.SCAN_AND_ATTACK;
	}

}
