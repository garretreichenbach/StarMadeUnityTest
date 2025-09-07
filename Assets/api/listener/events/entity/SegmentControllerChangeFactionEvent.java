package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

public class SegmentControllerChangeFactionEvent extends Event {
    private final SimpleTransformableSendableObject<?> entity;
    private final int oldFaction;
    private int newFaction;

    public SegmentControllerChangeFactionEvent(SimpleTransformableSendableObject<?> entity, int oldFaction, int newFaction) {
        this.entity = entity;
        this.oldFaction = oldFaction;
        this.newFaction = newFaction;
    }

    public int getOldFaction() {
        return oldFaction;
    }

    public int getNewFaction() {
        return newFaction;
    }

    public void setNewFaction(int f) {
        newFaction = f;
    }

    public SimpleTransformableSendableObject getEntity() {
        return entity;
    }
}
