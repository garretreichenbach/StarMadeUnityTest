package org.schema.schine.network.commands;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;


public class SynchronizePrivateCommandHandler implements CommandHandler<SynchronizePrivateCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, SynchronizePrivateCommandPackage pack) throws IOException {
		
		state.receivedPrivateSynchronization(recipient, pack);
		
		
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (SynchronizePrivateCommandPackage) pack);
	}

}
