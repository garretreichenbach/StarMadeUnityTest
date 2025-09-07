package org.schema.game.server.ai.program.creature;

import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.game.server.ai.program.creature.character.states.CharacterEngaging;
import org.schema.game.server.ai.program.creature.character.states.CharacterFollowing;
import org.schema.game.server.ai.program.creature.character.states.CharacterGoingToEnemy;
import org.schema.game.server.ai.program.creature.character.states.CharacterInEnemyProximity;
import org.schema.game.server.ai.program.creature.character.states.CharacterMovingToPosition;
import org.schema.game.server.ai.program.creature.character.states.CharacterOnAttackingOrder;
import org.schema.game.server.ai.program.creature.character.states.CharacterRallying;
import org.schema.game.server.ai.program.creature.character.states.CharacterRandomWaiting;
import org.schema.game.server.ai.program.creature.character.states.CharacterRoaming;
import org.schema.game.server.ai.program.creature.character.states.CharacterSearchingForTarget;
import org.schema.game.server.ai.program.creature.character.states.CharacterShooting;
import org.schema.game.server.ai.program.creature.character.states.CharacterState;
import org.schema.game.server.ai.program.creature.character.states.CharacterUnderFire;
import org.schema.game.server.ai.program.creature.character.states.CharacterWaiting;
import org.schema.game.server.ai.program.creature.character.states.CharacterWaitingForInput;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class NPCMachine extends FiniteStateMachine<String> implements AICreatureMachineInterface {

	/**
	 *
	 */
	
	private CharacterUnderFire characterUnderFire;
	private CharacterInEnemyProximity characterInEnemyProximity;
	private CharacterWaiting waiting;

	public NPCMachine(AiEntityStateInterface obj, NPCProgram program) {
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
		Transition t_roam = Transition.ROAM;
		Transition t_follow = Transition.FOLLOW;
		Transition t_attack = Transition.ATTACK;;

		CharacterRandomWaiting randomWaiting = new CharacterRandomWaiting(gObj, 100, 500, this);
		CharacterRandomWaiting randomWaitingAfterRoam = new CharacterRandomWaiting(gObj, 2000, 12000, this);
		CharacterSearchingForTarget searchingForTarget = new CharacterSearchingForTarget(gObj, this);
		CharacterMovingToPosition moving = new CharacterMovingToPosition(gObj, this);
		CharacterOnAttackingOrder attacking = new CharacterOnAttackingOrder(gObj, this);
		CharacterRoaming roaming = new CharacterRoaming(gObj, this);
		CharacterFollowing following = new CharacterFollowing(gObj, this);
		CharacterEngaging engaging = new CharacterEngaging(gObj, this);
		CharacterRallying rallying = new CharacterRallying(gObj, this);
		CharacterGoingToEnemy goingToEnemy = new CharacterGoingToEnemy(gObj, this);
		CharacterShooting shooting = new CharacterShooting(gObj, this);
		CharacterWaitingForInput waitingForInput = new CharacterWaitingForInput(gObj, this);

		waiting.addTransition(t_waitCompleted, randomWaiting);

		randomWaiting.addTransition(t_waitCompleted, waitingForInput);

		waitingForInput.addTransition(t_roam, roaming);
		waitingForInput.addTransition(t_follow, following);
		waitingForInput.addTransition(t_move, moving);
		waitingForInput.addTransition(t_attack, attacking);

		//		waitingForInput.addTransition(t_patrol, patrolling);
		//		waitingForInput.addTransition(t_roam, roaming);
		//		waitingForInput.addTransition(t_roam, roaming);

		roaming.addTransition(t_restart, randomWaitingAfterRoam);
		following.addTransition(t_restart, waiting);
		moving.addTransition(t_restart, waiting);

		randomWaitingAfterRoam.addTransition(t_waitCompleted, randomWaiting);
		//		addMoveToLoop(roaming, t_move, randomWaiting_2_8);

		characterUnderFire.addTransition(Transition.RALLY, waiting);
		characterUnderFire.addTransition(Transition.ATTACK, waiting);

		characterInEnemyProximity.addTransition(Transition.RALLY, waiting);
		characterInEnemyProximity.addTransition(Transition.ATTACK, waiting);

		setStartingState(waiting);
	}

	@Override
	public void onMsg(Message message) {
		message.execute(this);
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
