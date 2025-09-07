package org.schema.game.common.data.world.space;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.network.objects.NetworkBlackHole;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;

public class BlackHole extends FixedSpaceEntity {

	private NetworkBlackHole networkBlackHole;

	public BlackHole(StateInterface state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.BLACK_HOLE;
	}

	@Override
	public NetworkEntity getNetworkObject() {
		return networkBlackHole;
	}

	@Override
	public String getRealName() {
				return null;
	}
	public CollisionType getCollisionType() {
		return CollisionType.SIMPLE;
	}
	@Override
	public void newNetworkObject() {
		this.networkBlackHole = new NetworkBlackHole(getState());
	}

	@Override
	public void sendHitConfirm(byte damageType) {
		
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
				return false;
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.BLACK_HOLE;
	}
	@Override
	public String getName() {
		return "Black Hole";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	@Override
	public void sendClientMessage(String str, byte type) {
				
	}
	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
	}
	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}


}
