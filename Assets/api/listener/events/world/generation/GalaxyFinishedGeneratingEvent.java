package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.game.server.data.Galaxy;


/**
 * STARMADE MOD
 * CREATOR: ITHIRAHAD
 * DATE: 1.09.2020
 * TIME: Who knows any more
 */
public class GalaxyFinishedGeneratingEvent extends Event {
    private Galaxy galaxy;
    /**
     * This event fires following the successful generation of a galaxy.
     */

    public GalaxyFinishedGeneratingEvent(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }
}
