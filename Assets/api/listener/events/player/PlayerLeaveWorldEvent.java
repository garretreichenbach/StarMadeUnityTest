package api.listener.events.player;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;

/**
 * PlayerLeaveWorldEvent.java
 * Called when a player leaves a world / server.
 * Note: This event uses the player's name rather than the actual PlayerState to prevent nullpointer errors.
 *
 * @since 03/12/2021
 * @author TheDerpGamer
 */
public class PlayerLeaveWorldEvent extends Event {

    private String playerName;
    private int factionId;
    private Vector3i sector;

    public PlayerLeaveWorldEvent(String playerName, int factionId, Vector3i sector) {
        this.playerName = playerName;
        this.factionId = factionId;
        this.sector = sector;
    }

    /**
     * Gets the name of the player that left.
     * @return The player's name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the player's faction id. Returns 0 if the player wasn't in a faction.
     * @return The player's faction id
     */
    public int getFactionId() {
        return factionId;
    }

    /**
     * Gets the sector the player logged out from.
     * @return The player's last sector coordinates
     */
    public Vector3i getSector() {
        return sector;
    }
}
