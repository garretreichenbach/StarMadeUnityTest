package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerState;

public abstract class DataRequest implements GameRequestInterface{

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
		DataRequestAnswer a = (DataRequestAnswer) getFactory().getAnswerInstance();
		a.serve(state);
		
		GameRequestAnswerFactory.send(a, p);
		
	}
	
	
}
