package org.schema.game.common.data.creature;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.meta.weapon.Weapon;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.PlayerInventory;
import org.schema.schine.input.KeyboardMappings;

import com.bulletphysics.linearmath.Transform;

public class AICompositeCreaturePlayer extends AIPlayer {

	int naturalDamage = 3;
	private long targetMoveCommand;
	private long moveTimeout;
	private Transform inv = new Transform();
	private Transform wt = new Transform();
	private Vector3f d = new Vector3f();
	private long vol_lastShot;
	private long reload = 200;
	private Vector4f color = new Vector4f(0.5f, 0, 0, 1);
	private float distance = 1;
	private float speed = 100;

	public AICompositeCreaturePlayer(AICreature<? extends AIPlayer> creature) {
		super(creature);

	}

	@Override
	protected boolean isFlying() {
		return !getCreature().getGravity().isGravityOn();
	}

	@Override
	protected Inventory initInventory() {

		return new PlayerInventory(this, AbstractOwnerState.NORM_INV);

	}

	@Override
	public void resetTargetToMove() {
		assert (isOnServer());
//		System.err.println("RESETTING TARGET "+getNetworkObject().hasTarget.get()+"; "+getNetworkObject().target.getVector());
		setTarget(null);
		getNetworkObject().hasTarget.set(false);

	}

	@Override
	public boolean isTargetReachTimeout() {
		return getTarget() != null && moveTimeout > 0 && System.currentTimeMillis() - targetMoveCommand > moveTimeout;
	}

	@Override
	public boolean isTargetReachLocalTimeout() {
		return getTarget() != null && moveTimeout > 0 && System.currentTimeMillis() - lastTargetSet > 1000;
	}

	@Override
	public void setTargetToMove(Vector3f pos, long timeout) {
		targetMoveCommand = System.currentTimeMillis();
		moveTimeout = timeout;
		Transform t = new Transform();
		t.setIdentity();
		t.origin.set(pos);
		setTarget(t);
		assert (isOnServer());
//		System.err.println("SETTING TARGET "+t.origin+"; "+getNetworkObject().target.getVector());
		getNetworkObject().target.set(t.origin);
		getNetworkObject().hasTarget.set(true);

	}

	@Override
	public float getMaxHealth() {
		return 85;
	}

	@Override
	public boolean isAtTarget() {
		if (getTarget() == null) {
			return true;
		}

		if (getCreature().getAffinity() != null) {
			inv.set(getCreature().getAffinity().getWorldTransform());
			wt.set(getWorldTransform());
			inv.inverse();
			inv.mul(wt);
			wt.set(inv);
		} else {
			wt.setIdentity();
		}

		d.sub(getTarget().origin, wt.origin);
//		System.err.println(getState()+" AT TARGET: "+d.length());
		return d.length() < 0.5f;
	}

	@Override
	protected void updateServer() {
		if (getTarget() == null) {

		}
	}

	@Override
	protected void updateClient() {

	}

	@Override
	public boolean hasNaturalWeapon() {
		return true;
	}

	@Override
	public void fireNaturalWeapon(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, Vector3f dir) {
		dir.scale(speed);
		if (!state.isOnServer() && ((GameClientState) state.getState()).getCurrentSectorId() != playerCharacter.getSectorId()) {
			return;
		}
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - vol_lastShot > reload) {
			
			long weaponId = Long.MIN_VALUE;
			int penetrationDepth = 1;
			float projectileWidth = 1;
			float impactForce = 1;
			playerCharacter.getParticleController().addProjectile(
					playerCharacter,
					new Vector3f(playerCharacter.getShoulderWorldTransform().origin),
					dir,
					naturalDamage,
					distance,
					0,
					projectileWidth,
					penetrationDepth,
					impactForce,
					weaponId, color, 0);
			vol_lastShot = currentTimeMillis;
		}
		getCreature().lastAttack = System.currentTimeMillis();
//		System.err.println("ATTACKING: "+getState()+" "+getCreature().isAttacking());
		assert (getCreature().isAttacking());
	}

	@Override
	public boolean isFactoryInUse() {
		return false;
	}


	@Override
	protected void onNoSlotFree(short type, int amount) {
		
	}

	@Override
	public void onFiredWeapon(Weapon object) {
		getCreature().lastAttack = System.currentTimeMillis();
	}

	@Override
	public String getConversationScript() {
		if (conversationScript == null) {
			conversationScript = "creature-general.lua";
		}
		return conversationScript;
	}

	@Override
	public void interactClient(AbstractOwnerState from) {
		GameClientState state = (GameClientState) getState();
		state.getPlayer().getPlayerConversationManager().clientInteracted(this);
	}

	@Override
	public long getDbId() {
		return Long.MIN_VALUE;
	}

	@Override
	public boolean isDown(KeyboardMappings mapping) {
		return false;
	}
}
