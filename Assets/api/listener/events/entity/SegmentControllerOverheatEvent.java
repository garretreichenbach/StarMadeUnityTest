package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;

public class SegmentControllerOverheatEvent extends Event {

    private SegmentController entity;
    private Damager lastDamager;

    public SegmentControllerOverheatEvent(SegmentController entity, Damager lastDamager) {
        this.entity = entity;
        this.lastDamager = lastDamager;
    }

    /**
     * test if victim is killed (despawned after overheat timer) or overheated.
     * Event fires for both.
     * @return
     */
    public boolean isKilled() {
        return entity.isMarkedForPermanentDelete();
    }

    public SegmentController getEntity() {
        return entity;
    }

    public Damager getLastDamager() {
        return lastDamager;
    }
}
