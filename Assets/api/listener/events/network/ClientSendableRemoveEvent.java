package api.listener.events.network;

import api.listener.events.Event;
import org.schema.game.client.controller.GameClientController;
import org.schema.schine.network.objects.Sendable;

/**
 * Created by Jake on 2/22/2021.
 * <insert description here>
 */
public class ClientSendableRemoveEvent extends Event {
    private final GameClientController controller;
    private final Sendable sendable;
    private final Condition condition;

    public ClientSendableRemoveEvent(GameClientController controller, Sendable sendable, Condition condition){

        this.controller = controller;
        this.sendable = sendable;
        this.condition = condition;
    }


    public GameClientController getController() {
        return controller;
    }

    public Sendable getSendable() {
        return sendable;
    }

}
