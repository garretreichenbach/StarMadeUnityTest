package org.schema.game.server.ai;

import org.schema.game.common.controller.PlanetIco;
import org.schema.schine.graphicsengine.core.Timer;

public class PlanetIcoAIEntity extends SegmentControllerAIEntity<PlanetIco> {

	/**
	 *
	 */
	

	public PlanetIcoAIEntity(String name, PlanetIco s) {
		super(name, s);
	}

	@Override
	public void updateAIClient(Timer timer) {
	}

	@Override
	public void updateAIServer(Timer timer) {
	}

}
