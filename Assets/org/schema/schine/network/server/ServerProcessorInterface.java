package org.schema.schine.network.server;

import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.network.common.NetworkProcessor;

public interface ServerProcessorInterface extends NetworkProcessor{

	public void setClient(RegisteredClientInterface client);
	public RegisteredClientInterface getClient();
	public void disconnectAfterSent();
	public String getIp();
	public String getClientIp();
	public boolean isConnectionAlive();
}
