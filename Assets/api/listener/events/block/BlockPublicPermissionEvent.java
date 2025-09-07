package api.listener.events.block;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

public class BlockPublicPermissionEvent extends Event {
    private final SegmentController seg;
    private final Vector3i pos;
    private final int forFactionId;
    private boolean permissionAllowed;
    private boolean allowingPersonalException;
    private boolean setByEvent = false;
    private String message;

    public BlockPublicPermissionEvent(SegmentController seg, Vector3i pos, int forFactionId, boolean result) {
        this.seg = seg;
        this.pos = pos;
        this.forFactionId = forFactionId;
        this.permissionAllowed = result;
        allowingPersonalException = true;
        message = null;
    }

    public boolean getPermission() {
        return permissionAllowed;
    }

    public void setPermission(boolean v) {
        permissionAllowed = v;
        setByEvent = true;
    }

    public SegmentController getSegmentController() {
        return seg;
    }

    public Vector3i getBlockPos() {
        return new Vector3i(pos);
    }

    public int getAccessingFactionId() {
        return forFactionId;
    }

    public int getOwningFactionId() {
        return seg.getFactionId();
    }

    public void setAllowingPersonalException(boolean allowingPersonalException) {
        this.allowingPersonalException = allowingPersonalException;
    }

    public String getActivationMessage(){
        return message;
    }

    public void setActivationMessage(String text){
        message = text;
    } //annoyingly, this cannot work outside of the activation system

    public boolean isAllowingPersonalException() {
        return allowingPersonalException;
    }

    public boolean isSetByEvent() {
        return setByEvent;
    }

    public boolean hasMessage() {
        return !(message == null);
    }
}
