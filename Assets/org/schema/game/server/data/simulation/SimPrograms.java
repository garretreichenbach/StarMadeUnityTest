package org.schema.game.server.data.simulation;

import org.schema.game.server.ai.program.simpirates.PirateSimulationProgram;
import org.schema.game.server.ai.program.simpirates.SimulationProgramInterface;
import org.schema.game.server.ai.program.simpirates.TradingRouteSimulationProgram;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.MachineProgram;

public enum SimPrograms {
	VISIT_SECTOR((SimulationProgramFactory<TradingRouteSimulationProgram>) TradingRouteSimulationProgram::new),
	SCAN_AND_ATTACK((SimulationProgramFactory<PirateSimulationProgram>) PirateSimulationProgram::new),;

	private SimulationProgramFactory<?> factory;

	SimPrograms(SimulationProgramFactory<?> factory) {
		this.factory = factory;
	}

	public static SimPrograms getFromClass(SimulationGroup group) throws NoSimstateFountException {

		MachineProgram<?> machineProgram = group.getCurrentProgram();

		if (machineProgram instanceof SimulationProgramInterface) {
			return ((SimulationProgramInterface) machineProgram).getProgram();
		}

		throw new NoSimstateFountException("Could not find simProgram for " + machineProgram);
	}

	public static MachineProgram<SimulationGroup> getProgram(SimPrograms p, SimulationGroup entityState, boolean startSuspended) {
		return p.factory.getInstance(entityState, startSuspended);
	}
}
