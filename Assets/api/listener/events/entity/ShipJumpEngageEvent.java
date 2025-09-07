package api.listener.events.entity;

import api.listener.events.Event;
import api.listener.type.ServerEvent;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;


@ServerEvent
public class ShipJumpEngageEvent extends Event {
    private final SegmentController controller;
    private Vector3i originalSector;
    private Vector3i newSector;

    public ShipJumpEngageEvent(SegmentController controller, Vector3i originalSector, Vector3i newSector){
        this.controller = controller;
        this.originalSector = originalSector;
        this.newSector = newSector;
    }

    public SegmentController getController() {
        return controller;
    }

    public Vector3i getOriginalSectorPos() {
        return originalSector;
    }

    public Vector3i getNewSector() {
        return newSector;
    }
}
