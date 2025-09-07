package org.schema.game.common.data.fleet.missions.machines.states;

import java.util.List;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.formation.FleetFormation;
import org.schema.game.common.data.fleet.formation.FleetFormationLine;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorTransformation;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetFormationingAbstract;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetIdleWaiting;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetMovingToSector;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class Formatoning extends FleetState{

	public Formatoning(Fleet gObj) {
		super(gObj);
	}


	@Override
	public boolean onExit() {
		return false;
	}
	private List<Ship> loadedForFormation = new ObjectArrayList<Ship>();
	private List<Transform> formationPoses = new ObjectArrayList<Transform>();
	
	
	
	@Override
	public boolean onEnterFleetState() {
		
		try {
			restartAllLoaded();
		} catch (FSMException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < getEntityState().getMembers().size(); i++){
			SegmentController l = getEntityState().getMembers().get(i).getLoaded();
			if(l != null){
				((Ship)l).getAiConfiguration().getAiEntityState().fleetFormationPos.clear();
			}
		}
		
		return super.onEnterFleetState();
	}


	@Override
	public boolean onUpdate() throws FSMException {
		
		formation();
		
		return true;
	}
	
	private void formation() throws FSMException{
		FleetMember flagShip = getEntityState().getFlagShip();
		
		if(flagShip == null){
			stateTransition(Transition.FLEET_EMPTY);
			return;
		}
		
		loadedForFormation.clear();
		formationPoses.clear();
		for(int i = 1; i < getEntityState().getMembers().size(); i++){
			FleetMember fleetMember = getEntityState().getMembers().get(i);
			if(fleetMember.isLoaded() ){
				
				if(!fleetMember.getLoaded().railController.isDockedOrDirty()){
					Ship s = (Ship)fleetMember.getLoaded();
					loadedForFormation.add(s);
					Transform transform = new Transform();
					transform.setIdentity();
					formationPoses.add(transform);
				}
			}else{
				if(!fleetMember.getSector().equals(flagShip.getSector())){
					fleetMember.moveRequestUnloaded(getEntityState(), flagShip.getSector());
				}
			}
		}
		
		handleLoaded(flagShip);
	}
	public void onMoreThanOneSectorAwayFromFlagship(Ship s, FleetMember flagShip) throws FSMException {
		if(!(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetMovingToSector)){
			((TargetProgram<?>) s.getAiConfiguration().getAiEntityState().getCurrentProgram())
			.setSectorTarget(new Vector3i(flagShip.getSector()));
			
			s.getAiConfiguration().getAiEntityState().getCurrentProgram().
			getMachine().getFsm().stateTransition(Transition.MOVE_TO_SECTOR);
		}
	}
	
	public void initFormation(Ship flagShip, List<Ship> loaded, List<Transform> formationPoses){
//		FleetFormation line = new FleetFormationSpherical();
		FleetFormation line = new FleetFormationLine();
		line.getFormation(flagShip, loaded, formationPoses);
	}
	
	public void handleLoaded(FleetMember flagShip) throws FSMException{
		if(!flagShip.isLoaded()){
			return;
		}
		
		if(flagShip.getLoaded().railController.isDockedAndExecuted()){
			stateTransition(Transition.FLEET_SENTRY);
			return;
		}
		
		initFormation((Ship) flagShip.getLoaded(), loadedForFormation, formationPoses);
		
		
		for(int i = 0; i < loadedForFormation.size(); i++){
			Ship s = loadedForFormation.get(i);
			if(s.isCoreOverheating()){
				continue;
			}
			if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)){
				continue;
			}
			if(s.railController.isDockedAndExecuted()){
				if(!(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetIdleWaiting)){
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.RESTART);
				}
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
			if(!Sector.isNeighbor(flagShip.getSector(), sector.pos) || !flagShip.isLoaded()){
				onMoreThanOneSectorAwayFromFlagship(s, flagShip);
			}else{
				if(formationPoses.get(i) != null){
					onProximityToFlagshipSector(s, flagShip, formationPoses.get(i));
				}
			}
		}
	}
	public Transition getEntityFormationTransition(){
		return Transition.FLEET_FORMATION;
	}
	public void transformAndAddLocalFormation(FleetMember flagShip, Ship s, Transform transform){
		flagShip.getLoaded().getWorldTransform().transform(transform.origin);
		SectorTransformation sectorTransformation = new SectorTransformation(transform, flagShip.getLoaded().getSectorId());
		s.getAiConfiguration().getAiEntityState().fleetFormationPos.add(sectorTransformation);
	}
	public boolean needsFormationTransition(Ship s){
		State currentState = s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState();
		return !(currentState instanceof FleetFormationingAbstract);
	}
	public void onProximityToFlagshipSector(Ship s, FleetMember flagShip,
			Transform transform) throws FSMException {
		if(needsFormationTransition(s)){
			s.getAiConfiguration().getAiEntityState().getCurrentProgram().
			getMachine().getFsm().stateTransition(getEntityFormationTransition());
		}
		
		transformAndAddLocalFormation(flagShip, s, transform);
		
		
		assert(flagShip.getLoaded().isOnServer());
		
//		System.err.println("ADDING: "+sectorTransformation);
		
		for(int j = 0 ; j < s.getAiConfiguration().getAiEntityState().fleetFormationPos.size() - 4; j++){
			SectorTransformation a = s.getAiConfiguration().getAiEntityState().fleetFormationPos.get(j);
			SectorTransformation b = s.getAiConfiguration().getAiEntityState().fleetFormationPos.get(j+1);
			SectorTransformation c = s.getAiConfiguration().getAiEntityState().fleetFormationPos.get(j+2);
			
			if(a.sectorId == b.sectorId && a.sectorId == c.sectorId && Vector3fTools.diffLength(a.t.origin, c.t.origin) < 3 && a.t.basis.epsilonEquals(b.t.basis, 0.3f)){
				s.getAiConfiguration().getAiEntityState().fleetFormationPos.remove(j+1);
			}
		}
		while(s.getAiConfiguration().getAiEntityState().fleetFormationPos.size() > 200){
			s.getAiConfiguration().getAiEntityState().fleetFormationPos.remove(0);
		}		
	}

	

}
