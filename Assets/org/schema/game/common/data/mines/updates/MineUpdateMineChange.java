package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class MineUpdateMineChange extends MineUpdate{
	public int mineId;
	public MineUpdateMineChange() {
	}
	

	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(mineId);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		mineId = b.readInt();
	}
}
