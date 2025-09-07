package org.schema.schine.network.commands;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;


public class MessageCommandHandler implements CommandHandler<MessageCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, MessageCommandPackage pack) throws IOException {
		
		((ClientStateInterface)state).message(pack.message);
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (MessageCommandPackage) pack);
	}

}
