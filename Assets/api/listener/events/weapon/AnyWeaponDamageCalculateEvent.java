package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.data.element.CustomOutputUnit;


/**
 * Called whenever the .handle() method of WeaponUnitModifier is called. This appears to correspond roughly to
 * calculating the current paramaters of a cannon with a slaved system, including cannon damage.
 */

public class AnyWeaponDamageCalculateEvent extends Event{
    final public CustomOutputUnit<?,?,?> customOutputUnit;
    public float damage;
    final public boolean isBase;

    public AnyWeaponDamageCalculateEvent(CustomOutputUnit<?,?,?> weaponUnit, float damage, boolean isBase) {
        this.customOutputUnit = weaponUnit;
        this.damage = damage;
        this.isBase = isBase;
    }
}