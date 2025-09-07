package org.schema.game.common.controller.elements.rail.massenhancer;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class RailMassEnhancerCollectionManager extends ElementCollectionManager<RailMassEnhancerUnit, RailMassEnhancerCollectionManager, VoidElementManager<RailMassEnhancerUnit, RailMassEnhancerCollectionManager>> implements PowerConsumer{

	private boolean on = true;
	private float powered;

	public RailMassEnhancerCollectionManager(
			SegmentController segController, VoidElementManager<RailMassEnhancerUnit, RailMassEnhancerCollectionManager> em) {
		super(ElementKeyMap.RAIL_MASS_ENHANCER, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<RailMassEnhancerUnit> getType() {
		return RailMassEnhancerUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public RailMassEnhancerUnit getInstance() {
		return new RailMassEnhancerUnit();
	}

	@Override
	protected void onChangedCollection() {
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ElementCollectionManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {

		if(getSegmentController().isUsingPowerReactors()){
			on = powered >= 0.999999999f;

		} else {
			if (getContainer() instanceof PowerManagerInterface) {
				on = ((PowerManagerInterface) getContainer()).getPowerAddOn().consumePower(getTotalSize() * VoidElementManager.RAIL_MASS_ENHANCER_POWER_CONSUMED_PER_ENHANCER, timer) > 0;
			} else {
				on = false;
			}

			
		}
		super.update(timer);

	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("Total Blocks: "), String.valueOf(getTotalSize())),
				new ModuleValueEntry(Lng.str("Mass Load Free: "), String.valueOf(VoidElementManager.RAIL_MASS_ENHANCER_FREE_MASS)),
				new ModuleValueEntry(Lng.str("Mass Load/Enhancer: "), String.valueOf(VoidElementManager.RAIL_MASS_ENHANCER_MASS_ADDED_PER_ENHANCER)),
				new ModuleValueEntry(Lng.str("Powered: "), on ? Lng.str("yes") : Lng.str("no")),
				new ModuleValueEntry(Lng.str("Max Total Mass Enhanced Currently: "), getTotalMassAllowed()),

		};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Rail Mass Enhancer System");
	}

	public float getTotalMassAllowed() {

		//only do any enhancement when the rail enhancers have enough power provided
		int size = on ? getTotalSize() : 0;

		return (VoidElementManager.RAIL_MASS_ENHANCER_FREE_MASS + size * VoidElementManager.RAIL_MASS_ENHANCER_MASS_ADDED_PER_ENHANCER);
	}

	public float getRailPercent(float dockedMass) {
		dockedMass -= getTotalMassAllowed();
		if (dockedMass <= 0) {
			return 1.0f;
		}

		float slowDown = dockedMass * VoidElementManager.RAIL_MASS_ENHANCER_PERCENT_COST_PER_MASS_ABOVE_ENHANCER_PROVIDED;

		return Math.min(1f, Math.max(0.05f, 1f - slowDown));
	}

	public boolean isOn() {
		return on;
	}

	private double getReactorPowerUsage(){
		double p = (double) VoidElementManager.RAIL_MASS_ENHANCER_REACTOR_POWER_CONSUMPTION_CHARGING * (double) getTotalSize();

		return getConfigManager().apply(StatusEffectType.RAIL_ENHANCER_POWER_EFFICIENCY, p);
	}
	
	@Override
	public double getPowerConsumedPerSecondResting() {
		return getReactorPowerUsage();
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getReactorPowerUsage();
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return true;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.DOCKS;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}
	@Override
	public void dischargeFully() {
	}
}
