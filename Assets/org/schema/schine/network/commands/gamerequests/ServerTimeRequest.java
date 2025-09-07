package org.schema.schine.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerState;

public class ServerTimeRequest implements GameRequestInterface {

	
	
	@Override
	public GameRequestAnswerFactory getFactory() {
		return BasicRequestAnswerFactories.SERVER_TIME;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
	}

	@Override
	public void free() {
	}

	@Override
	public void handleAnswer(NetworkProcessor p, ServerState state) throws IOException {
		
		ServerTimeAnswer a = new ServerTimeAnswer();
		a.serverTime = System.currentTimeMillis();
		GameRequestAnswerFactory.send(a, p);
		
	}

}
