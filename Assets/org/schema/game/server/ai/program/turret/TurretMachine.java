package org.schema.game.server.ai.program.turret;

import org.schema.game.server.ai.program.common.states.ShootAtTarget;
import org.schema.game.server.ai.program.common.states.Waiting;
import org.schema.game.server.ai.program.turret.states.EngagingTurretTarget;
import org.schema.game.server.ai.program.turret.states.SeachingForTurretTarget;
import org.schema.game.server.ai.program.turret.states.TurretShootAtTarget;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TurretMachine extends FiniteStateMachine<String> {

	/**
	 *
	 */
	

	public TurretMachine(AiEntityStateInterface obj, MachineProgram<?> program) {
		super(obj, program, "");
	}

	public void addTransition(State from, Transition t, State to) {
		from.addTransition(t, to);

	}

	@Override
	public void createFSM(String parameter) {
		Transition t_searchForTarget = Transition.SEARCH_FOR_TARGET;
		Transition t_enemyFire = Transition.ENEMY_FIRE;
		Transition t_restart = Transition.RESTART;
		Transition t_targetAquired = Transition.TARGET_AQUIRED;
		Transition t_stop = Transition.STOP;
		Transition t_targetOutOfRange = Transition.TARGET_OUT_OF_RANGE;
		Transition t_targetDestroyed = Transition.TARGET_DESTROYED;
		Transition t_inShootingPosition = Transition.IN_SHOOTING_POSITION;
		Transition t_shootingCompleted = Transition.SHOOTING_COMPLETED;;

		AiEntityStateInterface gObj = getObj();
		SeachingForTurretTarget searchingForTarget = new SeachingForTurretTarget(gObj);
		EngagingTurretTarget engagingTarget = new EngagingTurretTarget(gObj);
		Waiting waiting = new Waiting(gObj);
		ShootAtTarget shootAtTarget = new TurretShootAtTarget(gObj);

		waiting.addTransition(t_searchForTarget, searchingForTarget);
		waiting.addTransition(t_stop, waiting);
		waiting.addTransition(t_restart, waiting);

		searchingForTarget.addTransition(t_targetAquired, engagingTarget);
		searchingForTarget.addTransition(t_stop, waiting);
		searchingForTarget.addTransition(t_restart, waiting);

		engagingTarget.addTransition(t_targetDestroyed, searchingForTarget);
		engagingTarget.addTransition(t_inShootingPosition, shootAtTarget);
		engagingTarget.addTransition(t_targetOutOfRange, searchingForTarget);
		engagingTarget.addTransition(t_enemyFire, searchingForTarget);
		engagingTarget.addTransition(t_stop, waiting);
		engagingTarget.addTransition(t_restart, waiting);

		shootAtTarget.addTransition(t_enemyFire, searchingForTarget);
		shootAtTarget.addTransition(t_shootingCompleted, engagingTarget);
		shootAtTarget.addTransition(t_stop, waiting);
		shootAtTarget.addTransition(t_restart, waiting);

		setStartingState(waiting);
	}

	@Override
	public void onMsg(Message message) {

	}

}
