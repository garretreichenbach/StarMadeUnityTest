package org.schema.schine.network.commands;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;


public class SynchronizeAllCommandHandler implements CommandHandler<SynchronizeAllCommandPackage>{

	@Override
	public void handle(NetworkProcessor recipient, StateInterface state, SynchronizeAllCommandPackage pack) throws IOException {
		state.receivedAllSynchronization(recipient, pack);
	}

	@Override
	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException {
		handle(recipient, state, (SynchronizeAllCommandPackage) pack);
	}

}
