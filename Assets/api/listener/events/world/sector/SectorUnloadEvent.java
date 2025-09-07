package api.listener.events.world.sector;

import api.listener.events.Event;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

/**
 * Created by Jake on 11/21/2020.
 * <insert description here>
 */
public class SectorUnloadEvent extends Event {
    private final ObjectArrayList<SimpleTransformableSendableObject<?>> entities;
    private final Sector sector;

    public SectorUnloadEvent(ObjectArrayList entities, Sector sector){
        this.entities = new ObjectArrayList<>(entities);
        this.sector = sector;
    }

    public ObjectArrayList<SimpleTransformableSendableObject<?>> getEntities() {
        return entities;
    }

    public Sector getSector() {
        return sector;
    }
}
