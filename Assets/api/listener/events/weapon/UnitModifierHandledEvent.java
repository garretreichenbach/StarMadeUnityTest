package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;
import org.schema.game.common.data.element.CustomOutputUnit;

/**
 * Called whenever the .handle() method of WeaponUnitModifier is called. This appears to correspond roughly to
 * calculating the current paramaters of a cannon with a slaved system, including cannon damage.
 */

public class UnitModifierHandledEvent extends Event{
    final public Modifier<?, ?> weaponUnitModifier;
    final public ControlBlockElementCollectionManager<?,?,?> controlBlockElementCollectionManager;
    final public CustomOutputUnit<?,?,?> customOutputUnit;
    final public float unknown_float;

    public UnitModifierHandledEvent(Modifier<?,?> weaponUnitModifier,
                                    CustomOutputUnit<?,?,?> weaponUnit,
                                    ControlBlockElementCollectionManager<?, ?, ?> controlBlockElementCollectionManager,
                                    float unknown_float) {
        this.weaponUnitModifier = weaponUnitModifier;
        this.customOutputUnit = weaponUnit;
        this.controlBlockElementCollectionManager = controlBlockElementCollectionManager;
        this.unknown_float = unknown_float;
    }
}
