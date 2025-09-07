package org.schema.game.common.data.blockeffects.config;

import org.schema.game.common.data.blockeffects.config.parameter.*;
import org.schema.schine.common.language.Lng;

import java.util.Locale;

import static org.schema.game.common.data.blockeffects.config.StatusEffectCategory.*;

public enum StatusEffectType {

	//SHIELDS
	SHIELD_RECHARGE_RATE(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Recharge Rate");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_CAPACITY(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Capacity");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_CAPACITY_UPKEEP(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Upkeep");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_UNDER_FIRE_RECHARGE_NERF(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Under-Fire Recharge Rate");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_UNDER_FIRE_TIMEOUT(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Under-Fire Timeout");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_ZERO_SHIELDS_TIMEOUT(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield-Zero Timeout");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_DAMAGE_RESISTANCE(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Damage Efficiency");
		}
	}, true, StatusEffectWeaponType.class, StatusEffectFloatValue.class),
	SHIELD_HEAT_DAMAGE_TAKEN(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Heat Damage Taken");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_HOTSPOT_ALPHA(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Hotspot Alpha");
		}
	}, true, StatusEffectBooleanValue.class),
	SHIELD_HOTSPOT_DPS(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Hotspot DPS");
		}
	}, true, StatusEffectBooleanValue.class),
	SHIELD_HOTSPOT_PERCENTAGE(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Hotspot %");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_HOTSPOT_RANGE(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Hotspot Range");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_HOTSPOT_RECHARGE_MODE(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Hotspot using Recharge instead of Capacity");
		}
	}, true, StatusEffectBooleanValue.class),
	SHIELD_DEFENSE_KINETIC(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Kinetic Shield");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_DEFENSE_HEAT(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Heat Shield");
		}
	}, true, StatusEffectFloatValue.class),
	SHIELD_DEFENSE_EM(SHIELDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("EM Shield");
		}
	}, true, StatusEffectFloatValue.class),

	//THRUSTERS
	THRUSTER_TOP_SPEED(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top Speed");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_ACCELERATION(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Acceleration");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_BRAKING(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Braking");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_TURN_RATE(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Turn Rate");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_ANTI_GRAVITY(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Anti-Gravity");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_POWER_CONSUMPTION(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Power Consumption");
		}
	}, true, StatusEffectFloatValue.class),

	THRUSTER_DAMPENING(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Dampening");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_CONFIG_CHANGE_TIMEOUT(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Config Change Timeout");
		}
	}, true, StatusEffectFloatValue.class),

	THRUSTER_BLAST_STRENGTH(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Blast Strength (m/s)");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_BLAST_POWER_CONSUMPTION_CHARGING(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Blast Consumption");
		}
	}, true, StatusEffectFloatValue.class),
	THRUSTER_BLAST_COOLDOWN(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Blast Cooldown");
		}
	}, true, StatusEffectFloatValue.class),

	THRUSTER_BLAST(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Blast");
		}
	}, true, StatusEffectBooleanValue.class),
	THRUSTER_EVADE(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Evade");
		}
	}, true, StatusEffectBooleanValue.class),

	THRUSTER_BLAST_MULTI_CHARGE_COUNT(THRUSTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Blast Count");
		}
	}, true, StatusEffectIntValue.class),

	//POWER
	POWER_RECHARGE_EFFICIENCY(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Power Recharge Efficiency");
		}
	}, true, StatusEffectFloatValue.class),
	POWER_MODULE_CHARGING_RATE_MOD(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Module-Charging Power Cost");
		}
	}, true, StatusEffectFloatValue.class), //change cost of reload charging on e.g. weapons
	POWER_TOP_OFF_RATE_MOD(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top-off Power Cost");
		}
	}, true, StatusEffectFloatValue.class), //change cost of top off for e.g. weapons
	POWER_STABILIZER_DISTANCE(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Stabilizer Distance");
		}
	}, true, StatusEffectFloatValue.class),
	POWER_CONDUIT_POWER_USAGE(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Conduit Power Usage");
		}
	}, true, StatusEffectFloatValue.class),

	REACTOR_FAILSAFE(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Failsafe");
		}
	}, false, StatusEffectBooleanValue.class),
	REACTOR_FAILSAFE_THRESHOLD(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Failsafe Threshold");
		}
	}, true, StatusEffectFloatValue.class),
	REACTOR_FAILSAFE_HPPERCENT_MIN_TARGET_THRESHOLD(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Failsafe Target Min HP%");
		}
	}, true, StatusEffectFloatValue.class),
	REACTOR_BOOST(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Boost");
		}
	}, false, StatusEffectBooleanValue.class),
	REACTOR_BOOST_STRENGTH(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Boost Strength");
		}
	}, true, StatusEffectFloatValue.class),
	REACTOR_BOOST_DURATION(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Boost Duration");
		}
	}, false, StatusEffectFloatValue.class),
	REACTOR_BOOST_COOLDOWN(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Boost Cooldown");
		}
	}, false, StatusEffectFloatValue.class),

	//TODO
	REACTOR_SHIELD(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Shield");
		}
	}, false, StatusEffectBooleanValue.class),
	CHAMBER_SHIELD(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Chamber Shield");
		}
	}, false, StatusEffectBooleanValue.class),

	REACTOR_EXPLOSIVENESS(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Explosiveness");
		}
	}, true, StatusEffectFloatValue.class),
	REACTOR_SHIELD_CAPACITY(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Shield Capacity ");
		}
	}, false, StatusEffectFloatValue.class),
	REACTOR_SHIELD_POWER_CONSUMPTION(POWER, new Object() {
		@Override
		public String toString() {
			return Lng.str("Reactor Shield Powerefficiency");
		}
	}, false, StatusEffectFloatValue.class),

	//JUMP
	JUMP_DISTANCE(JUMP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Distance");
		}
	}, false, StatusEffectFloatValue.class),
	JUMP_CHARGE_TIME(JUMP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Time");
		}
	}, true, StatusEffectFloatValue.class),
	JUMP_AUTO_CHARGE(JUMP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Auto Charge");
		}
	}, false, StatusEffectBooleanValue.class),
	JUMP_POWER_CHARGE_RATE(JUMP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	JUMP_POWER_TOPOFF_RATE(JUMP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top Off Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	JUMP_MULTI_CHARGE_COUNT(JUMP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Count");
		}
	}, false, StatusEffectIntValue.class),
	JUMP_DRIVE_ENABLE(JUMP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Jump Drive Capability");
		}
	}, false, StatusEffectBooleanValue.class),

	//WARP
	WARP_DISTANCE(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Distance");
		}
	}, false, StatusEffectFloatValue.class),
	WARP_POWER_EFFICIENCY(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Power Efficiency");
		}
	}, true, StatusEffectFloatValue.class),
	WARP_FREE_TARGET(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Free Target");
		}
	}, false, StatusEffectBooleanValue.class),
	WARP_INTERDICTION(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction");
		}
	}, false, StatusEffectBooleanValue.class),
	WARP_INTERDICTION_DISTANCE(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Distance");
		}
	}, false, StatusEffectIntValue.class),
	WARP_INTERDICTION_COOLDOWN(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Cooldown");
		}
	}, false, StatusEffectFloatValue.class),
	WARP_INTERDICTION_STRENGTH(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Strength (Max Reactor Lvl)");
		}
	}, false, StatusEffectIntValue.class),
	WARP_INTERDICTION_RANGE(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Range (in sectors)");
		}
	}, false, StatusEffectFloatValue.class),
	WARP_INTERDICTION_POWER_CONSUMPTION(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Power Consumption");
		}
	}, false, StatusEffectFloatValue.class),
	WARP_INTERDICTION_ACTIVE_RESTING_POWER_CONS(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Active Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	WARP_INTERDICTION_INACTIVE_RESTING_POWER_CONS(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Inactive Power Cost");
		}
	}, true, StatusEffectFloatValue.class),

	WARP_INTERDICTION_ACTIVE(WARP, new Object() {
		@Override
		public String toString() {
			return Lng.str("Interdiction Active");
		}
	}, false, StatusEffectBooleanValue.class),

	//RECON
	SCAN_STRENGTH(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Strength");
		}
	}, false, StatusEffectFloatValue.class),
	SCAN_POWER_CHARGE_RATE(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	SCAN_POWER_TOPOFF_RATE(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top-Off Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	SCAN_USAGE_TIME(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Usage Time");
		}
	}, true, true, StatusEffectFloatValue.class),
	SCAN_CHARGE_TIME(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Time");
		}
	}, true, StatusEffectFloatValue.class),
	SCAN_ACTIVE_RESTING_POWER_CONS(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Active Power Cost");
		}
	}, true, StatusEffectBooleanValue.class),
	SCAN_INACTIVE_RESTING_POWER_CONS(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Inactive Power Cost");
		}
	}, true, StatusEffectBooleanValue.class),
	SCAN_ACTIVE_RESTING_POWER_CONS_MULT(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Active Power Cost (x charging)");
		}
	}, true, StatusEffectFloatValue.class),
	SCAN_INACTIVE_RESTING_POWER_CONS_MULT(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Inactive Power Cost (x charging)");
		}
	}, true, StatusEffectFloatValue.class),
	ORE_SCANNER(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Ore Scanner");
		}
	}, false, StatusEffectBooleanValue.class),
	CARGO_SCANNER(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Cargo Scanner");
		}
	}, false, StatusEffectBooleanValue.class),
	SCAN_LONG_RANGE_DISTANCE(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Long Range Distance");
		}
	}, false, StatusEffectFloatValue.class),
	SCAN_LONG_RANGE_SCANNER(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Long Range Scanners");
		}
	}, false, StatusEffectBooleanValue.class),
    @Deprecated //this is for the old Power 2 non-block scanning addon
	SCAN_SHORT_RANGE_SCANNER_ENABLE(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Short Range Scanner Capability");
		}
	}, false, StatusEffectBooleanValue.class),
	SCAN_SHORT_RANGE_SHARING_ENABLE(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Scan Data Sharing With Allies");
		}
	}, false, StatusEffectBooleanValue.class),

	RESOURCE_SCANNER_CORE(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Resource Scanner (Core Resources)");
		}
	}, false, StatusEffectBooleanValue.class),

	RESOURCE_SCANNER_NEBULA(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Resource Scanner (Nebula Resources)");
		}
	}, false, StatusEffectBooleanValue.class),

	RESOURCE_SCANNER_ATMOSPHERE(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Resource Scanner (Atmospheric Resources)");
		}
	}, false, StatusEffectBooleanValue.class),

	RESOURCE_SCANNER_PROSPECTOR(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Prospector Scanner");
		}
	}, false, StatusEffectBooleanValue.class),

	//TODO
	ANOMALY_SCANNER(SCANNERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Anomaly Scanner");
		}
	}, false, StatusEffectBooleanValue.class),

	//JAMMING
	JAM_POWER(OLD_RADARJAM, new Object() {
		@Override
		public String toString() {
			return Lng.str("Power");
		}
	}, false, StatusEffectFloatValue.class),
	JAM_POWER_EFFICIENCY(OLD_RADARJAM, new Object() {
		@Override
		public String toString() {
			return Lng.str("Power efficiency");
		}
	}, true, StatusEffectFloatValue.class),

	//CARGO
	CARGO_WEIGHT(CARGO, new Object() {
		@Override
		public String toString() {
			return Lng.str("Weight");
		}
	}, true, StatusEffectFloatValue.class),
	CARGO_VOLUME(CARGO, new Object() {
		@Override
		public String toString() {
			return Lng.str("Volume");
		}
	}, true, StatusEffectFloatValue.class),
	CARGO_DAMAGE_RESISTANCE(CARGO, new Object() {
		@Override
		public String toString() {
			return Lng.str("Resistance");
		}
	}, true, StatusEffectFloatValue.class),

	//STEALTH
	STEALTH_STRENGTH(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Strength");
		}
	}, false, StatusEffectFloatValue.class),
	STEALTH_POWER_CHARGE_RATE(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	STEALTH_POWER_TOPOFF_RATE(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top Off Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	STEALTH_USAGE_TIME(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Usage Time");
		}
	}, true, true, StatusEffectFloatValue.class),
	STEALTH_CHARGE_TIME(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Time");
		}
	}, true, StatusEffectFloatValue.class),
	STEALTH_MISSILE_LOCK_ON_TIME(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Missile Lock On Time");
		}
	}, true, StatusEffectFloatValue.class),
	STEALTH_JAMMER_CAPABILITY(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Jammer");
		}
	}, false, StatusEffectBooleanValue.class),
	STEALTH_CLOAK_CAPABILITY(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Cloaker");
		}
	}, false, StatusEffectBooleanValue.class),
	STEALTH_ACTIVE_RESTING_POWER_CONS(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Active Power Cost");
		}
	}, true, StatusEffectBooleanValue.class),
	STEALTH_INACTIVE_RESTING_POWER_CONS(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Inactive Power Cost");
		}
	}, true, StatusEffectBooleanValue.class),
	STEALTH_ACTIVE_RESTING_POWER_CONS_MULT(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Active Power Cost (x charging)");
		}
	}, true, StatusEffectFloatValue.class),
	STEALTH_INACTIVE_RESTING_POWER_CONS_MULT(STEALTH, new Object() {
		@Override
		public String toString() {
			return Lng.str("Inactive Power Cost (x charging)");
		}
	}, true, StatusEffectFloatValue.class),

	//CLOAK (OLD)
	CLOAK_TIME(OLD_CLOAKERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Time");
		}
	}, true, StatusEffectFloatValue.class),
	CLOAK_CHARGE_TIME(OLD_CLOAKERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Time");
		}
	}, true, StatusEffectFloatValue.class),
	CLOAK_POWER_CHARGE_RATE(OLD_CLOAKERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	CLOAK_POWER_TOPOFF_RATE(OLD_CLOAKERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top Off Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	CLOAK_STRENGTH(OLD_CLOAKERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Volume");
		}
	}, true, StatusEffectFloatValue.class),

	//TRANSPORTER
	TRANSPORTER_DISTANCE(TRANSPORTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Distance");
		}
	}, false, StatusEffectIntValue.class),
	TRANSPORTER_NO_SHIELD_DOWN(TRANSPORTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Shield Requirement");
		}
	}, true, StatusEffectFloatValue.class),
	TRANSPORTER_POWER_CHARGE_RATE(TRANSPORTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	TRANSPORTER_POWER_TOPOFF_RATE(TRANSPORTERS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top Off Power Cost");
		}
	}, true, StatusEffectFloatValue.class),

	//SHIPYARDS
	SHIPYARD_POWER_EFFICIENCY(SHIPYARDS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Power Efficiency");
		}
	}, true, StatusEffectFloatValue.class),

	//FACTORIES
	FACTORIES_POWER_CHARGE_RATE(FACTORIES, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	FACTORIES_POWER_TOPOFF_RATE(FACTORIES, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top-Off Power Cost");
		}
	}, true, StatusEffectFloatValue.class),
	FACTORY_BAKE_TIME_MULT(FACTORIES, new Object() {
		@Override
		public String toString() {
			return Lng.str("Factory Bake Time");
		}
	}, true, StatusEffectFloatValue.class),
	SHIP_FACTORY(FACTORIES, new Object() {
		@Override
		public String toString() {
			return Lng.str("Ship Factory");
		}
	}, false, StatusEffectBooleanValue.class),

	//MINING
	GAS_HARVEST_BONUS_ACTIVE(MINING, new Object() {
		@Override
		public String toString() {
			return Lng.str("Active Bonus Gas Harvest");
		}
	}, false, StatusEffectFloatValue.class),
	MINING_BONUS_ACTIVE(MINING, new Object() {
		@Override
		public String toString() {
			return Lng.str("Active Bonus Materials");
		}
	}, false, StatusEffectIntValue.class),
	MINING_BONUS_PASSIVE(MINING, new Object() {
		@Override
		public String toString() {
			return Lng.str("Passive Bonus Materials");
		}
	}, false, StatusEffectIntValue.class),
	//TODO
	MINERAL_TO_RESOURCE(MINING, new Object() {
		@Override
		public String toString() {
			return Lng.str("Minerals to Resource conversion");
		}
	}, true, StatusEffectFloatValue.class),

	//WARHEADS
	WARHEAD_RADIUS(WARHEADS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Radius");
		}
	}, false, StatusEffectIntValue.class),
	WARHEAD_DAMAGE(WARHEADS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Damage");
		}
	}, true, StatusEffectFloatValue.class),
	WARHEAD_CHANCE_FOR_EXPLOSION_ON_HIT(WARHEADS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Chance for explosion on hit");
		}
	}, true, StatusEffectFloatValue.class),
	WARHEAD_CHANCE_FOR_SPONTANIOUS_EXPLODE(WARHEADS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Chance for spontaneous explosion");
		}
	}, true, StatusEffectFloatValue.class),

	//AI
	AI_DISABLE(AI, new Object() {
		@Override
		public String toString() {
			return Lng.str("Disable");
		}
	}, false, StatusEffectBooleanValue.class),
	AI_ACCURACY_TURRET(AI, new Object() {
		@Override
		public String toString() {
			return Lng.str("Turret Accuracy");
		}
	}, true, StatusEffectFloatValue.class),
	AI_ACCURACY_DRONE(AI, new Object() {
		@Override
		public String toString() {
			return Lng.str("Drone Accuracy");
		}
	}, true, StatusEffectFloatValue.class),
	AI_ACCURACY_POINT_DEFENSE(AI, new Object() {
		@Override
		public String toString() {
			return Lng.str("Point Defense Accuracy");
		}
	}, true, StatusEffectFloatValue.class),

	//RAILS
	RAIL_SPEED(RAILS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Speed");
		}
	}, true, StatusEffectFloatValue.class),
	RAIL_ENHANCER_POWER_EFFICIENCY(RAILS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Power Efficiency");
		}
	}, true, StatusEffectFloatValue.class),

	//VISIBILITY
	CAMERA_INTERFERENCE(VISIBILITY, new Object() {
		@Override
		public String toString() {
			return Lng.str("Camera Interference");
		}
	}, false, StatusEffectFloatValue.class),

	//DAMAGE
	DAMAGE_TAKEN(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("Damage Taken");
		}
	}, true, StatusEffectWeaponType.class, StatusEffectFloatValue.class),
	HULL_HEAT_DAMAGE_TAKEN(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("Hull Heat Damage Taken");
		}
	}, true, StatusEffectFloatValue.class),
	//TODO
	DAMAGE_PHYSICS_DAMAGE_EFFECT_TAKEN(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("Physics Effect Dmg Taken");
		}
	}, true, StatusEffectFloatValue.class),
	DAMAGE_EMP_DAMAGE_TAKEN(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("Emp Dmg Taken");
		}
	}, true, StatusEffectFloatValue.class),
	DAMAGE_OUTPUT_MULTIPLIER(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("Damage Output");
		}
	}, true, StatusEffectWeaponType.class, StatusEffectFloatValue.class),

	GENERAL_DEFENSE_KINETIC(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("Kinetic Protection");
		}
	}, true, StatusEffectFloatValue.class),
	GENERAL_DEFENSE_HEAT(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("Heat Protection");
		}
	}, true, StatusEffectFloatValue.class),
	GENERAL_DEFENSE_EM(DAMAGE, new Object() {
		@Override
		public String toString() {
			return Lng.str("EM Protection ");
		}
	}, true, StatusEffectFloatValue.class),

	//WEAPON
	WEAPON_RANGE(WEAPON, new Object() {
		@Override
		public String toString() {
			return Lng.str("Range");
		}
	}, true, StatusEffectWeaponType.class, StatusEffectFloatValue.class),
	WEAPON_DAMAGE(WEAPON, new Object() {
		@Override
		public String toString() {
			return Lng.str("Damage");
		}
	}, true, StatusEffectWeaponType.class, StatusEffectFloatValue.class),
	WEAPON_TOP_OFF_RATE(WEAPON, new Object() {
		@Override
		public String toString() {
			return Lng.str("Top-off Power Cost");
		}
	}, true, StatusEffectWeaponType.class, StatusEffectFloatValue.class),
	WEAPON_CHARGE_RATE(WEAPON, new Object() {
		@Override
		public String toString() {
			return Lng.str("Charge Power Cost");
		}
	}, true, StatusEffectWeaponType.class, StatusEffectFloatValue.class),

	//MASS
	MASS_MOD(MASS, new Object() {
		@Override
		public String toString() {
			return Lng.str("Mass");
		}
	}, true, StatusEffectFloatValue.class),

	//ARMOR
	ARMOR_HP_EFFICIENCY(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("Armor HP Efficiency");
		}
	}, true, StatusEffectFloatValue.class),
	ARMOR_HP_ABSORPTION(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("Armor HP Absorption");
		}
	}, true, StatusEffectFloatValue.class),
	ARMOR_HP_REGENERATION(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("Armor HP Regeneration");
		}
	}, true, StatusEffectFloatValue.class),
	ARMOR_HP_BLEEDTHROUGH_THRESHOLD(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("Armor HP Bleedthrough Threshold");
		}
	}, true, StatusEffectFloatValue.class),
	ARMOR_DEFENSE_KINETIC(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("Kinetic Armor");
		}
	}, true, StatusEffectFloatValue.class),
	ARMOR_DEFENSE_HEAT(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("Heat Armor");
		}
	}, true, StatusEffectFloatValue.class),
	ARMOR_DEFENSE_EM(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("EM Armor");
		}
	}, true, StatusEffectFloatValue.class),

	ARMOR_DEFENSE_ENVIRONMENTAL(ARMOR, new Object() {
		@Override
		public String toString() {
			return Lng.str("Environmental Armor");
		}
	}, true, StatusEffectFloatValue.class),

	//GRAVITY
	GRAVITY_OVERRIDE_ENTITY_DIR(GRAVITY, new Object() {
		@Override
		public String toString() {
			return Lng.str("Gravity Override Dir");
		}
	}, true, StatusEffectFloatValue.class),
	GRAVITY_OVERRIDE_ENTITY_CENTRAL(GRAVITY, new Object() {
		@Override
		public String toString() {
			return Lng.str("Gravity Override Central");
		}
	}, true, StatusEffectFloatValue.class),
	GRAVITY_OVERRIDE_ENTITY_SWITCH(GRAVITY, new Object() {
		@Override
		public String toString() {
			return Lng.str("Gravity Override Switch");
		}
	}, false, StatusEffectBooleanValue.class),

	//TODO
	BUILD_INHIBITOR(BUILDING, new Object() {
		@Override
		public String toString() {
			return Lng.str("Build Inhibitor");
		}
	}, false, StatusEffectBooleanValue.class),

	//TODO
	// I know this is a really messy way to do this, but i think its the best option for now
	//What i've done before:
	// Turned it into a class, with a bunch of public static variables
	// You could add stuff to it on the fly, all of the enums (values(), valueOf(), etc) worked
	// but switch statements wont work
	// I could go and change all the switch statements, but that is going to be 100000x easier once I get the real source
	CUSTOM_EFFECT_01(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_02(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_03(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_04(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_05(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_06(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_07(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_08(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_09(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_10(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_11(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_12(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_13(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_14(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_15(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_16(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_17(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_18(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_19(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_20(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),
	CUSTOM_EFFECT_21(POWER, new Object() {
		public String toString() {
			return "Custom Effect";
		}
	}, false, StatusEffectFloatValue.class),

	;

	///

	private final StatusEffectCategory category;
	private final boolean percentage;
	public final Object nameObj;
	public final Class<? extends StatusEffectParameter>[] effectParameters;
	public final StatusEffectParameter[] effectParameterInstances;
	public final boolean usageTime;

	static {
		assert (values().length < 256) : values().length; //network types by ordinal have to fit in a byte
	}

	StatusEffectType(StatusEffectCategory category, Object nm, boolean percentage, Class<? extends StatusEffectParameter>... effectParameters) {
		this(category, nm, percentage, false, effectParameters);
	}

	StatusEffectType(StatusEffectCategory category, Object nm, boolean percentage, boolean usageTime, Class<? extends StatusEffectParameter>... effectParameters) {
		this.category = category;
		nameObj = nm;
		this.effectParameters = effectParameters;
		this.usageTime = usageTime;
		effectParameterInstances = new StatusEffectParameter[effectParameters.length];
		this.percentage = percentage;
		for(int i = 0; i < effectParameters.length; i++) {
			Class<? extends StatusEffectParameter> e = effectParameters[i];
			try {
				effectParameterInstances[i] = e.newInstance();
			} catch(InstantiationException e1) {
				e1.printStackTrace();
			} catch(IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "[" + category.getName() + "]" + getName();
	}

	public String getName() {
		return nameObj.toString();
	}

	public static String getAll() {
		StringBuffer b = new StringBuffer();
		for(int i = 0; i < values().length; i++) {
			StatusEffectType statusEffectType = values()[i];
			b.append(statusEffectType.name().toLowerCase(Locale.ENGLISH));
			if(i < values().length - 1) {
				b.append(", ");
			}
		}
		return b.toString();
	}

	public StatusEffectCategory getCategory() {
		return category;
	}

	public StatusEffectParameter getInstance(Class<? extends StatusEffectParameter> a) {
		for(int i = 0; i < effectParameters.length; i++) {
			if(effectParameters[i] == a) {
				return effectParameterInstances[i];
			}
		}
		throw new RuntimeException("no effect Class found " + a.getName());
	}

	public String getFullName() {
		return "[" + category + "] " + getName();
	}

	public boolean isPercentage() {
		return percentage;
	}

	public boolean isTimed() {
		return usageTime;
	}

	/**
	 * determines if "sets X to 100% will show up in the stats"
	 *
	 * @return true if it should show up
	 */
	public boolean respectOnePointZero() {
		return this == REACTOR_BOOST_STRENGTH;
	}
}
