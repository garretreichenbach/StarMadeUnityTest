package org.schema.game.common.data.world.space;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.network.objects.NetworkSun;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;

public class Sun extends FixedSpaceEntity {

	private NetworkSun networkSun;

	public Sun(StateInterface state) {
		super(state);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.SUN;
	}

	@Override
	public NetworkEntity getNetworkObject() {
		return networkSun;
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.SUN;
	}
	@Override
	public String getRealName() {
		return "name";
	}

	@Override
	public void newNetworkObject() {
		this.networkSun = new NetworkSun(getState());
	}

	@Override
	public void sendHitConfirm(byte damageType) {
		
	}
	public CollisionType getCollisionType() {
		return CollisionType.SIMPLE;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
		return false;
	}

	@Override
	public String getName() {
		return "Sun";
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
