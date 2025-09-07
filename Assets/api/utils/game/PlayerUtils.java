package api.utils.game;

import api.common.GameClient;
import api.common.GameServer;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.RegisteredClientOnServer;

import java.io.IOException;
import java.util.Set;

public class PlayerUtils {

    public static void sendMessage(PlayerState player, String message) {
        if(GameServer.getServerState() != null) {
            RegisteredClientOnServer registeredClientOnServer = GameServer.getServerState().getClients().get(player.getClientId());
            try {
                registeredClientOnServer.serverMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            GameClient.sendMessage(message);
        }
    }

    public static PlayerControllable getCurrentControl(PlayerState state){
        Set<ControllerStateUnit> units = state.getControllerState().getUnits();
        if(units.isEmpty()) return null;
        ControllerStateUnit unit = units.iterator().next();
        return unit.playerControllable;
    }
}
