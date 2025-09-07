package org.schema.game.common.data.blockeffects;

public class FastSegmentControllerStatus {
	public float pierchingProtection;
	public float armorHarden;
	public float powerShield;
	public float shieldHarden;
	public float topSpeed;
	public float antiGravity;
	public float gravEffectIgnorance;
	public float armorHPDeductionBonus;
	public float armorHPAbsorbtionBonus;

	public float shieldRegenPercent = 1;
	public float powerRegenPercent = 1;
	public float thrustPercent = 1;

	public FastSegmentControllerStatus(FastSegmentControllerStatus status) {
		pierchingProtection = status.pierchingProtection;
		armorHarden = status.armorHarden;
		powerShield = status.powerShield;
		shieldHarden = status.shieldHarden;
		armorHPAbsorbtionBonus = status.armorHPAbsorbtionBonus;
		armorHPDeductionBonus = status.armorHPDeductionBonus;
		topSpeed = status.topSpeed;
		antiGravity = status.antiGravity;
		gravEffectIgnorance = status.gravEffectIgnorance;
		shieldRegenPercent = status.shieldRegenPercent;
		powerRegenPercent = status.powerRegenPercent;
		thrustPercent = status.thrustPercent;
	}

	public FastSegmentControllerStatus() {
	}

	public void reset() {
		pierchingProtection = 0;
		armorHarden = 0;
		powerShield = 0;
		shieldHarden = 0;
		armorHPAbsorbtionBonus = 0;
		armorHPDeductionBonus = 0;
		topSpeed = 0;
		antiGravity = 0;
		gravEffectIgnorance = 0;
		shieldRegenPercent = 1;
		powerRegenPercent = 1;
		thrustPercent = 1;
	}
}
