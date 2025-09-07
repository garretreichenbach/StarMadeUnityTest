package api.listener.events.fleet;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.fleet.Fleet;

public class FleetLoadSectorEvent extends Event {

    private Fleet fleet;
    private Vector3i newPosition;
    private Vector3i oldPosition;

    public FleetLoadSectorEvent(Fleet fleet, Vector3i newPosition, Vector3i oldPosition) {
        this.fleet = fleet;
        this.newPosition = newPosition;
        this.oldPosition = oldPosition;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public Vector3i getNewPosition() {
        return newPosition;
    }

    public Vector3i getOldPosition() {
        return oldPosition;
    }
}
