package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.simple.ParticleSimpleController;

public class ShipFlareParticleController extends ParticleSimpleController {

	Vector3f tmp = new Vector3f();

	public ShipFlareParticleController(boolean sorted) {
		super(sorted, 16);
	}

	public void addFlare(Vector3i absolutePos) {
		tmp.set(0, 0, 0);
		int addParticle = addParticle(tmp, tmp);
		//		System.err.println("ADDING PARTICLE AT "+absolutePos);
		getParticles().setPos(addParticle, absolutePos.x - Ship.core.x, absolutePos.y - Ship.core.y, absolutePos.z - Ship.core.z);
		getParticles().setStart(addParticle, absolutePos.x, absolutePos.y, absolutePos.z);

		getParticles().setColor(addParticle, 1, 1, 1, 1);
	}

	public void removeFlare(Vector3i absolutePos) {
		for (int i = 0; i < getParticleCount(); i++) {
			getParticles().getStart(i, tmp);

			if (tmp.x == absolutePos.x && tmp.y == absolutePos.y && tmp.z == absolutePos.z) {
				deleteParticle(i);
				System.err.println("FOUND PARTICLE TO DELETE: " + i);
				break;
			}
		}
	}

	@Override
	public boolean updateParticle(int i, Timer timer) {
		return true;
	}

}
