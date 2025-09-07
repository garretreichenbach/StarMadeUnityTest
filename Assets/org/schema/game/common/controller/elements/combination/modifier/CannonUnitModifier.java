package org.schema.game.common.controller.elements.combination.modifier;

import api.listener.events.weapon.UnitModifierHandledEvent;
import api.mod.StarLoader;
import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.combination.CannonCombiSettings;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.BasicModifier;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.SetFomula;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.controller.elements.cannon.CannonUnit;

public class CannonUnitModifier extends Modifier<CannonUnit, CannonCombiSettings> {

	@ConfigurationElement(name = "Damage")
	public BasicModifier damageModifier;

	@ConfigurationElement(name = "ComboBlocksAddDamage", description = "0 = no damage contribution from secondary except modifier, 1 or skip = secondary system blocks are treated like more primary weapon block count for damage scaling. Decimal values will contribute secondary size * value.")
	public BasicModifier secondaryScalesDamage;

	@ConfigurationElement(name = "Reload")
	public BasicModifier reloadModifier;

	@ConfigurationElement(name = "PowerConsumption")
	public BasicModifier powerModifier;

	@ConfigurationElement(name = "Distance")
	public BasicModifier distanceModifier;

	@ConfigurationElement(name = "Speed")
	public BasicModifier speedModifier;

	@ConfigurationElement(name = "Recoil")
	public BasicModifier recoilModifier;
	
	@ConfigurationElement(name = "ImpactForce")
	public BasicModifier impactForceModifier;
	
	@ConfigurationElement(name = "AcidFormula")
	public BasicModifier acidType;

	@ConfigurationElement(name = "ProjectileWidth")
	public BasicModifier projectileWidthModifier;
	
	
	@ConfigurationElement(name = "ChargeMax")
	public BasicModifier chargeMaxModifier;
	
	
	@ConfigurationElement(name = "ChargeSpeed")
	public BasicModifier chargeSpeedModifier;
	
	
	@ConfigurationElement(name = "CursorRecoilX")
	public BasicModifier cursorRecoilXModifier;
	
	
	@ConfigurationElement(name = "CursorRecoilMinX")
	public BasicModifier cursorRecoilMinXModifier;
	
	
	@ConfigurationElement(name = "CursorRecoilMaxX")
	public BasicModifier cursorRecoilMaxXModifier;
	
	@ConfigurationElement(name = "CursorRecoilDirX")
	public BasicModifier cursorRecoilDirXModifier;
	
	
	@ConfigurationElement(name = "CursorRecoilY")
	public BasicModifier cursorRecoilYModifier;
	
	
	@ConfigurationElement(name = "CursorRecoilMinY")
	public BasicModifier cursorRecoilMinYModifier;
	
	
	@ConfigurationElement(name = "CursorRecoilMaxY")
	public BasicModifier cursorRecoilMaxYModifier;
	
	@ConfigurationElement(name = "CursorRecoilDirY")
	public BasicModifier cursorRecoilDirYModifier;
	
	@ConfigurationElement(name = "PossibleZoom")
	public BasicModifier possibleZoomMod;
	
	@ConfigurationElement(name = "Aimable")
	public BasicModifier aimableModifier;

	@ConfigurationElement(name = "AdditiveDamage")
	public BasicModifier additiveDamageModifier;

	@ConfigurationElement(name = "BaseCapacityUsedPerShot", description = "Ammo consumed per shot. Non combined cannon base usage is 1 per shot")
	public BasicModifier baseCapacityUsedPerShotMod;

	@ConfigurationElement(name = "AdditionalCapacityUsedPerDamage")
	public BasicModifier additionalCapacityUsedPerDamageMod;

	
	public float outputProjectileWidth;
	
	public float outputDamage;
	public float outputDistance;
	public float outputSpeed;
	public float outputReload;
	public float outputPowerConsumption;

	public float outputImpactForce;
	public float outputRecoil;


	
	public int outputAcidType;

	public boolean outputAimable;

	public float baseCapacityUsedPerShot;
	public float additionalCapacityUsedPerDamage;

	

	public CannonUnitModifier() {

	}

