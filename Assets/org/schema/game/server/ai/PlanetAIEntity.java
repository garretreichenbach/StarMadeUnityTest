package org.schema.game.server.ai;

import org.schema.game.common.controller.Planet;
import org.schema.schine.graphicsengine.core.Timer;

public class PlanetAIEntity extends SegmentControllerAIEntity<Planet> {

	/**
	 *
	 */
	

	public PlanetAIEntity(String name, Planet s) {
		super(name, s);
	}

	@Override
	public void updateAIClient(Timer timer) {
	}

	@Override
	public void updateAIServer(Timer timer) {
	}

}
