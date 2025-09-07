package org.schema.game.common.controller.damage;

import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;

public interface DamageDealer {
	public DamageDealerType getDamageDealerType();
	public InterEffectSet getAttackEffectSet();
	public MetaWeaponEffectInterface getMetaWeaponEffect();
}
