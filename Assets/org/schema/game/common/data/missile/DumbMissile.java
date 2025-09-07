package org.schema.game.common.data.missile;

import org.schema.game.common.data.missile.updates.MissileSpawnUpdate.MissileType;
import org.schema.schine.network.StateInterface;

public class DumbMissile extends StraightFlyingMissile {
	public DumbMissile(StateInterface state) {
		super(state);
	}



	@Override
	public MissileType getType() {
		return MissileType.DUMB;
	}

	@Override
	public void onSpawn() {
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.missile.Missile#toString()
	 */
	@Override
	public String toString() {
		return "Dumb" + super.toString();
	}
}
