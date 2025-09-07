package org.schema.game.common.controller;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.network.objects.NetworkSpaceCreature;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.CollisionShape;

public class SpaceCreature extends AbstractSpaceCreature {

	private NetworkSpaceCreature networkObject;


	public SpaceCreature(StateInterface state) {
		super(state);
	}

	@Override
	public void onCollision(ManifoldPoint pt, Sendable sendableB) {
	}

	@Override
	public boolean checkAttack(Damager from, boolean checkDocked, boolean notifyFaction) {
		return true;
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.SPACE_CREATURE;
	}
	@Override
	public void newNetworkObject() {
		this.networkObject = new NetworkSpaceCreature(getState());
	}

	@Override
	public NetworkSpaceCreature getNetworkObject() {
		return this.networkObject;
	}

	@Override
	protected CollisionShape createCreatureCollisionShape() {
		return null;
	}

	@Override
	public String toNiceString() {
		return getName();
	}
	public CollisionType getCollisionType() {
		return CollisionType.SIMPLE;
	}
	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
	}
	@Override
	public void sendClientMessage(String str, byte type) {
				
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
				return null;
	}

	@Override
	public byte getFactionRights() {
				return 0;
	}

	@Override
	public byte getOwnerFactionRights() {
				return 0;
	}
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		throw new RuntimeException("TODO");
	}

	@Override
	public DamageBeamHitHandler getDamageBeamHitHandler() {
		throw new RuntimeException("TODO");
	}
	
}
