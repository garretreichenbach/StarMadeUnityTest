package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.Galaxy;

/**
 * STARMADE MOD
 * CREATOR: ITHIRAHAD
 * DATE: Wish I had one
 * TIME: Good
 *
 * An event which fires every time the universe tries to retrieve a galaxy that does not yet exist, prompting the construction of a new one.
 * This allows you to substitute a galaxy of your own, if you so choose.
 * Note that even if
 */

public class GalaxyInstantiateEvent extends Event {
    private Galaxy galaxy;
    private final Vector3i pos;

    public GalaxyInstantiateEvent(Galaxy galaxy, Vector3i pos) {
        this.galaxy = galaxy;
        this.pos = pos;
    }

    /**
     * @return the newly-created Galaxy.
     */
    public Galaxy getGalaxy() {
        return galaxy;
    }

    /**
     * @param galaxy assign a Galaxy to the position getPosition.
     */
    public void setGalaxy(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public Vector3i getPosition() {
        return new Vector3i(pos);
    }
}
