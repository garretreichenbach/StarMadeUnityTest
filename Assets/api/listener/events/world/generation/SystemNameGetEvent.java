package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.Galaxy;

/**
 * This class is intended for applications such as player-assigned star system names,
 * or appending changeable information to names before they are displayed.
 * It WILL override the name cache retrieval, and it WILL NOT write event-modified system names to the cache by default.
 * You can, of course, do so yourself if you so desire, and it will override the caching within the default name retrieval
 * process, including mods that subscribe to the SystemNameGenerationEvent.
 * Unless you're using a custom cache, this class is not intended for implementing custom name generation, and hence
 * does not supply the same types of information as SystemNameGenerationEvent (though of course the information can be
 * retrieved relatively easily).
 */
public class SystemNameGetEvent extends Event {
    private String name;
    private final Galaxy containingGalaxy;

    private final Vector3i position;

    public SystemNameGetEvent(Galaxy galaxy, String name, Vector3i pos) {
        position = new Vector3i(pos);
        containingGalaxy = galaxy;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Galaxy getContainingGalaxy() {
        return containingGalaxy;
    }

    public Vector3i getPosition() {
        return position;
    }
}
