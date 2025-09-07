package api.listener.events.world.sector;

import api.listener.events.Event;
import org.schema.game.common.controller.SendableSegmentController;

/**
 * Created by Jake on 11/21/2020.
 * <insert description here>
 */
public class SegmentControllerUnloadEvent extends Event {
    private SendableSegmentController controller;

    public SegmentControllerUnloadEvent(SendableSegmentController controller) {
        this.controller = controller;
    }

    public SendableSegmentController getController() {
        return controller;
    }

}
