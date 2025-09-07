package org.schema.game.common.controller.elements.behavior.managers.activation;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedActivationInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;

import javax.annotation.Nullable;

public class ActivationManager {
    protected final ManagedActivationInterface ifc;
    private boolean active;
    private long lastActivation;
    private long lastDeactivation;
    protected boolean sendUpdates;

    public ActivationManager(ManagedActivationInterface i, boolean updateSender) {
        ifc = i;
        sendUpdates = updateSender;
    }

    /**
     * Update the activation state of the system.<br/>
     * This method <i>only</i> needs to be called if finite activation durations are possible in your implementation.
     * @param t The timer used to determine the time since last update
     */
    public void update(Timer t){
        if(active) {
            long duration = ifc.getActivationTimeMs();
            if (duration >= 0 && t.currentTime > lastActivation + duration) { //treat negative durations as infinite
                ifc.onDurationEnd();
                setActive(false, t);
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Set the system's activation state, treating activation time as 'now', then propagate update if possible.
     * @param activationVal
     */
    public void setActive(boolean activationVal){
        setActive(activationVal,null);
    }

    /**
     * Set the system's activation state, treating activation time as the timer's current time value, then propagate update if possible.
     * @param activationVal
     */
    public void setActive(boolean activationVal, @Nullable Timer t) {
        if(t != null) setActiveState(activationVal,t.currentTime);
        else setActiveState(activationVal,System.currentTimeMillis());
    }

    public void setActiveState(boolean activationVal, long currTime){
        if(activationVal != active) {
            this.active = activationVal;
            if (activationVal) {
                lastActivation = currTime;
                ifc.onActivate();
            } else {
                lastDeactivation = currTime;
                ifc.onDeactivate();
            }
            sendUpdateIfPossible();
        }
    }


    protected void sendUpdateIfPossible(){
        if(sendUpdates) ifc.sendActiveStateUpdate();
    }

    public long getLastActivation(){
        return lastActivation;
    }

    public long getLastDeactivation(){
        return lastDeactivation;
    }

    public boolean sendsUpdates(){
        return sendUpdates;
    }

    public void setSendUpdates(boolean v){
        sendUpdates = v;
    }

    /**
     * Draw a "reload" visual based on the state of this activation manager.
     */
    public void drawReloads(Vector3i iconPos, Vector3i iconSize, InputState state, long currentTime){
        //draw usage time
        float activeDuration = ifc.getActivationTimeMs();
        long timeSinceActivated = currentTime - getLastActivation();
        float percent = 1.0f;
        if(activeDuration > 0){ //use duration
            percent -= ((float) timeSinceActivated / activeDuration);
        }
        UsableControllableElementManager.drawReload(state, iconPos, iconSize, ifc.getActiveIndicatorColor(), true, percent);
    }
}