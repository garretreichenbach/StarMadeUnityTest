package org.schema.game.common.controller.elements.ammo.damagebeam;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.UnitCalcStyle;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.data.SegmentPiece;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponCapacityReloadMode.SINGLE;

public class DamageBeamCapacityElementManager extends AmmoCapacityElementManager<DamageBeamCapacityUnit, DamageBeamCapacityCollectionManager, DamageBeamCapacityElementManager>{
	private static final byte VERSION = 2;
	
	@ConfigurationElement(name = "BasicDamageBeamCapacity")
	public static float BEAM_CAPACITY_BASIC= 0;
    
	@ConfigurationElement(name = "DamageBeamCapacityReloadMode")
	public static WeaponCapacityReloadMode BEAM_CAPACITY_RELOAD_MODE = SINGLE;
	
	@ConfigurationElement(name = "DamageBeamCapacityPerSec")
	public static float BEAM_CAPACITY_PER_SEC = 0;
	
	@ConfigurationElement(name = "DamageBeamCapacityReloadConstant")
	public static float BEAM_CAPACITY_RELOAD_CONSTANT = 300;
	
	@ConfigurationElement(name = "DamageBeamCapacityReloadResetOnFireManual")
	public static boolean BEAM_CAPACITY_RESET_ON_FIRE_MANUAL = false;
	@ConfigurationElement(name = "DamageBeamCapacityReloadResetOnFireAI")
	public static boolean BEAM_CAPACITY_RESET_ON_FIRE_AI = true;
	
	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float POWER_CONSUMPTION_RESTING = 0;
	
	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float POWER_CONSUMPTION_CHARGING = 0;

	@ConfigurationElement(name = "DamageBeamCapacityCalcStyle", description = "LINEAR, EXP, LOG")
	public static UnitCalcStyle BEAM_CAPACITY_CALC_STYLE = UnitCalcStyle.LINEAR;
	
	//linear
	@ConfigurationElement(name = "DamageBeamCapacityPerBlock")
	public static float BEAM_CAPACITY_PER_BLOCK = 0.5f;
	//exp
	@ConfigurationElement(name = "DamageBeamCapacityExp")
	public static float BEAM_CAPACITY_EXP = 0.5f;
	@ConfigurationElement(name = "DamageBeamCapacityExpMult")
	public static float BEAM_CAPACITY_EXP_MULT = 0.5f;
	
	//double exp
	@ConfigurationElement(name = "DamageBeamCapacityExpFirstHalf")
	public static float BEAM_CAPACITY_EXP_FIRST_HALF = 0.5f;
	@ConfigurationElement(name = "DamageBeamCapacityExpMultFirstHalf")
	public static float BEAM_CAPACITY_EXP_MULT_FIRST_HALF = 0.5f;
	
	@ConfigurationElement(name = "DamageBeamCapacityExpThreshold")
	public static float BEAM_CAPACITY_EXP_THRESHOLD = 500000f;
	
	@ConfigurationElement(name = "DamageBeamCapacityExpSecondHalf")
	public static float BEAM_CAPACITY_EXP_SECOND_HALF = 0.5f;
	@ConfigurationElement(name = "DamageBeamCapacityExpMultSecondHalf")
	public static float BEAM_CAPACITY_EXP_MULT_SECOND_HALF = 0.5f;
	
	//log
	@ConfigurationElement(name = "DamageBeamCapacityLogFactor")
	public static float BEAM_CAPACITY_LOG_FACTOR = 0.5f;
	@ConfigurationElement(name = "DamageBeamCapacityLogOffset")
	public static float BEAM_CAPACITY_LOG_OFFSET = 0.5f;

	public DamageBeamCapacityElementManager(SegmentController segmentController) {
		super(segmentController, DamageBeamCapacityCollectionManager.class);

		timer = BEAM_CAPACITY_RELOAD_CONSTANT;
	}

	@Override
	protected String getTag() {
		return "damagebeamcapacity";
	}

	@Override
	public float getBasicCapacity() {
		return BEAM_CAPACITY_BASIC;
	}

	@Override
	public WeaponCapacityReloadMode getReloadMode() {
		return BEAM_CAPACITY_RELOAD_MODE;
	}

	@Override
	public float getAmmoReloadPerCapacity() {
		return BEAM_CAPACITY_PER_SEC;
	}

	@Override
	public float getAmmoReloadAllTime() {
		return BEAM_CAPACITY_RELOAD_CONSTANT;
	}

	@Override
	public boolean ammoReloadResetsOnManualFire() {
		return BEAM_CAPACITY_RESET_ON_FIRE_MANUAL;
	}

	@Override
	public boolean ammoReloadResetsOnAIFire() {
		return BEAM_CAPACITY_RESET_ON_FIRE_AI;
	}

	@Override
	public double getPowerConsumptionPerBlockResting() {
		return POWER_CONSUMPTION_RESTING;
	}

	@Override
	public double getPowerConsumptionPerBlockCharging() {
		return POWER_CONSUMPTION_CHARGING;
	}

	@Override
	public UnitCalcStyle getCapacityCalcStyle() {
		return BEAM_CAPACITY_CALC_STYLE;
	}

	@Override
	public float getCapacityPerBlockLinear() {
		return BEAM_CAPACITY_PER_BLOCK;
	}

	@Override
	public float getCapacityExp() {
		return BEAM_CAPACITY_EXP;
	}

	@Override
	public float getCapacityExpMult() {
		return BEAM_CAPACITY_EXP_MULT;
	}

	@Override
	public float getCapacityDoubleExpFirstHalf() {
		return BEAM_CAPACITY_EXP_FIRST_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultFirstHalf() {
		return BEAM_CAPACITY_EXP_MULT_FIRST_HALF;
	}

	@Override
	public float getCapacityExponentThreshold() {
		return BEAM_CAPACITY_EXP_THRESHOLD;
	}

	@Override
	public float getCapacityDoubleExpSecondHalf() {
		return BEAM_CAPACITY_EXP_SECOND_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultSecondHalf() {
		return BEAM_CAPACITY_EXP_MULT_SECOND_HALF;
	}

	@Override
	public float getCapacityLogFactor() {
		return BEAM_CAPACITY_LOG_FACTOR;
	}

	@Override
	public float getCapacityLogOffset() {
		return BEAM_CAPACITY_LOG_OFFSET;
	}

	@ConfigurationElement(name = "MultitaskPowerPenaltyPerSizeRatio",description = "Extra power consumption when multiple ammo reserves are below full. Calculated as (1 + (penaltyWeap1*min(ratioWeap1,cap) + (penaltyWeap2*min(ratioWeap2,cap))")
	public static float FULL_MULTITASK_PENALTY = 0.08f;
	@ConfigurationElement(name = "MultitaskPenaltyCapRatio",description = "Multitask penalty stops increasing when other ammo systems reach at least this size ratio.")
	public static float MULTITASK_PENALTY_CAP_RATIO = 0.5f;

	@Override
	public float getFullMultitaskPenalty() {
		return FULL_MULTITASK_PENALTY;
	}

	@Override
	public float getMaxRatioForMultitaskPenalty() {
		return MULTITASK_PENALTY_CAP_RATIO;
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.BEAM;
	}

	@Override
	public DamageBeamCapacityCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<DamageBeamCapacityCollectionManager> clazz) {
		return new DamageBeamCapacityCollectionManager(getSegmentController(), this);
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.BEAMS;
	}

	@Override
	public String getName() {
		return "Damage Beam Capacity Element Manager";
	}
}
