package org.schema.game.common.data.explosion;

import javax.vecmath.Vector3f;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public interface ExplosionDamageInterface {
	public float damageBlock(int x, int y, int z, float damage);

	public void setDamage(int x, int y, int z, float damage);

	public float getDamage(int x, int y, int z);

	public void resetDamage();

	public ShortOpenHashSet getClosedList();

	public ShortOpenHashSet getCpy();

	public Vector3f getExplosionCenter();

	public float modifyDamageBasedOnBlockArmor(int i, int j, int k, float damage);
}
