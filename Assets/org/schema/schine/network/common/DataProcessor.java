package org.schema.schine.network.common;

import java.io.IOException;
import java.net.Socket;


public interface DataProcessor {

	
	public int readPackage(InputPacket packet) throws IOException;
	public void close(NetworkProcessor proc) throws IOException;
	public boolean isConnected();
	public String getInetAddress();
	public String getRemoteIp();
	public int getLocalPort();
	public void flushOut() throws IOException;
	public void sendPacket(OutputPacket s) throws IOException;
	public Socket getSocket();

	boolean hasData();
}
