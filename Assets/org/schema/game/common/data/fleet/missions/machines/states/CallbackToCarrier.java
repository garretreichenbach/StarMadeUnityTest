package org.schema.game.common.data.fleet.missions.machines.states;

import java.util.List;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.formation.FleetFormationCallback;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

import com.bulletphysics.linearmath.Transform;

public class CallbackToCarrier extends Formatoning{

	public CallbackToCarrier(Fleet gObj) {
		super(gObj);
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.CALLBACK_TO_CARRIER;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		boolean allDocked = true;
		for(int i = 1; i < getEntityState().getMembers().size(); i++){
			FleetMember fleetMember = getEntityState().getMembers().get(i);
			if(fleetMember.isLoaded() ){
				if(!fleetMember.getLoaded().railController.isDockedAndExecuted()){
					allDocked = false;
					break;
				}
			}else{
				allDocked = false;
				break;
			}
		}
		
		if(allDocked){
			stateTransition(Transition.RESTART);
			return false;
		}else{
			return super.onUpdate();
		}
	}

	@Override
	public void initFormation(Ship flagShip, List<Ship> loaded,
			List<Transform> formationPoses) {

		
		
		FleetFormationCallback line = new FleetFormationCallback();
		line.getFormation(flagShip, loaded, formationPoses);
		
		
		
	}

	
	
	
}
