package org.schema.game.common.controller.damage;

import org.schema.game.common.data.player.AbstractOwnerState;

public interface Hittable {
//	public ParticleHitCallback handleHit(ParticleHitCallback callback, Damager particleOwner, float damage, float damageBeforeShield, Vector3f startPos, Vector3f endPos, boolean shieldAbsorbed, long weaponId);


	public boolean isVulnerable();

	public boolean isPhysicalForDamage();

	public boolean checkAttack(Damager from, boolean checkDocked, boolean notifyFaction);

	public int getFactionId();

	public byte getFactionRights();

	public byte getOwnerFactionRights();

	public AbstractOwnerState getOwnerState();

	public boolean canBeDamagedBy(Damager from, DamageDealerType type);
}
