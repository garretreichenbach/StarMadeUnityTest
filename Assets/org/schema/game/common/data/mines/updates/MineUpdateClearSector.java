package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateClearSector extends MineUpdate{
	public int sectorId;
	public MineUpdateClearSector() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.CLEAR_SECTOR;
	}

	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(sectorId);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		sectorId = b.readInt();
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		mineController.clearClientMinesInSector(sectorId);
	}
}
