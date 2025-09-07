package org.schema.schine.network.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


public class DataProcessorSocket implements DataProcessor{

	private CommandSocketInterface commandSocket;
	private final DataInputStream dataInputStream;
	private final DataOutputStream dataOutputStream;
	private final NetworkSettings settings;

	public DataProcessorSocket(CommandSocketInterface commandSocket, NetworkSettings settings) throws SocketException, IOException {
		this.commandSocket = commandSocket;
		this.settings = settings;
		
		
		dataInputStream = new DataInputStream(new BufferedInputStream(commandSocket.getInputStream(), settings.getSocketReceiveBufferSize().getInt()));
		dataOutputStream = new DataOutputStream(new BufferedOutputStream(commandSocket.getOutputStream(), settings.getSocketSendBufferSize().getInt()));
	}

	@Override
	public int readPackage(InputPacket packet) throws IOException {
		
		int size = dataInputStream.readInt();
		
		packet.readFully(dataInputStream, size);
		
		return size;
	}

	@Override
	public void close(NetworkProcessor proc) throws IOException {
		commandSocket.close();		
	}

	@Override
	public boolean isConnected() {
		return commandSocket.isConnected();
	}

	@Override
	public String getInetAddress() {
		return commandSocket.isClosed() ? "n/a" : commandSocket.getInetAddress().toString();
	}

	@Override
	public String getRemoteIp() {
		return commandSocket.isClosed() ? "n/a" :commandSocket.getRemoteSocketAddress().toString();
	}

	@Override
	public int getLocalPort() {
		return commandSocket.isClosed() ? -1 : commandSocket.getLocalPort();
	}

	@Override
	public void flushOut() throws IOException {
		dataOutputStream.flush();
	}

	@Override
	public void sendPacket(OutputPacket s) throws IOException {
		s.writeTo(dataOutputStream);
	}

	@Override
	public Socket getSocket() {
		return commandSocket.getSocket();
	}

	@Override
	public boolean hasData() {
        try {
            return commandSocket.getInputStream().available() > 0;
        } catch (IOException e) {
			e.printStackTrace();
            return false;
        }
    }

}
