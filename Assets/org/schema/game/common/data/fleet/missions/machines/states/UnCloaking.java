package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.stealth.StealthCollectionManager;
import org.schema.game.common.controller.elements.stealth.StealthElementManager;
import org.schema.game.common.controller.elements.stealth.StealthAddOn;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class UnCloaking extends FleetState{

	public UnCloaking(Fleet gObj) {
		super(gObj);
	}


	@Override
	public boolean onExit() {
		return false;
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
				if(!s.isCoreOverheating()){
					StealthCollectionManager collection = s.getManagerContainer().getStealth().getElementManager().getCollection();
					if(!collection.getElementCollections().isEmpty()){
						//unjam
						StealthElementManager entityStealth = s.getManagerContainer().getStealthElementManager();
						if(entityStealth.hasCloakCapability()) entityStealth.stopStealth(StealthElementManager.REUSE_DELAY_ON_SWITCHED_OFF_MS);
					}
				}
			}
		}
		stateTransition(Transition.FLEET_ACTION_DONE);
		return false;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.UNCLOAKING;
	}

}
