package org.schema.game.common.controller.elements.behavior.interfaces;

import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.behavior.managers.activation.ActivationManager;

import javax.vecmath.Vector4f;

import static org.schema.game.common.controller.elements.UsableControllableFiringElementManager.activeColor;

/**
 * Denotes a system with an activation state behaviour managed by an ActivationManager.<br/>
 * Systems with instant activation procedures (e.g. weapon firing) generally should not implement this interface, as it is primarily intended for systems with activation durations or toggle states.
 */
public non-sealed interface ManagedActivationInterface extends ManagedBehaviourInterfaceGeneric, ManagerActivityInterface {
    /**
     * @return The activation manager associated with this object.
     * <b>Implementors must call getActivationManager().update() every update while active, if timed activation behaviour is desired.</b>
     */
    ActivationManager getActivationManager();

    /**
     * @return the time in ms that the system should remain active once activated. Negative values will be treated as infinite.
     */
    long getActivationTimeMs();

    /**
     * Send a state update. Typically, this is only called on the server in order to preserve server authority, as PlayerUsable input handling from clients is synchronized by default.
     * Implementations of this method usually involve creating and populating a ValueUpdate.
     */
    void sendActiveStateUpdate();

    /**
     * Called when the activation manager activates the system.
     */
    void onActivate();

    /**
     * Called when the activation manager deactivates the system.
     */
    void onDeactivate();

    /**
     * Called when the activation manager deactivates the system after a full active duration.
     * Immediately after this method, OnDeactivate() will still be called.
     */
    default void onDurationEnd(){}

    @Override
    default boolean isActive(){
        return getActivationManager().isActive();
    }

    /**
     * @return what colour this system should use for the hotbar indicator when the system is active. This defaults to a green colour.
     */
    default Vector4f getActiveIndicatorColor(){
        return activeColor;
    }
}
