package org.schema.game.server.ai.program.simpirates.states;

import java.sql.SQLException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class GoToRandomSector extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	

	public GoToRandomSector(AiEntityStateInterface gObj) {
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
		if (simGroup.getMembers().size() > 0) {
			try {
				Vector3i sector = simGroup.getSector(simGroup.getMembers().get(0), new Vector3i());

				sector.add(Universe.getRandom().nextInt(20) - 10, Universe.getRandom().nextInt(20) - 10, Universe.getRandom().nextInt(20) - 10);

				((TargetProgram<?>) getEntityState().getCurrentProgram()).setSectorTarget(sector);
			} catch (EntityNotFountException e) {
				e.printStackTrace();
				stateTransition(Transition.DISBAND);
			} catch (SQLException e) {
				e.printStackTrace();
				stateTransition(Transition.DISBAND);
			}
			stateTransition(Transition.MOVE_TO_SECTOR);

		} else {
			System.err.println("[SIM][AI] RETURNING HOME TO (NO MEMBERS) " + simGroup.getStartSector());
			((TargetProgram<?>) getEntityState().getCurrentProgram()).setSectorTarget(new Vector3i(simGroup.getStartSector()));

			stateTransition(Transition.MOVE_TO_SECTOR);
		}

		return false;
	}

}
