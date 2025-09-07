package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;

/**
 * Event fires when the constructor of SegmentController.class is called.
 * Use it for detecting new created SegmentControllers: Shipcreation, loading in ships etc.
 *
 * @author IR0NSIGHT
 */
public class SegmentControllerInstantiateEvent extends Event {
    private final SegmentController controller;
    /**
     * @param entity SegmentController
     */
    public SegmentControllerInstantiateEvent(/*Vector3i sector, */SegmentController entity) {
        controller = entity;
    }

    /**
     * @return SegmentController that was created
     */
    public SegmentController getController() {
        return controller;
    }

}



