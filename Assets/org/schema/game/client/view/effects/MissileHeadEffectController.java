package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.view.tools.ColorTools;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.missile.ClientMissileManager;
import org.schema.game.common.data.missile.Missile;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;

public class MissileHeadEffectController extends ParticleController<MissileHeadParticleContainer> {

	private Vector3f p = new Vector3f();
	private Vector3f tmp = new Vector3f();

	private ClientMissileManager missileController;

	public MissileHeadEffectController(boolean sorted, ClientMissileManager missileController) {
		super(sorted, MissileTrailDrawer.MAX_TRAILS * 2);
		this.missileController = missileController;
		//		this.setOrderedDelete(true);
	}

	public void add(Missile m) {
		//		System.err.println("ADDING IN "+index);
		int thisParticleIndex = addParticle(m.getWorldTransformOnClient().origin, tmp);

		getParticles().setId(thisParticleIndex, m.getId());
		ElementInformation info;
		if (ElementKeyMap.isValidType(m.getColorType()) && (info = ElementKeyMap.getInfo(m.getColorType())).isLightSource()) {
			Vector4f color = new Vector4f();
			color.set(info.getLightSourceColor());
			ColorTools.brighten(color);
			getParticles().setColor(thisParticleIndex, color);
		} else {
			getParticles().setColor(thisParticleIndex, 1, 1, 1, 1);
		}
	}

	@Override
	public boolean updateParticle(int particle, Timer timer) {


		Missile m = missileController.getMissile((short) getParticles().getId(particle));

		if (m != null && m.isAlive()) {
			p.set(m.getWorldTransformOnClient().origin);
			getParticles().setPos(particle, p.x, p.y, p.z);

			return true;
		} else {
//			System.err.println("[MISSILETRAIL] HEAD DIED");
			return false;
		}

	}

	@Override
	protected MissileHeadParticleContainer getParticleInstance(int size) {
		return new MissileHeadParticleContainer(size);
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
			particlePointer++;
			return pointer;
		} else {
			int pointer = particlePointer % getParticles().getCapacity();
			getParticles().setPos(pointer, from.x, from.y, from.z);
			getParticles().setStart(pointer, from.x, from.y, from.z);
			getParticles().setVelocity(pointer, toForce.x, toForce.y, toForce.z);
			getParticles().setLifetime(pointer, 0);
			getParticles().setId(pointer, id);
			particlePointer++;
			return pointer;
		}
	}

}
