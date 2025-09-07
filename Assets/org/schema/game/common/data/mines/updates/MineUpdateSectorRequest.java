package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public class MineUpdateSectorRequest extends MineUpdate{
	public Vector3i s;
	public int clientId;
	public MineUpdateSectorRequest() {
	}
	

	@Override
	public MineUpdateType getType() {
		return MineUpdateType.SECTOR_REQUEST;
	}

	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(clientId);
		s.serialize(b);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		clientId = b.readInt();
		s = Vector3i.deserializeStatic(b);
	}


	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		mineController.handleClientRequest(clientChannel, s);
	}
}
