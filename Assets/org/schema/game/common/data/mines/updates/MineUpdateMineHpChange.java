package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateMineHpChange extends MineUpdateMineChange{
	short hp;
	public MineUpdateMineHpChange() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.MINE_CHANGE_HP;
	}


	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		super.serializeData(b, isOnServer);
		b.writeShort(this.hp);
	}


	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);
		this.hp = b.readShort();
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		mineController.changeMineHpClient(mineId, hp);
	}
	
	
}
