package org.schema.game.server.ai.program.fleetcontrollable.states;

import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;

public class FleetIdleWaiting extends ShipGameState {

	/**
	 *
	 */
	

	public FleetIdleWaiting(AiEntityStateInterface gObj) {
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
