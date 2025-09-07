package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.power.reactor.PowerImplementation;

public class ReactorRecalibrateEvent extends Event {
    private PowerImplementation implementationf;

    public ReactorRecalibrateEvent(PowerImplementation implementation) {
        implementationf = implementation;
    }

    public PowerImplementation getImplementation() {
        return implementationf;
    }
}
