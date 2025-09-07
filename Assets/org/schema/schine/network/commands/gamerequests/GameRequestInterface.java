package org.schema.schine.network.commands.gamerequests;

import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerState;

public interface GameRequestInterface extends SerializationInterface{
	public GameRequestAnswerFactory getFactory();
	
	public void free();
	
	
	public void handleAnswer(NetworkProcessor p, ServerState state) throws IOException;
}
