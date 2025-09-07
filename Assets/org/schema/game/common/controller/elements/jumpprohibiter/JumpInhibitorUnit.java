package org.schema.game.common.controller.elements.jumpprohibiter;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

public class JumpInhibitorUnit extends FiringUnit<JumpInhibitorUnit, JumpInhibitorCollectionManager, JumpInhibitorElementManager> {

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JumpInhibitorUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return elementCollectionManager.isActive(); //we will treat working as 'charging' for now
	}

	@Override
	public float getBasePowerConsumption() {
		assert (false);
		return 0;
	}

	@Override
	public float getPowerConsumption() {
		return (float) getPowerConsumedPerSecondResting();
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return (float) getPowerConsumedPerSecondResting();
	}

	@Override
	public float getReloadTimeMs() {
		return 100;//getEffects()[RELOAD].getValue();
	}

	@Override
	public float getInitializationTime() {
		return 100;
	}

	//	/**
	//	 * @return the damage
	//	 */
	//	public float getDamage() {
	//
	//		return ((size())*JumpDriveElementManager.CHARGE_NEEDED_FOR_JUMP);BASE_DISTANCE_SECTORS
	//	}
	@Override
	public float getDistanceRaw() {
		return 1;//getEffects()[DISTANCE].getValue();
	}
	@Override
	public float getFiringPower() {
		return 0;
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_POWER_CONSUMPTION,(double)size() * (double)JumpInhibitorElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING);
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_POWER_CONSUMPTION,(double)size() * (double)JumpInhibitorElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_CHARGING);
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.OTHERS;
	}
	@Override
	public float getDamage() {
		return 0;
	}
}