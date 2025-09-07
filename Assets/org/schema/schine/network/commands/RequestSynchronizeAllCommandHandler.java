package org.schema.schine.network.commands;

import java.io.IOException;

import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.server.ServerProcessorInterface;


public class RequestSynchronizeAllCommandHandler implements CommandHandler<RequestSynchronizeAllCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, RequestSynchronizeAllCommandPackage pack) throws IOException {
		((GameServerState)state).getController().synchronizeClientFully((RegisteredClientOnServer) ((ServerProcessorInterface)recipient).getClient());
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (RequestSynchronizeAllCommandPackage) pack);
	}

}
