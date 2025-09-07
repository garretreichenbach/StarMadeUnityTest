package api.listener.fastevents;

import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;

import javax.vecmath.Vector3f;

/**
 * Created by Jake on 10/26/2021.
 * Various calculations handled in ThrusterElementManager
 */
public interface ThrusterElementManagerListener {
    void instantiate(ThrusterElementManager manager);

    // THRUST EVENTS
    //  Called for ships with NO thrust sharing enabled.
    float getSingleThrust(ThrusterElementManager em, float thrustIn);
    //  Called for ships WITH thrust sharing enabled
    float getSharedThrust(ThrusterElementManager em, float thrustIn);

    // The TMR. Between 0.0 and 5.0 in QuickFire default configs
    float getThrustMassRatio(ThrusterElementManager em, float tmrIn);

    // Get max speed, this happens AFTER thrust speed chambers are applied
    float getMaxSpeed(ThrusterElementManager em, float velIn);

    // Get the max speed, this happens BEFORE chambers are applied
    float getMaxSpeedAbsolute(ThrusterElementManager em, float velIn);

    // Get the orientation power of the ship.
    Vector3f getOrientationPower(ThrusterElementManager em, Vector3f pwr);

    // Document this
    void handle(ThrusterElementManager em);

    double getPowerConsumptionResting(ThrusterElementManager em, double in);
    double getPowerConsumptionCharging(ThrusterElementManager em, double in);
}
