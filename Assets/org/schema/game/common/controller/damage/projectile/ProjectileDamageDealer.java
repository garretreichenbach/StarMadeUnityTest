package org.schema.game.common.controller.damage.projectile;

import org.schema.game.common.controller.damage.DamageDealer;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;

public interface ProjectileDamageDealer extends DamageDealer{
	public AcidFormulaType getAcidFormula();
}
