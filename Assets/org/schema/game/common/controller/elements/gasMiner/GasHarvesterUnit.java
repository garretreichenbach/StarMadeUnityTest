package org.schema.game.common.controller.elements.gasMiner;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

import static java.lang.Math.min;
import static org.schema.game.common.controller.elements.gasMiner.GasHarvesterElementManager.HARVESTER_CYCLE_TIME_SECONDS;

public class GasHarvesterUnit extends CustomOutputUnit<GasHarvesterUnit, GasHarvesterCollectionManager, GasHarvesterElementManager> {
    @Override
    public void doShot(ControllerStateInterface unit, Timer timer, ShootContainer shootContainer) {
        boolean focus = false;
        boolean lead = false;
        unit.getShootingDir(
                getSegmentController(),
                shootContainer,
                getDistanceFull(),
                1,
                elementCollectionManager.getControllerPos(),
                focus,
                lead);
        shootContainer.shootingDirTemp.normalize();
        GasHarvesterElementManager em = elementCollectionManager.getElementManager();
        em.doShot(this, elementCollectionManager, shootContainer, unit.getPlayerState(), timer);
    }

    @Override
    public float getBasePowerConsumption() {
        return 0;
    }

    @Override
    public float getPowerConsumption() {
        return 0;
    }

    @Override
    public float getPowerConsumptionWithoutEffect() {
        return 0;
    }

    @Override
    public float getReloadTimeMs() {
        return HARVESTER_CYCLE_TIME_SECONDS * 1000;
    }

    @Override
    public float getInitializationTime() {
        return 0;
    }

    @Override
    public float getDistanceRaw() {
        return 0;
    }

    @Override
    public float getFiringPower() {
        return 0;
    }

    @Override
    public float getDamage() {
        return 0;
    }

    @Override
    public double getPowerConsumedPerSecondResting() {
        return (double)size() * (double) GasHarvesterElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
    }

    @Override
    public double getPowerConsumedPerSecondCharging() {
        return (double)size() * (double) GasHarvesterElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
    }

    @Override
    public PowerConsumerCategory getPowerConsumerCategory() {
        return PowerConsumerCategory.MINING_BEAMS;
    }

    @Override
    public ControllerManagerGUI createUnitGUI(GameClientState gameClientState, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
        return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
    }

    @Override
    public String toString() {
        return "GasHarvestUnit [significator=" + significator + "]";
    }

    public float getHarvestPower() {
        return getStaticHarvestPower() * getSpeedMultiplier();
    }

    public float getSpeedMultiplier(){
        float speedFrac = getSegmentController().getSpeedCurrent()/getSegmentController().getMaxServerSpeed();
        speedFrac = min(speedFrac,5.0f); //sanity check. You shouldn't be able to one-tap a whole Parsyne system via wormhole accel.
        return 1 + (speedFrac * GasHarvesterElementManager.HARVEST_MAX_BONUS_FROM_SPEED);
    }

    public float getStaticHarvestPower() {
        return size() * GasHarvesterElementManager.GAS_ITEM_HARVEST_PER_BLOCK;
    }
}
