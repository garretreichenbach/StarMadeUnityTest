package org.schema.schine.graphicsengine.forms.particle;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import javax.vecmath.Vector3f;

public abstract class ParticleController<E extends ParticleContainer> {

	private final boolean sorted;
	protected int particlePointer;
	protected int offset;
	protected int idGen = 0;
	private final E particles;
	private boolean orderedDelete;
	public long collisionTime;
	public ParticleController(boolean sorted) {
		this.sorted = sorted;
		particles = (getParticleInstance(ParticleContainer.DEFAULT_CAPACITY));

	}
	protected abstract E getParticleInstance(int size);
	public ParticleController(boolean sorted, int size) {
		this.sorted = sorted;
		particles = (getParticleInstance(size));

	}

	protected int addEmptyParticle() {
		if (particlePointer >= particles.getCapacity() - 1) {
			particles.growCapacity();
			//TODO shrink capacity if count remains low
		}
		if (orderedDelete) {
			int pointer = particlePointer % particles.getCapacity();
			particlePointer++;
			return pointer;
		} else {
			int pointer = particlePointer % particles.getCapacity();
			particlePointer++;
			return pointer;
		}
	}

	/**
	 * @param from
	 * @param toForce
	 * @return pointer in array where projectile has been added
	 */
	public abstract int addParticle(Vector3f from, Vector3f toForce);

	public void deleteParticle(int toDelete) {
		if (orderedDelete) {
			deleteParticleOrdered(toDelete);
		} else {
			//replace particle to delete with last from array and decrement num of projectiles
			particles.copy((particlePointer - 1) % particles.getCapacity(), toDelete);
			particlePointer--;
		}
	}

	private void deleteParticleOrdered(int toDelete) {
		//this is only used when the particles that are dying
		//are the lowest in the array (timed particles)
		offset++;
	}
	
	public int getOffset() {
		return offset;
	}

	public int getParticleCount() {
		return particlePointer - offset;
	}

	public E getParticles() {
		return particles;
	}


	/**
	 * @return the orderedDelete
	 */
	public boolean isOrderedDelete() {
		return orderedDelete;
	}

	/**
	 * @param orderedDelete the orderedDelete to set
	 */
	public void setOrderedDelete(boolean orderedDelete) {
		this.orderedDelete = orderedDelete;
	}

	protected void onUpdateStart() {

	}

	public void reset() {
		//		particles.reset();
		particlePointer = 0;
		offset = 0;
	}

	private void sortParticles(Vector3f from, int count) {
		int start = 0;
		int end = count;
		particles.sort(from, start, end);
	}

	public abstract boolean updateParticle(int particleIndex, Timer timer);

	//	private Vector3f velocityHelper = new Vector3f();
	//	private Vector4f colorHelper = new Vector4f();
	//	private Vector3f posHelper = new Vector3f();
	//	private Vector3f speedHelper = new Vector3f();
	//	private Vector3f posBeforeUpdate = new Vector3f();
	
	public void update(Timer timer) {

		collisionTime = 0;
		onUpdateStart();
		if (orderedDelete) {
			for (int i = offset; i < particlePointer; i++) {
				int p = i % particles.getCapacity();

				boolean alive = updateParticle(p, timer);

				if (!alive) {
					deleteParticle(p);
				}
			}
		} else {
			for (int i = 0; i < particlePointer; i++) {

				boolean alive = updateParticle(i, timer);

				if (!alive) {

					deleteParticle(i);
					//				System.err.println("DEAD "+projectilePointer);
					i--; //do this index again
					continue;
				}
			}

			if (EngineSettings.G_PARTICLE_SORTING.isOn() && sorted && Controller.getCamera() != null) {
				sortParticles(Controller.getCamera().getPos(), getParticleCount() - 1);
			}
		}
		collisionTime = (collisionTime / 1000000L);
		if(collisionTime > 150){
			System.err.println("''WARN''' PATICLE COLLISION TIME "+collisionTime);
		}
	}

}
