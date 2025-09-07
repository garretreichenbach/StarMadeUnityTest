package org.schema.schine.graphicsengine.psys.modules.iface;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.physics.Physics;

public interface ParticleMoveInterface {
	void onParticleMove(Physics physics, Timer timer, ParticleContainer particle);
}
