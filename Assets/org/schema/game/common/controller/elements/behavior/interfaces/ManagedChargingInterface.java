package org.schema.game.common.controller.elements.behavior.interfaces;

import org.schema.game.common.controller.elements.behavior.managers.charging.ChargeManager;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Denotes a system with charge-up behaviour managed by a ChargeManager.
 */
public non-sealed interface ManagedChargingInterface extends ManagedBehaviourInterfaceGeneric {
    /**
     * @return The charge-up manager associated with this object.
     * <b>Implementors must call getChargeManager().update() every update cycle if automatic charging behaviour is desired.</b>
     */
    ChargeManager getChargeManager();

    /**
     * This method will be polled before every charging update, and so should not be an intensive process.
     * @return Whether the system should charge up automatically.
     */
    boolean canCharge(long currentTime);

    /**
     * <i>This method will be polled before every charge cycle, and so should not be an intensive process.</i>
     * @return How much charge to add per second, if canCharge() returns true.
     */
    float getChargeAddedPerSecond();

    /**
     * <i>This method will be polled before every charge cycle, and so should not be an intensive process.</i>
     * @return How much charge to remove per second, if canDischarge() returns true.
     */
    float getDechargePerSecond();

    /**
     * @return The current charge level.
     */
    default float getCurrentCharge(){
        return getChargeManager().getCharge();
    }

    /**
     * @return The maximum charge level for one charge.<br/>
     * If multicharging is enabled and the system is below the maximum amount of charges, after surpassing this value a charge will be stored and the next charge will begin to fill.
     */
    float getMaxCharge();

    /**
     * @return The number of complete charges currently stored.
     */
    default int getCharges(){
        return getChargeManager().getChargesCount();
    }

    /**
     * @return The maximum amount of charges that can be stored.
     */
    default int getMaxCharges(){
        return 1;
    }

    /**
     * @return The number of charges added per charge cycle.
     */
    default int getChargesAddedPerChargeCycle(){
        return 1;
    }

    /**
     * Send an update.<br/>
     * Typically, this is only called on the server in order to preserve server authority, as PlayerUsable input handling from clients is synchronized by default.<br/>
     * Implementations of this method usually involve creating and populating a ValueUpdate.
     */
    void sendChargeUpdate();

    /**
     * Called when any one charge is completed.
     */
    default void onFullCharge(){

    }

    /**
     * Whether the system should discharging complete charges or not.
     */
    default boolean allowDischargeFromFull(){
        return true;
    }
}
