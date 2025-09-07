package org.schema.game.common.controller.damage.projectile;

import javax.vecmath.Vector3f;

public class ParticleHitCallback {

	public boolean hit = false;
	public boolean killedBlock;
	private float damageDone;
	public Vector3f hitPointWorld;
	public Vector3f hitNormalWorld;
	public ProjectileBlockHit blockHit;
	public boolean abortWhole;
	public long beforeBlockIndex = Long.MIN_VALUE;
	public long afterBlockIndex = Long.MIN_VALUE;

	public boolean hasDoneDamage() {
		return hit && damageDone > 0;
	}

	public void reset() {
		beforeBlockIndex = Long.MIN_VALUE;
		afterBlockIndex = Long.MIN_VALUE;
		hit = false;
		abortWhole = false;
		killedBlock = false;
		damageDone = 0;
	}

	public float getDamageDone() {
		return damageDone;
	}

	public void addDamageDone(float damageDone) {
		this.damageDone += damageDone;
	}

}
