package org.schema.game.network.commands;

import java.io.IOException;

import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.server.ServerProcessorInterface;


public class AdminCommandCommandHandler implements CommandHandler<AdminCommandCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, AdminCommandCommandPackage pack) throws IOException {
		RegisteredClientOnServer client = (RegisteredClientOnServer) ((ServerProcessorInterface)recipient).getClient();
		if (((GameServerState) state).getController().isAdmin(client)) {

			AdminCommands command = pack.adminCommand;
			if (((GameServerState) state).getController().allowedToExecuteAdminCommand(client, command)) {
				((GameServerState) state).getController().enqueueAdminCommand(client, command, pack.commandParams);
			} else {
				client.serverMessage("[ADMIN COMMAND] [ERROR] You are an admin but not allowed to execute this command. Please ask your server admin or super admin!");
			}
		} else {
			client.serverMessage("[ADMIN COMMAND] [ERROR] YOU ARE NOT AN ADMIN!");
		}
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (AdminCommandCommandPackage) pack);
		
		
	}

}
