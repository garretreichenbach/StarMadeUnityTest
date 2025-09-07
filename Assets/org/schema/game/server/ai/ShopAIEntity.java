package org.schema.game.server.ai;

import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.graphicsengine.core.Timer;

public class ShopAIEntity extends SegmentControllerAIEntity<ShopSpaceStation> {

	/**
	 *
	 */
	

	public ShopAIEntity(String name, MachineProgram<?> program, ShopSpaceStation s) {
		super(name, s);
	}

	@Override
	public void updateAIClient(Timer timer) {
	}

	@Override
	public void updateAIServer(Timer timer) {
	}

}
