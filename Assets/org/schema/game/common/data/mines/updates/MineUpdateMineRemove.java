package org.schema.game.common.data.mines.updates;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateMineRemove extends MineUpdateMineChange{
	public MineUpdateMineRemove() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.MINE_REMOVE;
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		mineController.removeMineClient(mineId);
	}
}
