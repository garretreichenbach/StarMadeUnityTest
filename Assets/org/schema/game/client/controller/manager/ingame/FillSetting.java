package org.schema.game.client.controller.manager.ingame;

import org.schema.game.client.data.GameClientState;

public class FillSetting extends AbstractSizeSetting{

	
	private GameClientState state;

	public FillSetting(GameClientState state) {
		super();
		this.state = state;
	}
	
	@Override
	public int getMin() {
		return 1;
	}

	@Override
	public int getMax() {
		//Server config for max? 
		return Math.min(
			(int) Math.pow(state.getMaxBuildArea(), 3),
			5000);
	}
	
}
