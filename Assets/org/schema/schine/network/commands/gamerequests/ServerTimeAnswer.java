package org.schema.schine.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ServerTimeAnswer implements GameAnswerInterface{

	public long serverTime;
	@Override
	public GameRequestAnswerFactory getFactory() {
		return BasicRequestAnswerFactories.SERVER_TIME;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(serverTime);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		serverTime = b.readLong();
	}

	@Override
	public void free() {
	}


}
