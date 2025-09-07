package org.schema.game.common.controller.elements;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.HpConditionList;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

import java.io.File;

public class VoidElementManager<
		E extends ElementCollection<E, CM, VoidElementManager<E, CM>>,
		CM extends ElementCollectionManager<E, CM, VoidElementManager<E, CM>>>
		extends UsableControllableSingleElementManager<E, CM, VoidElementManager<E, CM>> {

	@ConfigurationElement(name = "ShieldEffectConfiguration")
	public static InterEffectSet shieldEffectConfiguration = new InterEffectSet();

	@ConfigurationElement(name = "BasicEffectConfiguration")
	public static InterEffectSet basicEffectConfiguration = new InterEffectSet();

	@ConfigurationElement(name = "ArmorEffectConfiguration")
	public static InterEffectSet armorEffectConfiguration = new InterEffectSet();

	@ConfigurationElement(name = "ArmorHPMultiplier")
	public static float ARMOR_HP_MULTIPLIER = 20.0f;

	@ConfigurationElement(name = "ArmorHPMaxDamageAbsorptionMultiplier", description = "Maximum multiplier to armor absorption at 100% AHP. E.g.: if the armor formula lets through 1/3 of an incoming damage tick, and this value is 10.0, if the ship has 100% AHP the armor block will take 1/30 of the original incoming damage.")
	public static float ARMOR_HP_MAX_ARMOR_EFFECTIVENESS_MULTIPLIER = 10.0f;

	@ConfigurationElement(name = "ArmorHPLossPerDamageAbsorbed", description = "Block damage absorbed by AHP will be multiplied by this factor, and applied as AHP damage")
	public static float ARMOR_HP_LOST_PER_DAMAGE_ABSORBED = 1.0f;

	@ConfigurationElement(name = "BaseArmorHPBleedthroughStart")
	public static float BASE_ARMOR_HP_BLEEDTHROUGH_START = 1.0f;

	@ConfigurationElement(name = "MinArmorHPBleedthrough")
	public static float MIN_ARMOR_HP_BLEEDTHROUGH_START = 0.5f;

	@ConfigurationElement(name = "IndividualBlockEffectArmorOnShieldHit")
	public static boolean individualBlockEffectArmorOnShieldHit;

	@ConfigurationElement(name = "VolumeMassMultiplier")
	public static float VOLUME_MASS_MULT = 0;

	@ConfigurationElement(name="EnableLowDamage", description="Enables Low Damage Chamber, which is disabled by default.")
	public static boolean ENABLE_LOW_DAMAGE = false;

	@ConfigurationElement(name="EnableHighDamage", description="Enables High Damage Chamber, which is disabled by default.")
	public static boolean ENABLE_HIGH_DAMAGE = false;

	@ConfigurationElement(name = "DefensiveEffectMaxPercentMassMult", description = "multiplication of mass used as max percent for defensive effects")
	public static float DEVENSIVE_EFFECT_MAX_PERCENT_MASS_MULT = 0;

	@ConfigurationElement(name = "ShieldExtraCapacityMultPerUnit", description = "size of each unit gets multiplied with this value")
	public static float SHIELD_EXTRA_CAPACITY_MULT_PER_UNIT = 1;

	@ConfigurationElement(name = "ShieldExtraRechargeMultPerUnit", description = "size of each unit gets multiplied with this value")
	public static float SHIELD_EXTRA_RECHARGE_MULT_PER_UNIT = 1;

	@ConfigurationElement(name = "ShieldDoInitialWithoutFromCore", description = "")
	public static boolean SHIELD_INITIAL_CORE = true;

	@ConfigurationElement(name = "ShieldCapacityInitial", description = "Initial Capacity")
	public static double SHIELD_CAPACITY_INITIAL = 5000;

	@ConfigurationElement(name = "ShieldRechargeInitial", description = "Initial Recharge")
	public static double SHIELD_RECHARGE_INITIAL = 5000;

	@ConfigurationElement(name = "ShieldCapacityPow", description = "((x*pre)^pow)*total")
	public static double SHIELD_CAPACITY_POW = 0.66666;

	@ConfigurationElement(name = "ShieldCapacityPreMul", description = "((x*pre)^pow)*total")
	public static double SHIELD_CAPACITY_PRE_POW_MUL = 3.5;

	@ConfigurationElement(name = "ShieldCapacityTotalMul", description = "((x*pre)^pow)*total")
	public static double SHIELD_CAPACITY_TOTAL_MUL = 350;

	@ConfigurationElement(name = "ShieldRechargePow", description = "((x*pre)^pow)*total")
	public static double SHIELD_RECHARGE_POW = 0.5;

	@ConfigurationElement(name = "ShieldRechargePreMul", description = "((x*pre)^pow)*total")
	public static double SHIELD_RECHARGE_PRE_POW_MUL = 5;

	@ConfigurationElement(name = "ShieldRechargeTotalMul", description = "((x*pre)^pow)*total")
	public static double SHIELD_RECHARGE_TOTAL_MUL = 50;

	@ConfigurationElement(name = "ShieldRechargeCycleTime")
	public static double SHIELD_RECHARGE_CYCLE_TIME = 1;

	@ConfigurationElement(name = "ShieldRecoveryTimeAfterOutage", description = "time in seconds for shield to start recharge again after reaching 0")
	public static int SHIELD_RECOVERY_TIME_IN_SEC = 15;

	@ConfigurationElement(name = "ShieldDirectRecoveryTime", description = "")
	public static int SHIELD_DIRECT_RECOVERY_TIME_IN_SEC = 3;

	@ConfigurationElement(name = "ShieldRecoveryMultPerPercent", description = "")
	public static float SHIELD_RECOVERY_NERF_MULT_PER_PERCENT = 0.25f;
	@ConfigurationElement(name = "ShieldRecoveryMult", description = "")
	public static float SHIELD_RECOVERY_NERF_MULT = 0.25f;

	@ConfigurationElement(name = "ShieldRechargePowerConsumption")
	public static int SHIELD_RECHARGE_POWER_CONSUMPTION = 1;

	@ConfigurationElement(name = "ShieldFullPowerConsumption")
	public static int SHIELD_FULL_POWER_CONSUMPTION = 1;

	@ConfigurationElement(name = "ShieldDockTransferLimit", description = "Up to which fill status a rail docked entity up in the chain (towards root) will take the hit")
	public static double SHIELD_DOCK_TRANSFER_LIMIT = 0.0;

	@ConfigurationElement(name = "PowerDivFactor")
	public static double POWER_DIV_FACTOR = 0.333;

	@ConfigurationElement(name = "PowerCeiling")
	public static double POWER_CEILING = 1000000;

	@ConfigurationElement(name = "PowerGrowth")
	public static double POWER_GROWTH = 1.000696;

	@ConfigurationElement(name = "PowerLinearGrowth")
	public static double POWER_LINEAR_GROWTH = 25;

	@ConfigurationElement(name = "PowerRecoveryTime")
	public static long POWER_RECOVERY_TIME = 1000;

	@ConfigurationElement(name = "PowerBaseCapacity")
	public static int POWER_FIXED_BASE_CAPACITY = 20000;

	@ConfigurationElement(name = "PowerTankCapacityLinear")
	public static float POWER_TANK_CAPACITY_LINEAR = 1;

	@ConfigurationElement(name = "PowerTankCapacityPow")
	public static float POWER_TANK_CAPACITY_POW = 1.75f;

	@ConfigurationElement(name = "PowerBatteryCapacityLinear")
	public static float POWER_BATTERY_CAPACITY_LINEAR = 1;

	@ConfigurationElement(name = "PowerBatteryCapacityPow")
	public static float POWER_BATTERY_CAPACITY_POW = 1.75f;

	@ConfigurationElement(name = "PowerBatteryTransferPercentRatePerSec")
	public static float POWER_BATTERY_TRANSFER_RATE_PER_SEC = 1.75f;

//	@ConfigurationElement(name = "PowerBatteryDivFactor")
//	public static double POWER_BATTERY_DIV_FACTOR = 0.333;
//
//	@ConfigurationElement(name = "PowerBatteryCeiling")
//	public static double POWER_BATTERY_CEILING = 1000000;
//
//	@ConfigurationElement(name = "PowerBatteryGrowth")
//	public static double POWER_BATTERY_GROWTH = 1.000696;

	@ConfigurationElement(name = "PowerBatteryLinearGrowth")
	public static double POWER_BATTERY_LINEAR_GROWTH = 25;

	@ConfigurationElement(name = "PowerBatteryGroupMultiplier")
	public static double POWER_BATTERY_GROUP_MULTIPLIER = 25;
	@ConfigurationElement(name = "PowerBatteryGroupPow")
	public static double POWER_BATTERY_GROUP_POW = 25;

	@ConfigurationElement(name = "PowerBatteryGroupGrowth")
	public static double POWER_BATTERY_GROUP_GROWTH = 25;
	@ConfigurationElement(name = "PowerBatteryGroupCeiling")
	public static double POWER_BATTERY_GROUP_CEILING = 25;


	@ConfigurationElement(name = "PowerBatteryTurnedOnRegenMultiplier")
	public static double POWER_BATTERY_TURNED_ON_MULT = 25;
	@ConfigurationElement(name = "PowerBatteryTurnedOffRegenMultiplier")
	public static double POWER_BATTERY_TURNED_OFF_MULT = 25;


	@ConfigurationElement(name = "PowerBatteryTransferTopOffOnly")
	public static boolean POWER_BATTERY_TRANSFER_TOP_OFF_ONLY = false;

	@ConfigurationElement(name = "PowerBatteryExplosionsPerSecond")
	public static double POWER_BATTERY_EXPLOSION_RATE = 1;
	@ConfigurationElement(name = "PowerBatteryExplosionRadiusPerBlocksInGroup")
	public static double POWER_BATTERY_EXPLOSION_RADIUS_PER_BLOCKS = 25;
	@ConfigurationElement(name = "PowerBatteryExplosionRadiusMax")
	public static double POWER_BATTERY_EXPLOSION_RADIUS_MAX = 25;
	@ConfigurationElement(name = "PowerBatteryExplosionDamagePerBlocksInGroup")
	public static double POWER_BATTERY_EXPLOSION_DAMAGE_PER_BLOCKS = 25;
	@ConfigurationElement(name = "PowerBatteryExplosionDamageMax")
	public static double POWER_BATTERY_EXPLOSION_DAMAGE_MAX = 25;

	@ConfigurationElement(name = "PowerBatteryExplosionCountPerBlocksInGroup")
	public static double POWER_BATTERY_EXPLOSION_COUNT_PER_BLOCKS = 25;

	@ConfigurationElement(name = "PowerBatteryExplosionCountMax")
	public static int POWER_BATTERY_EXPLOSION_COUNT_MAX = 25;

	@ConfigurationElement(name = "PowerBatteryExplosionCountMaxPercent")
	public static double POWER_BATTERY_EXPLOSION_COUNT_PERCENT = 25;



	@ConfigurationElement(name = "EvadeEffectPowerConsumptionMult")
	public static float EVADE_EFFECT_POWER_CONSUMPTION_MULT = 1.75f;

	@ConfigurationElement(name = "TakeOffEffectPowerConsumptionMult")
	public static float TAKE_OFF_EFFECT_POWER_CONSUMPTION_MULT = 1.75f;

	@ConfigurationElement(name = "PersonalSalvageBeamBonus", description = "bonus mult of raw resources when salvaging with handheld salvage beam")
	public static float PERSONAL_SALVAGE_BEAM_BONUS = 2f;

	@ConfigurationElement(name = "RailMassEnhancerFreeMass")
	public static float RAIL_MASS_ENHANCER_FREE_MASS = 5f;

	@ConfigurationElement(name = "RailMassEnhancerMassPerEnhancer")
	public static float RAIL_MASS_ENHANCER_MASS_ADDED_PER_ENHANCER = 0.5f;

	@ConfigurationElement(name = "RailMassEnhancerPowerConsumedPerEnhancer")
	public static double RAIL_MASS_ENHANCER_POWER_CONSUMED_PER_ENHANCER = 10f;

	@ConfigurationElement(name = "RailMassEnhancerPercentCostPerMassAboveEnhancerProvided")
	public static float RAIL_MASS_ENHANCER_PERCENT_COST_PER_MASS_ABOVE_ENHANCER_PROVIDED = 0.01f;

	@ConfigurationElement(name = "RailMassEnhancerReactorPowerConsumptionResting")
	public static float RAIL_MASS_ENHANCER_REACTOR_POWER_CONSUMPTION_RESTING = 0;

	@ConfigurationElement(name = "RailMassEnhancerReactorPowerConsumptionCharging")
	public static float RAIL_MASS_ENHANCER_REACTOR_POWER_CONSUMPTION_CHARGING = 1f;

	@ConfigurationElement(name = "PlanetPowerBaseCapacity")
	public static int POWER_FIXED_PLANET_BASE_CAPACITY = 500;

	@ConfigurationElement(name = "AsteroidPowerBaseCapacity")
	public static int POWER_FIXED_ASTEROID_BASE_CAPACITY = 500;

	@ConfigurationElement(name = "ShipRebootTimeInSecPerMissingHpPercent")
	public static double SHIP_REBOOT_TIME_IN_SEC_PER_MISSING_HP_PERCENT = 1;

	@ConfigurationElement(name = "ShipRebootTimeMultiplierPerMass")
	public static double SHIP_REBOOT_TIME_MULTIPLYER_PER_MASS = 0.0001;

	@ConfigurationElement(name = "ShipRebootTimeMinSec")
	public static double SHIP_REBOOT_TIME_MIN_SEC = 30;

	@ConfigurationElement(name = "HpConditionTriggerList")
	public static final HpConditionList HP_CONDITION_TRIGGER_LIST = new HpConditionList();

	@ConfigurationElement(name = "HpDeductionLogFactor")
	public static float HP_DEDUCTION_LOG_FACTOR = 0;
	@ConfigurationElement(name = "HpDeductionLogOffset")
	public static float HP_DEDUCTION_LOG_OFFSET = 0;


	@ConfigurationElement(name = "StructureHpBlockMultiplier")
	public static double STRUCTURE_HP_BLOCK_MULTIPLIER = 1.0;


	@ConfigurationElement(name = "AITurretMinOrientationSpeed")
	public static float AI_TURRET_ORIENTATION_SPEED_MIN = 0.5f;

	@ConfigurationElement(name = "AITurretMaxOrientationSpeed")
	public static float AI_TURRET_ORIENTATION_SPEED_MAX = 3.0f;

	@ConfigurationElement(name = "AITurretOrientationSpeedDivByMass")
	public static float AI_TURRET_ORIENTATION_SPEED_DIV_BY_MASS = 30.0f;

	@ConfigurationElement(name = "ExplosionShieldDamageBonus", description = "")
	public static float EXPLOSION_SHIELD_DAMAGE_BONUS = 0;

	@ConfigurationElement(name = "ExplosionHullDamageBonus", description = "")
	public static float EXPLOSION_HULL_DAMAGE_BONUS = 0;


	//REACTOR POWER
	@ConfigurationElement(name = "ReactorChamberBlocksPerMainReactor")
	public static float REACTOR_CHAMBER_BLOCKS_PER_MAIN_REACTOR_AND_LEVEL = 0.5f;
	@ConfigurationElement(name = "ReactorConduitPowerConsuptionPerSec")
	public static float POWER_REACTOR_CONDUIT_POWER_CONSUMPTION_PER_SEC = 1;
	@ConfigurationElement(name = "ReactorSwitchCooldownSec")
	public static float REACTOR_SWITCH_COOLDOWN_SEC = 1;
	@ConfigurationElement(name = "ReactorMainCountMultiplier")
	public static float REACTOR_MAIN_COUNT_MULTIPLIER = 1;
	@ConfigurationElement(name = "ReactorPowerCapacityMultiplier")
	public static float REACTOR_POWER_CAPACITY_MULTIPLIER = 1;
	@ConfigurationElement(name = "ReactorRechargePercentPerSecond")
	public static float REACTOR_RECHARGE_PERCENT_PER_SECOND = 0.1f;
	@ConfigurationElement(name = "ReactorRechargeMultiplierWhenEmpty")
	public static float REACTOR_RECHARGE_EMPTY_MULTIPLIER = 0.1f;


	//REACTOR STABILIZER
	@ConfigurationElement(name = "ReactorStabilizerLinearFalloffOne")
	public static float REACTOR_STABILIZER_LINEAR_FALLOFF_ONE = 1;
	@ConfigurationElement(name = "ReactorStabilizerLinearFalloffZero")
	public static float REACTOR_STABILIZER_LINEAR_FALLOFF_ZERO = 1;
	@ConfigurationElement(name = "ReactorStabilizerFreeMainReactorBlocks")
	public static int REACTOR_STABILIZER_FREE_MAIN_REACTOR_BLOCKS = 1;
	@ConfigurationElement(name = "ReactorStabilizerDistanceTotalMult")
	public static float REACTOR_STABILIZER_DISTANCE_TOTAL_MULT = 2f;
	@ConfigurationElement(name = "ReactorStabilizationMultiplier")
	public static float REACTOR_STABILIZATION_MULTIPLIER = 1;
	@ConfigurationElement(name = "ReactorStabilizerDistanceLogLeveledSteps")
	public static boolean REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_STEPS = true;
	@ConfigurationElement(name = "ReactorStabilizerDistanceLogLeveledMultiplier")
	public static float REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_MULTIPLIER = 10f;
	@ConfigurationElement(name = "ReactorStabilizerDistanceLogLeveledExp")
	public static float REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_EXP = 1;
	@ConfigurationElement(name = "ReactorCalcStyle", description = "LINEAR, EXP, LOG, LOG_LEVELED")
	public static UnitCalcStyle REACTOR_CALC_STYLE = UnitCalcStyle.LINEAR;

	//REACTOR STABILIZER linear
	@ConfigurationElement(name = "ReactorStabilizerDistancePerMainReactorBlock", description = "ReactorStabilizerStartingDistance + blocks * ReactorStabilizerDistancePerMainReactorBlock")
	public static float REACTOR_STABILIZER_DISTANCE_PER_MAIN_REACTOR_BLOCK = 1;


//REACTOR STABILIZER exp
	@ConfigurationElement(name = "ReactorStabilizerDistanceExpMult", description = "ReactorStabilizerStartingDistance + (blocks ^ ReactorStabilizerDistanceExp) * ReactorStabilizerDistanceExp")
	public static float REACTOR_STABILIZER_DISTANCE_EXP_MULT = 1;
	@ConfigurationElement(name = "ReactorStabilizerDistanceExp")
	public static float REACTOR_STABILIZER_DISTANCE_EXP = 1;

	@ConfigurationElement(name = "ReactorStabilizerDistanceExpSoftcapMult")
	public static float REACTOR_STABILIZER_DISTANCE_EXP_SOFTCAP_MULT = 1;
	@ConfigurationElement(name = "ReactorStabilizerDistanceExpSoftcapExp")
	public static float REACTOR_STABILIZER_DISTANCE_EXP_SOFTCAP_EXP = 1;
	@ConfigurationElement(name = "ReactorStabilizerDistanceExpSoftCapBlocksStart")
	public static float REACTOR_STABILIZER_DISTANCE_EXP_SOFTCAP_BLOCKS_START = 1;

	//REACTOR STABILIZER log
	@ConfigurationElement(name = "ReactorStabilizerDistanceLogFactor", description = "ReactorStabilizerStartingDistance + (Log10(blocks) + ReactorStabilizerDistanceLogOffset) * ReactorStabilizerDistanceLogFactor")
	public static float REACTOR_STABILIZER_DISTANCE_LOG_FACTOR = 1;
	@ConfigurationElement(name = "ReactorStabilizerDistanceLogOffset")
	public static float REACTOR_STABILIZER_DISTANCE_LOG_OFFSET = 1;
	@ConfigurationElement(name = "ReactorStabilizerStartingDistance")
	public static float REACTOR_STABILIZER_STARTING_DISTANCE = -20;

	@ConfigurationElement(name = "StabilizerBonusCalc")
	public static StabBonusCalcStyle STABILIZER_BONUS_CALC = StabBonusCalcStyle.BY_SIDE;

	@ConfigurationElement(name = "ReactorStablizationAngleBonus2Groups")
	public static float STABILIZATION_ANGLE_BONUS_2_GROUPS = 1;
	@ConfigurationElement(name = "ReactorStablizationAngleBonus3Groups")
	public static float STABILIZATION_ANGLE_BONUS_3_GROUPS = 1;
	@ConfigurationElement(name = "ReactorStablizationAngleBonus4Groups")
	public static float STABILIZATION_ANGLE_BONUS_4_GROUPS = 1;
	@ConfigurationElement(name = "ReactorStablizationAngleBonus5Groups")
	public static float STABILIZATION_ANGLE_BONUS_5_GROUPS = 1;
	@ConfigurationElement(name = "ReactorStablizationAngleBonus6Groups")
	public static float STABILIZATION_ANGLE_BONUS_6_GROUPS = 1;


	@ConfigurationElement(name = "ReactorStablizationBonus2")
	public static float STABILIZATION_DIMENSION_BONUS_2 = 1;
	@ConfigurationElement(name = "ReactorStablizationBonus3")
	public static float STABILIZATION_DIMENSION_BONUS_3 = 1;
	@ConfigurationElement(name = "ReactorStablizationBonus4")
	public static float STABILIZATION_DIMENSION_BONUS_4 = 1;
	@ConfigurationElement(name = "ReactorStablizationBonus5")
	public static float STABILIZATION_DIMENSION_BONUS_5 = 1;
	@ConfigurationElement(name = "ReactorStablizationBonus6")
	public static float STABILIZATION_DIMENSION_BONUS_6 = 1;

//	@ConfigurationElement(name = "ReactorStabilizationShieldDamagePercentToReactorHpStart")
//	public static float REACTOR_STABILIZATION_SHIELD_DAMAGE_PERCENT_TO_REACTOR_HP_START = 1;
//
//	@ConfigurationElement(name = "ReactorStabilizationShieldDamagePercentToReactorHpEnd")
//	public static float REACTOR_STABILIZATION_SHIELD_DAMAGE_PERCENT_TO_REACTOR_HP_END = 0;
//
//	@ConfigurationElement(name = "ReactorStabilizationShieldDamagePercentToReactorHpMin")
//	public static float REACTOR_STABILIZATION_SHIELD_DAMAGE_PERCENT_TO_REACTOR_HP_MIN = 0;
//
//	@ConfigurationElement(name = "ReactorStabilizationShieldDamagePercentToReactorHpMax")
//	public static float REACTOR_STABILIZATION_SHIELD_DAMAGE_PERCENT_TO_REACTOR_HP_MAX = 1;
//
//
//	@ConfigurationElement(name = "ReactorStabilizationSystemDamagePercentToReactorHpStart")
//	public static float REACTOR_STABILIZATION_SYSTEM_DAMAGE_PERCENT_TO_REACTOR_HP_START = 1;
//
//	@ConfigurationElement(name = "ReactorStabilizationSystemDamagePercentToReactorHpEnd")
//	public static float REACTOR_STABILIZATION_SYSTEM_DAMAGE_PERCENT_TO_REACTOR_HP_END = 0;
//
//	@ConfigurationElement(name = "ReactorStabilizationSystemDamagePercentToReactorHpMin")
//	public static float REACTOR_STABILIZATION_SYSTEM_DAMAGE_PERCENT_TO_REACTOR_HP_MIN = 0;
//
//	@ConfigurationElement(name = "ReactorStabilizationSystemDamagePercentToReactorHpMax")
//	public static float REACTOR_STABILIZATION_SYSTEM_DAMAGE_PERCENT_TO_REACTOR_HP_MAX = 1;
//
//
//	@ConfigurationElement(name = "ReactorStabilizationBlockDamagePercentToReactorHpStart")
//	public static float REACTOR_STABILIZATION_BLOCK_DAMAGE_PERCENT_TO_REACTOR_HP_START = 1;
//
//	@ConfigurationElement(name = "ReactorStabilizationBlockDamagePercentToReactorHpEnd")
//	public static float REACTOR_STABILIZATION_BLOCK_DAMAGE_PERCENT_TO_REACTOR_HP_END = 0;
//
//	@ConfigurationElement(name = "ReactorStabilizationBlockDamagePercentToReactorHpMin")
//	public static float REACTOR_STABILIZATION_BLOCK_DAMAGE_PERCENT_TO_REACTOR_HP_MIN = 0;
//
//	@ConfigurationElement(name = "ReactorStabilizationBlockDamagePercentToReactorHpMax")
//	public static float REACTOR_STABILIZATION_BLOCK_DAMAGE_PERCENT_TO_REACTOR_HP_MAX = 1;


//	@ConfigurationElement(name = "ReactorStabilizationTriggerExplosionOnDamage")
//	public static float REACTOR_STABILIZATION_TRIGGER_EXPLOSION_ON_DAMAGE = 1;
//
//	@ConfigurationElement(name = "ReactorStabilizationTriggerExplosionOnDamageToType")
//	public static int REACTOR_STABILIZATION_TRIGGER_EXPLOSION_ON_DAMAGE_TO_TYPE = 1;
//
//
//	@ConfigurationElement(name = "ReactorStabilizationTriggerExplosionOnChargeOverload")
//	public static float REACTOR_STABILIZATION_TRIGGER_EXPLOSION_ON_CHARGE_OVERLOAD = 1;
//
//	@ConfigurationElement(name = "ReactorStabilizationTriggerExplosionOnChargeOverloadTimeSec")
//	public static float REACTOR_STABILIZATION_TRIGGER_EXPLOSION_ON_CHARGE_OVERLOAD_TIME_SEC = 1;

	@ConfigurationElement(name = "ReactorLowStabilizationExtraDamageStart")
	public static float REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_START = 1;

	@ConfigurationElement(name = "ReactorLowStabilizationExtraDamageEnd")
	public static float REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_END = 0.2f;

	@ConfigurationElement(name = "ReactorLowStabilizationExtraDamageStartDamage")
	public static float REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_START_DAMAGE = 1.0f;

	@ConfigurationElement(name = "ReactorLowStabilizationExtraDamageEndDamage")
	public static float REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_END_DAMAGE = 2.0f;


	//JUMPDRIVE
	@ConfigurationElement(name = "ReactorJumpPowerConsumptionRestingPerMass")
	public static float REACTOR_JUMP_POWER_CONSUMPTION_RESTING_PER_MASS = 0.0001f;
	@ConfigurationElement(name = "ReactorJumpPowerConsumptionChargingPerMass")
	public static float REACTOR_JUMP_POWER_CONSUMPTION_CHARGING_PER_MASS = 0.01f;
	@ConfigurationElement(name = "ReactorJumpDistanceDefault")
	public static float REACTOR_JUMP_DISTANCE_DEFAULT = 4f;
	@ConfigurationElement(name = "ReactorJumpChargeNeededInSecondsDefault")
	public static float REACTOR_JUMP_CHARGE_NEEDED_IN_SEC = 10;
	@ConfigurationElement(name = "ReactorJumpChargeNeededInSecondsExtraPerMass")
	public static float REACTOR_JUMP_CHARGE_NEEDED_IN_SEC_EXTRA_PER_MASS = 0.0001f;
	@ConfigurationElement(name = "ReactorJumpChargeNeededInSecondsLogFactor")
	public static float REACTOR_JUMP_CHARGE_NEEDED_IN_SEC_LOG_FACTOR = 0.5f;
	@ConfigurationElement(name = "ReactorJumpChargeNeededInSecondsLogOffset")
	public static float REACTOR_JUMP_CHARGE_NEEDED_IN_SEC_LOG_OFFSET = 0.5f;


	//STEALTH
	@ConfigurationElement(name = "ReactorStealthChargeNeeded")
	public static float STEALTH_CHARGE_NEEDED = 0;
	@ConfigurationElement(name = "ReactorStealthChargeConsumptionResting")
	public static float STEALTH_CONSUMPTION_RESTING = 0;
	@ConfigurationElement(name = "ReactorStealthChargeConsumptionCharging")
	public static float STEALTH_CONSUMPTION_CHARGING = 0;
	@ConfigurationElement(name = "ReactorStealthChargeConsumptionRestingAddedByMass")
	public static float STEALTH_CONSUMPTION_RESTING_ADDED_BY_MASS = 0;
	@ConfigurationElement(name = "ReactorStealthChargeConsumptionChargingAddedByMass")
	public static float STEALTH_CONSUMPTION_CHARGING_ADDED_BY_MASS = 0;
	@ConfigurationElement(name = "ReactorStealthDurationBasic")
	public static float STEALTH_DURATION_BASIC = 0;
	@ConfigurationElement(name = "ReactorStealthStrengthBasic")
	public static float STEALTH_STRENGTH_BASIC = 0;


	//RECON + SCANNER
	@ConfigurationElement(name = "ReactorScanChargeNeeded")
	public static float SCAN_CHARGE_NEEDED = 0;
	@ConfigurationElement(name = "ReactorScanChargeConsumptionResting")
	public static float SCAN_CONSUMPTION_RESTING = 0;
	@ConfigurationElement(name = "ReactorScanChargeConsumptionCharging")
	public static float SCAN_CONSUMPTION_CHARGING = 0;
	@ConfigurationElement(name = "ReactorScanChargeConsumptionRestingAddedByMass")
	public static float SCAN_CONSUMPTION_RESTING_ADDED_BY_MASS = 0;
	@ConfigurationElement(name = "ReactorScanChargeConsumptionChargingAddedByMass")
	public static float SCAN_CONSUMPTION_CHARGING_ADDED_BY_MASS = 0;
	@ConfigurationElement(name = "ReactorScanDurationBasic")
	public static float SCAN_DURATION_BASIC = 0;
	@ConfigurationElement(name = "ReactorScanStrengthBasic")
	public static float SCAN_STRENGTH_BASIC = 0;

	@ConfigurationElement(name = "ReconDifferenceMinCloaking")
	public static  int RECON_DIFFERENCE_MIN_CLOAKING = 0;
	@ConfigurationElement(name = "ReconDifferenceMinJamming")
	public static  int RECON_DIFFERENCE_MIN_JAMMING = 0;
	@ConfigurationElement(name = "ReconDifferenceMinReactor")
	public static  int RECON_DIFFERENCE_MIN_REACTOR = 0;
	@ConfigurationElement(name = "ReconDifferenceMinChambers")
	public static  int RECON_DIFFERENCE_MIN_CHAMBERS = 0;
	@ConfigurationElement(name = "ReconDifferenceMinWeapons")
	public static  int RECON_DIFFERENCE_MIN_WEAPONS = 0;


	//REACTOR EXPLOSION
	@ConfigurationElement(name = "ReactorExplosionStabilityMargin")
	public static double REACTOR_EXPLOSION_STABILITY = 1;
	@ConfigurationElement(name = "ReactorExplosionStabilityLossMult")
	public static double REACTOR_EXPLOSION_STABILITY_LOSS_MULT = 1;
	@ConfigurationElement(name = "ReactorExplosionsPerSecond")
	public static double REACTOR_EXPLOSION_RATE = 1;
	@ConfigurationElement(name = "ReactorExplosionRadiusPerBlocksInGroup")
	public static double REACTOR_EXPLOSION_RADIUS_PER_BLOCKS = 25;
	@ConfigurationElement(name = "ReactorExplosionRadiusMax")
	public static double REACTOR_EXPLOSION_RADIUS_MAX = 25;
	@ConfigurationElement(name = "ReactorExplosionDamagePerBlocksInGroup")
	public static double REACTOR_EXPLOSION_DAMAGE_PER_BLOCKS = 25;
	@ConfigurationElement(name = "ReactorExplosionDamageMax")
	public static double REACTOR_EXPLOSION_DAMAGE_MAX = 25;
	@ConfigurationElement(name = "ReactorExplosionCountPerBlocksInGroup")
	public static double REACTOR_EXPLOSION_COUNT_PER_BLOCKS = 25;
	@ConfigurationElement(name = "ReactorExplosionCountMax")
	public static int REACTOR_EXPLOSION_COUNT_MAX = 25;
	@ConfigurationElement(name = "ReactorExplosionCountMaxPercent")
	public static double REACTOR_EXPLOSION_COUNT_PERCENT = 25;

	@ConfigurationElement(name = "ReactorModuleDischargeMargin")
	public static double REACTOR_MODULE_DISCHARGE_MARGIN = 0.05f;



	//SHIELDS
	@ConfigurationElement(name = "ShieldLocalCapacityPerBlock")
	public static float SHIELD_LOCAL_CAPACITY_PER_BLOCK = 400;
	@ConfigurationElement(name = "ShieldLocalRechargePerBlock")
	public static float SHIELD_LOCAL_RECHARGE_PER_BLOCK = 20;
	@ConfigurationElement(name = "ShieldLocalDefaultCapacity")
	public static float SHIELD_LOCAL_DEFAULT_CAPACITY = 200;
	@ConfigurationElement(name = "ShieldLocalRadiusCalcStyle", description = "LINEAR, EXP, LOG")
	public static UnitCalcStyle SHIELD_LOCAL_RADIUS_CALC_STYLE = UnitCalcStyle.LINEAR;

	@ConfigurationElement(name = "ReactorLevelCalcStyle", description = "LOG10, LINEAR")
	public static ReactorLevelCalcStyle REACTOR_LEVEL_CALC_STYLE = ReactorLevelCalcStyle.LOG10;

	@ConfigurationElement(name = "ReactorLevelCalcLinearBlocksNeededPerLevel")
	public static int REACTOR_LEVEL_CALC_LINEAR_BLOCKS_NEEDED_PER_LEVEL = 0;

	@ConfigurationElement(name = "ShieldLocalDefaultRadius")
	public static float SHIELD_LOCAL_DEFAULT_RADIUS = 50;
	@ConfigurationElement(name = "ShieldUpkeepPerSecondOfTotalCapacity")
	public static float SHIELD_LOCAL_UPKEEP_PER_SECOND_OF_TOTAL_CAPACITY = 0.001f;
	@ConfigurationElement(name = "ShieldLocalPowerConsumptionPerRechargePerSecondResting")
	public static float SHIELD_LOCAL_CONSUMPTION_PER_CURRENT_RECHARGE_PER_SECOND_RESTING = 1f;
	@ConfigurationElement(name = "ShieldLocalPowerConsumptionPerRechargePerSecondCharging")
	public static float SHIELD_LOCAL_CONSUMPTION_PER_CURRENT_RECHARGE_PER_SECOND_CHARGING = 1f;

	@ConfigurationElement(name = "ShieldLocalRechargeUnderFireModeSec")
	public static float SHIELD_LOCAL_RECHARGE_UNDER_FIRE_MODE_SEC = 1f;

	@ConfigurationElement(name = "ShieldLocalRechargeUnderFireMinPercent")
	public static float SHIELD_LOCAL_RECHARGE_UNDER_FIRE_MIN_PERCENT = 1f;

	@ConfigurationElement(name = "ShieldLocalRechargeUnderFireStartAtCharged")
	public static float SHIELD_LOCAL_RECHARGE_UNDER_FIRE_START_AT_CHARGED = 1f;

	@ConfigurationElement(name = "ShieldLocalRechargeUnderFireEndAtCharged")
	public static float SHIELD_LOCAL_RECHARGE_UNDER_FIRE_END_AT_CHARGED = 1f;

	@ConfigurationElement(name = "ShieldLocalOnZeroShieldsRechargePreventionSec")
	public static float SHIELD_LOCAL_ON_ZERO_SHIELDS_RECHARGE_PREVENTION_SEC = 1f;
	@ConfigurationElement(name = "ShieldLocalHitAllOverlapping")
	public static boolean SHIELD_LOCAL_HIT_ALL_OVERLAPPING = true;
	@ConfigurationElement(name = "ShieldLocalMaxCapacityGroupsPerLocalShield")
	public static int SHIELD_LOCAL_MAX_CAPACITY_GROUPS_PER_LOCAL_SHIELD =  20;

	//SHIELDS linear
	@ConfigurationElement(name = "ShieldLocalRadiusPerRechargeBlock")
	public static float SHIELD_LOCAL_RADIUS_PER_RECHARGE_BLOCK = 0.5f;
	//SHIELDS exp
	@ConfigurationElement(name = "ShieldLocalRadiusExpMult")
	public static float SHIELD_LOCAL_RADIUS_EXP_MULT = 0.5f;
	@ConfigurationElement(name = "ShieldLocalRadiusExp")
	public static float SHIELD_LOCAL_RADIUS_EXP = 0.5f;
	//SHIELDS log
	@ConfigurationElement(name = "ShieldLocalRadiusLogFactor")
	public static float SHIELD_LOCAL_RADIUS_LOG_FACTOR = 0.5f;
	@ConfigurationElement(name = "ShieldLocalRadiusLogOffset")
	public static float SHIELD_LOCAL_RADIUS_LOG_OFFSET = 0.5f;


	@ConfigurationElement(name = "CollectionIntegrityStartValue")
	public static double COLLECTION_INTEGRITY_START_VALUE = 100f;

	@ConfigurationElement(name = "CollectionIntegrityBaseTouching0")
	public static double COLLECTION_INTEGRITY_BASE_TOUCHING_0 = -3f;

	@ConfigurationElement(name = "CollectionIntegrityBaseTouching1")
	public static double COLLECTION_INTEGRITY_BASE_TOUCHING_1 = -2f;

	@ConfigurationElement(name = "CollectionIntegrityBaseTouching2")
	public static double COLLECTION_INTEGRITY_BASE_TOUCHING_2 = -1f;

	@ConfigurationElement(name = "CollectionIntegrityBaseTouching3")
	public static double COLLECTION_INTEGRITY_BASE_TOUCHING_3 =  0f;

	@ConfigurationElement(name = "CollectionIntegrityBaseTouching4")
	public static double COLLECTION_INTEGRITY_BASE_TOUCHING_4 =  1f;

	@ConfigurationElement(name = "CollectionIntegrityBaseTouching5")
	public static double COLLECTION_INTEGRITY_BASE_TOUCHING_5 =  2f;

	@ConfigurationElement(name = "CollectionIntegrityBaseTouching6")
	public static double COLLECTION_INTEGRITY_BASE_TOUCHING_6 =  3f;

	@ConfigurationElement(name = "CollectionIntegrityMargin")
	public static double INTEGRITY_MARGIN =  0f;

	@ConfigurationElement(name = "CollectionIntegrityExplosionAmount")
	public static int COLLECTION_INTEGRITY_EXPLOSION_AMOUNT =  3;

	@ConfigurationElement(name = "CollectionIntegrityExplosionRadius")
	public static int COLLECTION_INTEGRITY_EXPLOSION_RADIUS =  3;


	//REACTOR POWER
	@ConfigurationElement(name = "OverheatTimerMin")
	public static long OVERHEAT_TIMER_MIN = 60;
	@ConfigurationElement(name = "OverheatTimerMax")
	public static long OVERHEAT_TIMER_MAX = 600;
	@ConfigurationElement(name = "OverheatTimerAddedSecondsPerBlock")
	public static float OVERHEAT_TIMER_ADDED_PER_BLOCK = 0.001f;

	//COLLECTION INTEGRITY EXPLOSION

	@ConfigurationElement(name = "CollectionIntegrityExplosionRate")
	public static long COLLECTION_INTEGRITY_EXPLOSION_RATE = 1L;


	@ConfigurationElement(name = "CollectionIntegrityExplosionDamagePerBlocksInGroup")
	public static double COLLECTION_INTEGRITY_DAMAGE_PER_BLOCKS = 25;
	@ConfigurationElement(name = "CollectionIntegrityExplosionDamageMax")
	public static double COLLECTION_INTEGRITY_DAMAGE_MAX = 25;

	@ConfigurationElement(name = "ReactorStabilizerGroupingProximity")
	public static float REACTOR_STABILIZER_GROUPING_PROXIMITY = 25;



	@ConfigurationElement(name = "ReactorStabilizerPathRadiusDefault")
	public static float REACTOR_STABILIZER_PATH_RADIUS_DEFAULT = 1f;

	@ConfigurationElement(name = "ReactorStabilizerPathRadiusPerLevel")
	public static float REACTOR_STABILIZER_PATH_RADIUS_PER_LEVEL = 0.1f;


	@ConfigurationElement(name = "ReactorStabilizationPowerEffectiveFull")
	public static float REACTOR_STABILIZATION_POWER_EFFECTIVE_FULL = 0.2f;

	@ConfigurationElement(name = "ReactorStabilizerGroupsMax")
	public static int REACTOR_STABILIZER_GROUPS_MAX = 20;


	public final static String configPath = "." + File.separator + "data" + File.separator + "config" + File.separator + "customBlockBehaviorConfigTemplate.xml";
	public final static String configPathHOWTO = "." + File.separator + "data" + File.separator + "config" + File.separator + "customBlockBehaviorConfigHOWTO.txt";







	@ConfigurationElement(name = "CollectionIntegrityUnderFireUpdateDelaySec")
	public static float COLLECTION_INTEGRITY_UNDER_FIRE_UPDATE_DELAY_SEC;



	@ConfigurationElement(name = "RepulseMultiplicator")
	public static float REPULSE_MULT = 1;

    @ConfigurationElement(name = "ArmorThicknessBonus")
    public static float ARMOR_THICKNESS_BONUS = 0;

    @ConfigurationElement(name = "ArmorBeamDamageResistance")
    public static float ARMOR_BEAM_DAMAGE_SCALING = 0;



//  Damage Dealt = (Damage Incoming^3)/((Armour Value In Line of Shot)^3+Damage Incoming^2)

    @ConfigurationElement(name = "ArmorCalcStyle")
    public static ArmorDamageCalcStyle ARMOR_CALC_STYLE = ArmorDamageCalcStyle.LINEAR;

    /** LINEAR ARMOR (CANNON) **/

    @ConfigurationElement(name = "CannonArmorFlatDamageReduction")
    public static float CANNON_ARMOR_FLAT_DAMAGE_REDUCTION = 0;
    @ConfigurationElement(name = "CannonArmorThicknessDamageReduction")
    public static float CANNON_ARMOR_THICKNESS_DAMAGE_REDUCTION = 0;

    @ConfigurationElement(name = "CannonArmorThicknessDamageReductionMax")
    public static float CANNON_ARMOR_THICKNESS_DAMAGE_REDUCTION_MAX = 0;

    /** LINEAR ARMOR (BEAM)**/

    @ConfigurationElement(name = "BeamArmorFlatDamageReduction")
    public static float BEAM_ARMOR_FLAT_DAMAGE_REDUCTION = 0;
    @ConfigurationElement(name = "BeamArmorThicknessDamageReduction")
    public static float BEAM_ARMOR_THICKNESS_DAMAGE_REDUCTION = 0;

    @ConfigurationElement(name = "BeamArmorThicknessDamageReductionMax")
    public static float BEAM_ARMOR_THICKNESS_DAMAGE_REDUCTION_MAX = 0;

	/** LINEAR ARMOR (MISSILE)**/

	@ConfigurationElement(name = "MissileArmorFlatDamageReduction")
	public static float MISSILE_ARMOR_FLAT_DAMAGE_REDUCTION = 0;
	@ConfigurationElement(name = "MissileArmorThicknessDamageReduction")
	public static float MISSILE_ARMOR_THICKNESS_DAMAGE_REDUCTION = 0;

	@ConfigurationElement(name = "MissileArmorThicknessDamageReductionMax")
	public static float MISSILE_ARMOR_THICKNESS_DAMAGE_REDUCTION_MAX = 0;


    /** EXPONENT ARMOR (CANNON)**/


    @ConfigurationElement(name = "CannonArmorExponentialIncomingExponent")
    public static float CANNON_ARMOR_EXPONENTIAL_INCOMING_EXPONENT = 0;

    @ConfigurationElement(name = "CannonArmorExponentialArmorValueTotalExponent")
    public static float CANNON_ARMOR_EXPONENTIAL_ARMOR_VALUE_TOTAL_EXPONENT = 0;

    @ConfigurationElement(name = "CannonArmorExponentialIncomingDamageAddedExponent")
    public static float CANNON_ARMOR_EXPONENTIAL_INCOMING_DAMAGE_ADDED_EXPONENT = 0;

    /** EXPONENT ARMOR (BEAM)**/

    @ConfigurationElement(name = "BeamArmorExponentialIncomingExponent")
    public static float BEAM_ARMOR_EXPONENTIAL_INCOMING_EXPONENT = 0;

    @ConfigurationElement(name = "BeamArmorExponentialArmorValueTotalExponent")
    public static float BEAM_ARMOR_EXPONENTIAL_ARMOR_VALUE_TOTAL_EXPONENT = 0;

    @ConfigurationElement(name = "BeamArmorExponentialIncomingDamageAddedExponent")
    public static float BEAM_ARMOR_EXPONENTIAL_INCOMING_DAMAGE_ADDED_EXPONENT = 0;

	/** EXPONENT ARMOR (MISSILE)**/

	@ConfigurationElement(name = "MissileArmorExponentialIncomingExponent")
	public static float MISSILE_ARMOR_EXPONENTIAL_INCOMING_EXPONENT = 0;

	@ConfigurationElement(name = "MissileArmorExponentialArmorValueTotalExponent")
	public static float MISSILE_ARMOR_EXPONENTIAL_ARMOR_VALUE_TOTAL_EXPONENT = 0;

	@ConfigurationElement(name = "MissileArmorExponentialIncomingDamageAddedExponent")
	public static float MISSILE_ARMOR_EXPONENTIAL_INCOMING_DAMAGE_ADDED_EXPONENT = 0;



    @ConfigurationElement(name = "ArmorOverPenetrationMarginMultiplicator")
    public static float ARMOR_OVER_PENETRATION_MARGIN_MULTIPLICATOR = 0;

	@ConfigurationElement(name = "NonArmorOverpenetrationMargin")
	public static float NON_ARMOR_OVER_PENETRATION_MARGIN = 0;


	@ConfigurationElement(name = "ReactorRebootMinCooldownSec")
	public static float REACTOR_REBOOT_MIN_COOLDOWN_SEC = 10;

	@ConfigurationElement(name = "ReactorRebootLogFactor")
	public static float REACTOR_REBOOT_LOG_FACTOR = 0.5f;

	@ConfigurationElement(name = "ReactorRebootLogOffset")
	public static float REACTOR_REBOOT_LOG_OFFSET = 0.5f;

	@ConfigurationElement(name = "ReactorRebootCooldownInSecPerMissingHpPercent")
	public static float REACTOR_REBOOT_SEC_PER_HP_PERCENT = 30;




	@ConfigurationElement(name = "ReactorStabilizationEnergyStreamHitCooldownPerDamageInSec")
	public static float REACTOR_STABILIZATION_ENERGY_STREAM_HIT_COOLDOWN_PER_DAMAGE_IN_SEC = 0.01f;

	@ConfigurationElement(name = "ReactorStabilizationEnergyStreamHitMinCooldownInSec")
	public static float REACTOR_STABILIZATION_ENERGY_STREAM_HIT_MIN_COOLDOWN_IN_SEC = 1;

	@ConfigurationElement(name = "ReactorStabilizationEnergyStreamHitMaxCooldownInSec")
	public static float REACTOR_STABILIZATION_ENERGY_STREAM_HIT_MAX_COOLDOWN_IN_SEC = 100;

	@ConfigurationElement(name = "ReactorStabilizationEnergyStreamDistance")
	public static float REACTOR_STABILIZATION_ENERGY_STREAM_DISTANCE = -1f;

	@ConfigurationElement(name = "ReactorStabilizationEnergyStreamHitMaxCooldownReactorEfficiency")
	public static float REACTOR_STABILIZATION_ENERGY_STREAM_HIT_COOLDOWN_REACTOR_EFFICIENCY = 0.2f;

	@ConfigurationElement(name = "AcidDamageArmorStoppedMargin")
	public static float ACID_DAMAGE_ARMOR_STOPPED_MARGIN = 0;

	@ConfigurationElement(name = "DisableRailCollisions")
	public static boolean DISABLE_RAIL_COLLISIONS = false;

	public static double getIntegrityBaseTouching(final int touching){
		switch(touching){
		case 0: return COLLECTION_INTEGRITY_BASE_TOUCHING_0;
		case 1: return COLLECTION_INTEGRITY_BASE_TOUCHING_1;
		case 2: return COLLECTION_INTEGRITY_BASE_TOUCHING_2;
		case 3: return COLLECTION_INTEGRITY_BASE_TOUCHING_3;
		case 4: return COLLECTION_INTEGRITY_BASE_TOUCHING_4;
		case 5: return COLLECTION_INTEGRITY_BASE_TOUCHING_5;
		case 6: return COLLECTION_INTEGRITY_BASE_TOUCHING_6;
		}
		throw new RuntimeException("Illegal amount of touching "+touching);
	}

	public VoidElementManager(SegmentController segmentController, Class<CM> clazz) {
		super(segmentController, clazz);
	}

	@Override
	public void onControllerChange() {

	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(E firingUnit, CM col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		assert (false);
		throw new IllegalArgumentException();
	}

	//	@Override
	//	public void handleSingleActivation(SegmentPiece controller) {
	//		//nothing to do
	//	}
	@Override
	protected String getTag() {
		return "general";
	}

	@Override
	public CM getNewCollectionManager(
			SegmentPiece position, Class<CM> clazz) {
		try {
			return clazz.getConstructor(SegmentController.class, VoidElementManager.class).newInstance(getSegmentController(), this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}



	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}

	public static boolean isUsingReactorDistance() {
		return REACTOR_STABILIZER_LINEAR_FALLOFF_ONE <= 0.000001f;
	}

	public static boolean hasAngleStabBonus() {
		return STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE && (STABILIZATION_ANGLE_BONUS_2_GROUPS + STABILIZATION_ANGLE_BONUS_3_GROUPS + STABILIZATION_ANGLE_BONUS_4_GROUPS + STABILIZATION_ANGLE_BONUS_5_GROUPS + STABILIZATION_ANGLE_BONUS_6_GROUPS > 0);
	}
	public static boolean hasAngleOrSideStabBonus() {
		return hasAngleStabBonus() || hasSideStabBonus();
	}
	public static boolean hasSideStabBonus() {
		return STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_SIDE && (STABILIZATION_DIMENSION_BONUS_2 + STABILIZATION_DIMENSION_BONUS_3 + STABILIZATION_DIMENSION_BONUS_4 + STABILIZATION_DIMENSION_BONUS_5 + STABILIZATION_DIMENSION_BONUS_6 > 0);
	}

}
