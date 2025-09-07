package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.game.common.data.element.ControlElementMap;

/**
 * Created by Jake on 7/22/2021.
 * <insert description here>
 */
public class SegmentControllerControlMapConnectionRemoveEvent extends Event {
    private long from;
    private long to;
    private short controlledType;
    private boolean sendToClient;
    private ControlElementMap controlElementMap;

    public void set(long from, long to, short controlledType, boolean sendToClient, ControlElementMap controlElementMap) {

        this.from = from;
        this.to = to;
        this.controlledType = controlledType;
        this.sendToClient = sendToClient;
        this.controlElementMap = controlElementMap;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public short getControlledType() {
        return controlledType;
    }

    public boolean isSendToClient() {
        return sendToClient;
    }

    public ControlElementMap getControlElementMap() {
        return controlElementMap;
    }
}
