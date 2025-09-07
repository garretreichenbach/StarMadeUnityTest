package org.schema.game.common.data.blockeffects;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.dynamics.RigidBody;

public class PushEffect extends BlockEffect {

	public final Vector3f relPos;
	private final Vector3f from;
	private boolean applyTorque;
	private boolean alive = true;
	private float force;
	private boolean pushed;

	public PushEffect(SendableSegmentController controller, Vector3f relPos, Vector3f from, float force, boolean applyTorque) {
		super(controller, BlockEffectTypes.PUSH);
		assert (from != null);
		this.from = from;
		this.relPos = relPos;
		this.force = force;
		this.applyTorque = applyTorque;
	}

	/**
	 * @return the from
	 */
	public Vector3f getFrom() {
		return from;
	}

	/**
	 * @return the maxVelocity
	 */
	public float getMaxVelocity() {
		if (segmentController.isOnServer()) {
			return ((GameServerState) segmentController.getState()).getGameState().getMaxGalaxySpeed();
		}
		return ((GameClientState) segmentController.getState()).getGameState().getMaxGalaxySpeed();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.blockeffects.BlockEffect#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public void update(Timer timer, FastSegmentControllerStatus status) {
		if (!pushed) {
			SendableSegmentController segCon = (SendableSegmentController) segmentController.railController.getRoot();
 
			RigidBody r = segCon.getPhysicsObject();
			if (r != null) {

				force *= (Math.max(0f, 1f - segCon.getBlockEffectManager().status.gravEffectIgnorance));
				if (force > 0) {
					from.normalize();
					from.scale(force);

					//System.err.println(segCon.getState() + " PUSHING " + segCon + " WITH FORCE " + getFrom() + "; (" + getFrom().length() + ")");
					r.applyImpulse(from, relPos);

					if (applyTorque) {
						r.applyTorqueImpulse(from);
					}
					Vector3f tmp = new Vector3f();
					r.getLinearVelocity(tmp);
					if (tmp.length() > getMaxVelocity()) {
						tmp.normalize();
						tmp.scale(getMaxVelocity());
						r.setLinearVelocity(tmp);
					}
					r.getAngularVelocity(tmp);
					if (tmp.length() > 10) {
						tmp.normalize();
						tmp.scale(10);
						r.setAngularVelocity(tmp);
					}

					r.activate(true);
				}
			}
			pushed = true;
		} else {
			alive = false;
		}
	}

	@Override
	public void end() {
		alive = false;
	}

	@Override
	public boolean needsDeadUpdate() {
		return false;
	}

	/**
	 * @return the applyTorque
	 */
	public boolean isApplyTorque() {
		return applyTorque;
	}

	/**
	 * @param applyTorque the applyTorque to set
	 */
	public void setApplyTorque(boolean applyTorque) {
		this.applyTorque = applyTorque;
	}

	/**
	 * @return the force
	 */
	public float getForce() {
		return force;
	}

	/**
	 * @param force the force to set
	 */
	public void setForce(float force) {
		this.force = force;
	}
	@Override
	public boolean affectsMother() {
		return true;
	}
}
