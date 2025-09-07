package org.schema.game.common.controller.elements.beam.repair;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamUnit;

public class RepairUnit extends BeamUnit<RepairUnit, RepairBeamCollectionManager, RepairElementManager> {

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
		//		return ControllerManagerGUI.create(state, "Repair Module", this,
		//				new ModuleValueEntry(Lng.str("Repair/hit", getBeamPower()),
		//				new ModuleValueEntry(Lng.str("HP/sec", StringTools.formatPointZero((1f/repairSpeedFactor) * getBeamPower()))
		//		);
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
		return RepairElementManager.REPAIR_PER_HIT.get(getSegmentController().isUsingPowerReactors());
	}

	@Override
	public float getPowerConsumption() {
		return size() * RepairElementManager.POWER_CONSUMPTION;
	}

	@Override
	public float getDistanceRaw() {
		//return RepairElementManager.DISTANCE * ((GameStateInterface) getSegmentController().getState()).getGameState().getWeaponRangeReference();
		return 1000;
	}

	@Override
	public float getBasePowerConsumption() {
		return RepairElementManager.POWER_CONSUMPTION;
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return size() * RepairElementManager.POWER_CONSUMPTION;
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return (double)size() * (double)RepairElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return (double)size() * (double)RepairElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
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
