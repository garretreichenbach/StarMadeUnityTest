package api.listener.events.controller.fleet;

import api.listener.events.Event;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetManager;

public class FleetEvent extends Event {
    private final FleetManager fleetManager;
    private final Fleet fleet;

    public FleetEvent(FleetManager fleetManager, Fleet fleet){

        this.fleetManager = fleetManager;
        this.fleet = fleet;
    }

    public FleetManager getFleetManager() {
        return fleetManager;
    }

    public Fleet getFleet() {
        return fleet;
    }
}
