package org.schema.schine.graphicsengine.forms.particle.simple;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;

public class ParticleSimpleController extends ParticleController<SimpleParticleContainer> {

	private static final float MAX_LIFETIME = 1300;

	Vector3f pos = new Vector3f();

	Vector3f vel = new Vector3f();

	public ParticleSimpleController(boolean sorted) {
		super(sorted);

	}

	public ParticleSimpleController(boolean sorted, int size) {
		super(sorted, size);
	}

	public void addOne() {
		Camera camera = Controller.getCamera();

		if (camera == null) {
			return;
		}

		Vector3f dir = new Vector3f(Controller.getCamera().getForward());
		Vector3f pos = new Vector3f(Controller.getCamera().getViewable()
				.getPos());
		dir.scale(0.2f);

		addParticle(pos, dir);
	}

	@Override
	public boolean updateParticle(int i, Timer timer) {
		float lived = getParticles().getLifetime(i);
		getParticles().getPos(i, pos);
		getParticles().getVelocity(i, vel);

		getParticles().setPos(i, pos.x + vel.x, pos.y + vel.y, pos.z + vel.z);
		return lived < MAX_LIFETIME;
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
