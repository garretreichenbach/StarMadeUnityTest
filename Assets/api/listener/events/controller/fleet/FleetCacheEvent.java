package api.listener.events.controller.fleet;

import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.Fleet;

public class FleetCacheEvent extends FleetEvent{
    public FleetCacheEvent(FleetManager fleetManager, Fleet fleet) {
        super(fleetManager, fleet);
    }
}
