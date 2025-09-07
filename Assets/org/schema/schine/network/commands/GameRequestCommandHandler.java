package org.schema.schine.network.commands;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.server.ServerStateInterface;


public class GameRequestCommandHandler implements CommandHandler<GameRequestCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, GameRequestCommandPackage pack) throws IOException {
		((ServerStateInterface)state).handleGameRequestAnswer(recipient, pack);
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (GameRequestCommandPackage) pack);
	}

}
