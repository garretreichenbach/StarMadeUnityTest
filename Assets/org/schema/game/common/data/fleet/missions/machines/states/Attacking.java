package org.schema.game.common.data.fleet.missions.machines.states;

import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetMovingToSector;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Attacking extends FleetState{

	public Attacking(Fleet gObj) {
		super(gObj);
		// TODO Auto-generated constructor stub
	}


	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		moveToCurrentTargetAndAttack();
		return false;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.ATTACKING;
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
	private List<Ship> loaded = new ObjectArrayList<Ship>();
	public void moveToCurrentTargetAndAttack() throws FSMException{
		FleetMember flagShip = getEntityState().getFlagShip();
		
		if(flagShip == null){
			stateTransition(Transition.FLEET_EMPTY);
			return;
		}
		if(getEntityState().getCurrentMoveTarget() == null){
			stateTransition(Transition.TARGET_SECTOR_REACHED);
			return;
		}
		loaded.clear();
		for(int i = 0; i < getEntityState().getMembers().size(); i++){
			FleetMember fleetMember = getEntityState().getMembers().get(i);
			
				
			if(fleetMember.isLoaded() ){
				Ship s = (Ship)fleetMember.getLoaded();
				
				loaded.add(s);
			}else{
				if(flagShip.getSector().equals(getEntityState().getCurrentMoveTarget()) && fleetMember.getSector().equals(flagShip.getSector())){
					//unloaded at target sector
				}else{
					fleetMember.moveRequestUnloaded(getEntityState(), getEntityState().getCurrentMoveTarget());
				}
			}
		}
		handleLoadedMoving(flagShip, loaded, getEntityState().getCurrentMoveTarget());
		return;
	}
	private void handleLoadedMoving(FleetMember flagShip, List<Ship> loaded, Vector3i goal) throws FSMException{
		
		
		for(int i = 0; i < loaded.size(); i++){
			Ship s = loaded.get(i);
			if(s.isCoreOverheating()){
				continue;
			}
			if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)){
				continue;
			}
			assert(s.getAiConfiguration() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm() != null):s;
			assert(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() != null):s;
			
			
			
			Sector sector = ((GameServerState)s.getState()).getUniverse().getSector(s.getSectorId());
			
			if(sector == null){
				continue;
			}
			;
			if(Sector.isNeighbor(sector.pos, goal)){
				if(!isInAttackCycle(s)){
					
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
				}
			}else {
				
				((TargetProgram<?>) s.getAiConfiguration().getAiEntityState().getCurrentProgram())
				.setSectorTarget(new Vector3i(goal));				
				if(!(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetMovingToSector)){
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.MOVE_TO_SECTOR);
				}
			}
		}
	}
}
