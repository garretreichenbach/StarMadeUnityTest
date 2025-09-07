package org.schema.game.common.controller.elements.ammo.cannon;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.UnitCalcStyle;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.data.SegmentPiece;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponCapacityReloadMode.ALL;

public class CannonCapacityElementManager extends AmmoCapacityElementManager<CannonCapacityUnit, CannonCapacityCollectionManager, CannonCapacityElementManager>{
	private static final byte VERSION = 2;
	@ConfigurationElement(name = "BasicCannonCapacity")
	public static float CANNON_CAPACITY_BASIC= 0;
    
	@ConfigurationElement(name = "CannonCapacityReloadMode")
	public static WeaponCapacityReloadMode CANNON_CAPACITY_RELOAD_MODE = ALL;
	
	@ConfigurationElement(name = "CannonCapacityPerSec")
	public static float CANNON_CAPACITY_PER_SEC = 0;
	
	@ConfigurationElement(name = "CannonCapacityReloadConstant")
	public static float CANNON_CAPACITY_RELOAD_CONSTANT = 300;
	
	@ConfigurationElement(name = "CannonCapacityReloadResetOnFireManual")
	public static boolean CANNON_CAPACITY_RESET_ON_FIRE_MANUAL = false;
	@ConfigurationElement(name = "CannonCapacityReloadResetOnFireAI")
	public static boolean CANNON_CAPACITY_RESET_ON_FIRE_AI = true;
	
	
	
	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float POWER_CONSUMPTION_RESTING = 0;
	
	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float POWER_CONSUMPTION_CHARGING = 0;

	@ConfigurationElement(name = "CannonCapacityCalcStyle", description = "LINEAR, EXP, LOG")
	public static UnitCalcStyle CANNON_CAPACITY_CALC_STYLE = UnitCalcStyle.LINEAR;
	
	//linear
	@ConfigurationElement(name = "CannonCapacityPerBlock")
	public static float CANNON_CAPACITY_PER_BLOCK = 0.5f;
	//exp
	@ConfigurationElement(name = "CannonCapacityExp")
	public static float CANNON_CAPACITY_EXP = 0.5f;
	@ConfigurationElement(name = "CannonCapacityExpMult")
	public static float CANNON_CAPACITY_EXP_MULT = 0.5f;
	
	//double exp
	@ConfigurationElement(name = "CannonCapacityExpFirstHalf")
	public static float CANNON_CAPACITY_EXP_FIRST_HALF = 0.5f;
	@ConfigurationElement(name = "CannonCapacityExpMultFirstHalf")
	public static float CANNON_CAPACITY_EXP_MULT_FIRST_HALF = 0.5f;
	
	@ConfigurationElement(name = "CannonCapacityExpThreshold")
	public static float CANNON_CAPACITY_EXP_THRESHOLD = 500000f;
	
	@ConfigurationElement(name = "CannonCapacityExpSecondHalf")
	public static float CANNON_CAPACITY_EXP_SECOND_HALF = 0.5f;
	@ConfigurationElement(name = "CannonCapacityExpMultSecondHalf")
	public static float CANNON_CAPACITY_EXP_MULT_SECOND_HALF = 0.5f;
	
	//log
	@ConfigurationElement(name = "CannonCapacityLogFactor")
	public static float CANNON_CAPACITY_LOG_FACTOR = 0.5f;
	@ConfigurationElement(name = "CannonCapacityLogOffset")
	public static float CANNON_CAPACITY_LOG_OFFSET = 0.5f;

	public CannonCapacityElementManager(SegmentController segmentController) {
		super(segmentController, CannonCapacityCollectionManager.class);

		timer = CANNON_CAPACITY_RELOAD_CONSTANT;
	}

	@Override
	protected String getTag() {
		return "cannoncapacity";
	}

	@Override
	public float getBasicCapacity() {
		return CANNON_CAPACITY_BASIC;
	}

	@Override
	public WeaponCapacityReloadMode getReloadMode() {
		return CANNON_CAPACITY_RELOAD_MODE;
	}

	@Override
	public float getAmmoReloadPerCapacity() {
		return CANNON_CAPACITY_PER_SEC;
	}

	@Override
	public float getAmmoReloadAllTime() {
		return CANNON_CAPACITY_RELOAD_CONSTANT;
	}

	@Override
	public boolean ammoReloadResetsOnManualFire() {
		return CANNON_CAPACITY_RESET_ON_FIRE_MANUAL;
	}

	@Override
	public boolean ammoReloadResetsOnAIFire() {
		return CANNON_CAPACITY_RESET_ON_FIRE_AI;
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
		return CANNON_CAPACITY_CALC_STYLE;
	}

	@Override
	public float getCapacityPerBlockLinear() {
		return CANNON_CAPACITY_PER_BLOCK;
	}

	@Override
	public float getCapacityExp() {
		return CANNON_CAPACITY_EXP;
	}

	@Override
	public float getCapacityExpMult() {
		return CANNON_CAPACITY_EXP_MULT;
	}

	@Override
	public float getCapacityDoubleExpFirstHalf() {
		return CANNON_CAPACITY_EXP_FIRST_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultFirstHalf() {
		return CANNON_CAPACITY_EXP_MULT_FIRST_HALF;
	}

	@Override
	public float getCapacityExponentThreshold() {
		return CANNON_CAPACITY_EXP_THRESHOLD;
	}

	@Override
	public float getCapacityDoubleExpSecondHalf() {
		return CANNON_CAPACITY_EXP_SECOND_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultSecondHalf() {
		return CANNON_CAPACITY_EXP_MULT_SECOND_HALF;
	}

	@Override
	public float getCapacityLogFactor() {
		return CANNON_CAPACITY_LOG_FACTOR;
	}

	@Override
	public float getCapacityLogOffset() {
		return CANNON_CAPACITY_LOG_OFFSET;
	}

	@ConfigurationElement(name = "MultitaskPowerPenaltyPerSizeRatio",description = "Extra power consumption when multiple weapon types' ammo reserves are below full/reloading. Calculated as (1 + (penaltyWeap1*min(ratioWeap1,cap) + (penaltyWeap2*min(ratioWeap2,cap))")
	public static float FULL_MULTITASK_PENALTY = 0.08f;
	@ConfigurationElement(name = "MultitaskPenaltyCapRatio",description = "Multitask penalty stops increasing when other ammo systems reach at least this size ratio compared to this ammo system.")
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
		return WeaponType.CANNON;
	}

	@Override
	public CannonCapacityCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<CannonCapacityCollectionManager> clazz) {
		return new CannonCapacityCollectionManager(getSegmentController(), this);
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.CANNONS;
	}

	@Override
	public String getName() {
		return "Cannon Capacity Element Manager";
	}
}
