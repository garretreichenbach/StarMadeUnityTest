package org.schema.game.common.data.blockeffects;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.graphicsengine.core.Timer;


import javax.vecmath.Vector3f;

public class TakeOffEffect extends BlockEffect {

	private boolean alive = true;

	private boolean pushed;

	private float force;

	private Vector3f dir = new Vector3f(0,0,1);

	public TakeOffEffect(SendableSegmentController controller, float force, float x, float y, float z) {
		super(controller, BlockEffectTypes.TAKE_OFF);
		this.force = force;
		dir.set(x, y, z);
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
		if (!pushed && (RigidBody) segmentController.getPhysicsDataContainer().getObject() != null) {
			RigidBody r = (RigidBody) segmentController.getPhysicsDataContainer().getObject();
			Vector3f speed = r.getLinearVelocity(new Vector3f());
			PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer()).getPowerAddOn();
			
			if ((segmentController.isUsingPowerReactors() || powerAddOn.consumePowerInstantly(VoidElementManager.EVADE_EFFECT_POWER_CONSUMPTION_MULT * force)) &&
					segmentController.getPhysicsDataContainer().getObject() != null &&
					segmentController.getPhysicsDataContainer().getObject() instanceof RigidBody) {

				

				

				Transform worldTransform = r.getWorldTransform(new Transform());
				
				
				
				Vector3f forwardVector = new Vector3f(dir);
//				worldTransform.basis.transform(forwardVector);

				
				float force = this.force;
				forwardVector.scale(force);
				Vector3f speedTest = new Vector3f(speed);
				speedTest.scaleAdd(r.getInvMass(), forwardVector, speedTest);
				float fBef = force;
				int tries = 0;
                float maxVel = getMaxVelocity();
				while(force > 0 && speedTest.length() > maxVel && speedTest.length() > speed.length()){
					if(tries > 10000) {
						force *= 0.5f;
					}
					//cant use to gain more speed
					force = Math.max(0, force - 1f);
					forwardVector.normalize();
					forwardVector.scale(force);
					speedTest = new Vector3f(speed);
					Vector3f speedOnly = new Vector3f(speed);
					Vector3f addOnly = new Vector3f(forwardVector);
					addOnly.scale(r.getInvMass());
					
					speedTest.add(speedOnly, addOnly);
					if(addOnly.length() > getMaxVelocityAbsolute() * 3) {
						force *= 0.5f;
					}else if(addOnly.length() > getMaxVelocityAbsolute() * 3) {
						force = Math.max(0, force - 10f);
					}
					tries++;
				}
				
				if(force != fBef ){
					System.err.println(segmentController.getState() + " TAKING OFF CAPPED " + segmentController+" -> "+dir+" -> "+forwardVector+" (*"+force+") Initial Force: "+ this.force);
				}else{
					System.err.println(segmentController.getState() + " TAKING OFF " + segmentController+" -> "+dir+" -> "+forwardVector+" (*"+force+") Initial Force: "+ this.force);
				}
				if(force <= 0){
					pushed = true;
					return;
				}
				
				
				r.applyCentralImpulse(forwardVector);
				
				
				
				Vector3f tmp = new Vector3f();
				r.getLinearVelocity(tmp);
				System.err.println(segmentController.getState() + " AFTER TAKING OFF " + segmentController+" -> "+forwardVector+" -> "+force+" -> speed: "+tmp+"; "+tmp.length()+" m/s");
//				if (tmp.length() > getMaxVelocity()) {
//					tmp.normalize();
//					tmp.scale(getMaxVelocity());
//					r.setLinearVelocity(tmp);
//				}
//				r.getAngularVelocity(tmp);
//				if (tmp.length() > 10) {
//					tmp.normalize();
//					tmp.scale(10);
//					r.setAngularVelocity(tmp);
//				}

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



	public Vector3f getDirection() {
		return dir;
	}
}
