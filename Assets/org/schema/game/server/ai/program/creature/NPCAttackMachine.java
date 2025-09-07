package org.schema.game.server.ai.program.creature;

import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.game.server.ai.program.creature.character.states.CharacterEngaging;
import org.schema.game.server.ai.program.creature.character.states.CharacterFollowing;
import org.schema.game.server.ai.program.creature.character.states.CharacterGoingToEnemy;
import org.schema.game.server.ai.program.creature.character.states.CharacterInEnemyProximity;
import org.schema.game.server.ai.program.creature.character.states.CharacterRallying;
import org.schema.game.server.ai.program.creature.character.states.CharacterRandomWaiting;
import org.schema.game.server.ai.program.creature.character.states.CharacterRoaming;
import org.schema.game.server.ai.program.creature.character.states.CharacterSearchingForTarget;
import org.schema.game.server.ai.program.creature.character.states.CharacterShooting;
import org.schema.game.server.ai.program.creature.character.states.CharacterState;
import org.schema.game.server.ai.program.creature.character.states.CharacterUnderFire;
import org.schema.game.server.ai.program.creature.character.states.CharacterWaiting;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class NPCAttackMachine extends FiniteStateMachine<String> implements AICreatureMachineInterface {

	private CharacterUnderFire characterUnderFire;
	private CharacterInEnemyProximity characterInEnemyProximity;
	private CharacterWaiting waiting;

	public NPCAttackMachine(AiEntityStateInterface obj, NPCProgram program) {
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
		CharacterRallying rallying = new CharacterRallying(gObj, this);
		CharacterGoingToEnemy goingToEnemy = new CharacterGoingToEnemy(gObj, this);
		CharacterShooting shooting = new CharacterShooting(gObj, this);

		waiting.addTransition(t_waitCompleted, randomWaiting_2_8);
		randomWaiting_2_8.addTransition(t_waitCompleted, randomWaiting_2_8);

		characterInEnemyProximity.addTransition(Transition.RALLY, rallying);
		characterInEnemyProximity.addTransition(Transition.ATTACK, engaging);

		characterUnderFire.addTransition(Transition.RALLY, rallying);
		characterUnderFire.addTransition(Transition.ATTACK, engaging);

		engaging.addTransition(Transition.TARGET_IN_RANGE, shooting);

		engaging.addTransition(Transition.TARGET_OUT_OF_RANGE, goingToEnemy);

		//		addMoveToLoop(goingToEnemy, new Move(), engaging, engaging);

		shooting.addTransition(t_shootingCompleted, engaging);

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
