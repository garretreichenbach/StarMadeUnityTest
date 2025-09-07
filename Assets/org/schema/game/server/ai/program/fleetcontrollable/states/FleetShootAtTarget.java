package org.schema.game.server.ai.program.fleetcontrollable.states;

import org.schema.game.server.ai.program.common.states.ShootAtTarget;
import org.schema.schine.ai.AiEntityStateInterface;


public class FleetShootAtTarget extends ShootAtTarget implements FleetAttackCycle {


	public FleetShootAtTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	

}
