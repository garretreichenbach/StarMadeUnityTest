package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.ai.program.common.states.GameState;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Transition;

public abstract class CharacterState extends GameState<AICreature<? extends AIPlayer>> {

	/**
	 *
	 */
	

	public CharacterState(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.State#init(org.schema.schine.ai.stateMachines.FiniteStateMachine)
	 */
	@Override
	public void init(FiniteStateMachine finiteStateMachine) {
		super.init(finiteStateMachine);
		init((AICreatureMachineInterface) finiteStateMachine);
	}

	protected void init(AICreatureMachineInterface mic) {
		assert (mic.getUnderFireState(this) != null);
		addTransition(Transition.ENEMY_FIRE, mic.getUnderFireState(this));
		addTransition(Transition.ENEMY_PROXIMITY, mic.getEnemyProximityState(this));
		addTransition(Transition.STOP, mic.getStopState(this));
		addTransition(Transition.RESTART, mic.getStopState(this));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.ai.program.common.states.GameState#getEntityState()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CreatureAIEntity<?, AICreature<?>> getEntityState() {
		return (CreatureAIEntity<? extends AIPlayer, AICreature<? extends AIPlayer>>) super.getEntityState();
	}

}
