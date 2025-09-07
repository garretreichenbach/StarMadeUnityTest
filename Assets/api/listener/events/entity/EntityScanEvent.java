package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.spacescanner.ScanAddOn;
import org.schema.game.common.data.player.AbstractOwnerState;

public class EntityScanEvent extends Event {
    private final ScanAddOn scanner;
    private final boolean success;
    private final AbstractOwnerState owner;
    private final SegmentController entity;

    public EntityScanEvent(ScanAddOn scanner, boolean success, AbstractOwnerState owner, SegmentController entity) {
        this.scanner = scanner;
        this.success = success;
        this.owner = owner;
        this.entity = entity;
    }

    public ScanAddOn getScanner() {
        return scanner;
    }

    public boolean isSuccess() {
        return success;
    }

    public AbstractOwnerState getOwner() {
        return owner;
    }

    public SegmentController getEntity() {
        return entity;
    }
}
