package org.schema.game.common.controller.ai;

import org.schema.game.common.controller.SpaceStation;
import org.schema.game.server.ai.SpaceStationAIEntity;
import org.schema.schine.network.StateInterface;

public class AISpaceStationConfiguration extends AIGameSegmentControllerConfiguration<SpaceStationAIEntity, SpaceStation> {

	public AISpaceStationConfiguration(StateInterface state, SpaceStation owner) {
		super(state, owner);

		//		setaIEntity( new SimpleSearchAndDestroyShipAIEntity(owner) );
	}

	@Override
	protected SpaceStationAIEntity getIdleEntityState() {
		return new SpaceStationAIEntity("shipAiEntity", getOwner());
	}

	@Override
	protected boolean isForcedHitReaction() {
		return getOwner().getFactionId() < 0;
	}

	@Override
	protected void prepareActivation() {
	}


}
