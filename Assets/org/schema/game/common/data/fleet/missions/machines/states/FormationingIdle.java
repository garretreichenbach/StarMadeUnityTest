package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetBreaking;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetIdleWaiting;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class FormationingIdle extends Formatoning{

	public FormationingIdle(Fleet gObj) {
		super(gObj);
	}
	@Override
	public FleetStateType getType() {
		return FleetStateType.FORMATION_IDLE;
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
		if(flagShip != null && flagShip.isLoaded()){
			
			Ship s = (Ship) flagShip.getLoaded();
			if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)){
				return false;
			}
			State st = s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState();
			if(s.getAttachedPlayers().isEmpty()){
				if(!(st instanceof FleetBreaking) ){
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.FLEET_BREAKING);
				}
			}else{
				if(!(st instanceof FleetIdleWaiting) ){
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.RESTART);
				}
			}
		}
		return super.onUpdate();
	}
	
	
}
