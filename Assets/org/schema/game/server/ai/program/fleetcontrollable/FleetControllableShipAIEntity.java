package org.schema.game.server.ai.program.fleetcontrollable;

import org.schema.game.common.controller.Ship;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.ShootingStateInterface;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetRepairing;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipEngagingTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipGettingToTarget;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

public class FleetControllableShipAIEntity extends ShipAIEntity {

	public FleetControllableShipAIEntity(Ship s, boolean startSuspended) {
		super("FLEET_ENT", s);
		if (s.isOnServer()) {
			setCurrentProgram(new FleetControllableProgram(this, startSuspended));
		}
		
		assert(isActive());

	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.ai.ShipAIEntity#updateAIServer(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateAIServer(Timer timer) throws FSMException {
		super.updateAIServer(timer);

		
		if(getStateCurrent() instanceof ShipGameState){
			((ShipGameState)getStateCurrent()).updateAI(unit, timer, getEntity(), this);
		}
		

		if (getEntity().getProximityObjects().size() > 0 && (getStateCurrent() instanceof ShipEngagingTarget || getStateCurrent() instanceof ShipGettingToTarget || getStateCurrent() instanceof ShootingStateInterface) || getStateCurrent() instanceof FleetRepairing) {
			try {
				//				System.err.println(getSendable()+" IS NOW EVADING OBJECTS: "+getProximityObjects().size());
				if (!getEntity().getDockingController().isDocked()) {
					getStateCurrent().stateTransition(Transition.ENEMY_PROXIMITY);
				}
			} catch (FSMException e) {
				e.printStackTrace();
			}
		} else {

		}

		
	}

}
