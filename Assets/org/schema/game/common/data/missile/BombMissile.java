package org.schema.game.common.data.missile;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate.MissileType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;

public class BombMissile extends StraightFlyingMissile implements ActivationMissileInterface{
	
	private float activationTimer;
	
	public BombMissile(StateInterface state) {
		super(state);
		this.selfDamage = true;
	}


	@Override
	public MissileType getType() {
		return MissileType.BOMB;
	}

	@Override
	protected DamageDealerType getDamageType() {
		return DamageDealerType.EXPLOSIVE;
	}

	@Override
	public void onSpawn() {
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.missile.Missile#toString()
	 */
	@Override
	public String toString() {
		return "Bomb" + super.toString();
	}



	@Override
	public float getActivationTimer() {
		return activationTimer;
	}

	@Override
	public void setActivationTimer(float activationTimer) {
		this.activationTimer = activationTimer;
	}
	public void onDud() {
		sendServerMessage(Lng.astr(
				"Your bomb was a dud, since it wasn't activated yet.\n"
				+ "It takes %s sec to become active after fire.", StringTools.formatPointZero(activationTimer)
						), 
				ServerMessage.MESSAGE_TYPE_ERROR);
	}
	protected boolean isDud() {
		return getLifetime() < activationTimer;
	}
	@Override
	protected boolean canHitSelf(){
		return getLifetime() > activationTimer;
	}
	@Override
	protected boolean isIgnoreShieldsSelf() {
		return true;
	}
	@Override
	protected boolean isIgnoreShields() {
		return true;
	}
	protected long getMissileTimeoutMs() {
		return 60000;
	}
}
