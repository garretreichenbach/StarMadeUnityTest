package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateArmMinesRequest extends MineUpdate{
	public Vector3i s;
	public boolean all;
	public int clientId;
	
	public MineUpdateArmMinesRequest() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.ARM_MINES_REQUEST;
	}

	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(clientId);
		b.writeBoolean(all);
		if(!all) {
			s.serialize(b);
		}
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		clientId = b.readInt();
		all = b.readBoolean();
		if(!all) {
			s = Vector3i.deserializeStatic(b);
		}
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		System.err.println("[SERVER] CLIENT ARM REQUEST: "+(all ? "ALL" : s.toString()));
		mineController.handleClientArmRequest(clientChannel, all, s);
	}
	
}
