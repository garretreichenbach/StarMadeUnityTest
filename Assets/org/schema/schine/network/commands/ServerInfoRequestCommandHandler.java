package org.schema.schine.network.commands;

import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.ServerInfo;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;

import java.io.IOException;

public class ServerInfoRequestCommandHandler implements CommandHandler<ServerInfoRequestCommandPackage> {

	public ServerInfoRequestCommandHandler() {
	}

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, ServerInfoRequestCommandPackage s) throws IOException {
		ServerInfoAnswerCommandPackage pack = new ServerInfoAnswerCommandPackage();
		pack.info = new ServerInfo();
		pack.info.populate((GameServerState) state);
		pack.send(recipient);
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (ServerInfoRequestCommandPackage) pack);
	}

}
