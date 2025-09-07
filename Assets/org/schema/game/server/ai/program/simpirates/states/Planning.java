package org.schema.game.server.ai.program.simpirates.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;

public class Planning extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	

	public Planning(AiEntityStateInterface gObj) {
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
		Vector3i dist = new Vector3i();

		for (PlayerState s : simGroup.getState().getPlayerStatesByName().values()) {
			dist.sub(s.getCurrentSector(), simGroup.getStartSector());
		}

		return false;
	}

}
