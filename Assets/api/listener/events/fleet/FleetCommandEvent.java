package api.listener.events.fleet;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.network.objects.remote.FleetCommand;

import javax.annotation.Nullable;

public class FleetCommandEvent extends Event {


    private final FleetManager fleetManager;
    private final FleetCommand command;
    private final FleetCommandTypes type;
    private final Fleet fleet;

    public FleetCommandEvent(FleetManager fleetManager, FleetCommand command, FleetCommandTypes type, Fleet fleet) {
        this.fleetManager = fleetManager;
        this.command = command;
        this.type = type;
        this.fleet = fleet;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public FleetCommandTypes getCommandType() {
        return type;
    }

    public FleetCommand getCommand() {
        return command;
    }

    /**
     * Gets the destination of a fleet command
     */
    @Nullable
    public Vector3i getDestination() {
        Object arg = command.getArgs()[0];
        if(arg == null) return null;
        return (Vector3i) arg;
    }
}
