package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.view.tools.ColorTools;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.missile.Missile;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;
import org.schema.schine.graphicsengine.forms.particle.trail.ParticleTrailDrawerVBO;
import org.schema.schine.graphicsengine.forms.particle.trail.TrailControllerInterface;

import com.bulletphysics.linearmath.Transform;

public class MissileTrailController extends ParticleController<MissileTrailParticleContainer> implements TrailControllerInterface {

	private static final float MAX_LIFETIME = 15;
	private final Vector3f pos = new Vector3f();
	private final Vector3f vel = new Vector3f();
	public int particleIndex = -1;
	boolean letItEnd;
	int sectorBefore = -1;
	private Missile missile;
	private boolean alive = false;
	private Vector3f posTmp = new Vector3f();
	private int MAX_ADDITIONAL_SECTIONS = 10;
	private Transform lastTarget;
	private Vector4f color;
	private Vector3f dirDummy = new Vector3f();

	public MissileTrailController(Missile missile, MissileTrailDrawer missileTrailDrawer) {
		super(false, 1024);
		setOrderedDelete(true);
		this.missile = missile;
		sectorBefore = -1;

		this.color = new Vector4f(1, 1, 1, 1);
	}

	private void addOne(Vector3f where) {
		if (!letItEnd) {
			addParticle(where, dirDummy);
		}
	}

	public void endTrail() {
		letItEnd = true;

	}

	/**
	 * @return the alive
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * @param alive the alive to set
	 */
	public void setAlive(boolean alive) {
		//		try{
		//			throw new NullPointerException(this.alive+" -> "+alive);
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		this.alive = alive;
	}

	/**
	 * @return the missile
	 */
	public Missile getMissile() {
		return missile;
	}

	/**
	 * @param target the target to set
	 */
	public void setMissile(Missile missile) {
		this.missile = missile;
		sectorBefore = -1;

	}

	public void startTrail(Missile missile) {
		reset();
		this.missile = missile;
		color.set(1, 1, 1, 1);

		ElementInformation info;
		if (missile != null && ElementKeyMap.isValidType(missile.getColorType()) && (info = ElementKeyMap.getInfoFast(missile.getColorType())).isLightSource()) {
			color.set(info.getLightSourceColor());
			ColorTools.brighten(color);
		}

		lastTarget = null;
		letItEnd = false;
		alive = true;

	}

	@Override
	public boolean updateParticle(int i, Timer timer) {
		float lived = getParticles().getLifetime(i);
		getParticles().getPos(i, pos);
		getParticles().getVelocity(i, vel);
		getParticles().setLifetime(i, lived + timer.getDelta());

		getParticles().setPos(i, pos.x + vel.x, pos.y + vel.y, pos.z + vel.z);
		return lived < MAX_LIFETIME;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleController#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		
		float distBetween = Math.max(ParticleTrailDrawerVBO.DIST_BETWEEN_SECTIONS, missile.getSpeed()*0.01f);
//		System.err.println("DIS: "+distBetween);
		if (lastTarget == null) {
			//			System.err.println("ADDING NEW PARTICLE");
			addOne(missile.getWorldTransformOnClient().origin);
			lastTarget = new Transform(missile.getWorldTransformOnClient());
		} else {
			posTmp.sub(lastTarget.origin, missile.getWorldTransformOnClient().origin);
			if (posTmp.length() > 300) {

				System.err.println("MISSILE SRCTOR CHANGE FOR DRAWER " + posTmp);
				reset();
				//				for(int i = 0; i < getParticleCount(); i++){
				//					getParticles().getPos(i, pos);
				////					posTmp.sub(pos, lastTarget.origin);
				//
				//
				//					getParticles().setPos(i,
				//							pos.x-posTmp.x,
				//							pos.y-posTmp.y,
				//							pos.z-posTmp.z);
				//				}
				lastTarget.set(missile.getWorldTransformOnClient());
			}

			posTmp.set(missile.getWorldTransformOnClient().origin);
			posTmp.sub(lastTarget.origin);

			float len = posTmp.length();

			if (len > distBetween) {
//				posTmp.normalize();

//				lastTarget.origin.add(posTmp);

//				float r = ParticleTrailDrawerVBO.DIST_BETWEEN_SECTIONS;
//				int i = 0;
//				int sections = (int) (len / r);
//				float secDist = ParticleTrailDrawerVBO.DIST_BETWEEN_SECTIONS;
//				if (sections > MAX_ADDITIONAL_SECTIONS) {
//					//creating too many new particles would
//					//overload the memory
//					secDist = len / MAX_ADDITIONAL_SECTIONS; //fill distance
//				}
//
//				posTmp.scale(secDist);
//				while (r < len) {
//					lastTarget.origin.add(posTmp);
//					addOne(lastTarget.origin);
//					r += secDist;
//				}
				addOne(missile.getWorldTransformOnClient().origin);
				
				lastTarget.set(missile.getWorldTransformOnClient());
			}

		}

		sectorBefore = missile.getSectorId();
		super.update(timer);

		if (letItEnd && getParticleCount() <= 0) {
			alive = false;
		}
	}

	@Override
	public Vector4f getColor() {
		return color;
	}
	@Override
	protected MissileTrailParticleContainer getParticleInstance(int size) {
		return new MissileTrailParticleContainer(size);
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
