package org.schema.game.common.controller.elements.jumpdrive;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;

public class JumpDriveUnit extends FiringUnit<JumpDriveUnit, JumpDriveCollectionManager, JumpDriveElementManager> {

	private float getBaseConsume() {
		return 0; //handled by element manager
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JumpDriveUnit " + super.toString();
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
		return getBaseConsume();
	}

	@Override
	public float getReloadTimeMs() {
		return JumpDriveElementManager.RELOAD_AFTER_USE_MS;//getEffects()[RELOAD].getValue();
	}

	@Override
	public float getInitializationTime() {
		return JumpDriveElementManager.RELOAD_AFTER_USE_MS;
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
		return JumpDriveElementManager.BASE_DISTANCE_SECTORS;//getEffects()[DISTANCE].getValue();
	}
	@Override
	public float getFiringPower() {
		return 0;
	}
	
	//NOT USED IN NORMAL SITUATIONS
	@Override
	public double getPowerConsumedPerSecondResting() {
		double powCons = size();
		
		return powCons;
	}

	//NOT USED IN NORMAL SITUATIONS
	@Override
	public double getPowerConsumedPerSecondCharging() {
		double powCons = size();
		
		return powCons;
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.JUMP_DRIVE;
	}

	@Override
	public float getDamage() {
		return 0;
	}
}
