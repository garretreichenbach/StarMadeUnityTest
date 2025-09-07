package api.listener.events.entity.rail;

import api.listener.events.Event;
import org.schema.game.common.controller.rails.RailController;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Created by Jake on 2021-10-01
 * Called when a SegCon shoots out an entity
 */

public class RailShootoutEvent extends Event {
    private final RailController railController;
    private final Timer timer;

    public RailShootoutEvent(RailController railController, Timer timer) {
        this.railController = railController;
        this.timer = timer;
    }

    public RailController getRailController() {
        return railController;
    }

    public Timer getTimer() {
        return timer;
    }
}
