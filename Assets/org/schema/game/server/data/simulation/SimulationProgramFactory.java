package org.schema.game.server.data.simulation;

import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.MachineProgram;

public interface SimulationProgramFactory<E extends MachineProgram<SimulationGroup>> {

	public E getInstance(SimulationGroup g, boolean startSuspended);
}
