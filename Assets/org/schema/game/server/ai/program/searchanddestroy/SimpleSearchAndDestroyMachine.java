package org.schema.game.server.ai.program.searchanddestroy;

import org.schema.game.server.ai.program.common.states.ShootAtTarget;
import org.schema.game.server.ai.program.common.states.Waiting;
import org.schema.game.server.ai.program.searchanddestroy.states.EvadingTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.Rally;
import org.schema.game.server.ai.program.searchanddestroy.states.SeachingForTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipEngagingTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipGettingToTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipMovingToSector;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipShootAtTarget;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class SimpleSearchAndDestroyMachine extends FiniteStateMachine<String> {

	/**
	 *
	 */
	

	public SimpleSearchAndDestroyMachine(AiEntityStateInterface obj, SimpleSearchAndDestroyProgram program) {
		super(obj, program, "");
	}

	public void addTransition(State from, Transition t, State to) {
		from.addTransition(t, to);

	}

	@Override
	public void createFSM(String parameter) {
		Transition t_moveToSector = Transition.MOVE_TO_SECTOR;
		Transition t_searchForTarget = Transition.SEARCH_FOR_TARGET;
		Transition t_enemyFire = Transition.ENEMY_FIRE;
		Transition t_restart = Transition.RESTART;
		Transition t_healthLow = Transition.HEALTH_LOW;
		Transition t_targetAquired = Transition.TARGET_AQUIRED;
		Transition t_stop = Transition.STOP;
		Transition t_enemyProximity = Transition.ENEMY_PROXIMITY;
		Transition t_targetInRange = Transition.TARGET_IN_RANGE;
		Transition t_targetOutOfRange = Transition.TARGET_OUT_OF_RANGE;
		Transition t_targetDestroyed = Transition.TARGET_DESTROYED;
		Transition t_inShootingPosition = Transition.IN_SHOOTING_POSITION;
		Transition t_shootingCompleted = Transition.SHOOTING_COMPLETED;

		AiEntityStateInterface gObj = getObj();
		SeachingForTarget searchingForTarget = new SeachingForTarget(gObj);
		ShipMovingToSector movingToSector = new ShipMovingToSector(gObj);
		ShipEngagingTarget engagingTarget = new ShipEngagingTarget(gObj);
		EvadingTarget evadingTarget = new EvadingTarget(gObj);
		ShipGettingToTarget gettingToTarget = new ShipGettingToTarget(gObj);
		Rally rally = new Rally(gObj);
		Waiting waiting = new Waiting(gObj);
		ShootAtTarget shootAtTarget = new ShipShootAtTarget(gObj);

		waiting.addTransition(t_searchForTarget, searchingForTarget);
		waiting.addTransition(t_stop, waiting);
		waiting.addTransition(t_restart, waiting);
		waiting.addTransition(t_healthLow, rally);

		searchingForTarget.addTransition(t_targetAquired, gettingToTarget);
		searchingForTarget.addTransition(t_stop, waiting);
		searchingForTarget.addTransition(t_restart, waiting);
		searchingForTarget.addTransition(t_healthLow, rally);
		searchingForTarget.addTransition(t_moveToSector, movingToSector);

		movingToSector.addTransition(t_searchForTarget, searchingForTarget);
		movingToSector.addTransition(t_stop, waiting);
		movingToSector.addTransition(t_restart, waiting);
		movingToSector.addTransition(t_healthLow, rally);

		gettingToTarget.addTransition(t_targetInRange, engagingTarget);
		gettingToTarget.addTransition(t_enemyProximity, evadingTarget);
		gettingToTarget.addTransition(t_inShootingPosition, shootAtTarget);
		gettingToTarget.addTransition(t_stop, waiting);
		gettingToTarget.addTransition(t_restart, waiting);
		gettingToTarget.addTransition(t_healthLow, rally);

		evadingTarget.addTransition(t_stop, waiting);
		evadingTarget.addTransition(t_restart, gettingToTarget);
		evadingTarget.addTransition(t_healthLow, rally);
		evadingTarget.addTransition(t_inShootingPosition, shootAtTarget);

		engagingTarget.addTransition(t_targetDestroyed, searchingForTarget);
		engagingTarget.addTransition(t_enemyProximity, evadingTarget);
		engagingTarget.addTransition(t_inShootingPosition, shootAtTarget);
		engagingTarget.addTransition(t_targetOutOfRange, gettingToTarget);
		engagingTarget.addTransition(t_enemyFire, searchingForTarget);
		engagingTarget.addTransition(t_healthLow, rally);
		engagingTarget.addTransition(t_stop, waiting);
		engagingTarget.addTransition(t_restart, waiting);

		shootAtTarget.addTransition(t_enemyProximity, evadingTarget);
		shootAtTarget.addTransition(t_enemyFire, searchingForTarget);
		shootAtTarget.addTransition(t_shootingCompleted, gettingToTarget);
		shootAtTarget.addTransition(t_healthLow, rally);
		shootAtTarget.addTransition(t_stop, waiting);
		shootAtTarget.addTransition(t_restart, waiting);

		rally.addTransition(t_stop, waiting);
		rally.addTransition(t_restart, waiting);

		setStartingState(waiting);
	}

	@Override
	public void onMsg(Message message) {

	}

}
