package org.schema.schine.network.server;

import java.io.IOException;

public interface ServerListenerInterface {

	public boolean isListening();

	public void startServerListener(int port, int backlog) throws IOException;

	public boolean isErrorSetup();

	public boolean isStopRequested();

	public void stop();


	public int getInboundPacketCount();
	public int getOutboundPacketCount();

	public int getBytesPerSecond();
	
}
