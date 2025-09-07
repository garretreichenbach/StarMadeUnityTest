package org.schema.schine.graphicsengine.forms.particle.explosion.particle;

import java.util.Random;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ExplosionParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;

public class ParticleExplosionController extends ParticleController<ExplosionParticleContainer> {

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
	public ParticleExplosionController(boolean sorted) {
		super(sorted);
	}

	public void addExplosion(Vector3f center, Vector3f cameraPos, float size, int count, long weaponId) {
		
		for (int i = 0; i < count; i++) {
			dir.set(0.5f - FastMath.rand.nextFloat(), 0.5f - FastMath.rand.nextFloat(), 0.5f - FastMath.rand.nextFloat());
			dir.scale(explosionSpeed * (size / 4.0f));
			int p = addParticle(center, dir);
			getParticles().setDamage(p, (size / 4.0f));
			getParticles().setWeaponId(p, weaponId);
			int index = r.nextInt(ParticleExplosionDrawer.explTex.length);
			getParticles().setSpriteCode(p, index, ParticleExplosionDrawer.explTex[index].getMultiSpriteMaxX(), ParticleExplosionDrawer.explTex[index].getMultiSpriteMaxY());
//			getParticles().setUserdata(p, (size / 4.0f), 0, 0, weaponId);
		}
	}

	@Override
	public boolean updateParticle(int i, Timer timer) {
		getParticles().getVelocity(i, velocityHelper);
		getParticles().getColor(i, colorHelper);
		getParticles().getPos(i, posHelper);
		getParticles().getStart(i, startHelper);

		posBeforeUpdate.set(posHelper);
		velocityBefore.set(velocityHelper);

		float lived = getParticles().getLifetime(i);
		velocityHelper.scale((float) (timer.getDelta() * 1000 * 0.2));

		getParticles().setLifetime(i, (float) (lived + timer.getDelta()));

		posHelper.add(velocityHelper);
		//		System.err.println(timer.getDelta()+" on "+state+": "+posHelper);
		//		System.err.println("put particle: "+posHelper+", "+velocityHelper);
		velocityBefore.scale(0.995f);
		getParticles().setVelocity(i, velocityBefore.x, velocityBefore.y, velocityBefore.z);
		getParticles().setPos(i, posHelper.x, posHelper.y, posHelper.z);
		//		if(lived >= MAX_LIFETIME){
		//			System.err.println(lived);
		//		}
		//		System.err.println("l "+lived+"/"+MAX_LIFETIME);
		return lived < MAX_LIFETIME;
	}

	@Override
	protected ExplosionParticleContainer getParticleInstance(int size) {
		return new ExplosionParticleContainer(size);
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
	
}
