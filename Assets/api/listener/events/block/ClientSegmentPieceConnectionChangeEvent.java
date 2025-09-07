package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.element.ControlElementMap;

/**
 * Created by Jake on 7/20/2021.
 * Called when a player presses V on a module
 */
public class ClientSegmentPieceConnectionChangeEvent extends Event {

    private ControlElementMap controlElementMap;
    private long controller;
    private long controlled;
    private short controlledType;
    private boolean connected;

    public void setEvent(ControlElementMap controlElementMap, long controller, long controlled, short controlledType, boolean connected) {
        this.controlElementMap = controlElementMap;

        this.controller = controller;
        this.controlled = controlled;
        this.controlledType = controlledType;
        this.connected = connected;
    }

    public long getControllerIndex() {
        return controller;
    }

    public long getControlledIndex() {
        return controlled;
    }

    public short getControlledType() {
        return controlledType;
    }

    public ControlElementMap getControlElementMap() {
        return controlElementMap;
    }
    public SendableSegmentController getSegmentController(){
        return controlElementMap.getSegmentController();
    }
    public boolean wasChangedToConnected(){
        return connected;
    }
}
