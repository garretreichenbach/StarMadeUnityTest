package org.schema.game.common.data.blockeffects;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.dynamics.RigidBody;

public class StopEffect extends BlockEffect {

	private boolean alive = true;
	private boolean applied = false;
	private float stoppingForce;

	public StopEffect(SendableSegmentController controller, float stoppingForce) {
		super(controller, BlockEffectTypes.STOP);
		this.stoppingForce = stoppingForce;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public void update(Timer timer, FastSegmentControllerStatus status) {
		if (!applied) {
			SendableSegmentController segCon = (SendableSegmentController) segmentController.railController.getRoot();
			
			RigidBody r = segCon.getPhysicsObject();
			if (r != null) {
				stoppingForce *= (Math.max(0f, 1f - segCon.getBlockEffectManager().status.gravEffectIgnorance));
				if (stoppingForce > 0) {

//					System.err.println(segmentController.getState()+" STOPPING "+segmentController+" WITH FORCE "+getStoppingForce());

//					int thrusters = segmentController.getElementClassCountMap().get(ElementKeyMap.THRUSTER_ID);

					float damping = Math.min(0.95f, stoppingForce / (segCon.getMass()));
					damping = Math.max(((GameStateInterface) segCon.getState()).getGameState().getLinearDamping(), damping);

					float befLin = r.getLinearDamping();
					float befRot = r.getAngularDamping();

					r.setDamping(damping, damping);

					r.applyDamping(timer.getDelta());

					r.setDamping(befLin, befRot);

					r.activate(true);
				}

			}
			applied = true;
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
	 * @return the stoppingForce
	 */
	public float getStoppingForce() {
		return stoppingForce;
	}

	/**
	 * @param stoppingForce the stoppingForce to set
	 */
	public void setStoppingForce(float stoppingForce) {
		this.stoppingForce = stoppingForce;
	}
	@Override
	public boolean affectsMother() {
		return true;
	}
}
