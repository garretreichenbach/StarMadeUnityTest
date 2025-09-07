package org.schema.game.common.controller.elements.power;

import org.schema.game.common.controller.elements.ManagerUpdatableInterface;
import org.schema.game.common.controller.elements.powerbattery.PowerBatteryCollectionManager;
import org.schema.game.common.controller.elements.powercap.PowerCapacityCollectionManager;

/**
 * Old power management interface from Power 1.x. Only preserved for backwards compatibility.
 */
@Deprecated
public interface PowerManagerInterface {
	public PowerAddOn getPowerAddOn();

	public PowerCapacityCollectionManager getPowerCapacityManager();

	public PowerBatteryCollectionManager getPowerBatteryManager();

	public PowerCollectionManager getPowerManager();
	
	public void addUpdatable(ManagerUpdatableInterface m);
}
