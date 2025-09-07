package org.schema.schine.graphicsengine.psys.modules.iface;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.psys.ParticleContainer;

public interface ParticleUpdateInterface {
	void onParticleUpdate(Timer timer, ParticleContainer particle);
}
