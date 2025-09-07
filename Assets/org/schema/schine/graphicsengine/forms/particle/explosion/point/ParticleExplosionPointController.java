package org.schema.schine.graphicsengine.forms.particle.explosion.point;

import java.util.Random;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ExplosionParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;

public class ParticleExplosionPointController extends ParticleController<ExplosionParticleContainer> {

	private static final float MAX_LIFETIME = 3;
	Vector3f dir = new Vector3f();
	private Vector3f velocityHelper = new Vector3f();
	private Vector4f colorHelper = new Vector4f();
	private Vector3f posHelper = new Vector3f();
	private Vector3f startHelper = new Vector3f();
	private Vector3f posBeforeUpdate = new Vector3f();
	private Vector3f velocityBefore = new Vector3f();
	private float explosionSpeed = 0.003f;
	private static Random r = new Random();
	public ParticleExplosionPointController(boolean sorted) {
		super(sorted);
	}

	public void addExplosion(Vector3f center, Vector3f cameraPos, float size, int count, float dmg, long weaponId) {
		
			dir.set(0,0,0);
			int p = addParticle(center, dir);
			getParticles().setDamage(p, Math.min( 80, Math.max(5, dmg * 0.05f)));
//			getParticles().setDamage(p, 50);
			float lifetime = 0.11f;
			getParticles().setImpactForce(p, lifetime );
			getParticles().setColor(p, 1,1,1,1);
	}
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
			getParticles().setId(pointer, id);
			getParticles().setBlockHitIndex(pointer, 0);
			getParticles().setShotStatus(pointer, 0);
			particlePointer++;
			return pointer;
		} else {
			int pointer = particlePointer % getParticles().getCapacity();
			getParticles().setPos(pointer, from.x, from.y, from.z);
			getParticles().setStart(pointer, from.x, from.y, from.z);
			getParticles().setVelocity(pointer, toForce.x, toForce.y, toForce.z);
			getParticles().setLifetime(pointer, 0);
			getParticles().setId(pointer, id);
			getParticles().setBlockHitIndex(pointer, 0);
			getParticles().setShotStatus(pointer, 0);
			particlePointer++;
			return pointer;
		}
	}
	@Override
	public boolean updateParticle(int i, Timer timer) {
//		getParticles().getVelocity(i, velocityHelper);
//		getParticles().getColor(i, colorHelper);
//		getParticles().getPos(i, posHelper);
//		getParticles().getStart(i, startHelper);
//
//		posBeforeUpdate.set(posHelper);
//		velocityBefore.set(velocityHelper);

		float lived = getParticles().getLifetime(i);
		float maxLife = getParticles().getImpactForce(i);
//		velocityHelper.scale((float) (timer.getDelta() * 1000 * 0.2));

		getParticles().setLifetime(i, (float) (lived + timer.getDelta()));

//		posHelper.add(velocityHelper);
//		//		System.err.println(timer.getDelta()+" on "+state+": "+posHelper);
//		//		System.err.println("put particle: "+posHelper+", "+velocityHelper);
//		velocityBefore.scale(0.995f);
//		getParticles().setVelocity(i, velocityBefore.x, velocityBefore.y, velocityBefore.z);
//		getParticles().setPos(i, posHelper.x, posHelper.y, posHelper.z);
		//		if(lived >= MAX_LIFETIME){
		//			System.err.println(lived);
		//		}
		//		System.err.println("l "+lived+"/"+MAX_LIFETIME);
		return lived < maxLife;
	}

	@Override
	protected ExplosionParticleContainer getParticleInstance(int size) {
		return new ExplosionParticleContainer(size);
	}
	
}
