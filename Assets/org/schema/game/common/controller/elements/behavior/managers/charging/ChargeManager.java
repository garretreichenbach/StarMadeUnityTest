package org.schema.game.common.controller.elements.behavior.managers.charging;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedChargingInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.schema.common.FastMath.fastFloor;
import static org.schema.game.common.controller.elements.UsableControllableElementManager.drawReload;
import static org.schema.game.common.controller.elements.UsableControllableFiringElementManager.disabledColor;
import static org.schema.game.common.controller.elements.UsableControllableFiringElementManager.reloadColor;

public class ChargeManager {
    protected final ManagedChargingInterface ifc; //whatever this is linked to
    protected float currCharge = 0; //charge level of current charge
    protected int charges = 0; //number of completed charges
    protected boolean sendUpdates;
    private boolean chargeZeroOrPositive;
    private GUITextOverlay chargesText;

    public ChargeManager(ManagedChargingInterface i, boolean updateSender){
        ifc = i;
        sendUpdates = updateSender;
    }

    public void update(Timer t){
        float lastCharge = getCharge();
        int lastCharges = getChargesCount();

        if(ifc.canCharge(t.currentTime)) charge(t);
        discharge(t);

        if(lastCharges < getChargesCount() || lastCharge <= getCharge()) chargeZeroOrPositive = true;
        else chargeZeroOrPositive = false;
    }

    /**
     * Charge the system according to its charge rate and the time elapsed since last update.<br/>
     * Extra charge will overflow to begin the next charge, if possible.
     * @param t The timer used to determine the elapsed time since last update.
     */
    protected void charge(Timer t) {
        final float max = ifc.getMaxCharge();
        if(getCharge() < max || getChargesCount() < ifc.getMaxCharges()) {
            //we need to charge up
            final float added = ifc.getChargeAddedPerSecond() * t.getDelta(); //may be 0 due to no power, effect applied etc.
            addCharge(added);
        }
    }

    /**
     * Discharge the system according to the specified discharge rate.
     * If the charge level is completely empty and the implementation of allowMultiDischarge() returns true, this will discharge the next available charge if there are multiple charges stored.<br/>
     * If the discharge rate is zero, this method will have no effect and most of its logic will be bypassed.<br/>
     * @param t The timer used to determine the elapsed time since last update.
     */
    protected void discharge(Timer t) {
        if(getCharge() > 0 || (ifc.allowDischargeFromFull() && getChargesCount() > 0)) {
            final float dechargeAmt = ifc.getDechargePerSecond() * t.getDelta();
            if (dechargeAmt > 0) {
                addCharge(-dechargeAmt);
            }
        }
    }

    public void setCharge(float v){
        if(v < 0) throw new IllegalArgumentException("Cannot set negative charge value!");
        currCharge = min(ifc.getMaxCharge(),v);
    }

    /**
     * @return The current charge level.
     */
    public float getCharge(){
        return currCharge;
    }


    public boolean consumeFullCharge(int count) {
        if(count > getChargesCount()) return false;
        else{
            setChargesCount(getChargesCount() - count);
            sendUpdateIfPossible();
            return true;
        }
    }

    public void resetCurrentCharge(){
        boolean update = false;
        if(getCharge() > 0){
            setCharge(0);
            update = true;
        }
        else{
            int charges = getChargesCount();
            if(charges > 0) {
                setChargesCount(charges - 1);
                update = true;
            }
        }
        if(update) sendUpdateIfPossible();
    }

    public void resetAllCharges(){
        setChargesCount(0);
        setCharge(0);
        sendUpdateIfPossible();
    }

    /**
     * @return the number of complete charges stored.
     */
    public int getChargesCount() {
        return charges;
    }

    public void setChargesCount(int v) {
        if(v < 0) throw new IllegalArgumentException("Cannot set negative charges!");
        charges = min(ifc.getMaxCharges(),v);
    }

    public void sendUpdateIfPossible() {
        if(sendsUpdates()) ifc.sendChargeUpdate();
    }

    public boolean sendsUpdates(){
        return sendUpdates;
    }

    /**
     * @param v Whether or not the charge manager should send updates when a full charge completes or empties.
     */
    public void setSendUpdates(boolean v){
        sendUpdates = v;
    }

    public boolean fullyCharged() {
        return getChargesCount() == ifc.getMaxCharges();
    }

    public void drawReloads(Vector3i iconPos, Vector3i iconSize, InputState state) {
        float percent = getCharge()/ifc.getMaxCharge();
        if(ifc.getMaxCharges() > 1) {
            if(chargesText == null){
                chargesText = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, state);
                chargesText.onInit();
            }

            drawReload(state, iconPos, iconSize, reloadColor, false, percent, false, getChargesCount(), ifc.getMaxCharges(), -1, chargesText);
            //timeleft can be -1, as the activation manager's reload draw method will handle this part instead if relevant
        } else {
            if (percent < 1) drawReload(state, iconPos, iconSize, chargeZeroOrPositive ? reloadColor:disabledColor, false, percent);
        }
    }

    public void addCharge(float added) {
        boolean update = false;
        if (added != 0 && (added < 0 || ifc.getCharges() < ifc.getMaxCharges())) {
            float chargeAfter = getCharge() + added;
            final float max = ifc.getMaxCharge();

            if (chargeAfter >= max) { //finished a charge
                final int wholeCharges = fastFloor(chargeAfter / max);
                final int newCharges = min(ifc.getMaxCharges(), getChargesCount() + (ifc.getChargesAddedPerChargeCycle() * wholeCharges));
                setChargesCount(newCharges); //add all full charges if possible
                if(newCharges == ifc.getMaxCharges())
                    chargeAfter = 0; //can't charge any further
                else
                    chargeAfter = chargeAfter % max;

                update = true;
            } else if (chargeAfter < 0) { //discharged past a full charge limit
                if(ifc.allowDischargeFromFull() && ifc.getCharges() > 0) {
                    assert added < 0;
                    final int wholeCharges = fastFloor(chargeAfter / max); //should be negative
                    setChargesCount(max(0, getChargesCount() + (ifc.getChargesAddedPerChargeCycle() * wholeCharges)));
                    chargeAfter = max(0, max + (chargeAfter % max));
                } else chargeAfter = 0;
                update = true;
            } //else just set charge

            setCharge(chargeAfter);
            if (update) sendUpdateIfPossible();
        }
    }
}
