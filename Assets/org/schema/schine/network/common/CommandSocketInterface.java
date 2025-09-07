package org.schema.schine.network.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public interface CommandSocketInterface {

	public void close() throws IOException;

	public InputStream getInputStream() throws IOException;

	public OutputStream getOutputStream() throws IOException;

	public InetAddress getInetAddress();

	public boolean isConnected();

	public Object getRemoteSocketAddress();

	public int getLocalPort();

	public boolean isClosed();

	public boolean isBound();

	public boolean isInputShutdown();

	public boolean isOutputShutdown();

	public void shutdownInput() throws IOException;

	public void shutdownOutput() throws IOException;

	public Socket getSocket();


}
