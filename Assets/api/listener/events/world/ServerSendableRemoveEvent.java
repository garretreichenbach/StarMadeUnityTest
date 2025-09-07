package api.listener.events.world;

import api.listener.events.Event;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

/**
 * Created by Jake on 2/25/2021.
 * Called when a sendable is removed
 */
public class ServerSendableRemoveEvent extends Event {
    private final GameServerState serverState;
    private final Sendable sendable;

    public ServerSendableRemoveEvent(GameServerState serverState, Sendable sendable, Condition cond) {
        this.serverState = serverState;
        this.sendable = sendable;
        this.condition = cond;
    }

    public GameServerState getServerState() {
        return serverState;
    }

    public Sendable getSendable() {
        return sendable;
    }


}
