package org.schema.game.common.controller.elements.stealth;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

/**
 * Formerly "cloaking unit".
 */
public class StealthUnit extends FiringUnit<StealthUnit, StealthCollectionManager, StealthElementManager> {
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#significatorUpdate(int, int, int, int, int, int, int, int, int, long)
	 */
	@Override
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin,
	                                  int zMin, int xMax, int yMax, int zMax, long index) {
		super.significatorUpdateMin(x, y, z, xMin, yMin, zMin, xMax, yMax, zMax, index);
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
		return 0;
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
		return getStrength();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
		//		return ControllerManagerGUI.create(state, "Cloaking Module", this, new EmptyValueEntry());
	}

	/**
	 * @return the stealth system strength
	 */
	public float getStrength() {
		return size() * StealthElementManager.STEALTH_STRENGTH_PER_BLOCK;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return getPowerConsumption();
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getPowerConsumption();
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.STEALTH;
	}
}
