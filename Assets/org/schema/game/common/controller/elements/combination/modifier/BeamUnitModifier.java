package org.schema.game.common.controller.elements.combination.modifier;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamUnit;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamUnit;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.BasicModifier;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.SetFomula;

public class BeamUnitModifier<E extends BeamUnit<?, ?, ?>> extends Modifier<E, BeamCombiSettings> {

	@ConfigurationElement(name = "HitSpeed")
	public BasicModifier hitSpeed;

	@ConfigurationElement(name = "PowerPerHit")
	public BasicModifier powerPerHit;

	@ConfigurationElement(name = "ComboBlocksAddDamage", description = "0 = no damage contribution from secondary except modifier, 1 or skip = secondary system blocks are treated like more primary weapon block count for damage scaling. Decimal values will contribute secondary size * value.")
	public BasicModifier secondaryScalesDamage;

	@ConfigurationElement(name = "PowerConsumptionCharging")
	public BasicModifier powerConsumptionCharging;
	
	@ConfigurationElement(name = "Distance")
	public BasicModifier distance;

	@ConfigurationElement(name = "CoolDown")
	public BasicModifier coolDown;
	
	@ConfigurationElement(name = "BurstTime")
	public BasicModifier burstTime;
	
	@ConfigurationElement(name = "InitialTicks")
	public BasicModifier initialTicks;

	@ConfigurationElement(name = "LatchOn")
	public BasicModifier latchModifier;

	@ConfigurationElement(name = "CheckLatchConnection")
	public BasicModifier checkLatchConnectionModifier;

	@ConfigurationElement(name = "Penetration")
	public BasicModifier penetrationModifier;
	
	@ConfigurationElement(name = "AcidDamagePercentage")
	public BasicModifier acidModifier;
	
	@ConfigurationElement(name = "FriendlyFire")
	public BasicModifier friendlyFireModifier;
	
	@ConfigurationElement(name = "Aimable")
	public BasicModifier aimableModifier;
	
	@ConfigurationElement(name = "ChargeTime")
	public BasicModifier chargeTimeMod;
	
	@ConfigurationElement(name = "MinEffectiveValue")
	public BasicModifier minEffectiveValueMod;
	
	@ConfigurationElement(name = "MinEffectiveRange")
	public BasicModifier minEffectiveRangeMod;
	
	@ConfigurationElement(name = "MaxEffectiveValue")
	public BasicModifier maxEffectiveValueMod;
	
	@ConfigurationElement(name = "MaxEffectiveRange")
	public BasicModifier maxEffectiveRangeMod;
	
	@ConfigurationElement(name = "PowerConsumptionResting")
	public BasicModifier restingPowerConsumptionMod;

	
	@ConfigurationElement(name = "PossibleZoom")
	public BasicModifier possibleZoomMod;

	@ConfigurationElement(name = "BaseCapacityUsedPerTick")
	public BasicModifier baseCapacityUsedPerTickMod;

	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamage")
	public BasicModifier additionalCapacityUsedPerDamageMod;

	@ConfigurationElement(name = "AdditiveDamage")
	public BasicModifier additiveDamageMod;
	
	
	
	public float outputPowerConsumption;
	
	public float outputTickRate;
	public float outputDamagePerHit;

	public float outputDistance;

	public float outputCoolDown;
	public float outputBurstTime;
	public float outputInitialTicks;

	public float outputAcidPercentage;
	public boolean outputFriendlyFire;
	public boolean outputAimable;
	
	public boolean outputPenetration;
	public boolean outputLatchMode;
	public boolean outputCheckLatchConnection;

	
	public float outputMinEffectiveValue;
	
	public float outputMinEffectiveRange;
	
	public float outputMaxEffectiveValue;
	
	public float outputMaxEffectiveRange;
	public float baseCapacityUsedPerTick;
	public float additionalCapacityUsedPerDamage;

	public BeamUnitModifier() {

	}

