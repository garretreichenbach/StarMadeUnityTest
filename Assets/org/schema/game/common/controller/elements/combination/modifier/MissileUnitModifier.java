package org.schema.game.common.controller.elements.combination.modifier;

import api.listener.events.weapon.UnitModifierHandledEvent;
import api.mod.StarLoader;
import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.combination.MissileCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.BasicModifier;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.SetFomula;
import org.schema.game.common.controller.elements.missile.MissileUnit;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;

public class MissileUnitModifier<E extends MissileUnit<?, ?, ?>> extends Modifier<E, MissileCombiSettings> {

	@ConfigurationElement(name = "Damage")
	public BasicModifier damageModifier;

	@ConfigurationElement(name = "ComboBlocksAddDamage", description = "0 = no damage contribution from secondary except modifier, 1 or skip = secondary system blocks are treated like more primary weapon block count for damage scaling. Decimal values will contribute secondary size * value.")
	public BasicModifier secondaryScalesDamage;

	@ConfigurationElement(name = "AdditiveDamage")
	public BasicModifier additiveDamage;

	@ConfigurationElement(name = "Reload")
	public BasicModifier reloadModifier;

	@ConfigurationElement(name = "Distance")
	public BasicModifier distanceModifier;

	@ConfigurationElement(name = "Speed")
	public BasicModifier speedModifier;

	@ConfigurationElement(name = "Split")
	public BasicModifier splitModifier;

	@ConfigurationElement(name = "Mode")
	public BasicModifier modeModifier;

	@ConfigurationElement(name = "PowerConsumption")
	public BasicModifier powerConsumption;

	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamage")
	public BasicModifier additionalCapacityUsedPerDamageModifier;

	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamageMult")
	public BasicModifier additionalCapacityUsedPerDamageMultModifier;

	@ConfigurationElement(name = "PercentagePowerUsageResting")
	public BasicModifier percentagePowerUsageRestingModifier;

	@ConfigurationElement(name = "PercentagePowerUsageCharging")
	public BasicModifier percentagePowerUsageChargingModifier;
	
	@ConfigurationElement(name = "LockOnTimeSec")
	public BasicModifier lockOnTimeSecModifier;
	
	@ConfigurationElement(name = "PossibleZoom")
	public BasicModifier possibleZoomMod;
	
	public float outputPowerConsumption;
	public float outputDamage;
	public float outputDistance;
	public float outputSpeed;
	public float outputReload;
	public float outputMode;
	public int outputSplit = 1;

	public float outputAdditionalCapacityUsedPerDamage;
	public float outputAdditionalCapacityUsedPerDamageMult;

	public MissileUnitModifier() {

	}

	@Override
	public void handle(MissileUnit input, ControlBlockElementCollectionManager combi, float ratio) {

		float secondaryContribution = secondaryScalesDamage.getOutput(1, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputDamage =  damageModifier.getOutput(input.getDamage((int) (combi.getTotalSize() * secondaryContribution)), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio, true);
		outputDamage += additiveDamage.getOutput(DumbMissileElementManager.ADDITIVE_DAMAGE, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputReload = reloadModifier.getOutput(input.getReloadTimeMs(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		
		float d = distanceModifier.getOutput(input.getDistance(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputDistance = distanceModifier.formulas instanceof SetFomula ? d * ((GameStateInterface) input.getSegmentController().getState()).getGameState().getWeaponRangeReference() : d;
		
		
		outputSpeed = speedModifier.getOutput(input.getSpeed(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputSplit = (int) splitModifier.getOutput(1, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);


		outputAdditionalCapacityUsedPerDamage = additionalCapacityUsedPerDamageModifier.getOutput(input.getAdditionalCapacityUsedPerDamageStatic(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputAdditionalCapacityUsedPerDamageMult = additionalCapacityUsedPerDamageMultModifier.getOutput(input.AdditionalCapacityUsedPerDamageMultStatic(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		assert (modeModifier.formulas instanceof SetFomula) : modeModifier.formulas;
		outputMode = (int) modeModifier.getOutput(0, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		float pw = powerConsumption.getOutput((float) (input.getBasePowerConsumption() * input.getExtraConsume()), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		
		boolean charging = input.isPowerCharging(input.getSegmentController().getState().getUpdateTime());
		
		float plus = 0;
		if(charging) {
			//should only be used with set
			plus = percentagePowerUsageChargingModifier.getOutput(input.percentagePowerUsageCharging(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		}else {
			//should only be used with set
			plus = percentagePowerUsageRestingModifier.getOutput(input.percentagePowerUsageResting(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		}
		
		
		pw += input.getPowerInterface().getRechargeRatePercentPerSec() * plus;
		outputPowerConsumption = pw;

		//INSERTED CODE @...
		UnitModifierHandledEvent event = new UnitModifierHandledEvent(this, input, combi, ratio);
		StarLoader.fireEvent(event, input.getSegmentController().isOnServer());
		///
	}
	public double calculatePowerConsumption(double powerPerBlock, MissileUnit input, ControlBlockElementCollectionManager<?,?,?> combi, float ratio) {
		
		outputDamage = damageModifier.getOutput(input.getBaseDamage(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		
		float pw = powerConsumption.getOutput((float) (powerPerBlock * input.getExtraConsume()), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		
		boolean charging = input.isPowerCharging(input.getSegmentController().getState().getUpdateTime());
		
		float plus = 0;
		if(charging) {
			//should only be used with set
			plus = percentagePowerUsageChargingModifier.getOutput(input.percentagePowerUsageCharging(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		}else {
			//should only be used with set
			plus = percentagePowerUsageRestingModifier.getOutput(input.percentagePowerUsageResting(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		}
		
		
		pw += input.getPowerInterface().getRechargeRatePercentPerSec() * plus;
		outputPowerConsumption = pw;
		return pw;
	}
	public double calculateReload(MissileUnit input, ControlBlockElementCollectionManager<?, ?, ?>  combi, float ratio) {
		return reloadModifier.getOutput(input.getReloadTimeMs(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
	}

	@Override
	public void calcCombiSettings(MissileCombiSettings out,
			ControlBlockElementCollectionManager<?, ?, ?> col, ControlBlockElementCollectionManager<?, ?, ?> combi,
			float ratio) {
		out.lockOnTime = lockOnTimeSecModifier.getOutput(((DumbMissileCollectionManager)col).getLockOnTimeRaw(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.possibleZoom = possibleZoomMod.getOutput(((DumbMissileCollectionManager)col).getPossibleZoomRaw(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
	}
}
