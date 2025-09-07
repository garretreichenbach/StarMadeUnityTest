package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class Trading extends Moving{

	public Trading(Fleet gObj) {
		super(gObj);
		// TODO Auto-generated constructor stub
	}



	@Override
	public boolean onUpdate() throws FSMException {
		moveToCurrentTarget();
		return false;
	}
	
	@Override
	public Transition getMoveTrasition(){
		return Transition.FLEET_TRADE;
	}
	@Override
	public boolean isMovingSentry() {
		return true;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.TRADING;
	}

}
