package api.listener.events.world;

import api.listener.events.Event;
import org.schema.game.common.data.world.Universe;

/**
 * Event for when the world is saved to disk. Is fired twice, once before and once after.
 * @author lupoCani
 */
public class WorldSaveEvent extends Event {
    public final boolean terminate; //A parameter passed to the save method.
    public final boolean clear;     //A parameter passed to the save method.
    public final Condition condition;      //Whether the event is fired before (false) or after (true) a save.
    public final Universe universe; //The universe object the save method is invoked with.

    public WorldSaveEvent(boolean terminate, boolean clear, Universe universe, Condition condition) {
        this.terminate = terminate;
        this.clear = clear;
        this.universe = universe;
        this.condition = condition;
    }
}
