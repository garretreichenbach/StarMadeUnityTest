package api.listener.events.network;

import api.listener.events.Event;
import api.listener.type.ServerEvent;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.commands.LoginRequest;
import org.schema.schine.network.server.ServerProcessor;

/**
 * Created by Jake on 10/24/2020.
 * Called when a serverprocessor is registered for a client
 */
@ServerEvent
public class ClientLoginEvent extends Event {
    private final LoginRequest loginRequest;
    private final int returnCode;
    private final boolean authd;
    private final String version;
    private final ServerProcessor serverProcessor;
    private final RegisteredClientOnServer registeredClientOnServer;
    private final String playerName;
    public ClientLoginEvent(LoginRequest loginRequest, int returnCode, boolean authd, String version, ServerProcessor serverProcessor, RegisteredClientOnServer registeredClientOnServer, String playerName) {

        this.loginRequest = loginRequest;
        this.returnCode = returnCode;
        this.authd = authd;
        this.version = version;
        this.serverProcessor = serverProcessor;
        this.registeredClientOnServer = registeredClientOnServer;
        this.playerName = playerName;
    }

    public LoginRequest getLoginRequest() {
        return loginRequest;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public boolean isAuthd() {
        return authd;
    }

    public String getVersion() {
        return version;
    }

    public ServerProcessor getServerProcessor() {
        return serverProcessor;
    }

    public RegisteredClientOnServer getRegisteredClientOnServer() {
        return registeredClientOnServer;
    }

    public String getPlayerName() {
        return playerName;
    }
}
