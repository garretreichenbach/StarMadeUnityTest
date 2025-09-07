package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;

public class InterdictionCheckEvent extends Event {
    private final JumpAddOn addOn;
    private SegmentController segmentController;
    private boolean isInterdicted = false;
    public boolean useDefault = true;

    public InterdictionCheckEvent(JumpAddOn jumpAddOn, SegmentController segmentController, boolean retVal) {
        this.addOn = jumpAddOn;
        this.segmentController = segmentController;
        this.isInterdicted = retVal;
        setCanceled(true);

    }

    public void setInterdicted(boolean b) {
        isInterdicted = b;
        useDefault = false;
    }

    public SegmentController getSegmentController() {
        return segmentController;
    }

    public boolean isInterdicted() {
        return isInterdicted;
    }

    public JumpAddOn getAddOn() {
        return addOn;
    }
}
