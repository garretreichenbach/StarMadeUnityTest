package org.schema.game.common.controller.elements.spacescanner;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

public class LongRangeScannerUnit extends FiringUnit<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> {


	private float getBaseConsume() {
		return LongRangeScannerElementManager.SCAN_POWER_CONSUMPTION_FIXED + (size() * LongRangeScannerElementManager.CHARGE_TIME_ADDED_PER_SECOND_PER_BLOCK);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScannerUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	@Override
	public float getBasePowerConsumption() {
		assert (false);
		return 0;
	}

	@Override
	public float getPowerConsumption() {
		return getBaseConsume();
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return 0;
	}

	@Override
	public float getReloadTimeMs() {
		return LongRangeScannerElementManager.RELOAD_AFTER_USE_MS;//getEffects()[RELOAD].getValue();
	}

	@Override
	public float getInitializationTime() {
		return LongRangeScannerElementManager.RELOAD_AFTER_USE_MS;
	}

	@Override
	public float getDistanceRaw() {
		return getConfigManager().apply(StatusEffectType.SCAN_LONG_RANGE_DISTANCE, LongRangeScannerElementManager.DEFAULT_SCAN_DISTANCE);//getEffects()[DISTANCE].getValue();
	}

	@Override
	public float getFiringPower() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return 0;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SCANNER;
	}
	@Override
	public float getDamage() {
		return 0;
	}
}