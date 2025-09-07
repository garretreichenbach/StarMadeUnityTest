package org.schema.game.server.ai.program.simpirates.states;

import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.State;

public abstract class SimulationGroupState<E extends SimulationGroup> extends State {

	/**
	 *
	 */
	

	public SimulationGroupState(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@SuppressWarnings("unchecked")
	public E getSimGroup() {
		return (E) getEntityState();
	}
}
