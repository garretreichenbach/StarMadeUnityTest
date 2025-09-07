package org.schema.game.network.commands;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.server.AdminRemoteClient;
import org.schema.schine.network.server.ServerProcessorInterface;
import org.schema.schine.network.server.ServerState;


public class ExecuteAdminCommandCommandHandler implements CommandHandler<ExecuteAdminCommandCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, ExecuteAdminCommandCommandPackage pack) throws IOException {
		((ServerState)state).executeAdminCommand(pack.serverPassword, pack.command, new AdminRemoteClient((ServerProcessorInterface)recipient));
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (ExecuteAdminCommandCommandPackage) pack);
		
		
	}

}
