package org.schema.schine.graphicsengine.psys.modules.iface;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.psys.ParticleContainer;

public interface ParticleColorInterface {
	void onParticleColor(Vector4f color, ParticleContainer particle);
}
