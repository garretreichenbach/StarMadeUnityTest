package org.schema.game.common.controller.elements.powerbattery;

import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;

public class PowerBatteryUnit extends ElementCollection<PowerBatteryUnit, PowerBatteryCollectionManager, VoidElementManager<PowerBatteryUnit, PowerBatteryCollectionManager>> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		Vector3i dim = new Vector3i();
		dim.sub(getMax(new Vector3i()), getMin(new Vector3i()));
		return ControllerManagerGUI.create(state, Lng.str("Auxiliary Power Module"), this,
				new ModuleValueEntry(Lng.str("Dimension"), dim),
				new ModuleValueEntry(Lng.str("Size"), size()),
				new ModuleValueEntry(Lng.str("Recharge"), StringTools.formatPointZero(recharge+ (size()* VoidElementManager.POWER_BATTERY_LINEAR_GROWTH))),
				new ModuleValueEntry(Lng.str("Capacity"), StringTools.formatPointZero(maxPower))
				
				);
	}
	@Override
	public boolean hasMesh(){
		return false;
	}
	private double recharge;
	private double maxPower;
	/**
	 * @return the recharge
	 */
	public double getRecharge() {
		return recharge;
	}

	public void refreshPowerCapabilities() {
		
		recharge = size();
		
		recharge = Math.pow(recharge * VoidElementManager.POWER_BATTERY_GROUP_MULTIPLIER, VoidElementManager.POWER_BATTERY_GROUP_POW);
		
		recharge = Math.max(1f, FastMath.logGrowth(recharge, VoidElementManager.POWER_BATTERY_GROUP_GROWTH, VoidElementManager.POWER_BATTERY_GROUP_CEILING));
		
		
		maxPower = Math.pow(size(), VoidElementManager.POWER_BATTERY_CAPACITY_POW) * VoidElementManager.POWER_BATTERY_CAPACITY_LINEAR;
		
	}

	public double getMaxPower() {
		return maxPower;
	}
}