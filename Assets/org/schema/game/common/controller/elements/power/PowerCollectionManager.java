package org.schema.game.common.controller.elements.power;

import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class PowerCollectionManager extends ElementCollectionManager<PowerUnit, PowerCollectionManager, VoidElementManager<PowerUnit, PowerCollectionManager>> {

	private double recharge;

	public PowerCollectionManager(
			SegmentController segController, VoidElementManager<PowerUnit, PowerCollectionManager> em) {
		super(ElementKeyMap.POWER_ID_OLD, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<PowerUnit> getType() {
		return PowerUnit.class;
	}
	
	@Override
	public boolean isDetailedElementCollections() {
		return false;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public void update(Timer timer) {
	}

	@Override
	public PowerUnit getInstance() {
		return new PowerUnit();
	}

	@Override
	protected void onChangedCollection() {
		//		System.err.println("ON ADD POWER OF "+absPos);
		refreshMaxPower();
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("Total Recharge"), StringTools.formatPointZero(recharge))};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Power Recharge System");
	}

	/**
	 * (((1 / (1 + growth^-x))-0.5)*2.0)*max
	 */
	private void refreshMaxPower() {
		recharge = (0);
		int total = 0;
		for (int i = 0; i < getElementCollections().size(); i++) {
			PowerUnit p = getElementCollections().get(i);
			total += p.size();
			p.refreshPowerCapabilities();
			recharge = (recharge + p.getRecharge());
		}

		recharge = Math.max(1f, FastMath.logGrowth(recharge * VoidElementManager.POWER_DIV_FACTOR, VoidElementManager.POWER_GROWTH, VoidElementManager.POWER_CEILING));

		recharge += total * VoidElementManager.POWER_LINEAR_GROWTH;

		((PowerManagerInterface) (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())).getPowerAddOn().setRecharge(recharge);
	}
	@Override
	public float getSensorValue(SegmentPiece connected){
		PowerAddOn shieldAddOn = ((PowerManagerInterface)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer()).getPowerAddOn();
		return  (float) Math.min(1f, shieldAddOn.getPower() / Math.max(0.0001f, (shieldAddOn.getMaxPower())));
	}
}
