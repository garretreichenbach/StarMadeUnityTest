package org.schema.game.common.controller.ai;

import org.schema.game.common.controller.PlanetIco;
import org.schema.game.server.ai.PlanetIcoAIEntity;
import org.schema.schine.network.StateInterface;

public class AIPlanetIcoConfiguration extends AIGameSegmentControllerConfiguration<PlanetIcoAIEntity, PlanetIco> {

	public AIPlanetIcoConfiguration(StateInterface state, PlanetIco owner) {
		super(state, owner);

		//		setaIEntity( new SimpleSearchAndDestroyShipAIEntity(owner) );
	}

	@Override
	protected PlanetIcoAIEntity getIdleEntityState() {
		return new PlanetIcoAIEntity("PAICO", getOwner());
	}

	@Override
	protected boolean isForcedHitReaction() {
		return getOwner().getFactionId() < 0;
	}

	@Override
	protected void prepareActivation() {
		
	}


}
