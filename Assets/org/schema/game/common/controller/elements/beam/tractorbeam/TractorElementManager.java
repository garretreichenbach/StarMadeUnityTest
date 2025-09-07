package org.schema.game.common.controller.elements.beam.tractorbeam;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.TagModuleUsableInterface;
import org.schema.game.common.controller.elements.beam.BeamElementManager;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.config.FloatReactorDualConfigElement;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class TractorElementManager extends BeamElementManager<TractorUnit, TractorBeamCollectionManager, TractorElementManager> implements TagModuleUsableInterface {

	public static final String TAG_ID = "TRM";

	@ConfigurationElement(name = "TractorMassPerBlock")
	public static FloatReactorDualConfigElement TRACTOR_MASS_PER_BLOCK = new FloatReactorDualConfigElement();

	@ConfigurationElement(name = "TickRate")
	public static float TICK_RATE = 100f;

	@ConfigurationElement(name = "Distance")
	public static float DISTANCE = 30;

	@ConfigurationElement(name = "CoolDown")
	public static float COOL_DOWN = 3f;

	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float REACTOR_POWER_CONSUMPTION_RESTING = 10;

	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float REACTOR_POWER_CONSUMPTION_CHARGING = 10;

	@ConfigurationElement(name = "BurstTime")
	public static float BURST_TIME = 3f;

	@ConfigurationElement(name = "InitialTicks")
	public static float INITIAL_TICKS = 1f;

	@ConfigurationElement(name = "ForceToMassMax")
	public static float FORCE_TO_MASS_MAX = 10f;

	@ConfigurationElement(name = "RailHitMultiplierParent")
	public static double RAIL_HIT_MULTIPLIER_PARENT = 3f;

	@ConfigurationElement(name = "RailHitMultiplierChild")
	public static double RAIL_HIT_MULTIPLIER_CHILD = 3f;

	@Override
	public double getRailHitMultiplierParent() {
		return RAIL_HIT_MULTIPLIER_PARENT;
	}

	@Override
	public double getRailHitMultiplierChild() {
		return RAIL_HIT_MULTIPLIER_CHILD;
	}

	@Override
	public void doShot(TractorUnit c, TractorBeamCollectionManager m, ShootContainer shootContainer, PlayerState playerState, float beamTimeout, Timer timer, final boolean focusBeamOnClient) {
		// ai/logic caused shot
		m.calculateTractorModeFromLogic();
		super.doShot(c, m, shootContainer, playerState, beamTimeout, timer, focusBeamOnClient);
	}

	public TractorElementManager(SegmentController segmentController) {
		super(ElementKeyMap.TRACTOR_BEAM_COMPUTER, ElementKeyMap.TRACTOR_BEAM, segmentController);
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.TRACTOR_BEAM);
	}

	@Override
	protected String getTag() {
		return "tractorbeam";
	}

	@Override
	public TractorBeamCollectionManager getNewCollectionManager(SegmentPiece position, Class<TractorBeamCollectionManager> clazz) {
		return new TractorBeamCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Tractor Beam System Collective");
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableControllableBeamElementManager#getGUIUnitValues(org.schema.game.common.data.element.BeamUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(TractorUnit firingUnit, TractorBeamCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		float damage = firingUnit.getBeamPower();
		float tickRate = firingUnit.getTickRate();
		float distance = firingUnit.getDistance();
		float powerConsumption = firingUnit.getPowerConsumption();
		float split = 1;
		float mode = 1;
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Beam Unit"), firingUnit, new ModuleValueEntry(Lng.str("TractorPower"), StringTools.formatPointZero(damage)), new ModuleValueEntry(Lng.str("TickRate"), StringTools.formatPointZeroZero(tickRate)), new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(distance)), new ModuleValueEntry(Lng.str("PowerConsumptionResting"), firingUnit.getPowerConsumedPerSecondResting()), new ModuleValueEntry(Lng.str("PowerConsumptionCharging"), firingUnit.getPowerConsumedPerSecondCharging()));
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
	public String getTagId() {
		return TAG_ID;
	}

	@Override
	public float getInitialTicks() {
		return INITIAL_TICKS;
	}

	public double getMiningScore() {
		return totalSize;
	}

	@Override
	public CombinationAddOn<TractorUnit, TractorBeamCollectionManager, ? extends TractorElementManager, BeamCombiSettings> getAddOn() {
		return null;
	}

	@Override
	public TractorBeamMetaDataDummy getDummyInstance() {
		return new TractorBeamMetaDataDummy();
	}

	//@Override
	//protected void fireSoundEvent(TractorUnit c) {
	//	/*AudioController.fireAudioEvent("BEAM_FIRE", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.BEAM, AudioTags.TRACTOR, AudioTags.FIRE }, AudioParam.START, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c))*/
	//	AudioController.fireAudioEventID(880, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c));
	//}
}
