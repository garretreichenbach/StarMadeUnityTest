package org.schema.game.server.ai.program.fleetcontrollable.states;

import org.schema.game.server.ai.program.common.states.EngagingTargetSteady;
import org.schema.schine.ai.AiEntityStateInterface;

public class FleetEngagingTarget extends EngagingTargetSteady implements  FleetAttackCycle {


	public FleetEngagingTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	

}
