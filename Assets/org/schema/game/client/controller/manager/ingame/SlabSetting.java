package org.schema.game.client.controller.manager.ingame;

import org.schema.game.client.data.GameClientState;

public class SlabSetting extends AbstractSizeSetting{

	private GameClientState state;

	public SlabSetting(GameClientState state) {
		this.state = state;
	}

	
	
	@Override
	public void set(float value) {
		super.set(value);
		
		state.getGlobalGameControlManager()
		.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager()
		.checkOrienationForNewSelectedSlot();
	}



	@Override
	public int getMax() {
		return 3;
	}

	@Override
	public int getMin() {
		return 0;
	}

}
