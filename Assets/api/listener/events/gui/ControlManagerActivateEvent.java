package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.game.client.controller.manager.AbstractControlManager;

public class ControlManagerActivateEvent extends Event {
    private final AbstractControlManager controlManager;
    private final boolean active;

    public ControlManagerActivateEvent(AbstractControlManager controlManager, boolean active){

        this.controlManager = controlManager;
        this.active = active;
    }

    public AbstractControlManager getControlManager() {
        return controlManager;
    }

    public boolean isActive() {
        return active;
    }
}
