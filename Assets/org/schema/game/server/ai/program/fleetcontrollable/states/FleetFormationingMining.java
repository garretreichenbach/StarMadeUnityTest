package org.schema.game.server.ai.program.fleetcontrollable.states;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class FleetFormationingMining extends FleetFormationingAbstract{

	public FleetFormationingMining(AiEntityStateInterface gObj) {
		super(gObj);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDistance(float dist) throws FSMException{
		if(dist < 10f){
			stateTransition(Transition.FLEET_MINE);
		}
	}

}
