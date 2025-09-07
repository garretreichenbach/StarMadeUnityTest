package org.schema.game.common.controller.elements;

public interface WeaponElementManagerInterface {

	float MAX_SPEED = 100;

	public double calculateWeaponDamageIndex();

	public double calculateWeaponRangeIndex();

	public double calculateWeaponHitPropabilityIndex();

	public double calculateWeaponSpecialIndex();

	public double calculateWeaponPowerConsumptionPerSecondIndex();

}
