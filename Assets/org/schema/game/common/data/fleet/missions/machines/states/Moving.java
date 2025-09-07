package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class Moving extends FleetState{

	public Moving(Fleet gObj) {
		super(gObj);
		// TODO Auto-generated constructor stub
	}


	@Override
	public boolean onExit() {
		return false;
	}
	
	@Override
	public boolean onEnterFleetState() {
		try {
			restartAllLoaded();
		} catch (FSMException e) {
			e.printStackTrace();
		}
		return super.onEnterFleetState();
	}
	@Override
	public void stateTransition(Transition t, int subId) throws FSMException {
		super.stateTransition(t, subId);
		
		if(t == Transition.TARGET_SECTOR_REACHED){
			getEntityState().onCommandPartFinished(this);
		}
	}

	@Override
	public boolean onUpdate() throws FSMException {
		
		moveToCurrentTarget();
		return false;
	}
	
	
	@Override
	public FleetStateType getType() {
		return FleetStateType.MOVING;
	}

}
