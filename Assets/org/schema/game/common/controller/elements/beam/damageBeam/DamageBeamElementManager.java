package org.schema.game.common.controller.elements.beam.damageBeam;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.beam.BeamElementManager;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.Combinable;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.combination.DamageBeamCombinationAddOn;
import org.schema.game.common.controller.elements.combination.modifier.BeamUnitModifier;
import org.schema.game.common.controller.elements.config.FloatReactorDualConfigElement;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.BEAM;
import static org.schema.game.common.controller.elements.ammo.damagebeam.DamageBeamCapacityElementManager.BEAM_CAPACITY_RELOAD_MODE;

public class DamageBeamElementManager extends BeamElementManager<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> implements BlockActivationListenerInterface, Combinable<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager, BeamCombiSettings>, WeaponElementManagerInterface {

	static final double[] weightedLookupTable = new double[300];
	@ConfigurationElement(name = "DamageReductionPerArmorValueMult")
	public static float DAMAGE_REDUCTION_PER_ARMOR_VALUE_MULT = 1;
	@ConfigurationElement(name = "DamagePerHitLinear")
	public static FloatReactorDualConfigElement DAMAGE_PER_HIT_LINEAR = new FloatReactorDualConfigElement();

	@ConfigurationElement(name = "DamagePerHitExp")
	public static float DAMAGE_PER_HIT_EXP = 0.33f;

	@ConfigurationElement(name = "DamagePerHitExpMult")
	public static float DAMAGE_PER_HIT_EXP_MULT = 100f;

	@ConfigurationElement(name = "AdditiveDamage")
	public static float ADDITIVE_DAMAGE = 50;
	@ConfigurationElement(name = "PowerConsumptionPerTick")
	public static float POWER_CONSUMPTION = 10;

	@ConfigurationElement(name = "Distance")
	public static float DISTANCE = 30;

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

	@ConfigurationElement(name = "AdditionalPowerConsumptionPerUnitMult")
	public static float ADDITIONAL_POWER_CONSUMPTION_PER_UNIT_MULT = 100.0f;

	@ConfigurationElement(name = "RailHitMultiplierParent")
	public static double RAIL_HIT_MULTIPLIER_PARENT = 3f;

	@ConfigurationElement(name = "RailHitMultiplierChild")
	public static double RAIL_HIT_MULTIPLIER_CHILD = 3f;

	@ConfigurationElement(name = "EffectConfiguration")
	public static InterEffectSet basicEffectConfiguration = new InterEffectSet();

	@ConfigurationElement(name = "LatchOn")
	public static int LATCH_ON = 1;

	@ConfigurationElement(name = "CheckLatchConnection")
	public static int CHECK_LATCH_CONNECTION = 1;

	@ConfigurationElement(name = "Penetration")
	public static int PENETRATION = 1;

	@ConfigurationElement(name = "AcidDamagePercentage")
	public static float ACID_DAMAGE_PERCENTAGE = 1;

	@ConfigurationElement(name = "FriendlyFire")
	public static int FRIENDLY_FIRE = 1;

	@ConfigurationElement(name = "Aimable")
	public static int AIMABLE = 1;

	@ConfigurationElement(name = "ChargeTime")
	public static float CHARGE_TIME = 1;

	@ConfigurationElement(name = "MinEffectiveValue")
	public static float MIN_EFFECTIVE_VALUE = 1;

	@ConfigurationElement(name = "MinEffectiveRange")
	public static float MIN_EFFECTIVE_RANGE = 1;

	@ConfigurationElement(name = "MaxEffectiveValue")
	public static float MAX_EFFECTIVE_VALUE = 1;

	@ConfigurationElement(name = "MaxEffectiveRange")
	public static float MAX_EFFECTIVE_RANGE = 1;

	@ConfigurationElement(name = "DropShieldsOnCharging")
	public static boolean DROP_SHIELDS_ON_CHARGING = false;

	@ConfigurationElement(name = "BaseCapacityUsedPerTick")
	public static float BASE_CAPACITY_USED_PER_TICK = 1;

	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamage")
	public static float ADDITIONAL_CAPACITY_USED_PER_DAMAGE = 10;

	@ConfigurationElement(name = "PossibleZoom")
	public static float POSSIBLE_ZOOM = 0;

