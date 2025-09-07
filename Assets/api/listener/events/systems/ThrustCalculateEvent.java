package api.listener.events.systems;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.thrust.ThrusterCollectionManager;

/**
 * Called when an ElementCollectionManager is created, note that all info about this may not yet be created because its only the first super call
 * Usualy worth it to put it in a StarRunnable and get it next tick after its created
 */
public class ThrustCalculateEvent extends Event {
    public final ThrusterCollectionManager thrusterCollectionManager;
    private float calculatedThrust;
    private float calculatedThrustRaw;

    @Deprecated
    public ThrustCalculateEvent(ThrusterCollectionManager thrusterElementManager, float calculatedThrust)
    {
        this.thrusterCollectionManager = thrusterElementManager;
        this.calculatedThrust = calculatedThrust;
    }

    public ThrustCalculateEvent(ThrusterCollectionManager thrusterElementManager, float calculatedThrust, float calculatedThrustRaw)
    {
        this.thrusterCollectionManager = thrusterElementManager;
        this.calculatedThrust = calculatedThrust;
        this.calculatedThrustRaw = calculatedThrustRaw;
    }

    public ThrusterCollectionManager getThrusterElementManager() {
        return thrusterCollectionManager;
    }

    public float getCalculatedThrust() {
        return calculatedThrust;
    }
    public float getCalculatedThrustRaw() {
        return calculatedThrust;
    }

    public void setThrust(float thrust) {
        calculatedThrust = thrust;
    }
    public void setThrustRaw(float thrust) {
        calculatedThrust = thrust;
    }
}
