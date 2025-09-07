package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class Defending extends Attacking{

	public Defending(Fleet gObj) {
		super(gObj);
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
	public boolean onUpdate() throws FSMException {
		if(getEntityState().isEmpty()){
			stateTransition(Transition.FLEET_EMPTY);
			return false;
		}
		if(getEntityState().getCurrentMoveTarget() == null) getEntityState().setCurrentMoveTarget(getEntityState().getFlagShip().getSector());
		if(Vector3i.getDisatance(getEntityState().getFlagShip().getSector(), getEntityState().getCurrentMoveTarget()) > 1.35f){
			moveToCurrentTarget();
			return false;
		}else{
			return super.onUpdate();
		}
	}
	@Override
	public FleetStateType getType() {
		return FleetStateType.DEFENDING;
	}
}
