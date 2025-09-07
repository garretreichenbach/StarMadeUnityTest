package org.schema.game.server.ai.program.creature;

import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.game.server.ai.program.creature.character.states.CharacterEngaging;
import org.schema.game.server.ai.program.creature.character.states.CharacterEngagingWithPath;
import org.schema.game.server.ai.program.creature.character.states.CharacterEngagingWithPathMove;
import org.schema.game.server.ai.program.creature.character.states.CharacterFollowing;
import org.schema.game.server.ai.program.creature.character.states.CharacterGoingToEnemy;
import org.schema.game.server.ai.program.creature.character.states.CharacterHandlingPath;
import org.schema.game.server.ai.program.creature.character.states.CharacterInEnemyProximity;
import org.schema.game.server.ai.program.creature.character.states.CharacterPathMovingToPosition;
import org.schema.game.server.ai.program.creature.character.states.CharacterRallying;
import org.schema.game.server.ai.program.creature.character.states.CharacterRandomWaiting;
import org.schema.game.server.ai.program.creature.character.states.CharacterRoaming;
import org.schema.game.server.ai.program.creature.character.states.CharacterSearchingForTarget;
import org.schema.game.server.ai.program.creature.character.states.CharacterShooting;
import org.schema.game.server.ai.program.creature.character.states.CharacterState;
import org.schema.game.server.ai.program.creature.character.states.CharacterUnderFire;
import org.schema.game.server.ai.program.creature.character.states.CharacterWaiting;
import org.schema.game.server.ai.program.creature.character.states.CharacterWaitingForPath;
import org.schema.game.server.ai.program.creature.character.states.CharacterWaitingForPathPlot;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class NPCMoveMachine extends FiniteStateMachine<String> implements AICreatureMachineInterface {

	/**
	 *
	 */
	
	private CharacterUnderFire characterUnderFire;
	private CharacterInEnemyProximity characterInEnemyProximity;
	private CharacterWaiting waiting;

	public NPCMoveMachine(AiEntityStateInterface obj, NPCProgram program) {
		super(obj, program, "");
	}

	public void addTransition(State from, Transition t, State to) {
		from.addTransition(t, to);

	}

	@Override
	public void createFSM(String parameter) {
		AiEntityStateInterface gObj = getObj();

		characterUnderFire = new CharacterUnderFire(getObj(), this);
		characterInEnemyProximity = new CharacterInEnemyProximity(getObj(), this);
		waiting = new CharacterWaiting(gObj, this);

		Transition t_move = Transition.MOVE;
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
		Transition t_waitCompleted = Transition.WAIT_COMPLETED;

		CharacterRandomWaiting randomWaiting_1_5 = new CharacterRandomWaiting(gObj, 1000, 5000, this);
		CharacterRandomWaiting randomWaiting_2_8 = new CharacterRandomWaiting(gObj, 2000, 8000, this);
		CharacterRandomWaiting randomWaiting_5_15 = new CharacterRandomWaiting(gObj, 5000, 15000, this);
		CharacterSearchingForTarget searchingForTarget = new CharacterSearchingForTarget(gObj, this);
		CharacterRoaming roaming = new CharacterRoaming(gObj, this);
		CharacterFollowing following = new CharacterFollowing(gObj, this);
		CharacterEngaging engaging = new CharacterEngaging(gObj, this);
		CharacterEngagingWithPath engagingPath = new CharacterEngagingWithPath(gObj, this);
		CharacterRallying rallying = new CharacterRallying(gObj, this);
		CharacterGoingToEnemy goingToEnemy = new CharacterGoingToEnemy(gObj, this);
		CharacterShooting shooting = new CharacterShooting(gObj, this);
		CharacterEngagingWithPathMove engagingWithPathMove = new CharacterEngagingWithPathMove(gObj, this);
		CharacterWaitingForPathPlot waitingForPathPlot = new CharacterWaitingForPathPlot(gObj, this);

		waiting.addTransition(t_waitCompleted, waitingForPathPlot);

		addMoveToLoop(waitingForPathPlot, t_move, waitingForPathPlot, waiting);

		setStartingState(waiting);
	}

	@Override
	public void onMsg(Message message) {
		message.execute(this);
	}

	private void addMoveToLoop(State start, Transition trigger, State resultState, State failedState) {
		Transition t_move = Transition.MOVE;
		Transition t_targetInRange = Transition.TARGET_IN_RANGE;

		AiEntityStateInterface gObj = getObj();
		CharacterWaitingForPath waitingForPath = new CharacterWaitingForPath(gObj, this);
		CharacterHandlingPath pathHandling = new CharacterHandlingPath(gObj, this);
		CharacterPathMovingToPosition pathMoving = new CharacterPathMovingToPosition(gObj, this);

		start.addTransition(trigger, waitingForPath);
		waitingForPath.addTransition(t_move, pathHandling);
		waitingForPath.addTransition(Transition.PATH_FAILED, failedState);
		start.addTransition(Transition.PATH_FAILED, failedState);
		pathHandling.addTransition(t_move, pathMoving);
		pathMoving.addTransition(t_targetInRange, pathHandling);

		pathHandling.addTransition(Transition.PATH_FINISHED, resultState);

		start.addTransition(Transition.RESTART, resultState);
		waitingForPath.addTransition(Transition.RESTART, resultState);
		pathHandling.addTransition(Transition.RESTART, resultState);
		pathMoving.addTransition(Transition.RESTART, resultState);
		pathHandling.addTransition(Transition.RESTART, resultState);

		assert (waitingForPath.containsTransition(Transition.PATH_FAILED));
	}

	@Override
	public State getUnderFireState(CharacterState characterState) {
		return characterUnderFire;
	}

	@Override
	public State getEnemyProximityState(CharacterState characterState) {
		return characterInEnemyProximity;
	}

	@Override
	public State getStopState(CharacterState characterState) {
		return waiting;
	}
}
