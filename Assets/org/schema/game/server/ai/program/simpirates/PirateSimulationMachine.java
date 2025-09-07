package org.schema.game.server.ai.program.simpirates;

import org.schema.game.server.ai.program.common.states.WaitingTimed;
import org.schema.game.server.ai.program.simpirates.states.CheckingForPlayers;
import org.schema.game.server.ai.program.simpirates.states.Disbanding;
import org.schema.game.server.ai.program.simpirates.states.GoToRandomSector;
import org.schema.game.server.ai.program.simpirates.states.MovingToSector;
import org.schema.game.server.ai.program.simpirates.states.ReturningHome;
import org.schema.game.server.ai.program.simpirates.states.Starting;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class PirateSimulationMachine extends FiniteStateMachine<String> {

	/**
	 *
	 */
	

	public PirateSimulationMachine(AiEntityStateInterface obj, PirateSimulationProgram program) {
		super(obj, program, "");
	}

	public void addTransition(State from, Transition t, State to) {
		from.addTransition(t, to);

	}

	@Override
	public void createFSM(String parameter) {

		Transition t_moveToSector = Transition.MOVE_TO_SECTOR;
		Transition t_restart = Transition.RESTART;
		Transition t_plan = Transition.PLAN;
		Transition t_disband = Transition.DISBAND;
		Transition t_waitCompleted = Transition.WAIT_COMPLETED;
		Transition t_targetSectorReached = Transition.TARGET_SECTOR_REACHED;

		AiEntityStateInterface gObj = getObj();

		Starting starting = new Starting(gObj);
		MovingToSector movingToSectorPlayer = new MovingToSector(gObj);
		MovingToSector movingToSectorHome = new MovingToSector(gObj);
		Disbanding disbanding = new Disbanding(gObj);
		CheckingForPlayers checkingForPlayers = new CheckingForPlayers(gObj);
		ReturningHome returningHome = new ReturningHome(gObj);
		GoToRandomSector goToRandomSector = new GoToRandomSector(gObj);
		WaitingTimed waitingInTargetSector = new WaitingTimed(gObj, 60);

		starting.addTransition(t_restart, starting);
		starting.addTransition(t_plan, checkingForPlayers);

		checkingForPlayers.addTransition(t_restart, starting);
		checkingForPlayers.addTransition(t_disband, disbanding);
		checkingForPlayers.addTransition(t_moveToSector, movingToSectorPlayer);

		movingToSectorPlayer.addTransition(t_restart, starting);
		movingToSectorPlayer.addTransition(t_targetSectorReached, waitingInTargetSector);

		waitingInTargetSector.addTransition(t_restart, starting);
		waitingInTargetSector.addTransition(t_waitCompleted, returningHome);

		returningHome.addTransition(t_restart, starting);
		returningHome.addTransition(t_moveToSector, movingToSectorHome);

		movingToSectorHome.addTransition(t_restart, starting);
		movingToSectorHome.addTransition(t_targetSectorReached, disbanding);

		disbanding.addTransition(t_restart, starting);
		disbanding.addTransition(t_waitCompleted, goToRandomSector);

		goToRandomSector.addTransition(t_restart, starting);
		goToRandomSector.addTransition(t_moveToSector, movingToSectorHome);
		goToRandomSector.addTransition(t_disband, disbanding);

		setStartingState(starting);
	}

	@Override
	public void onMsg(Message message) {

	}

}
