package org.schema.game.common.controller.damage.effects;

import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

public abstract class InterEffectContainer {
	protected final InterEffectSet[] sets;
	
	public InterEffectContainer(){
		sets = setupEffectSets();
	}
	
	
	public abstract void update(ConfigEntityManager c);
	
	public void addGeneral(ConfigEntityManager c, InterEffectSet aSet) {
		aSet.applyAddEffectConfig(c, StatusEffectType.GENERAL_DEFENSE_EM, InterEffectType.EM);
		aSet.applyAddEffectConfig(c, StatusEffectType.GENERAL_DEFENSE_KINETIC, InterEffectType.KIN);
		aSet.applyAddEffectConfig(c, StatusEffectType.GENERAL_DEFENSE_HEAT, InterEffectType.HEAT);
	}
	public void addShield(ConfigEntityManager c, InterEffectSet aSet) {
		aSet.applyAddEffectConfig(c, StatusEffectType.SHIELD_DEFENSE_EM, InterEffectType.EM);
		aSet.applyAddEffectConfig(c, StatusEffectType.SHIELD_DEFENSE_KINETIC, InterEffectType.KIN);
		aSet.applyAddEffectConfig(c, StatusEffectType.SHIELD_DEFENSE_HEAT, InterEffectType.HEAT);
	}
	public void addArmor(ConfigEntityManager c, InterEffectSet aSet) {
		
		aSet.applyAddEffectConfig(c, StatusEffectType.ARMOR_DEFENSE_EM, InterEffectType.EM);
		aSet.applyAddEffectConfig(c, StatusEffectType.ARMOR_DEFENSE_KINETIC, InterEffectType.KIN);
		aSet.applyAddEffectConfig(c, StatusEffectType.ARMOR_DEFENSE_HEAT, InterEffectType.HEAT);
	}
	public abstract InterEffectSet[] setupEffectSets();
	public abstract InterEffectSet get(HitReceiverType type);


	public void reset() {
		for(InterEffectSet s : sets) {
			s.reset();
		}
	}
}
