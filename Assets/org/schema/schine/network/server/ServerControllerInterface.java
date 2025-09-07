package org.schema.schine.network.server;

import java.io.IOException;

import org.schema.common.util.Version;
import org.schema.schine.auth.SessionCallback;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.ControllerInterface;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.commands.LoginRequest.LoginCode;

public interface ServerControllerInterface extends ControllerInterface {

	public boolean authenticate(String playerName, SessionCallback sessionCallback) throws AuthenticationRequiredException;

	public void broadcastMessage(Object[] message, byte type);

	public boolean isListenting();

	public void protectUserName(String playerName, SessionCallback sessionCallback);

	LoginCode registerClient(RegisteredClientOnServer c, Version version, StringBuffer failReason) throws Exception;

	public void unregister(int id);

	void update(Timer timer) throws IOException, Exception;

}