	@Override
	public void handle(CannonUnit input, ControlBlockElementCollectionManager<?, ?, ?> combi, float ratio) {

		float secondaryContribution = secondaryScalesDamage.getOutput(1, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputDamage =          damageModifier.getOutput(input.getDamage((int) (combi.getTotalSize() * secondaryContribution)), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio, true);
		outputDamage += additiveDamageModifier.getOutput(CannonElementManager.ADDITIVE_DAMAGE,  input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputReload = reloadModifier.getOutput(input.getReloadTimeMs(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		
		float d = distanceModifier.getOutput(input.getDistance(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		outputDistance = distanceModifier.formulas instanceof SetFomula ? d * ((GameStateInterface) input.getSegmentController().getState()).getGameState().getWeaponRangeReference() : d;
		
		
		outputAimable = (int) aimableModifier.getOutput(input.isAimable() ? 1 : 0, input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio) != 0;
		
		
		outputSpeed = speedModifier.getOutput(input.getSpeed(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputImpactForce = (int) impactForceModifier.getOutput(input.getImpactForce(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputPowerConsumption = powerModifier.getOutput(input.getBasePowerConsumption() * input.getExtraConsume(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputRecoil = recoilModifier.getOutput(input.getRecoil(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		outputProjectileWidth = projectileWidthModifier.getOutput(input.getProjectileWidth(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		
		outputAcidType = (int) acidType.getOutput(1, input.size(), combi.getTotalSize(), 0, ratio);

		baseCapacityUsedPerShot = baseCapacityUsedPerShotMod.getOutput(input.getBaseCapacityUsedPerShot(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
		additionalCapacityUsedPerDamage = additionalCapacityUsedPerDamageMod.getOutput(input.getAdditionalCapacityUsedPerDamage(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);

		//INSERTED CODE @...
		UnitModifierHandledEvent event = new UnitModifierHandledEvent(this, input, combi, ratio);
		StarLoader.fireEvent(event, input.getSegmentController().isOnServer());
		///
		

	}

	public double calculateReload(CannonUnit input, ControlBlockElementCollectionManager<?, ?, ?>  combi, float ratio) {
		return reloadModifier.getOutput(input.getReloadTimeMs(), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
	}
	public double calculatePowerConsumption(double powerPerBlock, CannonUnit input, ControlBlockElementCollectionManager<?, ?, ?>  combi, float ratio) {
		return powerModifier.getOutput((float) (powerPerBlock * input.getExtraConsume()), input.size(), input.getCombiBonus(combi.getTotalSize()), input.getEffectBonus(), ratio);
	}
	@Override
	public void calcCombiSettings(CannonCombiSettings out, ControlBlockElementCollectionManager<?, ?, ?> col, ControlBlockElementCollectionManager<?, ?, ?> combi, float ratio) {
		int acidType = (int) this.acidType.getOutput(1, col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.acidType = AcidFormulaType.values()[acidType];
		out.damageChargeMax = chargeMaxModifier.getOutput(((CannonCollectionManager)col).getDamageChargeMax(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.damageChargeSpeed = chargeSpeedModifier.getOutput(((CannonCollectionManager)col).getDamageChargeSpeed(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		
		out.cursorRecoilX = cursorRecoilXModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilX(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.cursorRecoilMinX = cursorRecoilMinXModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilMinX(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.cursorRecoilMaxX= cursorRecoilMaxXModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilMaxX(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.cursorRecoilDirX= cursorRecoilDirXModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilDirX(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		
		out.cursorRecoilY = cursorRecoilYModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilY(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.cursorRecoilMinY = cursorRecoilMinYModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilMinY(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.cursorRecoilMaxY= cursorRecoilMaxYModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilMaxY(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		out.cursorRecoilDirY= cursorRecoilDirYModifier.getOutput(((CannonCollectionManager)col).getCursorRecoilDirY(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);

		out.possibleZoom = possibleZoomMod.getOutput(((CannonCollectionManager)col).getPossibleZoomRaw(), col.getTotalSize(), combi.getTotalSize(), 0, ratio);
		
		
	}


}
