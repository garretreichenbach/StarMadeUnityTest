package api.listener.events.inventory.metaobject;

import api.listener.events.Event;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.inventory.InventorySlot;

/**
 * Created by Jake on 4/3/2021.
 * <insert description here>
 */
public class InventoryPutMetaItemEvent extends Event {

    private final int count;
    private final MetaObject metaObject;
    private final short typeId;
    private final InventorySlot inventorySlot;

    public InventoryPutMetaItemEvent(int count, MetaObject metaObject, short typeId, InventorySlot inventorySlot) {
        this.count = count;
        this.metaObject = metaObject;
        this.typeId = typeId;
        this.inventorySlot = inventorySlot;
    }

    public int getCount() {
        return count;
    }

    public MetaObject getMetaObject() {
        return metaObject;
    }

    public short getTypeId() {
        return typeId;
    }

    public InventorySlot getInventorySlot() {
        return inventorySlot;
    }
}
