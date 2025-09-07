package org.schema.schine.network.server;

import api.listener.events.state.ServerStopEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.network.*;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ServerState implements ServerStateInterface {

	public static int entityCount;
	private static boolean shutdown;
	private static boolean flagShutdown;
	public static boolean serverIsOkToShutdown = true;
	private static boolean created;
	private final Int2ObjectMap<RegisteredClientOnServer> clients;
	private final Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> bytesSent = new Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap>();
	private final Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> bytesReceived = new Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap>();
	private final DataStatsManager dataStatsManager = new DataStatsManager();
	private final List<ServerMessage> toBroadCastMessages = new ObjectArrayList<ServerMessage>();
	private final ThreadPoolExecutor theadPoolLogins;
	private final ThreadPoolExecutor theadPoolExplosions;
	
	
	private final ServerEntityWriterThread threadQueue = new ServerEntityWriterThread();
	private final NetworkStateContainer stateContainer;
	private ServerControllerInterface controller;
	private boolean paused;
	private NetworkStatus networkStatus;
	private short updateNumber;
	private boolean debugBigChunk;


	public abstract void doDatabaseInsert(Sendable sendable);
	@Override
	public boolean isOnServer() {
		return true;
	}
	public ServerState() {
		serverIsOkToShutdown = false;
		created = true;
		stateContainer = new NetworkStateContainer(false, this);
		networkStatus = new NetworkStatus();
		
		theadPoolLogins = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		theadPoolExplosions = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		
		//"preheat" the pools so they don't cause freezes from allocating memory mid-game
		for(int i = 0; i < 20; i++) {
			theadPoolLogins.execute(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			theadPoolExplosions.execute(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
		
		clients = new Int2ObjectOpenHashMap<RegisteredClientOnServer>();
		threadQueue.start();
		
		
		
		System.err.println("[SERVER] Server State Created!");
	}
	public static void clearStatic() {
		if(!GraphicsContext.isFinished()){
			//only reset when actual exit. else the hook will do another state save
			created = false;
			shutdown = false;
		}
		serverIsOkToShutdown = true;
		entityCount = 0;
	}
	public static boolean isCreated() {
		return created;
	}

	@Override
	public Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> getSentData() {
		return bytesSent;
	}

	@Override
	public Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> getReceivedData() {
		return bytesReceived;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.StateInterface#getDataStatsManager()
	 */
	@Override
	public DataStatsManager getDataStatsManager() {
		return dataStatsManager;
	}

	@Override
	public int getId() {
		//server state has no ID
		return IdGen.SERVER_ID;
	}

	@Override
	public NetworkStateContainer getLocalAndRemoteObjectContainer() {
		return stateContainer;
	}

	@Override
	public NetworkStatus getNetworkStatus() {
		return networkStatus;
	}

	@Override
	public int getNextFreeObjectId() {
		return IdGen.getNextId();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.StateInterface#getServerTimeDifference()
	 */
	@Override
	public int getServerTimeDifference() {
		return 0; //no difference here obviously
	}


	@Override
	public ThreadPoolExecutor getConnectionThreadPool() {
		return theadPoolLogins;
	}
	@Override
	public short getNumberOfUpdate() {
		return updateNumber;
	}

	@Override
	public void incUpdateNumber() {
		updateNumber++;
	}

	/**
	 * @return the debugBigChunk
	 */
	@Override
	public boolean isReadingBigChunk() {
		return debugBigChunk;
	}

	@Override
	public boolean isReady() {
		return controller.isListenting();
	}



	/**
	 * @return the clients
	 */
	@Override
	public Int2ObjectMap<RegisteredClientOnServer> getClients() {
		return clients;
	}

	/**
	 * @return the controller
	 */
	@Override
	public ServerControllerInterface getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(ServerControllerInterface controller) {
		this.controller = controller;
	}

	public abstract int getClientIdByName(String name) throws ClientIdNotFoundException;


	/**
	 * @return the threadQueue
	 */
	public ServerEntityWriterThread getThreadQueue() {
		return threadQueue;
	}

	/**
	 * @return the toBroadCastMessages
	 */
	public List<ServerMessage> getToBroadCastMessages() {
		return toBroadCastMessages;
	}

	@Override
	public boolean isPassive(){
		return true;
	}
	public boolean isPaused() {

		return paused;
	}

	@Override
	public void setPaused(boolean pause) {
		this.paused = pause;

	}

	@Override
	public String toString() {
		return "Server" + "(" + getId() + ")";
	}

	public abstract String getBuild();

	@Override
	public void exit() {
		
	}
	public ThreadPoolExecutor getTheadPoolExplosions() {
		return theadPoolExplosions;
	}
	public static boolean isFlagShutdown() {
		return flagShutdown;
	}
	public static void setFlagShutdown(boolean flagShutdown) {
		ServerStopEvent event = new ServerStopEvent();
		StarLoader.fireEvent(event, true);
		if(event.isCanceled()) return;

//		try{
//			throw new NullPointerException("SERVER SHUDOWN FLAG "+flagShutdown);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		ServerState.flagShutdown = flagShutdown;
	}
	public static boolean isShutdown() {
		return shutdown;
	}
	public static void setShutdown(boolean shutdown) {

		ServerState.shutdown = shutdown;
	}
	public static void setCreated(boolean created) {
		ServerState.created = created;
	}
	public abstract byte[] getBlockBehaviorFile();
	public abstract byte[] getBlockConfigFile();
	public abstract byte[] getBlockPropertiesFile();
	public abstract byte[] getCustomTexturesFile();

	
	
	
}
