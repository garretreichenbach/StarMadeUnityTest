package org.schema.schine.network.client;

import java.io.IOException;

import org.schema.common.LogUtil;
import org.schema.common.util.Version;
import org.schema.schine.auth.Session;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.input.InputController;
import org.schema.schine.network.LoginFailedException;
import org.schema.schine.network.NetUtil;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientState.LoginStateEnum;
import org.schema.schine.network.commands.GameRequestCommandPackage;
import org.schema.schine.network.commands.LoginRequestCommandPackage;
import org.schema.schine.network.commands.RequestSynchronizeAllCommandPackage;
import org.schema.schine.network.commands.SynchronizePrivateCommandPackage;
import org.schema.schine.network.commands.SynchronizePublicCommandPackage;
import org.schema.schine.network.commands.gamerequests.GameAnswerInterface;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.commands.gamerequests.ServerTimeAnswer;
import org.schema.schine.network.commands.gamerequests.ServerTimeRequest;
import org.schema.schine.network.common.commands.UnknownCommandException;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.network.synchronization.SynchronizationReceiver;
import org.schema.schine.network.synchronization.SynchronizationSender;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/**
 * this class had the strangest bug ever: java crashed with an ACCESS_VIOLATION
 * when this class inherited Thread. The solution was, to let this thread implement
 * Runnable.
 * TODO: write a bug report to Java
 *
 * @author Schema
 */
public abstract class ClientController implements ClientControllerInterface, InputController {
	public static GLFrame currentGLFrame;

	private static boolean created;
	
	private ClientToServerConnection connection;
	
	private ClientStateInterface state;
	private IntOpenHashSet delHelper = new IntOpenHashSet();
	private long lastSynchronize;
	private long lastSetSnapshop;

