package api.listener.events.calculate;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.ShieldLocalAddOn;

public class ShieldRegenCalculateEvent extends Event {
    public final ShieldLocal shieldLocal;
    public final SegmentController segmentController;
    public final ShieldLocalAddOn shieldAddOn;
    public final double shieldBlocks;
    public double shieldRegen;

    public ShieldRegenCalculateEvent(ShieldLocal shieldLocal, SegmentController controller, ShieldLocalAddOn addOn, double shieldRegen, double shieldBlocks) {
        this.shieldLocal = shieldLocal;
        this.segmentController = controller;
        this.shieldAddOn = addOn;
        this.shieldRegen = shieldRegen;
        this.shieldBlocks = shieldBlocks;
    }
}
