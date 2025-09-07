package api.listener.events.controller;

import api.DebugFile;
import api.listener.events.Event;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;

/**
 * ADD DOCU
 * -when is it called
 * used to trigger the starmods "onClientCreated" method
 */
public class ClientInitializeEvent extends Event {
    private final GameClientController controller;
    private final GameClientState clientState;

    public ClientInitializeEvent(GameClientController controller, GameClientState clientState) {
        DebugFile.log("serverInitializeEvent created");
        this.controller = controller;
        this.clientState = clientState;
    }

    public GameClientController getController() {
        return controller;
    }

    public GameClientState getClientState() {
        return clientState;
    }
}
