package org.schema.game.common.controller.elements.behavior.interfaces;

import org.schema.game.common.controller.elements.behavior.managers.reload.CooldownManager;

/**
 * Denotes a system with a fixed-duration cooldown behaviour managed by a CooldownManager.
 */
public non-sealed interface ManagedCooldownInterface extends ManagedBehaviourInterfaceGeneric {
    CooldownManager getCooldownManager();

    long getCooldownDurationMs();
}
