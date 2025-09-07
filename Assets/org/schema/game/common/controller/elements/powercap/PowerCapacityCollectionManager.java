package org.schema.game.common.controller.elements.powercap;

import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class PowerCapacityCollectionManager extends ElementCollectionManager<PowerCapacityUnit, PowerCapacityCollectionManager, VoidElementManager<PowerCapacityUnit, PowerCapacityCollectionManager>> {

	private double maxPower;

	public PowerCapacityCollectionManager(
			SegmentController segController, VoidElementManager<PowerCapacityUnit, PowerCapacityCollectionManager> em) {
		super(ElementKeyMap.POWER_CAP_ID, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<PowerCapacityUnit> getType() {
		return PowerCapacityUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}
	@Override
	public boolean isDetailedElementCollections() {
		return false;
	}
	@Override
	public PowerCapacityUnit getInstance() {
		return new PowerCapacityUnit();
	}

	@Override
	protected void onChangedCollection() {
		//		System.err.println("ON ADD POWER OF "+absPos);
		refreshMaxPower();
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("Total Capacity"), StringTools.formatPointZero(maxPower))};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Power Tank System");
	}

	/**
	 * @return the maxPower
	 */
	public double getMaxPower() {
		return maxPower;
	}

	/**
	 * @param maxPower the maxPower to set
	 */
	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	private void refreshMaxPower() {
		maxPower = 0;
		for (PowerCapacityUnit p : getElementCollections()) {
			double power = Math.pow(p.size(), VoidElementManager.POWER_TANK_CAPACITY_POW) * VoidElementManager.POWER_TANK_CAPACITY_LINEAR;
			maxPower += power;
		}
		((PowerManagerInterface) (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())).getPowerAddOn().setMaxPower(maxPower);
	}
	@Override
	public float getSensorValue(SegmentPiece connected){
		PowerAddOn shieldAddOn = ((PowerManagerInterface)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer()).getPowerAddOn();
		return  (float) Math.min(1f, shieldAddOn.getPower() / Math.max(0.0001f, (shieldAddOn.getMaxPower())));
	}
}
