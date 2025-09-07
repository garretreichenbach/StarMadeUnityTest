package org.schema.schine.network;

import java.io.IOException;

public interface RegisteredClientInterface {

	void executedAdminCommand();

	int getId();

	String getClientName();

	void serverMessage(String msg) throws IOException;

	void blockFromLogout();

	void disconnect();
	

}
