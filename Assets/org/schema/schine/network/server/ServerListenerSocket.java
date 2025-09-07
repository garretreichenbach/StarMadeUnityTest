package org.schema.schine.network.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map.Entry;

//import org.schema.common.util.log.Log;
import org.schema.schine.network.common.CommandSocket;
import org.schema.schine.network.common.DataProcessorSocket;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ServerListenerSocket implements Runnable, ServerListenerInterface {

	private int port;
	private ServerSocket serverSocket;
	private ObjectOpenHashSet<String> spamExceptions = new ObjectOpenHashSet<String>();
	private boolean listening;
	private Object2ObjectOpenHashMap<String, LongArrayList> connectionHistory = new Object2ObjectOpenHashMap<String, LongArrayList>();

	private ServerSettingsInterface settings;
	private ServerStateInterface state;
	private boolean errorSetup;
	private boolean stopRequested;
	private int backlog;

	public ServerListenerSocket(ServerStateInterface state, ServerSettingsInterface serverSettings) {

		this.settings = serverSettings;
		this.state = state;
	}

	public boolean isPortOpen() {
		try {
			Socket connection = new Socket("localhost", this.port);
			connection.setSoTimeout(3000);
//			DataOutputStream s = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
//			DataInputStream sIn = new DataInputStream(new BufferedInputStream(connection.getInputStream()));

			connection.shutdownInput();
			connection.shutdownOutput();
			connection.close();
		} catch(IOException e) {
			if(e instanceof SocketTimeoutException) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public String toString() {
		return "SK(" + port + ")";
	}

	public void startServerListener(int port) {
		startServerListener(port, 0);
	}

	public void startServerListener(int port, int backlog) {
		this.backlog = backlog;
		this.port = port;
		state.getConnectionThreadPool().execute(this);
	}

	private void openSocket() throws IOException {
		String[] ips = settings.getSpamProtectException().getString().split(",");
		for(String ip : ips) {
			spamExceptions.add(ip);
		}

//		boolean portOpen = isPortOpen();
//
//		if (portOpen) {
//			System.err.println("[SERVER] port " + port + " is open. Opening server socket");
		if(settings.getAcceptingIP().getString().length() == 0 || settings.getAcceptingIP().getString().equals("all")) {
			serverSocket = new ServerSocket(this.port, this.backlog);
		} else {
			serverSocket = new ServerSocket(this.port, this.backlog, InetAddress.getByName(settings.getAcceptingIP().getString()));
		}
//		} else {
//			try {
//				throw new IOException("Port " + this.port + " is closed or already in use");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	public boolean isListening() {
		return listening;
	}

	public void stop() {
		System.err.println("[SERVER] Stopping Listener...");
		try {
			stopRequested = true;
			if(serverSocket != null) {
				serverSocket.close();
			}
			System.err.println("[SERVER] Terminating connections...");
			state.getNetworkManager().terminateAllServerConnections();
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.err.println("[SERVER] Stopped Listener & Terminated connections");
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {
			openSocket();
		} catch(RuntimeException | IOException e1) {
			System.err.println("Exception in ServerListener of " + state);
			e1.printStackTrace();
			errorSetup = true;
			return;
		}

//		System.err.println("[ServerListener] Server initialization OK... now waiting for connections");

		if(state.announceServer() && state.announceHost() != null && !state.announceHost().isEmpty()) {
			System.err.println("Announcing server to ServerList. (initial)");
			new ServerSender(ServerSender.DEFAULT_HB, ServerSender.DEFAULT_HOST, ServerSender.DEFAULT_PORT, state.announceHost(), port);
		}

		listening = true;
		synchronized(this) {
			//notify threads that are waiting for the server to start
			this.notifyAll();
		}
		try {
			while(listening && !stopRequested) {
				try {
					if(serverSocket.isClosed()) {
						System.err.println("server socket is closed!");
					}

					serverSocket.setPerformancePreferences(0, 2, 1);
					serverSocket.setReceiveBufferSize(settings.getSocketReceiveBufferSize().getInt());

					Socket accept = serverSocket.accept();
					if(stopRequested) {
						break;
					}

//					System.err.println("[SERVERSOCKET] Connection made. starting new processor " + accept.getPort() + ", " + accept.getInetAddress() + "; local: " + accept.getLocalPort() + ", " + accept.getLocalAddress() + ", keepalive " + accept.getKeepAlive());
//					Log.fine("[SERVERSOCKET] Incoming connection: " + accept.getPort() + ", " + accept.getInetAddress() + " -> " + accept.getLocalPort() + ", " + accept.getLocalAddress());

					if(isSpam(accept.getInetAddress())) {
						System.err.println("[SERVERSOCKET] Closing connection to " + accept.getInetAddress() + " because of too many connection tries in a short period");
						accept.close();
						continue;
					}

					accept.setKeepAlive(true);
					accept.setTcpNoDelay(true);
					accept.setTrafficClass(24); //low delay + throughput

					accept.setSendBufferSize(settings.getSocketSendBufferSize().getInt());
					accept.setReceiveBufferSize(settings.getSocketReceiveBufferSize().getInt());
					accept.setSoTimeout(settings.getClientTimeout().getInt());
					ServerProcessor serverProcessor = new ServerProcessor(new DataProcessorSocket(new CommandSocket(accept), settings), state);

					state.getConnectionThreadPool().execute(serverProcessor);

					state.getNetworkManager().registerConnection(serverProcessor);

//					System.out.println("[SERVER] ListenerId["+serverProcessor+"] connection registered (TRunning: " + ServerProcessor.PROCESSOR_THREADS_RUNNING +") ");

				} catch(IOException e) {
					if(!stopRequested) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			listening = false;
			synchronized(this) {
				notifyAll();
			}
		}
	}

	/**
	 * Checks if a connection attempt is spam (x connection made from the same address over y milliseconds of time)
	 *
	 * @param inetAddress
	 * @return true if no spam attempt detected
	 */
	private boolean isSpam(InetAddress inetAddress) {

		String ip = inetAddress.toString().replace("/", "");
		if(settings.getSpamProtectionAttempts().getInt() <= 0 || spamExceptions.contains(ip)) {
			return false;
		}

		ObjectIterator<Entry<String, LongArrayList>> iterator = connectionHistory.entrySet().iterator();
		while(iterator.hasNext()) {
			LongArrayList value = iterator.next().getValue();
			if(value.isEmpty() || (System.currentTimeMillis() - value.getLong(value.size() - 1) > (long) settings.getSpamProtectionAttemptTimeoutMs().getInt())) {
				iterator.remove();
			}
		}

		LongArrayList l = connectionHistory.get(ip);
		if(l == null) {
			l = new LongArrayList();
			connectionHistory.put(ip, l);
		}
		l.add(System.currentTimeMillis());

		if(l.size() > settings.getSpamProtectionAttempts().getInt()) {
//			Log.warning("[SERVER] WARNING: "+this.state+"; "+this.settings.getClass().getSimpleName()+"; Spam Protect active against: " + ip + ": " + l.size() + " (reduced to 100 records in memory to avoid memory-leak attack) connection attepts in less then " + (long)settings.getSpamProtectionAttemptTimeoutMs().getInt() / 1000L + "sec");

			while(l.size() > 100) {
				l.removeLong(0);

			}
			//spam detected
			return true;
		}
		//no spam
		return false;
	}

	public int getPort() {
		return port;
	}

	public boolean isErrorSetup() {
		return errorSetup;
	}

	public boolean isStopRequested() {
		return stopRequested;
	}

	@Override
	public int getInboundPacketCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getOutboundPacketCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBytesPerSecond() {
		// TODO Auto-generated method stub
		return 0;
	}

}