	public ClientController(ClientStateInterface state) {
		created = true;
		this.state = state;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (!GLFrame.isFinished()) {
						onShutDown();
						LogUtil.closeAll();
					}
				} catch (IOException e) {
					System.err
							.println("[ERROR] CLIENT SHUTDOWN. Failed to save ServerState!");
					e.printStackTrace();
				} catch (Exception e) {
					System.err
							.println("[ERROR] CLIENT SHUTDOWN. Failed to save ServerState!");
					e.printStackTrace();
				}
			}
		});
	}

	public static boolean hasGraphics(StateInterface state) {
		try {
			if (state.isPassive()) {
				return false;
			}
			return GraphicsContext.isInitialized();
		} catch (Exception e) {

		}
		return false;
	}

	public static boolean isCreated() {
		return created;
	}

	public static boolean isLocalHost() {
		return ServerState.isCreated();
	}

	public abstract void afterFullResynchronize();




	/* (non-Javadoc)
	 * @see org.schema.schine.network.ClientControllerInterface#handleBrokeConnection()
	 */
	@Override
	public void handleBrokeConnection() {
		connection.disconnect();
		System.err.println("[CLIENT] " + state + " CLIENT LOST CONNECTION -> BACK TO login SCREEN");

	}

	@Override
	public void login(String playerName, Version version, byte userAgent, Session session)
			throws IOException, InterruptedException, LoginFailedException {
	
		String uid = "";
		String login_code = "";

		if (session != null) {
			uid = session.getUniqueSessionId();
			login_code = session.getAuthTokenCode();

		}

		LoginRequestCommandPackage pack = new LoginRequestCommandPackage();
		pack.authCodeToken = login_code;
		pack.playerName = playerName;
		pack.uniqueSessionID = uid;
		pack.userAgent = userAgent;
		pack.version = version;
		pack.send(connection.getClientProcessor());
		
		setGuiConnectionState("logging in as " + playerName + "    (Version " + version + ")");
		System.out.println("[CLIENT] logging in now... " + playerName + "; " + uid + "; ");
		((ClientState)state).startedLoginTime = System.currentTimeMillis();
	}

	@Override
	public void logout(String reason) {
		System.err.println("logout received. exiting");
		kick(reason);
	}

	@Override
	public abstract void onShutDown() throws IOException;

	@Override
	public void requestSynchronizeAll() throws IOException {
		RequestSynchronizeAllCommandPackage pack = new RequestSynchronizeAllCommandPackage();
		pack.send(state.getProcessor());
		lastSynchronize = System.currentTimeMillis();

		
	}
	public void sendUnblockedRequest(GameRequestInterface request) throws IOException, UnknownCommandException {
		GameRequestCommandPackage pack = new GameRequestCommandPackage();
		pack.request = request;
		pack.send(getState().getProcessor());
	}
	public GameAnswerInterface sendBlockedRequest(GameRequestInterface request) throws IOException, UnknownCommandException {
		GameRequestCommandPackage pack = new GameRequestCommandPackage();
		pack.request = request;
		pack.send(state.getProcessor());
		return state.getNetworkManager().waitForRequestAnswer(pack.request.getFactory(), state.getUpdateSynch());
	}
	public ClientState getState() {
		return (ClientState)state;
	}
	@Override
	public void synchronize() throws IOException {

		SynchronizePublicCommandPackage pack = new SynchronizePublicCommandPackage();
		pack.prepareSending();
		
		
		
		
		final int returnCode = SynchronizationSender.encodeNetworkObjects(state.getLocalAndRemoteObjectContainer(), state,
						pack.out, false);
		if(returnCode == SynchronizationSender.RETURN_CODE_CHANGED_OBJECT) {
			pack.send(state.getProcessor());
		}else {
			pack.freeSending();
		}
		
		SynchronizationReceiver.handleDeleted(state.getLocalAndRemoteObjectContainer(), state, delHelper);
		
		
		synchronizePrivate();

		if (System.currentTimeMillis() - lastSetSnapshop > 1000) {

			state.getDataStatsManager().snapshotUpload(state.getSentData());

			lastSetSnapshop = System.currentTimeMillis();
		}
	}
	public void synchronizePrivate() throws IOException {

		SynchronizePrivateCommandPackage pack = new SynchronizePrivateCommandPackage();
		pack.prepareSending();

		final int returnCode = SynchronizationSender.encodeNetworkObjects(state.getPrivateLocalAndRemoteObjectContainer(), state,
				pack.out, false);
		
		if(returnCode == SynchronizationSender.RETURN_CODE_CHANGED_OBJECT) {
			pack.send(state.getProcessor());
		}else {
			pack.freeSending();
		}
			
		SynchronizationReceiver.handleDeleted(state.getPrivateLocalAndRemoteObjectContainer(), state, delHelper);
	}

	/**
	 * Connect.
	 *
	 * @param host      the host
	 * @param port      the port
	 * @param userAgent
	 * @throws IOException          Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 * @throws LoginFailedException
	 */
	public void connect(String host, int port, byte userAgent, String loginName, Session session) throws IOException, InterruptedException, LoginFailedException {
		synchronized(state) {
			state.setSynched();
			setGuiConnectionState("connecting to " + host + ":" + port);
			connection = new ClientToServerConnection(state, (ClientSettingsInterface) state.getSettings());
			connection.connect(host, port, 10);
	
			if (session != null) {
				session.setServerName(host + ":" + port);
			}
			login(loginName, state.getVersion(), userAgent, session);
			while(state.GetLoginState() == LoginStateEnum.NONE) {
				Thread.sleep(500);
				if(state.GetLoginState() == LoginStateEnum.NONE) {				
					System.out.println("[CLIENT] waiting for login to complete...");
				}
				if(state.GetLoginState() == LoginStateEnum.FAILED) {				
					System.err.println("[CLIENT] failed to login...");
					return;
				}
			}
			assert(state.GetLoginState() == LoginStateEnum.LOGGED_IN);
			System.out.println("[CLIENT] logged in as: " + state);
			onLogin();
			state.setUnsynched();
		}
	}

	public ClientToServerConnection getConnection() {
		return connection;
	}

	public void setConnection(ClientToServerConnection connection) {
		this.connection = connection;
	}

	protected abstract void onLogin() throws IOException, InterruptedException;

	protected abstract void onResynchRequest();

	public void requestServerTime() throws IOException, UnknownCommandException {
		//If the request takes more than 3 seconds, unblock it but log a warning
		ServerTimeRequest r = new ServerTimeRequest();
		boolean[] unblocked = {false};
		(new Thread(() -> {
			try {
				Thread.sleep(3000);
				if(!unblocked[0]) {
					System.err.println("[WARNING][CLIENT] ServerTimeRequest took more than 3 seconds to answer. This is not normal.");
					sendUnblockedRequest(r);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		})).start();
		ServerTimeAnswer a = (ServerTimeAnswer) sendBlockedRequest(r);
		state.setServerTimeOnLogin(a.serverTime);
		unblocked[0] = true;
	}

	public abstract void setGuiConnectionState(String state);

	

	public void updateSynchronization() throws IOException {
		if (!state.getProcessor().isConnected()) {
			if(state.isExitApplicationOnDisconnect()){
				System.err.println("[CLIENT] -------------------------------------------------------------");
				System.err.println("[CLIENT] ------ TERMINATING ----- EXIT: PROCESSOR DEAD ---------------");
				System.err.println("[CLIENT] -------------------------------------------------------------");
				try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
			}else{
//				System.err.println("[CLIENT] Not executing synchronize on state as it was disconnected");
				return;
			}
		}

		if (state.isNetworkSynchronized()) {
			if (lastSynchronize + NetUtil.UPDATE_RATE_CLIENT < System.currentTimeMillis()) {

				synchronize();


				/*
                 * execute all commands that are synchronized. the server may send a
				 * state update (e.g. objects transforms) while an ongoing update on
				 * client is taking place or in the drawing process. this method will
				 * execute all state updates, that came in since the last call.
				 */
				lastSynchronize = System.currentTimeMillis();
				//			AbstractScene.infoList.add("Synchronizing +"+lastSynchronize);
			}
		} 
		state.getDataStatsManager().update();
	}

	

	//	public void handleNotifiedChats(){
	//		if(!((ClientState)state).getChatsNotified().isEmpty()){
	//			synchronized(((ClientState)state).getChatsNotified()){
	//				while(!((ClientState)state).getChatsNotified().isEmpty()){
	//					ChatSystem c = ((ClientState)state).getChatsNotified().remove(0);
	//				}
	//			}
	//		}
	//	}
}
