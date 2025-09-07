package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.world.SectorTransformation;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetFormationingAbstract;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetIdleWaiting;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

import com.bulletphysics.linearmath.Transform;

public class FormationingSentry extends Formatoning{

	private static final long SEARCH_FOR_TARGET_DELAY = 1000;

	public FormationingSentry(Fleet gObj) {
		super(gObj);
	}

	
	
	@Override
	public boolean onUpdate() throws FSMException {
		
		if(getEntityState().getFlagShip() != null && getEntityState().getFlagShip().isLoaded()){
			Ship s = (Ship) getEntityState().getFlagShip().getLoaded();
			if(!isInAttackCycle(s) ){
				if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)){
				
				}else{
					s.getAiConfiguration().getAiEntityState().lastAttackStateSetByFleet = System.currentTimeMillis();
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
				}
			}
		}
		
		return super.onUpdate();
	}



	@Override
	public FleetStateType getType() {
		return FleetStateType.FORMATION_SENTRY;
	}
	
	
	@Override
	public void onProximityToFlagshipSector(Ship s, FleetMember flagShip,
			Transform transform) throws FSMException {
		State st = s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState();
		
		if(!isInAttackCycle(s) && (System.currentTimeMillis() - s.getAiConfiguration().getAiEntityState().lastAttackStateSetByFleet) > SEARCH_FOR_TARGET_DELAY){
			s.getAiConfiguration().getAiEntityState().lastAttackStateSetByFleet = System.currentTimeMillis();
			s.getAiConfiguration().getAiEntityState().getCurrentProgram().
			getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
		}else 
			if((isInAttackCycle(s) || st instanceof FleetIdleWaiting) && ((TargetProgram<?>) s.getAiConfiguration().getAiEntityState().getCurrentProgram()).getTarget() == null){
				
				//no taget. get in formation
				if(!(s.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetFormationingAbstract)){
					s.getAiConfiguration().getAiEntityState().getCurrentProgram().
					getMachine().getFsm().stateTransition(Transition.FLEET_FORMATION);
				}
			}
			if(!isInAttackCycle(s)){
				flagShip.getLoaded().getWorldTransform().transform(transform.origin);
				SectorTransformation sectorTransformation = new SectorTransformation(transform, flagShip.getLoaded().getSectorId());
				s.getAiConfiguration().getAiEntityState().fleetFormationPos.add(sectorTransformation);
				
				assert(flagShip.getLoaded().isOnServer());
				
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

	
	
}