	@Override
	public double getRailHitMultiplierParent() {
		return RAIL_HIT_MULTIPLIER_PARENT;
	}

	@Override
	public double getRailHitMultiplierChild() {
		return RAIL_HIT_MULTIPLIER_CHILD;
	}

	private final WeaponStatisticsData tmpOutput = new WeaponStatisticsData();

	private final DamageBeamCombinationAddOn addOn;

	public DamageBeamElementManager(SegmentController segmentController) {
		super(ElementKeyMap.DAMAGE_BEAM_COMPUTER, ElementKeyMap.DAMAGE_BEAM_MODULE, segmentController);
		addOn = new DamageBeamCombinationAddOn(this, (GameStateInterface) segmentController.getState());
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.DAMAGE_BEAM_MODULE);
	}

	@Override
	protected String getTag() {
		return "damagebeam";
	}

	@Override
	public DamageBeamCollectionManager getNewCollectionManager(SegmentPiece position, Class<DamageBeamCollectionManager> clazz) {
		return new DamageBeamCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Damage Beam System Collective");
	}

	@Override
	public CombinationAddOn<DamageBeamUnit, DamageBeamCollectionManager, ? extends DamageBeamElementManager, BeamCombiSettings> getAddOn() {
		return addOn;
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(DamageBeamUnit firingUnit, DamageBeamCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		if (effectCol != null) {
			col.setEffectTotal(effectCol.getTotalSize());
		} else {
			col.setEffectTotal(0);
		}
		float damage = firingUnit.getDamage();
		float distance = firingUnit.getDistance();
		float powerConsumption = firingUnit.getPowerConsumption();
		float tickRate = firingUnit.getTickRate();
		// float split = 1;
		float mode = 1;
		float effectRatio = 0;
		float powerEffectConsumption = 1;
		float coolDown = firingUnit.getCoolDownSec();
		float burstTime = firingUnit.getBurstTime();
		if (supportCol != null) {
			BeamUnitModifier<?> gui = (BeamUnitModifier<?>) getAddOn().getGUI(col, firingUnit, supportCol, effectCol);
			// split = gui.outputSplit;
			distance = gui.outputDistance;
			damage = gui.outputDamagePerHit;
			powerConsumption = gui.outputPowerConsumption;
			coolDown = gui.outputCoolDown;
			burstTime = gui.outputBurstTime;
		}
		if (effectCol != null) {
			EffectElementManager<?, ?, ?> effect = (EffectElementManager<?, ?, ?>) effectCol.getElementManager();
			effectRatio = CombinationAddOn.getRatio(col, effectCol);
		}
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Beam Unit"), firingUnit, new ModuleValueEntry(Lng.str("Damage/Tick"), StringTools.formatPointZero(damage)), new ModuleValueEntry(Lng.str("Tick Rate"), StringTools.formatPointZero(tickRate)), new ModuleValueEntry(Lng.str("Dmg/sec"), StringTools.formatPointZero((damage) * (1.0F / Math.max(0.0001f, tickRate)))), new ModuleValueEntry(Lng.str("Range"), StringTools.formatPointZero(distance)), new ModuleValueEntry(Lng.str("CoolDown"), Lng.str("%s sec", StringTools.formatPointZero(coolDown))), new ModuleValueEntry(Lng.str("Burst Time"), Lng.str("%s sec", StringTools.formatPointZero(burstTime))), new ModuleValueEntry(Lng.str("PowerConsumptionResting"), firingUnit.getPowerConsumedPerSecondResting()), new ModuleValueEntry(Lng.str("PowerConsumptionCharging"), firingUnit.getPowerConsumedPerSecondCharging()), new ModuleValueEntry(Lng.str("Effect Ratio(%):"), StringTools.formatPointZero(effectRatio)));
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

	public WeaponStatisticsData getStatistics(DamageBeamUnit firingUnit, DamageBeamCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol, WeaponStatisticsData output) {
		if (effectCol != null) {
			col.setEffectTotal(effectCol.getTotalSize());
		} else {
			col.setEffectTotal(0);
		}
		output.damage = firingUnit.getBaseBeamPower();
		output.speed = WeaponElementManagerInterface.MAX_SPEED;
		output.tickRate = firingUnit.getTickRate();
		output.burstTime = firingUnit.getBurstTime();
		output.distance = firingUnit.getDistance();
		output.reload = firingUnit.getReloadTimeMs();
		output.powerConsumption = (float) firingUnit.getPowerConsumedPerSecondCharging();
		output.split = 1;
		output.mode = 1;
		output.effectRatio = 0;
		if (supportCol != null) {
			BeamUnitModifier<?> gui = (BeamUnitModifier<?>) getAddOn().getGUI(col, firingUnit, supportCol, effectCol);
			// output.split = gui.outputSplit;
			output.distance = gui.outputDistance;
			output.damage = gui.outputDamagePerHit;
			output.powerConsumption = gui.outputPowerConsumption;
			// output.coolDown = gui.outputCoolDown;
			output.burstTime = gui.outputBurstTime;
		// output.blastRadius = gui.outputBlastRadius;
		// output.mode = gui.outputMode;
		// output.damage = gui.outputDamage;
		// output.speed = gui.outputSpeed;
		// output.distance = gui.outputDistance;
		// output.reload = gui.outputReload;
		// output.powerConsumption = gui.outputPowerConsumption;
		// output.split = gui.outputSplit;
		}
		if (effectCol != null) {
			EffectElementManager<?, ?, ?> effect = (EffectElementManager<?, ?, ?>) effectCol.getElementManager();
			output.effectRatio = CombinationAddOn.getRatio(col, effectCol);
		}
		return output;
	}

	@Override
	public double calculateWeaponDamageIndex() {
		double dps = 0;
		for (DamageBeamCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (DamageBeamUnit w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				dps += (tmpOutput.damage / tmpOutput.split) * (1.0F / Math.max(0.0001f, tmpOutput.tickRate)) / (tmpOutput.reload / 1000d);
			}
		}
		return dps;
	}

	@Override
	public double calculateWeaponRangeIndex() {
		double range = 0;
		double c = 0;
		for (DamageBeamCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (DamageBeamUnit w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				range += tmpOutput.distance;
				c++;
			}
		}
		return c > 0 ? range / c : 0;
	}

	@Override
	public double calculateWeaponHitPropabilityIndex() {
		double range = 0;
		for (DamageBeamCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (DamageBeamUnit w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				// rough estimate of mo much "stuff" is in the air and how fast it hits
				range += (WeaponElementManagerInterface.MAX_SPEED * tmpOutput.split) / (tmpOutput.reload / 1000d);
			}
		}
		return range;
	}

	@Override
	public double calculateWeaponSpecialIndex() {
		double special = 0;
		for (DamageBeamCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (DamageBeamUnit w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
			// special += tmpOutput.stop +
			// tmpOutput.pull +
			// tmpOutput.push +
			// tmpOutput.bonusShields +
			// tmpOutput.bonusBlocks +
			// tmpOutput.explosiveRadius +
			// tmpOutput.powerConsumptionBonus +
			// tmpOutput.armorEfficiency +
			// tmpOutput.addPowerDamage;
			}
		}
		return special;
	}

	@Override
	public double calculateWeaponPowerConsumptionPerSecondIndex() {
		double powerConsumption = 0;
		for (DamageBeamCollectionManager a : getCollectionManagers()) {
			ControlBlockElementCollectionManager<?, ?, ?> supportCollectionManager = a.getSupportCollectionManager();
			ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = a.getEffectCollectionManager();
			for (DamageBeamUnit w : a.getElementCollections()) {
				getStatistics(w, a, supportCollectionManager, effectCollectionManager, tmpOutput);
				powerConsumption += w.getPowerConsumption();
			}
		}
		return powerConsumption;
	}

    public boolean checkCapacity(float needed) {
		if (getSegmentController() == null) return false;

		SegmentController root = getSegmentController().railController.getRoot();
		if (root instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> mc = ((ManagedSegmentController<?>) root).getManagerContainer();
			return mc.getAmmoCapacity(BEAM) >= needed;
		}
		return false;
    }

	private final AmmoWeaponDrawReloadListener reloadListener = new AmmoWeaponDrawReloadListener(BEAM,BEAM_CAPACITY_RELOAD_MODE);

	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		handleReload(iconPos, iconSize, controllerPos, reloadListener);
	}
}
