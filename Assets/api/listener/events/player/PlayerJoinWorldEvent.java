package api.listener.events.player;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.RegisteredClientOnServer;

/**
 * PlayerJoinWorldEvent.java
 * Called when a player joins a world / server.
 * Note: This event uses the player's name rather than the actual PlayerState to prevent nullpointer errors.
 *
 * @since 03/12/2021
 * @author TheDerpGamer
 */
public class PlayerJoinWorldEvent extends Event {

    private final RegisteredClientOnServer client;
    private final PlayerState playerState;

    public PlayerJoinWorldEvent(RegisteredClientOnServer client, PlayerState playerState) {
        this.client = client;
        this.playerState = playerState;
    }

    // ==================== 3 useless getter methods on playerstate
    /**
     * Gets the name of the player that joined.
     * @return The player's name
     */
    @Deprecated
    public String getPlayerName() {
        return playerState.getName();
    }

    /**
     * Gets the player's faction id. Returns 0 if the player isn't in a faction.
     * @return The player's faction id
     */
    @Deprecated
    public int getFactionId() {
        return playerState.getFactionId();
    }

    /**
     * Gets the sector the player loaded into.
     * @return The player's sector coordinates
     */
    @Deprecated
    public Vector3i getSector() {
        return playerState.getCurrentSector();
    }

    // =======================================
    /**
     * Gets the joined player
     */
    public PlayerState getPlayerState() {
        return playerState;
    }

    /**
     * The client connection
     */
    public RegisteredClientOnServer getClient() {
        return client;
    }
}
