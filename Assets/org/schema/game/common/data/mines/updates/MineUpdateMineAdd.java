package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;
import org.schema.game.common.data.mines.Mine.MineDataException;
import org.schema.game.common.data.mines.updates.MineUpdateSectorData.MineData;

public class MineUpdateMineAdd extends MineUpdate{
	public MineData m;
	public int sectorId;
	public MineUpdateMineAdd() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.MINE_ADD;
	}

	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(sectorId);
		m.serialize(b, isOnServer);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		sectorId = b.readInt();
		m = new MineData();
		m.deserialize(b, updateSenderStateId, isOnServer);
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		try {
			mineController.addMineClient(this.m, sectorId);
		} catch (MineDataException e) {
			e.printStackTrace();
		}
	}
}
