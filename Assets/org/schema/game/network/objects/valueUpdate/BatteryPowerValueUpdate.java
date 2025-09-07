package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;

public class BatteryPowerValueUpdate extends DoubleValueUpdate {

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		((PowerManagerInterface) o).getPowerAddOn().setBatteryPower(this.val);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = ((PowerManagerInterface) o).getPowerAddOn().getBatteryPower();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.POWER_BATTERY;
	}

}
