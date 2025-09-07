package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

public class SegmentControllerSpawnEvent extends Event {
    private final Vector3i sector;
    SegmentController controller;

    public SegmentControllerSpawnEvent(Vector3i sector, SegmentController entity) {
        this.sector = sector;
        controller = entity;
    }

    public Vector3i getSector() {
        return sector;
    }

    public SegmentController getController() {
        return controller;
    }

}
