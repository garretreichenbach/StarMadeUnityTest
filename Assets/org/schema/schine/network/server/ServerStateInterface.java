package org.schema.schine.network.server;

import java.io.IOException;

import org.schema.schine.auth.SessionCallback;
import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.commands.GameRequestCommandPackage;
import org.schema.schine.network.common.ConnectionStats;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.objects.NetworkObject;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The Interface ServerStateInterface.
 */
public interface ServerStateInterface extends StateInterface {


	public void executeAdminCommand(String serverPassword, String command, RegisteredClientInterface client);

	public boolean filterJoinMessages();

	public boolean flushPingImmediately();

	public String getAcceptingIP();

	/**
	 * Gets the clients.
	 *
	 * @return the clients
	 */
	public Int2ObjectMap<RegisteredClientOnServer> getClients();

	/**
	 * Gets the controller.
	 *
	 * @return the controller
	 */
	@Override
	public ServerControllerInterface getController();

	public int getMaxClients();

	public NetworkProcessor getProcessor(int client);

	public String getServerDesc();

	public String getServerName();

	public int getSocketBufferSize();

	public long getStartTime();

	public int getNextFreeObjectId();
	/**
	 * Sets the paused.
	 *
	 * @param pause the new paused
	 */
	public void setPaused(boolean pause);

	public boolean tcpNoDelay();

	public boolean useUDP();

	public SessionCallback getSessionCallBack(String uid, String login_code);

	public void addNTReceivedStatistics(RegisteredClientOnServer client,
	                                    int size, int lastCheck, int lastHeader, int lastCommand,
	                                    Object[] lastParameters, ObjectArrayList<NetworkObject> lastReceived);

	public int getNTSpamProtectTimeMs();

	public int getNTSpamProtectMaxAttempty();

	public String getNTSpamProtectException();

	public boolean isNTSpamCheckActive();

	public boolean announceServer();

	public String announceHost();

	public boolean checkUserAgent(byte userAgent, String playerName);

	public void onClientRegistered(RegisteredClientOnServer c);


	public ConnectionStats getConnectionStats();

	public void onClientDisconnected(RegisteredClientInterface client);

	public void handleGameRequestAnswer(NetworkProcessor p, GameRequestCommandPackage pack) throws IOException;

}
