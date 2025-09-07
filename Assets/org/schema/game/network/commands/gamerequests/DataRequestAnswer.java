package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.commands.gamerequests.GameAnswerInterface;
import org.schema.schine.network.server.ServerState;

public abstract class DataRequestAnswer implements GameAnswerInterface{

	public byte[] data;
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(data.length);
		b.write(data);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		int size = b.readInt();
		data = new byte[size];
		b.readFully(data);
	}

	@Override
	public void free() {
		data = null;		
	}

	protected abstract void serve(ServerState state);
}
