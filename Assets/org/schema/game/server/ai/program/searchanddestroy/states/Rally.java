package org.schema.game.server.ai.program.searchanddestroy.states;

import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;

public class Rally extends ShipGameState {

	/**
	 *
	 */
	

	public Rally(AiEntityStateInterface gObj) {
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
				return false;
	}

}
