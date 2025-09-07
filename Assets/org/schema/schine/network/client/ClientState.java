package org.schema.schine.network.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.schema.common.util.Version;
import org.schema.schine.graphicsengine.core.ChatListener;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.psys.ParticleSystemManager;
import org.schema.schine.network.DataStatsManager;
import org.schema.schine.network.NetworkStateContainer;
import org.schema.schine.network.NetworkStatus;
import org.schema.schine.network.commands.LoginAnswerCommandPackage;
import org.schema.schine.network.commands.LoginRequest.LoginCode;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.sound.controller.RemoteAudioEntry;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class ClientState implements ClientStateInterface {

	public static final Integer NEW_ID_RANGE = 100;
	public static boolean loginFailed;
	public static Version serverVersion;
	//	private final ArrayList<ChatSystem> chatsNotified = new ArrayList<ChatSystem>();
	
	public static boolean setFinishedFrameAfterLocalServerShutdown = false;
	private final ObjectArrayList<ChatListener> chatListeners = new ObjectArrayList<ChatListener>();
	private final NetworkStateContainer networkStateContainer;
	private final NetworkStateContainer privateNetworkStateContainer;

	private final static ThreadPoolExecutor theadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	static {
		theadPool.prestartAllCoreThreads();
	}
	private final ParticleSystemManager particleSystemManager = new ParticleSystemManager();
	private final Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> bytesSent = new Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap>();
	private final Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> bytesReceived = new Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap>();
	private final DataStatsManager dataStatsManager = new DataStatsManager();
	public Object updateLock = new Object();


	
	private long ping;
	private boolean ready;
	private boolean synchronizedFlag;
	private boolean synchronizing;
	private int id = -1;
	private NetworkStatus networkStatus;
	private boolean debugBigChunk;
	private long serverTimeOnLogin;
	private int serverTimeDifference;
	private short updateNumber;
	private boolean doNotDisplayIOException;
	private boolean exitApplicationOnDisconnect = false;
	private String extraLoginFailReason;
	private final boolean passive;
	private boolean tabbed;
	public long startedLoginTime;
	public LoginStateEnum loginState = LoginStateEnum.FAILED;
	public ClientState(boolean passive) {
		this.passive = passive;
		networkStateContainer = new NetworkStateContainer(false, this);
		privateNetworkStateContainer = new NetworkStateContainer(true, this);
		networkStatus = new NetworkStatus();
		
	}
	
	public enum LoginStateEnum{
		NONE,
		LOGGED_IN,
		FAILED
	}
	@Override
	public boolean isPassive(){
		return passive;
	}
	public abstract List<DialogInterface> getPlayerInputs();

	@Override
	public void setExtraLoginFailReason(String extraReason) {
		this.extraLoginFailReason = extraReason;
	}

	@Override
	public String getExtraLoginFailReason() {
		return extraLoginFailReason;
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
		return id;
	}

	@Override
	public NetworkStateContainer getLocalAndRemoteObjectContainer() {
		return networkStateContainer;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.StateInterface#getNetworkStatus()
	 */
	@Override
	public NetworkStatus getNetworkStatus() {
		return networkStatus;
	}

	/**
	 * @return the theadPool
	 */
	@Override
	public ThreadPoolExecutor getConnectionThreadPool() {
		return theadPool;
	}

	@Override
	public boolean isOnServer() {
		return false;
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
		return debugBigChunk && ClientProcessor.DEBUG_BIG_CHUNKS;
	}

	/**
	 * @return the ready
	 */
	@Override
	public boolean isReady() {
		return ready;
	}

	/**
	 * @param ready the ready to set
	 */
	public void setReady(boolean ready) {
		this.ready = ready;

	}

	@Override
	public void setId(int id) {
		this.id = id;

	}


	/**
	 * SERVER_TIME - OUR_TIME
	 * <p/>
	 * if server later -> positive
	 * if server earlier -> negative
	 * <p/>
	 * <p/>
	 * this.serverTimeDifference = (int) (timeServer - System.currentTimeMillis());
	 *
	 * @return the serverTimeDifference
	 */
	@Override
	public int getServerTimeDifference() {
		return serverTimeDifference;
	}

	/**
	 * @return the mouseEvents
	 */
	

	/* (non-Javadoc)
	 * @see org.schema.schine.network.ClientStateInterface#getPing()
	 */
	@Override
	public long getPing() {
		return ping;
	}

	@Override
	public void setPing(long l) {
		this.ping = l;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.StateInterface#getPrivateLocalAndRemoteObjectContainer()
	 */
	@Override
	public NetworkStateContainer getPrivateLocalAndRemoteObjectContainer() {
		return privateNetworkStateContainer;
	}

	@Override
	public ClientProcessor getProcessor() {
		return getController().getConnection().getClientProcessor();
	}


	@Override
	public boolean isNetworkSynchronized() {

		return synchronizedFlag;
	}

	/**
	 * @return the synchronizing
	 */
	@Override
	public boolean isSynchronizing() {
		return synchronizing;
	}


	/**
	 * @param synchronizing the synchronizing to set
	 */
	public void setSynchronizing(boolean synchronizing) {
		this.synchronizing = synchronizing;
	}


	public void disconnect() throws IOException {
		getProcessor().closeSocket();

	}

	@Override
	public abstract void exit();


	@Override
	public abstract List<Object> getGeneralChatLog();
	@Override
	public void handleLoginAnswer(LoginAnswerCommandPackage pack) {
		final int code = pack.idOrFailedReturnCode;
		Version version = pack.serverVersion;
		
		String extraReason = pack.failReson;
		
		
		serverVersion = version;
		extraLoginFailReason = extraReason;
		if (code < 0) {
			LoginCode lCode = LoginCode.getById(code);
			switch(lCode) {
				case ERROR_ALREADY_LOGGED_IN -> {
					System.err.println("[Client] [LOGIN]: ERROR: Already logged in " + extraReason);
					id = code;
				}
				case ERROR_AUTHENTICATION_FAILED -> {
					System.err.println("[Client] [LOGIN]: ERROR: Authentication Failed " + extraReason);
					id = code;
				}
				case ERROR_ACCESS_DENIED -> {
					System.err.println("[Client] [LOGIN]: ERROR: Access Denied " + extraReason);
					id = code;
				}
				case ERROR_GENRAL_ERROR -> {
					System.err.println("[Client] [LOGIN]: ERROR: General Error " + extraReason);
					id = code;
				}
				case ERROR_WRONG_CLIENT_VERSION -> {
					System.err.println("[Client] [LOGIN]: ERROR: The version of your client is not equal to the server. Try updating with the StarMade-Starter. (client version: " + getClientVersion() + "; server version: " + version + ") " + extraReason);
					id = code;
				}
				case ERROR_SERVER_FULL -> {
					System.err.println("[Client] [LOGIN]: ERROR: Server FULL Error " + extraReason);
					id = code;
				}
				case ERROR_YOU_ARE_BANNED -> {
					System.err.println("[Client] [LOGIN]: ERROR: You are banned from this server " + extraReason);
					id = code;
				}
				case ERROR_NOT_ON_WHITELIST -> {
					System.err.println("[Client] [LOGIN]: ERROR: You are not whitelisted on this server " + extraReason);
					id = code;
				}
				case ERROR_INVALID_USERNAME -> {
					System.err.println("[Client] [LOGIN]: ERROR: The username is not accepted " + extraReason);
					id = code;
				}
				case ERROR_AUTHENTICATION_FAILED_REQUIRED -> {
					System.err.println("[Client] [LOGIN]: ERROR: Authentication required but not delivered " + extraReason);
					id = code;
				}
				case ERROR_NOT_ADMIN -> {
					System.err.println("[Client] [LOGIN]: ERROR: Not admin " + extraReason);
					id = code;
				}
				default -> {
					assert (false) : "something went wrong: " + code;
					id = code;
					throw new IllegalArgumentException("Unknown login return code. Your client might be out of date. Please update!");
				}
			}
			loginState = LoginStateEnum.FAILED;
		} else {
			id = code;

			long ended = System.currentTimeMillis();
			long roundTripTime = ended - startedLoginTime;

			// set the server time as the time
			// returned by sever
			// plus half of one round trip time
			// to compensate for delay
			setServerTimeOnLogin(pack.loginTime + roundTripTime / 2);

			System.err.println("[Client] [LOGIN]: Client sucessfully registered with id: " + id +"; Time Difference: "+ serverTimeDifference +" (W/O RTT "+pack.loginTime+"); (RTT: "+roundTripTime+")");
			setPlayerName(pack.playerName);
			
			loginState = LoginStateEnum.LOGGED_IN;
		}		
	}


	/**
	 * @return the serverTimeOnLogin
	 */
	public long getServerTimeOnLogin() {
		return serverTimeOnLogin;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.ClientStateInterface#setServerTicksOnLogin(long)
	 */
	@Override
	public void setServerTimeOnLogin(long timeServer) {
		this.serverTimeOnLogin = timeServer;
		this.serverTimeDifference = (int) (timeServer - System.currentTimeMillis());
	}

	/**
	 * @param serverVersion the serverVersion to set
	 */
	@Override
	public void setServerVersion(Version serverVersion) {
		ClientState.serverVersion = serverVersion;
	}

	@Override
	public void setSynchronized(boolean synchronizedFlag) {
		this.synchronizedFlag = synchronizedFlag;
	}

	/**
	 * @return the doNotDisplayIOException
	 */
	@Override
	public boolean isDoNotDisplayIOException() {
		return doNotDisplayIOException;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.client.ClientStateInterface#setDoNotDisplayIOException(boolean)
	 */
	@Override
	public void setDoNotDisplayIOException(boolean b) {
		doNotDisplayIOException = b;
	}

	@Override
	public abstract List<Object> getVisibleChatLog();

	@Override
	public void println(String s) {

	}
	/* (non-Javadoc)
     * @see org.schema.schine.network.ClientStateInterface#getSynchronizedCommands()
	 */

	/**
	 * @param debugBigChunk the debugBigChunk to set
	 */
	public void setDebugBigChunk(boolean debugBigChunk) {
		this.debugBigChunk = debugBigChunk;
	}

	@Override
	public String toString() {
		return "Client(" + id + ")";
	}

	public ParticleSystemManager getParticleSystemManager() {
		return particleSystemManager;
	}

	public abstract Transform getCurrentPosition();


	/**
	 * @return the chatListeners
	 */
	public ObjectArrayList<ChatListener> getChatListeners() {
		return chatListeners;
	}
	@Override
	public boolean isExitApplicationOnDisconnect() {
		return exitApplicationOnDisconnect;
	}
	public void setExitApplicationOnDisconnect(boolean exitApplicationOnDisconnect) {
		this.exitApplicationOnDisconnect = exitApplicationOnDisconnect;
	}
	public abstract void startLocalServer();
	
	@Override
	public boolean isInTextBox() {
		return tabbed;
	}
	@Override
	public void setInTextBox(boolean t){
		this.tabbed = t;
	}
	public abstract boolean canPlayAudioEntry(RemoteAudioEntry remoteAudioEntry);
}
