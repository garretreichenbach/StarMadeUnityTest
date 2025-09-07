package org.schema.game.common.data.missile;

import org.schema.game.common.data.missile.updates.MissileSpawnUpdate.MissileType;
import org.schema.game.common.data.missile.updates.MissileTargetUpdate;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;

public class FafoMissile extends TargetChasingMissile {

	private SimpleTransformableSendableObject designatedTarget;

	public FafoMissile(StateInterface state) {
		super(state);
	}

	/**
	 * @return the designatedTarget
	 */
	public SimpleTransformableSendableObject getDesignatedTarget() {
		return designatedTarget;
	}

	/**
	 * @param designatedTarget the designatedTarget to set
	 */
	public void setDesignatedTarget(SimpleTransformableSendableObject designatedTarget) {
		this.designatedTarget = designatedTarget;
	}

	@Override
	public MissileType getType() {
		return MissileType.FAFO;
	}

	@Override
	public void onSpawn() {
		super.onSpawn();
		this.setTarget((designatedTarget));
	}

	@Override
	public String toString() {
		return "Fafo" + super.toString() + ")->" + getTarget();
	}

	@Override
	public void updateTarget(Timer timer) {
		this.setTarget((designatedTarget));
		if (getTarget() != null && (!validTarget(getTarget()))) {
			setTarget(null);
			designatedTarget = null;
			MissileTargetUpdate missileTargetUpdate = new MissileTargetUpdate(getId());
			missileTargetUpdate.target = -1;
//			System.err.println("[SERVER][MISSILE] adding target update because fafo target is hidden " + getTarget() + " -> " + missileTargetUpdate.target);
			pendingBroadcastUpdates.add(missileTargetUpdate);
		}
	}

	private boolean validTarget(SimpleTransformableSendableObject target) {
		if (target.isHidden()) {
			return false;
		}
		if (!target.isNeighbor(getSectorId(), target.getSectorId())) {
			return false;
		}
		if (!getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(target.getId())) {
			return false;
		}

		return true;
	}
	
}
