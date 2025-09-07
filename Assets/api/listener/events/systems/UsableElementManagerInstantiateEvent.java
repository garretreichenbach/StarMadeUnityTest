package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.UsableElementManager;

/** by lupoCani on 2021-01-24
 * To be fired whenever a new UsableElementManager is constructed. Fires in the the super-constructor.
 * */

public class UsableElementManagerInstantiateEvent extends Event{
    public final UsableElementManager<?,?,?> elementManager;
    public final SegmentController segmentController;

    public UsableElementManagerInstantiateEvent(UsableElementManager<?,?,?> elementManager, SegmentController segmentController) {
        this.elementManager = elementManager;
        this.segmentController = segmentController;
    }
}