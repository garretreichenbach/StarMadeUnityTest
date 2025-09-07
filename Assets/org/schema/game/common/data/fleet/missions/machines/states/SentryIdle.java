package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class SentryIdle extends FleetState{

	public SentryIdle(Fleet gObj) {
		super(gObj);
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
	public boolean onUpdate() throws FSMException {
		FleetMember flagShip = getEntityState().getFlagShip();
		
		if(flagShip == null){
			stateTransition(Transition.FLEET_EMPTY);
			return false;
		}
		for(int i = 0; i < getEntityState().getMembers().size(); i++){
			FleetMember fleetMember = getEntityState().getMembers().get(i);
			
			if(fleetMember.isLoaded() ){
				Ship s = (Ship)fleetMember.getLoaded();
				if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)){
					continue;
				}
				if(!isInAttackCycle(s) && !s.isCoreOverheating()){
					s.getAiConfiguration().getAiEntityState().lastAttackStateSetByFleet = System.currentTimeMillis();
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
				}
			}
		}
		return false;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.SENTRY_IDLE;
	}

}
