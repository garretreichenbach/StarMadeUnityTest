package org.schema.game.common.controller.elements.ammo;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.RailDockingListener;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.WeaponCapacityValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ValueUpdate;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import static java.lang.Math.min;
import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponCapacityReloadMode.ALL;
import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponCapacityReloadMode.SINGLE;

public abstract class AmmoCapacityElementManager
        <
                E extends AmmoCapacityUnit<E,CM,EM>,
                CM extends AmmoCapacityCollectionManager<E,CM,EM>,
                EM extends AmmoCapacityElementManager<E,CM,EM>
        >
        extends UsableControllableSingleElementManager<E,CM,EM> implements PowerConsumer, RailDockingListener {

    private static final byte VERSION = 2;
    public enum WeaponType{ //TODO replace this enum with a mod-expandable system - maybe an interface, maybe a type param for the weapon, maybe just the block ID of the weapon computer
        BEAM,
        CANNON,
        MISSILE,
        REPAIR
    }
    /**
     * @return The baseline amount of ammo charges the system will provide, even without any blocks. Any other capacity will be added on top of this.
     */
    public abstract float getBasicCapacity();

    /**
     * @return The reloading mode - <code>SINGLE</code> means ammo will reload one-by-one based on the reload rate; <code>ALL</code> means the entire ammo reserve will replenish at once when the timer reaches zero.
     */
    public abstract WeaponCapacityReloadMode getReloadMode();

    /**
     * @return The added reload rate per second, based on the calculated ammo capacity.
     */
    public abstract float getAmmoReloadPerCapacity();
    /**
     * @return The baseline time to reload the ammo when in <code>ALL</code> reload mode.
     */
    public abstract float getAmmoReloadAllTime();

    /**
     * @return If the reload timer resets when a player fires the weapon being supplied by this ammo capacity system.
     */
    public abstract boolean ammoReloadResetsOnManualFire();
    /**
     * @return If the reload timer resets when the AI fires the weapon being supplied by this ammo capacity system.
     */
    public abstract boolean ammoReloadResetsOnAIFire();

    public abstract double getPowerConsumptionPerBlockResting();

    public abstract double getPowerConsumptionPerBlockCharging();

    /**
     * @return How the ammo capacity is calculated based on blockcount: LINEAR, EXP, or LOG. This method can be ignored if custom logic is being used to determine the ammo capacity.
     */
    public abstract UnitCalcStyle getCapacityCalcStyle();

    //linear
    public abstract float getCapacityPerBlockLinear();
    //exp
    public abstract float getCapacityExp();
    public abstract float getCapacityExpMult();

    //double exp
    public abstract float getCapacityDoubleExpFirstHalf();
    public abstract float getCapacityDoubleExpMultFirstHalf();

    public abstract float getCapacityExponentThreshold();

    public abstract float getCapacityDoubleExpSecondHalf();
    public abstract float getCapacityDoubleExpMultSecondHalf();

    //log
    public abstract float getCapacityLogFactor();
    public abstract float getCapacityLogOffset();

    /**
     * @return The amount of extra power consumption (as a multiplier) incurred by having this ammo system reloading at the same time as another one as large as, or larger than, the max ratio relative this one.
     * Below the max ratio, this value will get scaled down.
     */
    public abstract float getFullMultitaskPenalty();
    public abstract float getMaxRatioForMultitaskPenalty();

    private float powered;

    private float capacity = 1;
    private float capacityMax = 1;

    private boolean loadedFromTag;

    protected float timer;

    public AmmoCapacityElementManager(SegmentController segmentController, Class<CM> clazz) {
        super(segmentController, clazz);

        timer = getAmmoReloadAllTime();
    }


    @Override
    public void onControllerChange() {
    }



    @Override
    public ControllerManagerGUI getGUIUnitValues(E unit,
                                                 CM col,
                                                 ControlBlockElementCollectionManager<?, ?, ?> supportCol,
                                                 ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
        return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Weapon Capacity Unit (%s)", getWeaponType().name()), unit);
    }

    @Override
    public boolean canHandle(ControllerStateInterface unit) {
        return false;
    }

    @Override
    public void handle(ControllerStateInterface unit, Timer timer) {
    }


    @Override
    public double getPowerConsumedPerSecondResting() {
        double powCons = totalSize * getPowerConsumptionPerBlockResting();
//		powCons = getSegmentController().getConfigManager().apply(StatusEffectType.AMMO_POWER_CONSUMPTION, powCons);

        return powCons;
    }
    @Override
    public double getPowerConsumedPerSecondCharging() {
        double powCons = totalSize * getPowerConsumptionPerBlockCharging() * getMultitaskFactor();
//		powCons = getSegmentController().getConfigManager().apply(StatusEffectType.AMMO_POWER_CONSUMPTION, powCons);

        return powCons;
    }

    public float getMultitaskFactor() {
        if(this.totalSize == 0 || getFullMultitaskPenalty() == 0) return 1; //short circuit case

        AmmoCapacityCollectionManager<?,?,?> otherAmmo;
        ManagerContainer<?> managerCtr = getManagerContainer();
        WeaponType[] values = WeaponType.values();
        float cappedRatio;
        float result = 0;
        for (int i = 0; i < values.length; i++) {
            if(getWeaponType() != values[i]) {
                otherAmmo = managerCtr.getAmmoSystem(values[i]);
                if(otherAmmo != null && otherAmmo.getTotalSize() > 0){
                    AmmoCapacityElementManager<?,?,?> e = otherAmmo.getElementManager();
                    if(e.getCapacityFilled() < e.getCapacityMax()){ //incur penalty only if this is actively reloading
                        cappedRatio = min(getMaxRatioForMultitaskPenalty(),(float) e.totalSize / this.totalSize);
                        result += (cappedRatio/getMaxRatioForMultitaskPenalty()) * getFullMultitaskPenalty();
                    }
                }
            }
        }
        return 1 + result;
    }

    @Override
    public boolean isPowerCharging(long curTime) {
        return getCapacityFilled() < getCapacityMax();
    }

    @Override
    public void setPowered(float powered) {
        this.powered = powered;
    }

    @Override
    public float getPowered() {
        return powered;
    }

    @Override
    public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
        float befCap = this.capacity;
        WeaponCapacityReloadMode mode = getReloadMode();

        if(mode == SINGLE) {
            if(this.capacity < capacityMax) {
                this.capacity = this.capacity + ((float) secTime) * getAmmoReloadPerCapacity();
            }
        }else {
            assert(mode == ALL);
            if(this.capacity < capacityMax) {
                this.timer -= secTime;

                if(this.timer <= 0f) {
                    this.capacity = this.capacityMax;
                    this.timer = getAmmoCapacityReloadTime();
                }
            }
        }

        if(getSegmentController().isOnServer() &&
                befCap != capacity &&
                (capacity == 0 || capacity == capacityMax)) {
            sendAmmoCapacity();
        }
    }

    @Override
    public boolean isPowerConsumerActive() {
        return true;
    }

    @Override
    public void dischargeFully() {
        capacity = 0;
        if(getSegmentController().isOnServer()) {
            sendAmmoCapacity();
        }
    }

    public void sendAmmoCapacity() {
        assert (getSegmentController().isOnServer());

        WeaponCapacityValueUpdate capacityValueUpdate = new WeaponCapacityValueUpdate();
        assert (capacityValueUpdate.getType() == ValueUpdate.ValTypes.WEAPON_CAPACITY_UPDATE);
        capacityValueUpdate.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(),getWeaponType());
        ((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(capacityValueUpdate, getSegmentController().isOnServer()));
    }

    public Tag toTag() {
        return new Tag(Tag.Type.STRUCT, null, new Tag[] {
                new Tag(Tag.Type.BYTE, null, VERSION),
                new Tag(Tag.Type.FLOAT, null, capacity),
                new Tag(Tag.Type.FLOAT, null, capacityMax),
                new Tag(Tag.Type.FLOAT, null, timer),
                FinishTag.INST});
    }

    public void readFromTag(Tag tag) {
        Tag[] t = tag.getStruct();
        byte version = t[0].getByte();
        setCapacityMax(t[2].getFloat()); //yes, this is intentionally backwards
        setAmmoCapacity(t[1].getFloat());
        if(version > 0) {
            timer = t[3].getFloat();
        }
        loadedFromTag = true;
    }




    public float getCapacityMax() {
        return capacityMax;
    }




    public void setCapacityMax(float capacityMax) {

        this.capacityMax = capacityMax;
    }




    public float getCapacityFilled() {
        return capacity;
    }




    public void setAmmoCapacity(float ammoCapacity) {
        if(ammoCapacity > capacityMax)
            System.out.println("[WARNING] OVERFILLED " + getWeaponType().name() + " CAPACITY BY " + (ammoCapacity - capacityMax));
        this.capacity = min(ammoCapacity,capacityMax);
    }




    public float getAmmoTimer() {
        return timer;
    }




    public void setAmmoTimer(float timer) {
        this.timer = timer;
    }




    public float getAmmoCapacityReloadTime() {
        float perSec = 0;
        perSec += capacityMax * getAmmoReloadPerCapacity();
        perSec += getAmmoReloadAllTime();
        return perSec;
    }




    @Override
    public void dockingChanged(SegmentController c, boolean docked) {
        if(isOnServer()) {
            if(!docked) {
                //at the time of this call. the ship is still docked, so the root will be the entity we are currently undocking from
                SegmentController root = getSegmentController().railController.getRoot();
                if(root == getSegmentController()) {
                    System.err.println("[SERVER][WEAPON CAPACITY][WARNING] undocking reported docked to self");
                    capacity = 0;
                }else {
                    //only load ammo from mothership if undock something that is capable of using the weapon it goes to
                    if(isThisOrAnyDockWeaponCapable(getWeaponType(),getSegmentController())) {
                        //only take ammunition if the undocking entity actually has the capability to fire it
                        float toLoad = min(capacityMax, root.getAmmoCapacity(getWeaponType()));
                        capacity = toLoad;
                    }else {
                        capacity = 0;
                    }
                }
                sendAmmoCapacity();
            }
        }
    }

    /**
     * @return what kind of weapon this feeds.
     */
    public abstract WeaponType getWeaponType();


    private static boolean isThisOrAnyDockWeaponCapable(WeaponType w, SegmentController s) {
        if(!(s instanceof WeaponManagerInterface)) return false;
        WeaponManagerInterface entity = (WeaponManagerInterface) s;

        if(
            (w == WeaponType.CANNON && entity.getWeapon().hasAtLeastOneCoreUnit()) ||
            (w == WeaponType.BEAM && entity.getBeam().hasAtLeastOneCoreUnit()) ||
            (w == WeaponType.MISSILE && entity.getMissile().hasAtLeastOneCoreUnit())
        ) {
            return true;
        }
        if(!s.railController.next.isEmpty()) {
            for(RailRelation r : s.railController.next) {
                if(isThisOrAnyDockWeaponCapable(w, r.docked.getSegmentController())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public abstract CM getNewCollectionManager(SegmentPiece position, Class<CM> clazz);

    public enum WeaponCapacityReloadMode {
        ALL, SINGLE
    }
}
