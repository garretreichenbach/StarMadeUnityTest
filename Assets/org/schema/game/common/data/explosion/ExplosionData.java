package org.schema.game.common.data.explosion;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;

import com.bulletphysics.linearmath.Transform;

/**
 * container class for any explosion type damage
 * <p/>
 * (pulse, missile, explosive blocks)
 *
 * @author schema
 */
public class ExplosionData {
	public static final byte NORMAL = 0;
	public static final byte INNER = 1;
	public static final byte IGNORESHIELDS_GLOBAL = 2;

	public DamageDealerType damageType;
	public Transform centerOfExplosion;
	public Vector3f fromPos;
	public Vector3f toPos;
	public float radius;
	public float damageInitial;
	public float damageBeforeShields;
	public int sectorId;
	public boolean hitsFromSelf;
	public Damager from;
	public SegmentController to; //direct hit entity
	public long weaponId;
	public boolean ignoreShields;
	public AfterExplosionCallback afterExplosionHook;
	public boolean chain = true;
	public boolean ignoreShieldsSelf;
	public HitType hitType;
	public InterEffectSet attackEffectSet;
	public int originSectorId;
}
