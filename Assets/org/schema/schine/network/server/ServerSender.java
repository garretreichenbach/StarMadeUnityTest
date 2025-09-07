package org.schema.schine.network.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ServerSender extends Thread {
	public static final String DEFAULT_HOST = "files-origin.star-made.org";
	public static long DEFAULT_HB = 1000 * 60 * 5;
	public static int DEFAULT_PORT = 19138;
	private final long heartbeat;
	private int port;
	private String host;
	private String accounceServerHost;
	private int annouceServerPort;

	public ServerSender(long heartbeat, String accounceServerHost, int annouceServerPort, String host, int port) {
		setPriority(MIN_PRIORITY);
		setDaemon(true);
		this.heartbeat = heartbeat;
		this.host = host;
		this.port = port;
		this.accounceServerHost = accounceServerHost;
		this.annouceServerPort = annouceServerPort;
		start();
	}

	public static void main(String args[]) {
		ServerSender s = new ServerSender(DEFAULT_HB, "localhost", DEFAULT_PORT, "localhost", 4242);
	}

	@Override
	public void run() {
		annouceServer();
		while (true) {
			try {
				Thread.sleep(heartbeat);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			annouceServer();
		}
	}

	private void annouceServer() {
		try {
			Socket connection = new Socket(accounceServerHost, annouceServerPort);
			connection.setSoTimeout(3000);
			DataOutputStream s = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
			DataInputStream sIn = new DataInputStream(new BufferedInputStream(connection.getInputStream()));

			s.writeUTF(host + ":" + port);
			s.flush();
			connection.shutdownInput();
			connection.shutdownOutput();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
