package org.schema.game.common.controller.damage;

import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.StateInterface;

public interface Damager {

	public static final byte BLOCK = 1;
	public static final byte SHIELD = 2;
	public static final byte CHARACTER = 3;

	StateInterface getState();

	void sendHitConfirm(byte damageType);

	boolean isSegmentController();
	
	public SimpleTransformableSendableObject<?> getShootingEntity();

	public int getFactionId();

	String getName();

	public AbstractOwnerState getOwnerState();

	public void sendClientMessage(String str, byte type);

	public float getDamageGivenMultiplier();

	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType);

	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType);

	public int getSectorId();

	public void sendServerMessage(Object[] astr, byte msgType);


}
