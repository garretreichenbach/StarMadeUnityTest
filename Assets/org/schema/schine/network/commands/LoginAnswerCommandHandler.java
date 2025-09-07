package org.schema.schine.network.commands;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;


public class LoginAnswerCommandHandler implements CommandHandler<LoginAnswerCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, LoginAnswerCommandPackage pack) throws IOException {
		((ClientStateInterface)state).handleLoginAnswer(pack);
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (LoginAnswerCommandPackage) pack);
	}

}
