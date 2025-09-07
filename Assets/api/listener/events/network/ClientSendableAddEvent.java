package api.listener.events.network;

import api.listener.events.Event;
import org.schema.game.client.controller.GameClientController;
import org.schema.schine.network.objects.Sendable;

/**
 * Created by Jake on 2/22/2021.
 * Called whenever the client recieves pretty much anything from the server (ie. a Sendable)
 */
public class ClientSendableAddEvent extends Event {
    private final GameClientController controller;
    private final Sendable sendable;

    public ClientSendableAddEvent(GameClientController controller, Sendable sendable, Condition condition){
        this.condition = condition;
        this.controller = controller;
        this.sendable = sendable;
    }

    public GameClientController getController() {
        return controller;
    }

    public Sendable getSendable() {
        return sendable;
    }
}
