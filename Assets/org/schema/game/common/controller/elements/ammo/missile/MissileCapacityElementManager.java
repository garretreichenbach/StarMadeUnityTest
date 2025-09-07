package org.schema.game.common.controller.elements.ammo.missile;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.UnitCalcStyle;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.data.SegmentPiece;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponCapacityReloadMode.*;

public class MissileCapacityElementManager extends AmmoCapacityElementManager<MissileCapacityUnit, MissileCapacityCollectionManager, MissileCapacityElementManager>{
	private static final byte VERSION = 2;
	
	@ConfigurationElement(name = "BasicMissileCapacity")
	public static float MISSILE_CAPACITY_BASIC= 0;
    
	@ConfigurationElement(name = "MissileCapacityReloadMode")
	public static WeaponCapacityReloadMode MISSILE_CAPACITY_RELOAD_MODE = ALL;
	
	@ConfigurationElement(name = "MissileCapacityPerSec")
	public static float MISSILE_CAPACITY_PER_SEC = 0;
	
	@ConfigurationElement(name = "MissileCapacityReloadConstant")
	public static float MISSILE_CAPACITY_RELOAD_CONSTANT = 300;
	
	@ConfigurationElement(name = "MissileCapacityReloadResetOnFireManual")
	public static boolean MISSILE_CAPACITY_RESET_ON_FIRE_MANUAL = false;
	@ConfigurationElement(name = "MissileCapacityReloadResetOnFireAI")
	public static boolean MISSILE_CAPACITY_RESET_ON_FIRE_AI = true;
	
	
	
	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float POWER_CONSUMPTION_RESTING = 0;
	
	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float POWER_CONSUMPTION_CHARGING = 0;

	@ConfigurationElement(name = "MissileCapacityCalcStyle", description = "LINEAR, EXP, LOG")
	public static UnitCalcStyle MISSILE_CAPACITY_CALC_STYLE = UnitCalcStyle.LINEAR;
	
	//linear
	@ConfigurationElement(name = "MissileCapacityPerBlock")
	public static float MISSILE_CAPACITY_PER_BLOCK = 0.5f;
	//exp
	@ConfigurationElement(name = "MissileCapacityExp")
	public static float MISSILE_CAPACITY_EXP = 0.5f;
	@ConfigurationElement(name = "MissileCapacityExpMult")
	public static float MISSILE_CAPACITY_EXP_MULT = 0.5f;
	
	//double exp
	@ConfigurationElement(name = "MissileCapacityExpFirstHalf")
	public static float MISSILE_CAPACITY_EXP_FIRST_HALF = 0.5f;
	@ConfigurationElement(name = "MissileCapacityExpMultFirstHalf")
	public static float MISSILE_CAPACITY_EXP_MULT_FIRST_HALF = 0.5f;
	
	@ConfigurationElement(name = "MissileCapacityExpThreshold")
	public static float MISSILE_CAPACITY_EXP_THRESHOLD = 500000f;
	
	@ConfigurationElement(name = "MissileCapacityExpSecondHalf")
	public static float MISSILE_CAPACITY_EXP_SECOND_HALF = 0.5f;
	@ConfigurationElement(name = "MissileCapacityExpMultSecondHalf")
	public static float MISSILE_CAPACITY_EXP_MULT_SECOND_HALF = 0.5f;
	
	//log
	@ConfigurationElement(name = "MissileCapacityLogFactor")
	public static float MISSILE_CAPACITY_LOG_FACTOR = 0.5f;
	@ConfigurationElement(name = "MissileCapacityLogOffset")
	public static float MISSILE_CAPACITY_LOG_OFFSET = 0.5f;

	public MissileCapacityElementManager(SegmentController segmentController) {
		super(segmentController, MissileCapacityCollectionManager.class);

		timer = MISSILE_CAPACITY_RELOAD_CONSTANT;
	}

	@Override
	protected String getTag() {
		return "missilecapacity";
	}

	@Override
	public float getBasicCapacity() {
		return MISSILE_CAPACITY_BASIC;
	}

	@Override
	public WeaponCapacityReloadMode getReloadMode() {
		return MISSILE_CAPACITY_RELOAD_MODE;
	}

	@Override
	public float getAmmoReloadPerCapacity() {
		return MISSILE_CAPACITY_PER_SEC;
	}

	@Override
	public float getAmmoReloadAllTime() {
		return MISSILE_CAPACITY_RELOAD_CONSTANT;
	}

	@Override
	public boolean ammoReloadResetsOnManualFire() {
		return MISSILE_CAPACITY_RESET_ON_FIRE_MANUAL;
	}

	@Override
	public boolean ammoReloadResetsOnAIFire() {
		return MISSILE_CAPACITY_RESET_ON_FIRE_AI;
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
		return MISSILE_CAPACITY_CALC_STYLE;
	}

	@Override
	public float getCapacityPerBlockLinear() {
		return MISSILE_CAPACITY_PER_BLOCK;
	}

	@Override
	public float getCapacityExp() {
		return MISSILE_CAPACITY_EXP;
	}

	@Override
	public float getCapacityExpMult() {
		return MISSILE_CAPACITY_EXP_MULT;
	}

	@Override
	public float getCapacityDoubleExpFirstHalf() {
		return MISSILE_CAPACITY_EXP_FIRST_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultFirstHalf() {
		return MISSILE_CAPACITY_EXP_MULT_FIRST_HALF;
	}

	@Override
	public float getCapacityExponentThreshold() {
		return MISSILE_CAPACITY_EXP_THRESHOLD;
	}

	@Override
	public float getCapacityDoubleExpSecondHalf() {
		return MISSILE_CAPACITY_EXP_SECOND_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultSecondHalf() {
		return MISSILE_CAPACITY_EXP_MULT_SECOND_HALF;
	}

	@Override
	public float getCapacityLogFactor() {
		return MISSILE_CAPACITY_LOG_FACTOR;
	}

	@Override
	public float getCapacityLogOffset() {
		return MISSILE_CAPACITY_LOG_OFFSET;
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
		return WeaponType.MISSILE;
	}

	@Override
	public MissileCapacityCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<MissileCapacityCollectionManager> clazz) {
		return new MissileCapacityCollectionManager(getSegmentController(), this);
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.MISSILES;
	}

	@Override
	public String getName() {
		return "Missile Capacity Element Manager";
	}
}
