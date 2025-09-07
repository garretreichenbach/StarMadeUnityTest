package org.schema.game.common.controller.damage.projectile;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.DamageHitInterface;

public class ProjectileHit implements DamageHitInterface{
	
	private float impactDamageRaw;
	private final Vector3f impactWorldPosition = new Vector3f();
	private final Vector3f impactWorldNormal = new Vector3f();
	
	@Override
	public Vector3f getImpactWorldPosition(){
		return impactWorldPosition;
	}
	@Override
	public Vector3f getImpactWorldNormal(){
		return impactWorldNormal;
	}
	
	@Override
	public float getImpactDamageRaw(){
		return impactDamageRaw;
	}
	@Override
	public DamageDealerType getDamageDealerType() {
		return DamageDealerType.PROJECTILE;
	}
	
}
