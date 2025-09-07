package org.schema.game.common.controller.elements.ammo.repair;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.UnitCalcStyle;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.data.SegmentPiece;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponCapacityReloadMode.SINGLE;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class RepairCapacityElementManager extends AmmoCapacityElementManager<RepairCapacityUnit, RepairCapacityCollectionManager, RepairCapacityElementManager> {

	@ConfigurationElement(name = "BasicRepairCapacity")
	public static float REPAIR_CAPACITY_BASIC = 0;

	@ConfigurationElement(name = "RepairCapacityReloadMode")
	public static WeaponCapacityReloadMode REPAIR_CAPACITY_RELOAD_MODE = SINGLE;

	@ConfigurationElement(name = "RepairCapacityPerSec")
	public static float REPAIR_CAPACITY_PER_SEC = 0;
	
	@ConfigurationElement(name = "RepairCapacityReloadConstant")
	public static float REPAIR_CAPACITY_RELOAD_CONSTANT = 300;
	
	@ConfigurationElement(name = "RepairCapacityReloadResetOnFireManual")
	public static boolean REPAIR_CAPACITY_RESET_ON_FIRE_MANUAL = false;
	@ConfigurationElement(name = "RepairCapacityReloadResetOnFireAI")
	public static boolean REPAIR_CAPACITY_RESET_ON_FIRE_AI = true;
	
	
	
	@ConfigurationElement(name = "ReactorPowerConsumptionResting")
	public static float POWER_CONSUMPTION_RESTING = 0;
	
	@ConfigurationElement(name = "ReactorPowerConsumptionCharging")
	public static float POWER_CONSUMPTION_CHARGING = 0;

	@ConfigurationElement(name = "RepairCapacityCalcStyle", description = "LINEAR, EXP, LOG")
	public static UnitCalcStyle REPAIR_CAPACITY_CALC_STYLE = UnitCalcStyle.LINEAR;
	
	//linear
	@ConfigurationElement(name = "RepairCapacityPerBlock")
	public static float REPAIR_CAPACITY_PER_BLOCK = 0.5f;
	//exp
	@ConfigurationElement(name = "RepairCapacityExp")
	public static float REPAIR_CAPACITY_EXP = 0.5f;
	@ConfigurationElement(name = "RepairCapacityExpMult")
	public static float REPAIR_CAPACITY_EXP_MULT = 0.5f;
	
	//double exp
	@ConfigurationElement(name = "RepairCapacityExpFirstHalf")
	public static float REPAIR_CAPACITY_EXP_FIRST_HALF = 0.5f;
	@ConfigurationElement(name = "RepairCapacityExpMultFirstHalf")
	public static float REPAIR_CAPACITY_EXP_MULT_FIRST_HALF = 0.5f;
	
	@ConfigurationElement(name = "RepairCapacityExpThreshold")
	public static float REPAIR_CAPACITY_EXP_THRESHOLD = 500000.0f;
	
	@ConfigurationElement(name = "RepairCapacityExpSecondHalf")
	public static float REPAIR_CAPACITY_EXP_SECOND_HALF = 0.5f;
	@ConfigurationElement(name = "RepairCapacityExpMultSecondHalf")
	public static float REPAIR_CAPACITY_EXP_MULT_SECOND_HALF = 0.5f;
	
	//log
	@ConfigurationElement(name = "RepairCapacityLogFactor")
	public static float REPAIR_CAPACITY_LOG_FACTOR = 0.5f;
	@ConfigurationElement(name = "RepairCapacityLogOffset")
	public static float REPAIR_CAPACITY_LOG_OFFSET = 0.5f;

	@ConfigurationElement(name = "MultitaskPowerPenaltyPerSizeRatio",description = "Extra power consumption when multiple ammo reserves are below full. Calculated as (1 + (penaltyWeap1*min(ratioWeap1,cap) + (penaltyWeap2*min(ratioWeap2,cap))")
	public static float FULL_MULTITASK_PENALTY = 0.08f;
	@ConfigurationElement(name = "MultitaskPenaltyCapRatio",description = "Multitask penalty stops increasing when other ammo systems reach at least this size ratio.")
	public static float MULTITASK_PENALTY_CAP_RATIO = 0.5f;

	public RepairCapacityElementManager(SegmentController segmentController) {
		super(segmentController, RepairCapacityCollectionManager.class);
	}

	@Override
	public float getBasicCapacity() {
		return REPAIR_CAPACITY_BASIC;
	}

	@Override
	public WeaponCapacityReloadMode getReloadMode() {
		return REPAIR_CAPACITY_RELOAD_MODE;
	}

	@Override
	public float getAmmoReloadPerCapacity() {
		return REPAIR_CAPACITY_PER_SEC;
	}

	@Override
	public float getAmmoReloadAllTime() {
		return REPAIR_CAPACITY_RELOAD_CONSTANT;
	}

	@Override
	public boolean ammoReloadResetsOnManualFire() {
		return REPAIR_CAPACITY_RESET_ON_FIRE_MANUAL;
	}

	@Override
	public boolean ammoReloadResetsOnAIFire() {
		return REPAIR_CAPACITY_RESET_ON_FIRE_AI;
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
		return REPAIR_CAPACITY_CALC_STYLE;
	}

	@Override
	public float getCapacityPerBlockLinear() {
		return REPAIR_CAPACITY_PER_BLOCK;
	}

	@Override
	public float getCapacityExp() {
		return REPAIR_CAPACITY_EXP;
	}

	@Override
	public float getCapacityExpMult() {
		return REPAIR_CAPACITY_EXP_MULT;
	}

	@Override
	public float getCapacityDoubleExpFirstHalf() {
		return REPAIR_CAPACITY_EXP_FIRST_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultFirstHalf() {
		return REPAIR_CAPACITY_EXP_MULT_FIRST_HALF;
	}

	@Override
	public float getCapacityExponentThreshold() {
		return REPAIR_CAPACITY_EXP_THRESHOLD;
	}

	@Override
	public float getCapacityDoubleExpSecondHalf() {
		return REPAIR_CAPACITY_EXP_SECOND_HALF;
	}

	@Override
	public float getCapacityDoubleExpMultSecondHalf() {
		return REPAIR_CAPACITY_EXP_MULT_SECOND_HALF;
	}

	@Override
	public float getCapacityLogFactor() {
		return REPAIR_CAPACITY_LOG_FACTOR;
	}

	@Override
	public float getCapacityLogOffset() {
		return REPAIR_CAPACITY_LOG_OFFSET;
	}

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
		return WeaponType.REPAIR;
	}

	@Override
	protected String getTag() {
		return "repairpastecapacity";
	}

	@Override
	public RepairCapacityCollectionManager getNewCollectionManager(SegmentPiece position, Class<RepairCapacityCollectionManager> clazz) {
		return new RepairCapacityCollectionManager(getSegmentController(), this);
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.BEAMS;
	}

	@Override
	public String getName() {
		return "Repair Paste Element Manager";
	}
}
