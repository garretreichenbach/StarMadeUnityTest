package org.schema.schine.graphicsengine.psys.modules.iface;

import org.schema.schine.graphicsengine.psys.ParticleContainer;

import com.bulletphysics.linearmath.Transform;

public interface ParticleStartInterface {
	void onParticleSpawn(ParticleContainer newParticle, Transform systemTransform);
}
