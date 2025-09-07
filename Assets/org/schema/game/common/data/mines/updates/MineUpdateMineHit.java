package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateMineHit extends MineUpdateMineChange{
	public int sectorId;
	public MineUpdateMineHit() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.MINE_HIT;
	}

	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		super.serializeData(b, isOnServer);
		b.writeInt(this.sectorId);
	}


	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);
		this.sectorId = b.readInt();
	}
	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		mineController.mineHitServer(mineId, sectorId);
	}
}
