package org.schema.schine.network.client;

import org.schema.common.util.Version;
import org.schema.schine.graphicsengine.GraphicsStateInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.network.NetworkStateContainer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientState.LoginStateEnum;
import org.schema.schine.network.commands.GameRequestAnswerCommandPackage;
import org.schema.schine.network.commands.LoginAnswerCommandPackage;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.common.UpdateSynch;
import org.schema.schine.network.server.ServerMessage;

public interface ClientStateInterface extends StateInterface, GraphicsStateInterface, InputState {

	public ClientControllerInterface getController();

	public int getServerTimeDifference();

	long getPing();

	void setPing(long l);

	String getPlayerName();

	void setPlayerName(String playerName);

	public NetworkStateContainer getPrivateLocalAndRemoteObjectContainer();

	public NetworkProcessor getProcessor();

	public boolean isNetworkSynchronized();

	boolean isSynchronizing();

	void message(ServerMessage msg);

	void setId(int id);

	void setServerTimeOnLogin(long ticks);

	void setServerVersion(Version version);

	public void setSynchronized(boolean synchronizedFlag);

	boolean isDoNotDisplayIOException();

	public boolean isExitApplicationOnDisconnect();

	void setDoNotDisplayIOException(boolean b);

	public void stopClient();

	public void startClient(HostPortLoginName hostPortLogin, boolean b);

	public Version getClientVersion();

	public boolean isAdmin();

	public void setExtraLoginFailReason(String extraReason);

	public String getExtraLoginFailReason();

	boolean isDebugKeyDown();

	void exit();

	public void handleExceptionGraphically(Exception e);

	public void handleLoginAnswer(LoginAnswerCommandPackage pack);

	public void handleGameRequestAnswer(GameRequestAnswerCommandPackage pack);

	public void onClientDisconnected();

	public UpdateSynch getUpdateSynch();

	public LoginStateEnum GetLoginState();

}
