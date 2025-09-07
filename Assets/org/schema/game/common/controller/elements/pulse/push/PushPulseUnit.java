package org.schema.game.common.controller.elements.pulse.push;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.pulse.PulseUnit;

public class PushPulseUnit
		extends PulseUnit<PushPulseUnit, PushPulseCollectionManager, PushPulseElementManager> {

	//	/* (non-Javadoc)
	//	 * @see org.schema.game.common.data.element.ElementCollection#onChangeFinished()
	//	 */
	//	@Override
	//	public void onChangeFinished() {
	//		super.onChangeFinished();
	//
	//		int xMin=getPosX(getMin());
	//		int yMin=getPosY(getMin());
	//		int zMin=getPosZ(getMin());
	//
	//		int xMax=getPosX(getMax());
	//		int yMax=getPosY(getMax());
	//		int zMax=getPosZ(getMax());
	//
	//
	//	}
	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
		//		return ControllerManagerGUI.create(state, "Pulse Module", this,
		//
		//				new ModuleValueEntry(Lng.str("Force",StringTools.formatPointZero(getPulsePower())),
		//				new ModuleValueEntry(Lng.str("Radius",StringTools.formatPointZero(getRadius())),
		//				new ModuleValueEntry(Lng.str("Reload",StringTools.formatPointZero(getReloadTime()))
		//
		//				);
	}

	@Override
	public float getRadius() {
		return PushPulseElementManager.BASE_RADIUS;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PushPulseUnit [significator=" + significator + "]";
	}

	@Override
	public float getPulsePower() {
		return PushPulseElementManager.BASE_FORCE;
	}

	@Override
	public float getDamageWithoutEffect() {
		return getPulsePower();
	}

	@Override
	public float getBasePulsePower() {
		return PushPulseElementManager.BASE_FORCE;
	}

	@Override
	public float getBasePowerConsumption() {
		return PushPulseElementManager.BASE_POWER_CONSUMPTION;
	}

	@Override
	public float getPowerConsumption() {
		return size() * PushPulseElementManager.BASE_POWER_CONSUMPTION;
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return size() * PushPulseElementManager.BASE_POWER_CONSUMPTION;
	}

	@Override
	public float getReloadTimeMs() {
		return PushPulseElementManager.BASE_RELOAD;
	}

	@Override
	public float getInitializationTime() {
		return PushPulseElementManager.BASE_RELOAD;
	}
	@Override
	public float getFiringPower() {
		return 0;
	}
	
	@Override
	public double getPowerConsumedPerSecondResting() {
		return (double)size() * (double)PushPulseElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return (double)size() * (double)PushPulseElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.PULSE;
	}
	@Override
	public float getDamage() {
		return 0;
	}
}