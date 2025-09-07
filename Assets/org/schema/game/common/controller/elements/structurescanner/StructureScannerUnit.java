package org.schema.game.common.controller.elements.structurescanner;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;

public class StructureScannerUnit extends FiringUnit<StructureScannerUnit, StructureScannerCollectionManager, StructureScannerElementManager> {
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
		return 0; //collection manager handles this
	}

	@Override
	public float getPowerConsumption() {
		return 0; //collection manager handles this
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return getPowerConsumption();
	}

	@Override
	public float getReloadTimeMs() {
		return StructureScannerElementManager.RELOAD_AFTER_USE_MS;//getEffects()[RELOAD].getValue();
	}

	@Override
	public float getInitializationTime() {
		return StructureScannerElementManager.RELOAD_AFTER_USE_MS;
	}


	@Override
	public float getDistanceRaw() {
		return 1;
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
	public boolean isPowerCharging(long curTime) {
		return elementCollectionManager.isActive() || elementCollectionManager.getChargeManager().getCharge() > 0;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SCANNER;
	}
	@Override
	public float getDamage() {
		return getScanStrength();
	}

	public float getScanStrength() {
		return size() * StructureScannerElementManager.SCAN_STRENGTH_PER_BLOCK;
	}
}