	@Override
	public void handle(BeamUnit input, ControlBlockElementCollectionManager combi, float ratio) {
		assert (input != null);
		assert (hitSpeed != null);
		
		outputTickRate = hitSpeed.getOutput(input.getTickRate(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		if(input instanceof DamageBeamUnit){
			float secondaryContribution = secondaryScalesDamage.getOutput(1, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
			outputDamagePerHit = powerPerHit.getOutput(((DamageBeamUnit)input).getDamagePerTick((int) (combi.getTotalSize() * secondaryContribution)), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio, true);
		}
		else {
			outputDamagePerHit = powerPerHit.getOutput(input.getBeamPower(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		}
		outputDamagePerHit += additiveDamageMod.getOutput(input.getAdditiveBeamPower(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputPowerConsumption = powerConsumptionCharging.getOutput(input.getBasePowerConsumption() * input.getExtraConsume(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		float d = distance.getOutput(input.getDistance(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputDistance = distance.formulas instanceof SetFomula ? d * ((GameStateInterface) input.getSegmentController().getState()).getGameState().getWeaponRangeReference() : d;
		
		outputFriendlyFire = (int) friendlyFireModifier.getOutput(input.isFriendlyFire() ? 1 : 0, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio) != 0;
		
		outputAimable = (int) aimableModifier.getOutput(input.isAimable() ? 1 : 0, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio) != 0;
		
		outputPenetration = (int) penetrationModifier.getOutput(input.isPenetrating() ? 1 : 0, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio) != 0;

		outputLatchMode = (int) latchModifier.getOutput(input.isLatchOn() ? 1 : 0, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio) != 0;

		outputCheckLatchConnection = (int) checkLatchConnectionModifier.getOutput(input.isCheckLatchConnection() ? 1 : 0, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio) != 0;
		
		outputAcidPercentage = acidModifier.getOutput(input.getAcidDamagePercentage(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		
		outputCoolDown = coolDown.getOutput(input.getCoolDownSec(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		
		outputBurstTime = burstTime.getOutput(input.getBurstTime(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		
		outputInitialTicks = initialTicks.getOutput(input.getInitialTicks(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		
		outputMinEffectiveValue = minEffectiveValueMod.getOutput(input.getMinEffectiveValue(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputMinEffectiveRange = minEffectiveRangeMod.getOutput(input.getMinEffectiveRange(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputMaxEffectiveValue = maxEffectiveValueMod.getOutput(input.getMaxEffectiveValue(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputMaxEffectiveRange = maxEffectiveRangeMod.getOutput(input.getMaxEffectiveRange(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		baseCapacityUsedPerTick = baseCapacityUsedPerTickMod.getOutput(input.getBaseCapacityUsedPerTick(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		additionalCapacityUsedPerDamage = additionalCapacityUsedPerDamageMod.getOutput(input.getAdditionalCapacityUsedPerDamage(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
	}
	public double calculateReload(BeamUnit input, ControlBlockElementCollectionManager<?, ?, ?>  combi, float ratio) {
		return coolDown.getOutput(input.getReloadTimeMs(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
	}
	public double calculatePowerConsumption(double powerPerBlock, BeamUnit input, ControlBlockElementCollectionManager combi, float ratio) {
		
		
		if(input.isPowerCharging(input.getSegmentController().getState().getUpdateTime())) {
			return powerConsumptionCharging.getOutput((float) (powerPerBlock * input.getExtraConsume()), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		}else {
			return restingPowerConsumptionMod.getOutput((float) (powerPerBlock * input.getExtraConsume()), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		}
	}

	@Override
	public void calcCombiSettings(BeamCombiSettings out, ControlBlockElementCollectionManager<?, ?, ?> col,
			ControlBlockElementCollectionManager<?, ?, ?> combi, float ratio) {
		
		out.chargeTime = chargeTimeMod.getOutput(((BeamCollectionManager<?,?,?>)col).getChargeTime(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.possibleZoom = possibleZoomMod.getOutput(((BeamCollectionManager<?,?,?>)col).getPossibleZoomRaw(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.burstTime = burstTime.getOutput(((BeamCollectionManager<?,?,?>)col).getElementManager().getBurstTime(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
	}
}
