package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentControllerHpController;


public class SystemsRebootingEvent extends Event {
    public final SegmentController segmentController;
    public final SegmentControllerHpController hpController;
    public final boolean fast;

    public SystemsRebootingEvent(SegmentController segmentController, SegmentControllerHpController hpController, boolean fast) {
        this.segmentController = segmentController;
        this.hpController = hpController;
        this.fast = fast;
    }
}
