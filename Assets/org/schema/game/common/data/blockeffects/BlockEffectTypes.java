package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.blockeffects.factory.*;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public enum BlockEffectTypes {
	NULL_EFFECT(NullEffect.class, new BlockEffectFactoryFac<NullEffect>() {
		@Override
		public BlockEffectFactory<NullEffect> getInstance() {
			return new NullEffectFactory();
		}
	}, en -> Lng.str("n/a"), en -> Lng.str("NULL"),	PlayerUsableInterface.USABLE_ID_NULL_EFFECT ),
	CONTROLLESS(ControllessEffect.class, new BlockEffectFactoryFac<ControllessEffect>() {
		@Override
		public BlockEffectFactory<ControllessEffect> getInstance() {
			return new ControllessEffectFactory();
		}
	}, en -> Lng.str("Not Controllable"), en -> Lng.str("You lost control of your ship\n"), PlayerUsableInterface.USABLE_ID_CONTROLLESS
	),
	THRUSTER_OUTAGE(ThrusterOutageEffect.class, new BlockEffectFactoryFac<ThrusterOutageEffect>() {
		@Override
		public BlockEffectFactory<ThrusterOutageEffect> getInstance() {
			return new ThrusterOutageEffectFactory();
		}
	}, en -> Lng.str("Thusters failing"), en -> Lng.str("Your Thrusters are failing!\n"), PlayerUsableInterface.USABLE_ID_THRUSTER_OUTAGE
	),
	NO_POWER_RECHARGE(PowerRegenDownEffect.class, new BlockEffectFactoryFac<PowerRegenDownEffect>() {
		@Override
		public BlockEffectFactory<PowerRegenDownEffect> getInstance() {
			return new PowerRegenDownEffectFactory();
		}
	}, en -> Lng.str("Power Failing"), en -> Lng.str("Power regeneration failing!\n"), PlayerUsableInterface.USABLE_ID_NO_POWER_RECHARGE
	),
	NO_SHIELD_RECHARGE(ShieldRegenDownEffect.class, new BlockEffectFactoryFac<ShieldRegenDownEffect>() {
		@Override
		public BlockEffectFactory<ShieldRegenDownEffect> getInstance() {
			return new ShieldRegenDownEffectFactory();
		}
	}, en -> Lng.str("Shields Failing"), en -> Lng.str("Shield regeneration failing!\n"), PlayerUsableInterface.USABLE_ID_NO_SHIELD_RECHARGE
	),
	PUSH(PushEffect.class, new BlockEffectFactoryFac<PushEffect>() {
		@Override
		public BlockEffectFactory<PushEffect> getInstance() {
			return new PushEffectFactory();
		}
	}, en -> Lng.str("Pushing"), en -> Lng.str("You are being pushed!\n"), PlayerUsableInterface.USABLE_ID_PUSH
	),
	PULL(PullEffect.class, new BlockEffectFactoryFac<PullEffect>() {
		@Override
		public BlockEffectFactory<PullEffect> getInstance() {
			return new PullEffectFactory();
		}
	}, en -> Lng.str("Pulling"), en -> Lng.str("You are being pulled!\n"), PlayerUsableInterface.USABLE_ID_PULL
	),
	STOP(StopEffect.class, new BlockEffectFactoryFac<StopEffect>() {
		@Override
		public BlockEffectFactory<StopEffect> getInstance() {
			return new StopEffectFactory();
		}
	}, en -> Lng.str("Stopping"), en -> Lng.str("You are being stopped!\n"), PlayerUsableInterface.USABLE_ID_STOP),
	STATUS_ARMOR_HARDEN(StatusArmorHardenEffect.class, new BlockEffectFactoryFac<StatusArmorHardenEffect>() {
		@Override
		public BlockEffectFactory<StatusArmorHardenEffect> getInstance() {
			return new StatusArmorHardenEffectFactory();
		}
	}, en -> Lng.str("Block Armor Bonus"), en -> Lng.str("Adds armor to your blocks\n"
		+ "at the price of additional shield damage\n"
		+ "and power consumption.\n"
		+ "Put it in your hotbar and activate it"), PlayerUsableInterface.USABLE_ID_STATUS_ARMOR_HARDEN
	),
	STATUS_PIERCING_PROTECTION(StatusPiercingProtectionEffect.class, new BlockEffectFactoryFac<StatusPiercingProtectionEffect>() {
		@Override
		public BlockEffectFactory<StatusPiercingProtectionEffect> getInstance() {
			return new StatusPiercingProtectionEffectFactory();
		}
	}, en -> Lng.str("Protection against Piercing Projectiles"), en -> Lng.str("Protects against piering projectiles\n"
		+ "at the cost of power.\n"
		+ "Put it in your hotbar and activate it"), PlayerUsableInterface.USABLE_ID_STATUS_PIERCING_PROTECTION),
	STATUS_POWER_SHIELD(StatusPowerShieldEffect.class, new BlockEffectFactoryFac<StatusPowerShieldEffect>() {
		@Override
		public BlockEffectFactory<StatusPowerShieldEffect> getInstance() {
			return new StatusPowerShieldEffectFactory();
		}
	}, en -> Lng.str("Power Damage Protection"), en -> Lng.str("Protects against power draining weapons,\n"
		+ "but lowers your protection against\n"
		+ "other weapons."
		+ "Put it in your hotbar and activate it"), PlayerUsableInterface.USABLE_ID_STATUS_POWER_SHIELD),
	STATUS_SHIELD_HARDEN(StatusShieldHardenEffect.class, new BlockEffectFactoryFac<StatusShieldHardenEffect>() {
		@Override
		public BlockEffectFactory<StatusShieldHardenEffect> getInstance() {
			return new StatusShieldHardenEffectFactory();
		}
	}, en -> Lng.str("Shield Protection Bonus"), en -> Lng.str("Makes your shields stronger against damage\n"
		+ "at the price of additional block damage\n"
		+ "and power consumption.\n"
		+ "Put it in your hotbar and activate it"), PlayerUsableInterface.USABLE_ID_STATUS_SHIELD_HARDEN),
	STATUS_ARMOR_HP_HARDENING_BONUS(StatusArmorHpHardeningBonusEffect.class, new BlockEffectFactoryFac<>() {
		@Override
		public BlockEffectFactory<StatusArmorHpHardeningBonusEffect> getInstance() {
			return new StatusArmorHpDeductionEffectFactory();
		}
	}, en -> Lng.str("Armor HP Hardening Bonus"), en -> Lng.str("""
			Heavily increases armor resistance while
			active, at the cost of greatly reduced mobility.
			"""), PlayerUsableInterface.USABLE_ID_STATUS_ARMOR_HP_HARDENING_BONUS),
	STATUS_ARMOR_HP_ABSORPTION_BONUS(StatusArmorHpAbsorptionBonusEffect.class, new BlockEffectFactoryFac<StatusArmorHpAbsorptionBonusEffect>() {
		@Override
		public BlockEffectFactory<StatusArmorHpAbsorptionBonusEffect> getInstance() {
			return new StatusArmorHpAbsorbtionEffectFactory();
		}
	}, en -> Lng.str("Armor HP Absorption Bonus"), en -> Lng.str("""
			Lowers the Armor HP percentage
			at which damage bleed through starts,
			meaning your armor will fully protect you for longer."""), PlayerUsableInterface.USABLE_ID_STATUS_ARMOR_HP_ABSORPTION_BONUS),
	STATUS_ARMOR_HP_REGEN_BONUS(StatusArmorHpRegenBonusEffect.class, new BlockEffectFactoryFac<StatusArmorHpRegenBonusEffect>() {
		@Override
		public BlockEffectFactory<StatusArmorHpRegenBonusEffect> getInstance() {
			return new StatusArmorHpRegenBonusEffectFactory();
		}
	}, en -> Lng.str("Armor HP Regen Bonus"), en -> Lng.str("""
			Slowly regenerates your Armor HP
			while activated."""), PlayerUsableInterface.USABLE_ID_STATUS_ARMOR_HP_REGEN_BONUS),
	STATUS_TOP_SPEED(StatusTopSpeedEffect.class, new BlockEffectFactoryFac<>() {
		@Override
		public BlockEffectFactory<StatusTopSpeedEffect> getInstance() {
			return new StatusTopSpeedEffectFactory();
		}
	}, en -> Lng.str("Top Speed Bonus"), en -> Lng.str("Increases your top speed.\n"
		+ "Put it in your hotbar and activate it"), PlayerUsableInterface.USABLE_ID_STATUS_STATUS_TOP_SPEED),
	STATUS_ANTI_GRAVITY(StatusAntiGravityEffect.class, new BlockEffectFactoryFac<StatusAntiGravityEffect>() {
		@Override
		public BlockEffectFactory<StatusAntiGravityEffect> getInstance() {
			return new StatusAntiGravityEffectFactory();
		}
	}, en -> Lng.str("Anti Gravity"), en -> Lng.str("Negates the effect of structural\n"
		+ "gravity like Planets,\n"
		+ "but consumes power\n"
		+ "Put it in your hotbar and activate it"), PlayerUsableInterface.USABLE_ID_STATUS_STATUS_ANTI_GRAVITY),
	STATUS_GRAVITY_EFFECT_IGNORANCE(StatusGravityEffectIgnoranceEffect.class, new BlockEffectFactoryFac<StatusGravityEffectIgnoranceEffect>() {
		@Override
		public BlockEffectFactory<StatusGravityEffectIgnoranceEffect> getInstance() {
			return new StatusGravityEffectsIgnoranceEffectFactory();
		}
	}, en -> Lng.str("Moment Effect Protection"), en -> Lng.str("Protects against the effect of momentum\n"
		+ "weapons,\n"
		+ "but consumes power\n"
		+ "Put it in your hotbar and activate it"), PlayerUsableInterface.USABLE_ID_STATUS_GRAVITY_EFFECT_IGNORANCE),
	TAKE_OFF(TakeOffEffect.class, new BlockEffectFactoryFac<TakeOffEffect>() {
		@Override
		public BlockEffectFactory<TakeOffEffect> getInstance() {
			return new TakeOffEffectFactory();
		}
	}, en -> Lng.str("Thrust Blast"), en -> Lng.str("A strong speed burst in a chosen direction\n"
		+ "Strength is based on your thrust."), PlayerUsableInterface.USABLE_ID_TAKE_OFF, true),
	EVADE(EvadeEffect.class, new BlockEffectFactoryFac<EvadeEffect>() {
		@Override
		public BlockEffectFactory<EvadeEffect> getInstance() {
			return new EvadeEffectFactory();
		}
	}, en -> Lng.str("n/a"), en -> Lng.str("n/a"), PlayerUsableInterface.USABLE_ID_EVADE, true), ;

	public final BlockEffectFactoryFac<?> effectFactory;
	
	private final Class<? extends BlockEffect> clazz;
	private final Translatable name;
	private final Translatable description;
	private final long usableId;
	/**
	 * determines if the effect is a one time thing (like evade or blast)
	 */
	public final boolean oneTimeUse;
	private <E extends BlockEffect> BlockEffectTypes(Class<E> clazz, BlockEffectFactoryFac<E> effect, Translatable translation, Translatable description, long usableId) {
		this(clazz, effect, translation, description, usableId, false);
	}
	private <E extends BlockEffect> BlockEffectTypes(Class<E> clazz, BlockEffectFactoryFac<E> effect, Translatable translation, Translatable description, long usableId, boolean oneTimeUse) {
		this.effectFactory = effect;
		this.name = translation;
		this.description = description;
		this.clazz = clazz;
		this.usableId = usableId;
		this.oneTimeUse = oneTimeUse;
	}

	public String getName() {
		return name.getName(this);
	}
	
	
	public String getDescription(){
		return description.getName(this);
	}

	public CharSequence getShopDescription() {
		return getName() + ":\n" + getDescription();
	}

	/**
	 * @return the clazz
	 */
	public Class<? extends BlockEffect> getClazz() {
		return clazz;
	}

	public long getUsableId() {
		return usableId;
	}

	public StatusEffectType getAssociatedStatusEffectType() {
		return switch(this) {
			case EVADE -> StatusEffectType.THRUSTER_EVADE;
			case TAKE_OFF -> StatusEffectType.THRUSTER_BLAST;
			default -> throw new RuntimeException("no corresponding StatusEffectType for " + this.getName());
		};
	}

}
