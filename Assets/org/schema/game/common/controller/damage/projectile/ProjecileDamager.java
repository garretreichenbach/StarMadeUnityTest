package org.schema.game.common.controller.damage.projectile;

import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;

public interface ProjecileDamager extends Damager{
	public AcidFormulaType getAcidType(long weaponId);
}
