package org.schema.game.common.data.blockeffects;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

public class EvadeEffect extends BlockEffect {

	private boolean alive = true;

	private boolean pushed;

	private float force;

	public EvadeEffect(SendableSegmentController controller, float force) {
		super(controller, BlockEffectTypes.EVADE);
		this.force = force;
	}

	/**
	 * @return the maxVelocity
	 */
	public float getMaxVelocity() {
		if (segmentController instanceof Ship) {
			return ((Ship) segmentController).getCurrentMaxVelocity();
		} else {
			return 0;
		}
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
		if (!pushed && segmentController.getPhysicsDataContainer().getObject() != null &&
				segmentController.getPhysicsDataContainer().getObject() instanceof RigidBody) {
			PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer()).getPowerAddOn();
			RigidBody r = (RigidBody) segmentController.getPhysicsDataContainer().getObject();
			Vector3f speed = r.getLinearVelocity(new Vector3f());
			if (powerAddOn.consumePowerInstantly(VoidElementManager.EVADE_EFFECT_POWER_CONSUMPTION_MULT * force)
					) {

				System.err.println(segmentController.getState() + " EVADING " + segmentController);

				Transform worldTransform = r.getWorldTransform(new Transform());

				Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), worldTransform);

				forwardVector.scale(force);
				forwardVector.negate();
				
				Vector3f speedTest = new Vector3f(speed);
				
				speedTest.scaleAdd(r.getInvMass(), forwardVector, speedTest);
				if(speedTest.length() > getMaxVelocityAbsolute() && speedTest.length() > speed.length()){
					//cant use to gain more speed
					pushed = true;
					return;
				}
				
				r.applyCentralImpulse(forwardVector);
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
