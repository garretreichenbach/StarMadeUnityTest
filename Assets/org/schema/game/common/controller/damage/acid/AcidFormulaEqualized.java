package org.schema.game.common.controller.damage.acid;

import org.schema.game.common.controller.damage.projectile.ProjectileHandlerSegmentController.ShotStatus;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

public class AcidFormulaEqualized extends AcidDamageFormula{
	
	@Override
	public void getAcidDamageSetting(short type, int damage, int restDamage, int initialDamage, float totalArmorValue, int blockIndex,  
		float projectileWidth, int penetrationDepth, ShotStatus shotStatus, AcidSetting out) {
//		assert(penetrationDepth >= 1 && blockIndex <= penetrationDepth):blockIndex+" / "+penetrationDepth;
		
		float avgAcidDamage = (float) initialDamage / penetrationDepth;
		avgAcidDamage *= projectileWidth;
		
		ElementInformation hitBlock = ElementKeyMap.getInfoFast(type);

		
		float dmgVariable = 1f;
		switch(shotStatus) {
			case NORMAL -> {
				// System.out.println("[ACID] Normal hit");
				break;
			}
			case OVER_PENETRATION -> {
				// Less damage distributed depending on overpen factor
				float overPen = Math.min(CannonElementManager.ACID_DAMAGE_MAX_OVER_PEN_MOD, Math.max(CannonElementManager.ACID_DAMAGE_MIN_OVER_PEN_MOD, avgAcidDamage / (totalArmorValue * VoidElementManager.ARMOR_OVER_PENETRATION_MARGIN_MULTIPLICATOR)));
				//				System.out.println("[WEAPON] Acid damage overpen factor: " + overPen);
				dmgVariable /= overPen;
				break;
			}
			default -> {
				// System.out.println("[WEAPON] Acid damage -> ERROR, ShotStatus Default called");
				out.maxPropagation = 0;
				out.damage = 0;
				return;
			}
		}
		
		// More damage distributed towards thicker armor plates
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
		return AcidFormulaType.EQUAL_DIST;
	}
	
}
