package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;

public class BatteryPowerExpectedValueUpdate extends IntValueUpdate {

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		((PowerManagerInterface) o).getPowerAddOn().setExpectedBatteryClient(this.val);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = ((PowerManagerInterface) o).getPowerAddOn().getExpectedBatterySize();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.POWER_BATTERY_EXPECTED;
	}

}
