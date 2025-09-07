package api.listener.events.controller;

import api.DebugFile;
import api.listener.events.Event;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.data.GameServerState;

/**
 * Called right after server is assigned.
 */
public class ServerInitializeEvent extends Event {
    private final GameServerController controller;
    private final GameServerState serverState;

    public ServerInitializeEvent(GameServerController controller, GameServerState serverState){
        DebugFile.log("serverIntiitalizeEvent created");
        this.controller = controller;
        this.serverState = serverState;
    }

    public GameServerController getController() {
        return controller;
    }

    public GameServerState getServerState() {
        return serverState;
    }
}
