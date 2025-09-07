package org.schema.game.common.controller.damage.effects;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.HitType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public abstract class InterEffectHandler {
	public enum InterEffectType{
		HEAT("Heat", en -> {
			return Lng.str("Heat");
		}, en -> {
			return Lng.str("Heat");
		}),
		KIN("Kinetic", en -> {
			return Lng.str("Kin");
		}, en -> {
			return Lng.str("Kinetic");
		}),
		EM("EM", en -> {
			return Lng.str("EM");
		}, en -> {
			return Lng.str("Electro Magnetic");
		}),
		;
		
		public final Translatable fullName;
		public final Translatable shortName;
		public final String id;

		private InterEffectType(String id, Translatable shortName, Translatable name){
			this.id = id;
			this.fullName = name;
			this.shortName = shortName;
		}
		@Override
		public String toString() {
			return id;
		}
	}
	
	private static final InterEffectHandler[] EFFECTS = new InterEffectHandler[InterEffectType.values().length];
	static{
		for(int i = 0; i < InterEffectType.values().length; i++){
			switch(InterEffectType.values()[i]){
				case HEAT: EFFECTS[i] = new HeatEffectHandler(); break;
				case KIN: EFFECTS[i] = new KineticEffectHandler(); break;
				case EM: EFFECTS[i] = new EMEffectHandler(); break;
				default: assert false; break;
			}
			assert(EFFECTS[i].getType() == InterEffectType.values()[i]):InterEffectType.values()[i]+"; "+i;
		}
	}
	public abstract InterEffectType getType();
	
	
	public static float handleEffects(float inputDamage, InterEffectSet attack, InterEffectSet defense, HitType hitType,
			DamageDealerType damageType, HitReceiverType receiverType, short typeParam){
		float damage = 0;
		for(int i = 0; i < EFFECTS.length; i++){
			damage += EFFECTS[i].getOutputDamage(inputDamage, EFFECTS[i].getType(), attack, defense, hitType, damageType, receiverType, typeParam);
		}
		return Math.max(0, damage);
	}
	
	public float getOutputDamage(float inputDamage, InterEffectType t, InterEffectSet attack, InterEffectSet defense, HitType hitType, DamageDealerType damageType, HitReceiverType receiverType, short typeParam) {
		assert(attack != null);
		///INSERTED CODE
		// by Ithirahad
		//Original: return inputDamage * Math.max(0f, (attack != null ? attack.getStrength(t) : 0f) - (defense != null ? defense.getStrength(t) : 0f));
		// that formula creates strange and unintuitive situations where defense strength only matters if above a certain threshold,
		// and also can cancel out all of a given type of damage without being 100% defense.
		// It's also not typically the way that games (e.g. EvE Online) handle elemental damage types.
		// Doing the maths that way also created a strange situation where attack/defense scores were measured out of 3 rather than the expected 1.
		float attackStrengthInType;
		if(attack != null) attackStrengthInType = attack.getStrength(t); //apparently, in the wild, the assertion fails sometimes when hitting shields with explosions. Go figure.
		else attackStrengthInType = 1f/InterEffectType.values().length; //...so we will just assume equal damage type distribution in that case
		return inputDamage * Math.max(0f,attackStrengthInType) //the final damage in this type is whatever fraction of the total incoming damage is of type t...
				* (1 - (defense.getStrength(t))); //...times the fraction of type-t damage that isn't cancelled by the defense strength in that type
		///
	}

}
