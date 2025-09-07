package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateMineArmedChange extends MineUpdateMineChange{
	public boolean armed;
	public MineUpdateMineArmedChange() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.MINE_ARM;
	}


	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		super.serializeData(b, isOnServer);
		b.writeBoolean(this.armed);
	}


	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);
		armed = b.readBoolean();
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		mineController.changeMineArmedClient(mineId, armed);
	}
	
	
}
