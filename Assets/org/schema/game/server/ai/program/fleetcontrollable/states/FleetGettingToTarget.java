package org.schema.game.server.ai.program.fleetcontrollable.states;

import org.schema.game.server.ai.program.common.states.GettingToTarget;
import org.schema.schine.ai.AiEntityStateInterface;

public class FleetGettingToTarget extends GettingToTarget implements FleetAttackCycle {

	public FleetGettingToTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}


}
