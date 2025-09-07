package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;

public class BatteryActiveValueClientToServerUpdate extends BooleanValueUpdate {

	
	
	
	public BatteryActiveValueClientToServerUpdate() {
		super();
		deligateToClient = true;
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		((PowerManagerInterface) o).getPowerAddOn().setBatteryActive(this.val);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = ((PowerManagerInterface) o).getPowerAddOn().isBatteryActive();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.POWER_BATTERY_ACTIVE;
	}

}
