package org.schema.schine.network.server;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.LogUtil;
import org.schema.common.ParseException;
import org.schema.common.util.Version;
import org.schema.game.server.data.simulation.resource.PassiveResourceManager;
import org.schema.game.server.data.simulation.resource.PassiveResourceProvider;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.NetUtil;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.ServerClientChangeListener;
import org.schema.schine.network.client.ClientController;
import org.schema.schine.network.commands.LoginRequest.LoginCode;
import org.schema.schine.network.commands.SynchronizePrivateCommandPackage;
import org.schema.schine.network.commands.SynchronizePublicCommandPackage;
import org.schema.schine.network.synchronization.SynchronizationReceiver;
import org.schema.schine.network.synchronization.SynchronizationSender;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public abstract class ServerController implements Runnable,
		ServerControllerInterface {

	public static int port = 4242;
	private final IntOpenHashSet delHelper = new IntOpenHashSet();
	private final ObjectArrayFIFOQueue<String> systemInQueue = new ObjectArrayFIFOQueue<String>();
	protected HashSet<Integer> toRemoveClients = new HashSet<Integer>();
	private ServerState serverState;
	private ServerListenerInterface serverListener;
	public static boolean debugLogoutOnShutdown;
	private Timer timer = new Timer();
	private long lastSetSnapshop;
	protected ServerActiveCheck serverActiveChecker;
	public final List<ServerClientChangeListener> clientChangeListeners = new ObjectArrayList<ServerClientChangeListener>();
	protected static SystemInListener systemInListener = new SystemInListener();

	static {
		Thread sysIn = new Thread(systemInListener, "SysInListener");
		sysIn.setDaemon(true);
		sysIn.start();
	}

	public ServerController(ServerState serverState) {
		super();

		systemInListener.setState(serverState);

		serverActiveChecker = new ServerActiveCheck();
		serverActiveChecker.start();

		this.serverState = serverState;
		serverState.setController(this);

		// Add a shutdownHook to the JVM
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if(!ServerState.isShutdown()) {
					System.err.println("WARNING: USING EMERGENCY SHUTDOWN HOOK");
					ServerState.setShutdown(true);
					System.err.println("WARNING: USING EMERGENCY SHUTDOWN HOOK: onShutDown");
//						synchronized(ServerController.this.serverState){
					onShutDown(true);
//						}
					System.err.println("WARNING: USING EMERGENCY SHUTDOWN HOOK: closing log");
//						LogUtil.closeAll();
					System.err.println("WARNING: USING EMERGENCY SHUTDOWN HOOK DONE");
				}

			} catch(IOException e) {
				System.err
						.println("[ERROR] SERVER SHUTDOWN. Failed to save ServerState!");
				e.printStackTrace();
			}
		}));
	}

	@Override
	public void broadcastMessage(Object[] message, byte type) {
		synchronized(serverState.getToBroadCastMessages()) {
			serverState.getToBroadCastMessages().add(new ServerMessage(message, type));
		}
	}

	@Override
	public boolean isListenting() {
		return serverListener.isListening();
	}

	@Override
	public LoginCode registerClient(RegisteredClientOnServer client, Version version, StringBuffer failReason) throws Exception {

		if(!version.equals(getServerState().getVersion())) {
			return LoginCode.ERROR_WRONG_CLIENT_VERSION;
		}

		boolean admin = isAdmin(client);
		if(!admin) {
			if(serverState.getClients().size() >= serverState.getMaxClients()) {
				failReason.append("(currently " + serverState.getClients().size() + "/" + serverState.getMaxClients() + ")");
				return LoginCode.ERROR_SERVER_FULL;
			}
			if(isBanned(client, failReason)) {
				System.err.println("[SERVER][LOGIN] Denying banned user: " + client);
				return LoginCode.ERROR_YOU_ARE_BANNED;
			}
			if(!isWhiteListed(client)) {
				System.err.println("[SERVER][LOGIN] Denying not white listed user: " + client);
				return LoginCode.ERROR_NOT_ON_WHITELIST;
			}
		}
		String name = client.getClientName();

		System.err.println("[SERVER] Client register setup phase 1 completed. Name: " + name + "; checking already logged in");

		for(RegisteredClientOnServer c : serverState.getClients().values()) {
			if(c.getClientName().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH))) {
				return LoginCode.ERROR_ALREADY_LOGGED_IN;
			}
		}
		LoginCode returnCode = onLoggedIn(client);
		if(returnCode == LoginCode.SUCCESS_LOGGED_IN) {
			getServerState().getClients().put(client.getId(), client);
		}
		for(ServerClientChangeListener l : clientChangeListeners) {
			l.onClientsChanged();
		}
		System.err.println("[SERVER] Client register setup phase 2 completed. Name: " + name + "; sending success to client. ClientID: " + returnCode);
		return returnCode;
	}

	@Override
	public void unregister(int id) {
		synchronized(toRemoveClients) {
			toRemoveClients.add(id);
		}
	}

	public void broadcastMessageAdmin(Object[] message, byte type) {
		synchronized(serverState.getToBroadCastMessages()) {
			serverState.getToBroadCastMessages().add(new ServerMessage(message, type, true));
		}

	}

	public void synchronizeClientFully(RegisteredClientOnServer r) throws IOException {
		long t = System.currentTimeMillis();
		System.err.println("[SERVER] Synchronizing all objects for " + r);
		SynchronizationSender.sendSynchAll(
				serverState, r.getProcessor());
		r.wasFullSynched = true;
		System.err.println("[SERVER] Synchronizing all objects for " + r + " FINISHED; time taken (ms): " + (System.currentTimeMillis() - t));
	}

	public abstract void createThreadDump();

	protected abstract void displayError(Exception e);

	/**
	 * @return the serverState
	 */
	public ServerState getServerState() {
		return serverState;
	}

	/**
	 * @param serverState the serverState to set
	 */
	public void setServerState(ServerState serverState) {
		this.serverState = serverState;
	}

	/**
	 * @return the systemInQueue
	 */
	public ObjectArrayFIFOQueue<String> getSystemInQueue() {
		return systemInQueue;
	}

	/**
	 * @return the timer
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * @param timer the timer to set
	 */
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public abstract void initializeServerState() throws IOException, SQLException, NoSuchAlgorithmException, ResourceException, ParseException, SAXException, ParserConfigurationException;

	protected abstract boolean isAdmin(RegisteredClientOnServer client);

	public boolean isBanned(RegisteredClientOnServer client, StringBuffer failReason) {
		return false;
	}

	public boolean isWhiteListed(RegisteredClientOnServer client) {
		return false;
	}

	public abstract LoginCode onLoggedIn(RegisteredClientOnServer client)
			throws Exception;

	public abstract void onLoggedout(RegisteredClientOnServer client);

	protected abstract void onShutDown(boolean emergency) throws IOException;    /*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Thread#run()
	 */

	@Override
	public void run() {
		try {
			Thread.sleep(100);
			timer.updateFPS(true);
			assert (!ServerState.isShutdown()) : "Shutdown before start";

			while(!ServerState.isShutdown()) {

				if(!serverState.isPaused()) {
					long time = System.currentTimeMillis();

					update(timer);

					long diff = Math.max(0, System.currentTimeMillis() - time);
					long s = NetUtil.UPDATE_RATE_SERVER - diff;
					if(s > 0 && s < 60 * 1000) {
						Thread.sleep(s);
					}
					ServerState.entityCount = getServerState().getLocalAndRemoteObjectContainer().getLocalObjectsSize();
				} else {
					Thread.sleep(500);
				}
				timer.updateFPS(true);
				//				 System.err.println("server delta: "+timer.getDelta());
			}
		} catch(RuntimeException e) {
			System.err.println("Exiting (normal) because of exception " + e);
			e.printStackTrace();
			displayError(e);

		} catch(Exception e) {
			System.err.println("Exiting (normal) because of exception " + e);
			e.printStackTrace();
			displayError(e);

		}

		try {
			System.out.println("Culling passive resource providers as the server is shutting down.");
			PassiveResourceManager.cull();
		} catch(IOException exception) {
			exception.printStackTrace();
			System.out.println("[SERVER][FATAL] Exception: Failed to cull passive resource providers due to IOException:\n" + exception.getMessage());
		}

		if(!ServerState.isShutdown()) {
			try {
				onShutDown(false);
				LogUtil.closeAll();
				ServerState.serverIsOkToShutdown = true;
				if(!ClientController.isCreated()) {
					System.err.println("[SERVER][FATAL] Exception: KILLING SERVER BECAUSE OF ERROR");
					try {
						throw new Exception("System.exit() called");
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					System.exit(0);
				} else {
					System.err.println("[SERVER] Client detected: SET CLIENT FINISHED");
					GLFrame.setFinished(true);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("[SERVER][SHUTDOWN] Server shutdown update thread finished!");
		}
	}

	/**
	 * Send logout.
	 *
	 * @param id the logged out client id
	 * @throws IOException
	 */
	public void sendLogout(int id, String reason) throws IOException {
		RegisteredClientOnServer registeredClientOnServer = serverState.getClients().get(id);
		if(registeredClientOnServer != null) {
			System.err.println("[SERVER] SENDING ACTIVE LOGOUT TO CLIENT " + registeredClientOnServer);
			serverState.getNetworkManager().sendLogout(registeredClientOnServer.getProcessor());

			registeredClientOnServer.getProcessor().disconnectAfterSent();
		}
	}

	protected void sendMessages(Collection<RegisteredClientOnServer> clients) {
		while(getServerState().getToBroadCastMessages().size() > 0) {

			ServerMessage s = getServerState().getToBroadCastMessages().remove(0);
			try {
				for(RegisteredClientOnServer r : clients) {
					if(!s.adminOnly || isAdmin(r)) {
						r.serverMessage(s);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void stopListening() {
		if(serverListener != null && serverListener.isListening()) {
			serverListener.stop();
		}
	}

	public void startServerAndListen() throws Exception {
		// this.start();

		serverListener = new ServerListenerSocket(serverState, (ServerSettingsInterface) serverState.getNetworkManager().getNetworkSettings());
		initializeServerState();
		serverListener.startServerListener(port, 100);
		Thread controlThread = new Thread(this, "ServerController");
		controlThread.start();

	}

	public void synchronize(Collection<RegisteredClientOnServer> clientCopy) throws IOException {

		SynchronizePublicCommandPackage pack = new SynchronizePublicCommandPackage();
		pack.prepareSending(true);

		/*
		 * if there were clients, that have been re-synchronized, they now can
		 * be included in the regular synchronization cycle, because they should
		 * get any change in between their update and the following update
		 */
		final int returnCode = SynchronizationSender.encodeNetworkObjects(
				getServerState().getLocalAndRemoteObjectContainer(),
				serverState, pack.out, false);

		// write header for each client
		for(RegisteredClientOnServer r : clientCopy) {
			if(r.wasFullSynched) {
				r.wasFullSynched = false;
				//do not send update for clients that just
				//have been resynched
				//						continue;
			}

			if(!r.getProcessor().isConnected()) {
				System.err.println("[SERVER] Disconnect: Processor of " + r + " is dead: Removing client " + r.getId());
				toRemoveClients.add(r.getId());
				continue;
			}
			SynchronizePrivateCommandPackage privatePack = synchronizePrivate(r);
			if(privatePack != null) {
				privatePack.send(r.getProcessor());
			}

			if(returnCode == SynchronizationSender.RETURN_CODE_CHANGED_OBJECT) {
				pack.send(r.getProcessor());
			}
		}
		pack.freeSending();
		if(System.currentTimeMillis() - lastSetSnapshop > 1000) {

			serverState.getDataStatsManager().snapshotUpload(serverState.getSentData());

			lastSetSnapshop = System.currentTimeMillis();
		}
		if(returnCode == SynchronizationSender.RETURN_CODE_CHANGED_OBJECT) {
			SynchronizationReceiver.handleDeleted(
					serverState.getLocalAndRemoteObjectContainer(),
					serverState, delHelper);
		}

		getServerState().getLocalAndRemoteObjectContainer().checkGhostObjects();

		serverState.getDataStatsManager().update();
	}

	public SynchronizePrivateCommandPackage synchronizePrivate(RegisteredClientOnServer r) throws IOException {
		// write header for each client

		try {
			SynchronizePrivateCommandPackage pack = new SynchronizePrivateCommandPackage();
			pack.prepareSending();

			final int returnCode;

			returnCode = SynchronizationSender.encodeNetworkObjects(
					r.getLocalAndRemoteObjectContainer(), serverState,
					pack.out, false);
			if(returnCode == SynchronizationSender.RETURN_CODE_CHANGED_OBJECT) {

				if(!r.getProcessor().isConnected()) {
					pack.freeSending();
					return null;
				}
				return pack;
			} else {
				pack.freeSending();
				return null;
			}
		} finally {
			SynchronizationReceiver.handleDeleted(
					r.getLocalAndRemoteObjectContainer(), serverState, delHelper);

		}
	}

	public abstract boolean isUserProtectionAuthenticated(String playerName,
	                                                      String starmadeUsername);

	public class ServerActiveCheck extends Thread {
		private final static int waiting = 3000;
		private int serverOnUpdate = 0;
		private short lastUpdate = 0;
		private boolean shutdown;

		public ServerActiveCheck() {
			super("ServerActiveCheck");
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while(serverState == null) {
				try {
					Thread.sleep(waiting);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			while(!shutdown) {
				if(lastUpdate != serverState.getNumberOfUpdate()) {
					serverOnUpdate = 0;
					lastUpdate = serverState.getNumberOfUpdate();
				} else {
					serverOnUpdate++;
				}

				if(serverOnUpdate >= 5 && serverOnUpdate % 5 == 0) {
					try {
						throw new ThreadDeath();
					} catch(Exception e) {
						e.printStackTrace();
					}
					System.err.println("[SERVER] Exception Active checker: server not responding over " + (serverOnUpdate * waiting) / 1000 + " seconds");
					if(serverOnUpdate % 5 == 0) {
						createThreadDump();
					}
				}

				try {
					Thread.sleep(waiting);
				} catch(InterruptedException e) {
				}
			}

		}

		public void shutdown() {
			shutdown = true;
			interrupt();
		}

	}

}