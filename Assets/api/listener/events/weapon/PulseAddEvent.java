package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.Pulse;

public class PulseAddEvent extends Event {
    private Pulse pulsef;

    public PulseAddEvent(Pulse pulse){
        pulsef = pulse;
    }

    public Pulse getPulse() {
        return pulsef;
    }
    public void setPulse(Pulse n){
        pulsef = n;
    }
}
