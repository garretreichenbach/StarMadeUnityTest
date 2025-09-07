package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.client.view.gui.fleet.FleetScrollableListNew;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetManager;

import java.util.Collection;

public class GetAvailableFleetsEvent extends Event {

    private final FleetScrollableListNew fleetScrollableListNew;
    private final FleetManager fleetManager;
    private final Collection<Fleet> availableFleetsClient;

    public GetAvailableFleetsEvent(FleetScrollableListNew fleetScrollableListNew, FleetManager fleetManager, Collection<Fleet> availableFleetsClient) {

        this.fleetScrollableListNew = fleetScrollableListNew;
        this.fleetManager = fleetManager;
        this.availableFleetsClient = availableFleetsClient;
    }

    public FleetScrollableListNew getFleetScrollableListNew() {
        return fleetScrollableListNew;
    }

    public FleetManager getFleetManager() {
        return fleetManager;
    }

    public Collection<Fleet> getAvailableFleetsClient() {
        return availableFleetsClient;
    }
}
