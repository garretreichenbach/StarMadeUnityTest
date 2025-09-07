package org.schema.game.server.ai.program.fleetcontrollable.states;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.Ship;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

public class FleetBreaking extends ShipGameState {

	/**
	 *
	 */
	public static final Vector3f breakMoving = new Vector3f(0,0,0.01f);

	public FleetBreaking(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {
		return false;
	}
	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		
		if(getEntity().getVelocity().length() < 0.001f){
			getEntity().getNetworkObject().moveDir.set(0,0,0);
			stateTransition(Transition.RESTART);
		}
		return false;
	}
	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().orientationDir.set(0,0,0,0);
		getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));
		if(getEntity().getVelocity().length() > 0.001f){
			getEntity().getNetworkObject().moveDir.set(breakMoving);
			s.stop();
		}
	}
}
