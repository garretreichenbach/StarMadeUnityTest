package org.schema.game.server.ai.program.common.states;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.objects.Sendable;

public class Waiting extends GameState<Sendable> {

	/**
	 *
	 */
	

	public Waiting(AiEntityStateInterface gObj) {
		super(gObj);
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
		stateTransition(Transition.SEARCH_FOR_TARGET);
		return false;
	}

}
