package org.schema.schine.network.commands;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;

import java.io.IOException;

public class ServerInfoAnswerCommandHandler implements CommandHandler<ServerInfoAnswerCommandPackage> {

	public ServerInfoAnswerCommandHandler() {
	}

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, ServerInfoAnswerCommandPackage pack) throws IOException {
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (ServerInfoAnswerCommandPackage) pack);
	}
}
