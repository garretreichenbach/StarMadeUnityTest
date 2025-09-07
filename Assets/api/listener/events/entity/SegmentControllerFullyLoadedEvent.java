package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;

/**
 * Created by Jake on 11/19/2020.
 * <insert description here>
 */
public class SegmentControllerFullyLoadedEvent extends Event {
    private SegmentController controller;

    public SegmentControllerFullyLoadedEvent(SegmentController controller) {
        this.controller = controller;
    }

    public SegmentController getController() {
        return controller;
    }
}
