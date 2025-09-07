package org.schema.game.server.ai.program.simpirates.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CheckingForPlayers extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	
	private long startTime;

	public CheckingForPlayers(AiEntityStateInterface gObj) {
		super(gObj);
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

		SimulationGroup simGroup = getSimGroup();
		Vector3i dist = new Vector3i();
		for (PlayerState s : simGroup.getState().getPlayerStatesByName().values()) {
			dist.sub(s.getCurrentSector(), simGroup.getStartSector());
			if (dist.length() < 6) {
				((TargetProgram<?>) getEntityState().getCurrentProgram()).setSectorTarget(new Vector3i(s.getCurrentSector()));

				simGroup.sendInvestigationMessage(s);

				stateTransition(Transition.MOVE_TO_SECTOR);
				return true;
			}
		}

		if (System.currentTimeMillis() - startTime > 60000 * 1) {
			stateTransition(Transition.DISBAND);
			return true;
		}

		return false;
	}

}
