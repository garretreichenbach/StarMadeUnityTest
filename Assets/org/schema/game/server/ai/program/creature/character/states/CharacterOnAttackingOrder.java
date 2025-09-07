package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;

public class CharacterOnAttackingOrder extends CharacterState {

	/**
	 *
	 */
	

	public CharacterOnAttackingOrder(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {
		if (getEntityState().getAttackTarget() != null) {
			getEntityState().attackSecondary(getEntityState().getAttackTarget());
		}
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		return false;
	}

}
