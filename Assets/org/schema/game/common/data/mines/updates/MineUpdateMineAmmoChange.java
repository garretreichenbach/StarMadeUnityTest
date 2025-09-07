package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateMineAmmoChange extends MineUpdateMineChange{
	public short ammo;
	public MineUpdateMineAmmoChange() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.MINE_CHANGE_AMMO;
	}


	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		super.serializeData(b, isOnServer);
		b.writeShort(this.ammo);
	}


	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);
		this.ammo = b.readShort();
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		mineController.changeMineAmmoClient(mineId, ammo);
	}
	
	
}
