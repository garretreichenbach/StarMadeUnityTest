package org.schema.schine.network;

import java.io.IOException;

import org.schema.schine.network.common.DataProcessor;
import org.schema.schine.network.server.ServerStateInterface;

public class UnregisteredClient implements RegisteredClientInterface{


	private DataProcessor dataPipe;
	private ServerStateInterface state;

	public UnregisteredClient(ServerStateInterface state, DataProcessor dataPipe) {
		this.dataPipe = dataPipe;
		this.state = state;
	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public String getClientName() {
		return "UnregisteredClient["+state+"]["+(dataPipe.isConnected() ? dataPipe.getInetAddress() : "closedSocket")+"]";
	}

	@Override
	public void disconnect() {
	}
	
	public String toString() {
		return getClientName();
	}


	@Override
	public void executedAdminCommand() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void serverMessage(String msg) throws IOException {
		assert(false);
	}

	@Override
	public void blockFromLogout() {
		assert(false);		
	}
}
