package org.schema.schine.network.commands;

import java.io.IOException;

import org.schema.schine.network.IdGen;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.server.ServerProcessorInterface;
import org.schema.schine.network.server.ServerStateInterface;


public class LoginRequestCommandHandler implements CommandHandler<LoginRequestCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, LoginRequestCommandPackage pack) throws IOException {
		LoginRequest r = new LoginRequest();
		r.state = (ServerStateInterface)state;
		r.playerName = pack.playerName;
		r.version = pack.version;
		r.uniqueSessionID = pack.uniqueSessionID;
		r.authCodeToken = pack.authCodeToken;
		r.id = IdGen.getFreeStateId();
		r.serverProcessor = (ServerProcessorInterface) recipient;
		r.login();
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (LoginRequestCommandPackage) pack);
	}

}
