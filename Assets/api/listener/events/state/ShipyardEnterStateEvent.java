package api.listener.events.state;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.shipyard.orders.states.ShipyardState;

public class ShipyardEnterStateEvent extends Event {
    private ShipyardState enteredStatef;

    public ShipyardEnterStateEvent(ShipyardState enteredState){
        enteredStatef = enteredState;
    }

    public ShipyardState getEnteredState() {
        return enteredStatef;
    }
}
