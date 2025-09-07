package api.listener.events.inventory.metaobject;

import api.listener.events.Event;
import org.schema.game.common.data.element.meta.MetaObject;

/**
 * Called when a meta object is instansiated via the MetaObjectManager
 *
 * Read MetaObjectManager for type id info, etc.
 */
public class MetaObjectPreInstantiateEvent extends Event {

    private final short metaId;
    private final int subId;
    private final short weaponId;
    public MetaObjectPreInstantiateEvent(short metaId, int subId, short weaponId) {

        this.metaId = metaId;
        this.subId = subId;
        this.weaponId = weaponId;
    }

    /**
     * The meta object id as defined in MetaObjectManager.MetaObjectType
     */
    public short getMetaId() {
        return metaId;
    }

    /**
     * Weapon Sub-Id [TODO: Figure out what this is]
     */
    public int getSubId() {
        return subId;
    }

    /**
     * If the MetaObject is a weapon (Meta Object Id: -32)
     * Then it will have a sub weapon id as defined in Weapon.WeaponSubType
     * @see org.schema.game.common.data.element.meta.weapon.Weapon.WeaponSubType
     */
    public short getWeaponId() {
        return weaponId;
    }

    /**
     * If you have custom ids, and starmade cannot figure out what item it is, it will throw an IllegalArgumentException
     * This will swap that out with your own MetaObject
     */
    private MetaObject newMetaObject;
    private boolean customMetaObject = false;
    public void injectMetaObject(MetaObject customObject){
        newMetaObject = customObject;
        customMetaObject = true;
    }

    public boolean isCustomMetaObject() {
        return customMetaObject;
    }

    public MetaObject getNewMetaObject() {
        return newMetaObject;
    }
}
