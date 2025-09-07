package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterWaitingForPath extends CharacterState {

	/**
	 *
	 */
	

	public CharacterWaitingForPath(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
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
		if (getEntity().getOwnerState().getCurrentPath() != null && getEntity().getOwnerState().getCurrentPath().size() > 0) {
			stateTransition(Transition.MOVE);
		} else {
			getEntity().getOwnerState().resetTargetToMove();
		}

		return false;
	}

}
