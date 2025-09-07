package org.schema.game.server.ai.program.creature.character.states;

import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterEngagingWithPath extends CharacterState {

	/**
	 *
	 */
	

	public CharacterEngagingWithPath(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {

		System.err.println("[AI] CharacterEngagingWithPath PLOTTING PATH ");
		getEntity().getOwnerState().plotInstantPath();
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		stateTransition(Transition.MOVE);
		return false;
	}

}
