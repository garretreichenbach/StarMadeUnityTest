package org.schema.game.common.controller.elements.beam.harvest;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamUnit;

public class SalvageUnit extends BeamUnit<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> {

	//	/* (non-Javadoc)
	//	 * @see org.schema.game.common.data.element.ElementCollection#significatorUpdate(int, int, int, int, int, int, int, int, int, long)
	//	 */
	//	@Override
	//	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin,
	//			int zMin, int xMax, int yMax, int zMax, long index) {
	//		super.significatorUpdateZ(x, y, z, xMin, yMin, zMin, xMax, yMax, zMax, index);
	//	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
		//		return ControllerManagerGUI.create(state, "Salvage Module", this,
		//				new ModuleValueEntry(Lng.str("Salvage Speed", salvageSpeedFactor)
		//
		//				);
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
	public void flagBeamFiredWithoutTimeout() {
		elementCollectionManager.flagBeamFiredWithoutTimeout(this);
	}

	@Override
	public float getBaseBeamPower() {
		return SalvageElementManager.SALVAGE_DAMAGE_PER_HIT.get(getSegmentController().isUsingPowerReactors());
	}

	@Override
	public float getPowerConsumption() {
		return size() * SalvageElementManager.POWER_CONSUMPTION;
	}

	@Override
	public float getDistanceRaw() {
		return SalvageElementManager.DISTANCE * ((GameStateInterface) getSegmentController().getState()).getGameState().getWeaponRangeReference();
	}

	@Override
	public float getBasePowerConsumption() {
		return SalvageElementManager.POWER_CONSUMPTION;
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return size() * SalvageElementManager.POWER_CONSUMPTION;
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return (double)size() * (double)SalvageElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return (double)size() * (double)SalvageElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
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
	public float getDamage() {
		return 0;
	}
}
