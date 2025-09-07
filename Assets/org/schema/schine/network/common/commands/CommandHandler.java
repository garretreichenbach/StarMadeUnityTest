package org.schema.schine.network.common.commands;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.NetworkProcessor;


public interface CommandHandler<T extends CommandPackage> {
	public void handle(NetworkProcessor recipient, StateInterface state, T commandPackage) throws IOException;

	public void handleGeneric(NetworkProcessor recipient, StateInterface state, CommandPackage pack) throws IOException;
}
