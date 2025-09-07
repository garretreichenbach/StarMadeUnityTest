package api.listener.events.player;

import api.listener.events.Event;
import org.schema.game.common.data.player.PlayerState;

/**
 * Created by Jake on 9/27/2021.
 * <insert description here>
 */
public class PlayerChangeSectorEvent extends Event {
    private PlayerState state;
    private final int oldSectorId;
    private final int newSectorId;

    public PlayerChangeSectorEvent(PlayerState state, int oldSectorId, int newSectorId) {
        this.state = state;
        this.oldSectorId = oldSectorId;
        this.newSectorId = newSectorId;
    }

    public PlayerState getPlayerState() {
        return state;
    }

    public int getOldSectorId() {
        return oldSectorId;
    }

    public int getNewSectorId() {
        return newSectorId;
    }
}
