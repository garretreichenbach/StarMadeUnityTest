package api.listener.events.world;

import api.listener.events.Event;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

/**
 * Created by Jake on 2/25/2021.
 * <insert description here>
 */
public class ServerSendableAddEvent extends Event {
    private final GameServerState serverState;
    private final Sendable sendable;
    private final Condition cond;

    public ServerSendableAddEvent(GameServerState serverState, Sendable sendable, Condition cond) {
        this.serverState = serverState;
        this.sendable = sendable;
        this.cond = cond;
    }

    public GameServerState getServerState() {
        return serverState;
    }

    public Sendable getSendable() {
        return sendable;
    }

    public Condition getCond() {
        return cond;
    }
}
