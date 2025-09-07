package org.schema.game.common.controller.ai;

import org.schema.game.common.controller.Planet;
import org.schema.game.server.ai.PlanetAIEntity;
import org.schema.schine.network.StateInterface;

public class AIPlanetConfiguration extends AIGameSegmentControllerConfiguration<PlanetAIEntity, Planet>  {

	public AIPlanetConfiguration(StateInterface state, Planet owner) {
		super(state, owner);

		//		setaIEntity( new SimpleSearchAndDestroyShipAIEntity(owner) );
	}

	@Override
	protected PlanetAIEntity getIdleEntityState() {
		return new PlanetAIEntity("PAI", getOwner());
	}

	@Override
	protected boolean isForcedHitReaction() {
		return getOwner().getFactionId() < 0;
	}

	@Override
	protected void prepareActivation() {
		
	}


}
