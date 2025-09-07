package api.listener.events.player;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerCharacter;

public class PlayerSpawnEvent extends Event {
    private final Vector3i sector;
    private PlayerCharacter player;

    public PlayerSpawnEvent(Vector3i sector, PlayerCharacter player) {
        this.sector = sector;
        this.player = player;
    }

    public Vector3i getSector() {
        return sector;
    }

    public PlayerCharacter getPlayer() {
        return player;
    }
}
