package org.schema.schine.graphicsengine.forms.particle.movingeffect;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;
import org.schema.schine.graphicsengine.forms.particle.simple.SimpleParticleContainer;

import com.bulletphysics.linearmath.Transform;

public class ParticleMovingEffectController extends ParticleController<SimpleParticleContainer> {

	public float spawnCount = 3.3f;
	protected float MAX_LIFETIME = 1000;
	Vector3f front_direction = new Vector3f();
	Vector3f pos = new Vector3f();
	Vector3f camDir = new Vector3f();
	private float spawnPlaneSize = 30;
	private float spawnPlaneDistance = 30;
	private float spawnPlaneDistanceSquared = spawnPlaneDistance * spawnPlaneDistance;
	private Random random = new Random();
	private float accumTime = 0;
	private float maxSpeed;

	public ParticleMovingEffectController(boolean sorted) {
		super(sorted);
	}

	public ParticleMovingEffectController(boolean sorted, int size) {
		super(sorted, size);
	}

	/**
	 * @return the accumTime
	 */
	public float getAccumTime() {
		return accumTime;
	}

	/**
	 * @param accumTime the accumTime to set
	 */
	public void setAccumTime(float accumTime) {
		this.accumTime = accumTime;
	}

	@Override
	public boolean updateParticle(int i, Timer timer) {

		Transform wt = getCamera();

		float sc = 3f;
		float lived = getParticles().getLifetime(i);
		getParticles().getPos(i, pos);
		camDir.sub(pos, wt.origin);
		if (camDir.lengthSquared() > spawnPlaneDistanceSquared * 2) {
			return false;
		}

		double product = (pos.x - wt.origin.x) * front_direction.x
				+ (pos.y - wt.origin.y) * front_direction.y
				+ (pos.z - wt.origin.z) * front_direction.z;
		if (product <= 0) {
			return false;
		} else {
			getParticles().setLifetime(i, (float) (lived + sc * timer.getDelta() * 1000.0));
		}
		return lived < MAX_LIFETIME;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleController#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {

		Controller.getCamera().getForward(front_direction);
		super.update(timer);
		accumTime += timer.getDelta();

	}

	public Transform getCamera() {

		Camera camera = Controller.getCamera();
		if (camera == null) {
			return null;
		}
		return camera.getWorldTransform();
	}

	public void updateEffectFromCam(Transform where, float speed) {

		this.maxSpeed = speed;

		Vector3f dir = new Vector3f(0, 0, 0);
		int sc = (int) (spawnCount * (speed / maxSpeed));

		for (int i = 0; i < sc; i++) {
			Vector3f spawnPoint = new Vector3f(where.origin);
			Vector3f forward = new Vector3f();
			Vector3f up = new Vector3f();
			Vector3f left = new Vector3f();

			GlUtil.getForwardVector(forward, where);
			GlUtil.getUpVector(up, where);
			GlUtil.getLeftVector(left, where);

			forward.scale(spawnPlaneDistance);
			up.scale(random.nextFloat() * spawnPlaneSize - spawnPlaneSize / 2);
			left.scale(random.nextFloat() * spawnPlaneSize - spawnPlaneSize / 2);

			spawnPoint.add(forward);
			spawnPoint.add(up);
			spawnPoint.add(left);

			addParticle(spawnPoint, dir);

		}

	}

	public void updateEffectFromCam(float speed, float maxSpeed) {

		Transform wt = getCamera();

		if (wt == null) {
			return;
		}

		this.maxSpeed = maxSpeed;

		Vector3f dir = new Vector3f(0, 0, 0);
		int sc = (int) (spawnCount * (speed / maxSpeed));

		for (int i = 0; i < sc; i++) {
			Vector3f spawnPoint = new Vector3f(wt.origin);
			Vector3f forward = new Vector3f();
			Vector3f up = new Vector3f();
			Vector3f left = new Vector3f();

			GlUtil.getForwardVector(forward, wt);
			GlUtil.getUpVector(up, wt);
			GlUtil.getLeftVector(left, wt);

			forward.scale(spawnPlaneDistance);
			up.scale(random.nextFloat() * spawnPlaneSize - spawnPlaneSize / 2);
			left.scale(random.nextFloat() * spawnPlaneSize - spawnPlaneSize / 2);

			spawnPoint.add(forward);
			spawnPoint.add(up);
			spawnPoint.add(left);

			addParticle(spawnPoint, dir);

		}

	}

	@Override
	protected SimpleParticleContainer getParticleInstance(int size) {
		return new SimpleParticleContainer(size);
	}

	@Override
	public int addParticle(Vector3f from, Vector3f toForce) {
		if (particlePointer >= getParticles().getCapacity() - 1) {
			getParticles().growCapacity();
			//TODO shrink capacity if count remains low
		}
		int id = idGen++;
		if (isOrderedDelete()) {
			int pointer = particlePointer % getParticles().getCapacity();
			getParticles().setPos(pointer, from.x, from.y, from.z);
			getParticles().setStart(pointer, from.x, from.y, from.z);
			getParticles().setVelocity(pointer, toForce.x, toForce.y, toForce.z);
			getParticles().setLifetime(pointer, 0);
			particlePointer++;
			return pointer;
		} else {
			int pointer = particlePointer % getParticles().getCapacity();
			getParticles().setPos(pointer, from.x, from.y, from.z);
			getParticles().setStart(pointer, from.x, from.y, from.z);
			getParticles().setVelocity(pointer, toForce.x, toForce.y, toForce.z);
			getParticles().setLifetime(pointer, 0);
			particlePointer++;
			return pointer;
		}
	}

}
