package org.schema.game.common.controller.elements.beam.repair;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.BlockActivationListenerInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamElementManager;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.config.FloatReactorDualConfigElement;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class RepairElementManager extends BeamElementManager<RepairUnit, RepairBeamCollectionManager, RepairElementManager> implements BlockActivationListenerInterface {

	@ConfigurationElement(name = "RepairPerHit")
	public static FloatReactorDualConfigElement REPAIR_PER_HIT = new FloatReactorDualConfigElement();

	@ConfigurationElement(name = "PowerConsumptionPerTick")
	public static float POWER_CONSUMPTION = 10;

	@ConfigurationElement(name = "Distance")
	public static float DISTANCE = 1000;
	@ConfigurationElement(name = "TickRate")
	public static float TICK_RATE = 100f;

	@ConfigurationElement(name = "CoolDown")
	public static float COOL_DOWN = 3f;

	@ConfigurationElement(name = "BurstTime")
	public static float BURST_TIME = 3f;

	@ConfigurationElement(name = "InitialTicks")
	public static float INITIAL_TICKS = 1f;

	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float REACTOR_POWER_CONSUMPTION_RESTING = 10;

	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float REACTOR_POWER_CONSUMPTION_CHARGING = 10;

	@ConfigurationElement(name = "RailHitMultiplierParent")
	public static double RAIL_HIT_MULTIPLIER_PARENT = 3f;

	@ConfigurationElement(name = "RailHitMultiplierChild")
	public static double RAIL_HIT_MULTIPLIER_CHILD = 3f;

	@ConfigurationElement(name = "RepairOutOfCombatDelaySec")
	public static float REPAIR_OUT_OF_COMBAT_DELAY_SEC;

	@Override
	public double getRailHitMultiplierParent() {
		return RAIL_HIT_MULTIPLIER_PARENT;
	}

	@Override
	public double getRailHitMultiplierChild() {
		return RAIL_HIT_MULTIPLIER_CHILD;
	}

	public RepairElementManager(SegmentController segmentController) {
		super(ElementKeyMap.REPAIR_CONTROLLER_ID, ElementKeyMap.REPAIR_ID, segmentController);
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.REPAIR_ID);
	}

	protected boolean ignoreNonPhysical(BeamState con) {
		return false;
	}

	@Override
	protected String getTag() {
		return "repairbeam";
	}

	@Override
	public RepairBeamCollectionManager getNewCollectionManager(SegmentPiece position, Class<RepairBeamCollectionManager> clazz) {
		return new RepairBeamCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Repair Beam System Collective");
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableControllableBeamElementManager#getGUIUnitValues(org.schema.game.common.data.element.BeamUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(RepairUnit firingUnit, RepairBeamCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Repair Beam Unit"), firingUnit, new ModuleValueEntry(Lng.str("Repair"), StringTools.formatPointZero(firingUnit.getBeamPower())), new ModuleValueEntry(Lng.str("TickRate"), StringTools.formatPointZero(firingUnit.getTickRate())), new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(firingUnit.getDistance())), new ModuleValueEntry(Lng.str("Reload(ms)"), StringTools.formatPointZero(firingUnit.getReloadTimeMs())), new ModuleValueEntry(Lng.str("PowerConsumptionResting"), firingUnit.getPowerConsumedPerSecondResting()), new ModuleValueEntry(Lng.str("PowerConsumptionCharging"), firingUnit.getPowerConsumedPerSecondCharging()));
	}

	@Override
	public float getTickRate() {
		return TICK_RATE;
	}

	@Override
	public float getCoolDown() {
		return COOL_DOWN;
	}

	@Override
	public float getBurstTime() {
		return BURST_TIME;
	}

	@Override
	public float getInitialTicks() {
		return INITIAL_TICKS;
	}

	@Override
	public CombinationAddOn<RepairUnit, RepairBeamCollectionManager, ? extends RepairElementManager, BeamCombiSettings> getAddOn() {
		return null;
	}

	//@Override
	//protected void fireSoundEvent(RepairUnit c) {
	//	/*AudioController.fireAudioEvent("BEAM_FIRE", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.BEAM, AudioTags.REPAIR, AudioTags.FIRE }, AudioParam.START, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c))*/
	//	AudioController.fireAudioEventID(878, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c));
	//}
}
