package org.schema.game.client.controller.manager.ingame;

import org.schema.game.client.data.GameClientState;

public class SizeSetting extends AbstractSizeSetting{

	private GameClientState state;

	public SizeSetting(GameClientState state) {
		super();
		this.state = state;
	}

	@Override
	public int getMax() {
		return (int) state.getMaxBuildArea();
	}

	@Override
	public int getMin() {
		return 1;
	}
	
	
	
}
