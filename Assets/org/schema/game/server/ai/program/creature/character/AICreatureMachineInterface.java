package org.schema.game.server.ai.program.creature.character;

import org.schema.game.server.ai.program.creature.character.states.CharacterState;
import org.schema.schine.ai.stateMachines.State;

public interface AICreatureMachineInterface {

	public State getUnderFireState(CharacterState characterState);

	public State getEnemyProximityState(CharacterState characterState);

	public State getStopState(CharacterState characterState);

}
