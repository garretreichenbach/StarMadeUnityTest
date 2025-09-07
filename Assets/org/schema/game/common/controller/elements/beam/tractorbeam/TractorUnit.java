package org.schema.game.common.controller.elements.beam.tractorbeam;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamUnit;

public class TractorUnit extends BeamUnit<TractorUnit, TractorBeamCollectionManager, TractorElementManager> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	@Override
	public float getBeamPowerWithoutEffect() {
		return getBeamPower();
	}

	@Override
	public float getBeamPower() {
		return size() * getBaseBeamPower();
	}

	@Override
	public float getBaseBeamPower() {
		return TractorElementManager.TRACTOR_MASS_PER_BLOCK.get(getSegmentController().isUsingPowerReactors());
	}

	@Override
	public float getPowerConsumption() {
		return size() * 1000f;
	}
	@Override
	public void flagBeamFiredWithoutTimeout() {
		elementCollectionManager.flagBeamFiredWithoutTimeout(this);
	}

	@Override
	public float getDistanceRaw() {
		return TractorElementManager.DISTANCE * ((GameStateInterface) getSegmentController().getState()).getGameState().getWeaponRangeReference();
	}

	@Override
	public float getBasePowerConsumption() {
		return 1000f;
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return size() * 1000f;
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return (double)size() * (double)TractorElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return (double)size() * (double)TractorElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SUPPORT_BEAMS;
	}
	@Override
	public HitType getHitType() {
		return HitType.SUPPORT;
	}
	@Override
	public boolean isLatchOn() {
		return true;
	}
	@Override
	public float getDamage() {
		return 0;
	}
}
