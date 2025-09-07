package org.schema.schine.network.server;

import java.io.IOException;

import org.schema.schine.network.RegisteredClientInterface;

public class AdminLocalClient implements RegisteredClientInterface {

	public AdminLocalClient() {
	}

	@Override
	public void executedAdminCommand() {
	}

	@Override
	public int getId() {
		return -1337;
	}

	@Override
	public String getClientName() {
		return "#CONSOLE#";
	}

	@Override
	public void serverMessage(String msg) throws IOException {
		System.err.println("[SERVER-LOCAL-ADMIN] " + msg);
	}

	@Override
	public void blockFromLogout() {
	}

	@Override
	public void disconnect() {
	}

}
