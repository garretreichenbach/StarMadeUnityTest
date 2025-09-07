package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.ai.stateMachines.FSMException;

public class Idle extends FleetState{

	public Idle(Fleet gObj) {
		super(gObj);
	}


	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		restartAllLoaded();
		return false;
	}

	


	@Override
	public FleetStateType getType() {
		return FleetStateType.IDLING;
	}

}
