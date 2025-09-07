package org.schema.game.common.controller.elements.beam.harvest;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamElementManager;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.config.FloatReactorDualConfigElement;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class SalvageElementManager extends BeamElementManager<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> {

	@ConfigurationElement(name = "SalvageDamageNeededPerBlock")
	public static float SALVAGE_DAMAGE_NEEDED_PER_BLOCK = 3.0f;

	@ConfigurationElement(name = "SalvageDamagePerHit")
	public static FloatReactorDualConfigElement SALVAGE_DAMAGE_PER_HIT = new FloatReactorDualConfigElement();

	@ConfigurationElement(name = "TickRate")
	public static float TICK_RATE = 100.0f;

	@ConfigurationElement(name = "PowerConsumptionPerTick")
	public static float POWER_CONSUMPTION = 10;

	@ConfigurationElement(name = "Distance")
	public static float DISTANCE = 30;

	@ConfigurationElement(name = "CoolDown")
	public static float COOL_DOWN = 3.0f;

	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float REACTOR_POWER_CONSUMPTION_RESTING = 10;

	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float REACTOR_POWER_CONSUMPTION_CHARGING = 10;

	@ConfigurationElement(name = "BurstTime")
	public static float BURST_TIME = 3.0f;

	@ConfigurationElement(name = "InitialTicks")
	public static float INITIAL_TICKS = 1.0f;

	@ConfigurationElement(name = "RailHitMultiplierParent")
	public static double RAIL_HIT_MULTIPLIER_PARENT = 3.0f;

	@ConfigurationElement(name = "RailHitMultiplierChild")
	public static double RAIL_HIT_MULTIPLIER_CHILD = 3.0f;

	@ConfigurationElement(name = "SalvageBeamRadiusCap", description = "Maximum size of Salvage Beam radius scaling. Default value is 10, but you can go higher if you have a beefy computer.")
	public static int SALVAGE_BEAM_SCALAR_RADIUS_CAP = 10;

	public SalvageElementManager(SegmentController segmentController) {
		super(ElementKeyMap.SALVAGE_CONTROLLER_ID, ElementKeyMap.SALVAGE_ID, segmentController);
	}

	@Override
	public double getRailHitMultiplierParent() {
		return RAIL_HIT_MULTIPLIER_PARENT;
	}

	@Override
	public double getRailHitMultiplierChild() {
		return RAIL_HIT_MULTIPLIER_CHILD;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.SALVAGE_ID);
	}

	@Override
	protected String getTag() {
		return "salvagebeam";
	}

	@Override
	public SalvageBeamCollectionManager getNewCollectionManager(SegmentPiece position, Class<SalvageBeamCollectionManager> clazz) {
		return new SalvageBeamCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Salvage System Collective");
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableControllableBeamElementManager#getGUIUnitValues(org.schema.game.common.data.element.BeamUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(SalvageUnit firingUnit, SalvageBeamCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		float damage = firingUnit.getBeamPower();
		float tickRate = firingUnit.getTickRate();
		float distance = firingUnit.getDistance();
		float powerConsumption = firingUnit.getPowerConsumption();
		// float split = 1;
		float mode = 1;
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Beam Unit"), firingUnit, new ModuleValueEntry(Lng.str("SalvagePower"), StringTools.formatPointZero(damage)), new ModuleValueEntry(Lng.str("TickRate"), StringTools.formatPointZeroZero(tickRate)), new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(distance)), new ModuleValueEntry(Lng.str("PowerConsumptionResting"), firingUnit.getPowerConsumedPerSecondResting()), new ModuleValueEntry(Lng.str("PowerConsumptionCharging"), firingUnit.getPowerConsumedPerSecondCharging()));
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

	public double getMiningScore() {
		return totalSize;
	}

	@Override
	public CombinationAddOn<SalvageUnit, SalvageBeamCollectionManager, ? extends SalvageElementManager, BeamCombiSettings> getAddOn() {
		return null;
	}

	//@Override
	//protected void fireSoundEvent(SalvageUnit c) {
	//	/*AudioController.fireAudioEvent("BEAM_FIRE", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.BEAM, AudioTags.SALVAGE, AudioTags.FIRE }, AudioParam.START, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c))*/
	//	AudioController.fireAudioEventID(877, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c));
	//}
}
