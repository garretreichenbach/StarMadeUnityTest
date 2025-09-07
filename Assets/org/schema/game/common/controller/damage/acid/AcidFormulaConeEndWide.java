package org.schema.game.common.controller.damage.acid;

import org.schema.game.common.controller.damage.projectile.ProjectileHandlerSegmentController.ShotStatus;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

public class AcidFormulaConeEndWide extends AcidDamageFormula{
	
	@Override
	public void getAcidDamageSetting(short type, int damage, int restDamage, int initialDamage, float totalArmorValue, int blockIndex,
		float projectileWidth, int penetrationDepth, ShotStatus shotStatus, AcidSetting out) {
		assert(penetrationDepth >= 1 && blockIndex <= penetrationDepth);
				
		float avgAcidDamage = (float) initialDamage / (float) penetrationDepth;		
		float acidDamageWeight = CannonElementManager.ACID_DAMAGE_FORMULA_CONE_END_WIDE_WEIGHT 
			+ 2 * (Math.max(0f, Math.min(1f, (float) blockIndex / (float) penetrationDepth))) * (1 - CannonElementManager.ACID_DAMAGE_FORMULA_CONE_END_WIDE_WEIGHT);
		
		avgAcidDamage *= acidDamageWeight;
		avgAcidDamage *= projectileWidth;
				
		ElementInformation hitBlock = ElementKeyMap.getInfoFast(type);
		
		float dmgVariable = 1f;
		switch(shotStatus) {
			case NORMAL -> {
				//				 System.out.println("[ACID] Normal hit");
				// More damage distributed towards thicker armor plates
				break;
			}
			case OVER_PENETRATION -> {
				// Less damage distributed depending on overpen factor
				//				 System.out.println("[ACID] Overpen hit");
				float overPen = Math.min(CannonElementManager.ACID_DAMAGE_MAX_OVER_PEN_MOD, Math.max(CannonElementManager.ACID_DAMAGE_MIN_OVER_PEN_MOD, avgAcidDamage / (totalArmorValue * VoidElementManager.ARMOR_OVER_PENETRATION_MARGIN_MULTIPLICATOR)));
				//				System.out.println("[WEAPON] Acid damage overpen factor: " + overPen);
				dmgVariable /= overPen;
				break;
			}
			default -> {
				// System.out.println("[WEAPON] Acid damage -> ERROR, ShotStatus Default called");
				//				 System.out.println("[ACID] Default hit");
				out.maxPropagation = 0;
				out.damage = 0;
				return;
			}
		}
		
		if (hitBlock.isArmor() && hitBlock.getArmorValue() > 0 && totalArmorValue > 0f) {
			float overArmor = 
				Math.min(CannonElementManager.ACID_DAMAGE_MAX_OVER_ARMOR_MOD,
					Math.max(CannonElementManager.ACID_DAMAGE_MIN_OVER_ARMOR_MOD, 
						hitBlock.getArmorValue() / CannonElementManager.ACID_DAMAGE_OVER_ARMOR_BASE));
			dmgVariable *= overArmor;
		}
		
		avgAcidDamage *= dmgVariable;
		out.damage = Math.min((int) (avgAcidDamage), restDamage);
		out.maxPropagation = CannonElementManager.ACID_DAMAGE_MAX_PROPAGATION;

		// System.out.println("[WEAPON] Acid damage (" + getClass().getSimpleName() + ") -> " + shotStatus.name() + ": " + out.damage + " with dmg factor: " + dmgVariable);
	}

	@Override
	public AcidFormulaType getType() {
		return AcidFormulaType.CONE_END_WIDE;
	}
	
}
