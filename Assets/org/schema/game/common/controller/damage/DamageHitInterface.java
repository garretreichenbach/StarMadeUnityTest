package org.schema.game.common.controller.damage;

import javax.vecmath.Vector3f;

public interface DamageHitInterface {
	
	public DamageDealerType getDamageDealerType();
	public Vector3f getImpactWorldPosition();
	public Vector3f getImpactWorldNormal();
	public float getImpactDamageRaw();
}
