package org.schema.game.common.controller.damage.beam;

import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.world.space.PlanetCore;
import org.schema.schine.graphicsengine.core.Timer;

public class DamageBeamHitHandlerPlanetCore extends DamageBeamHitHandlerNonBlock<PlanetCore>{

	@Override
	public void reset() {
		
	}

	@Override
	public int onBeamDamage(PlanetCore e, BeamState hittingBeam, int hits, CubeRayCastResult cubeResult, Timer timer) {
		if(e.checkAttack(hittingBeam.getHandler().getBeamShooter(), true, true)) {
			if(e.isOnServer()) {
				e.setHitPoints(Math.max(0, e.getHitPoints()-hittingBeam.getPowerByBeamLength()*hits));
				if(e.getHitPoints() <= 0) {
					e.setDestroyed(true);
				}
			}
			
			return hits;
		}else {
			return 0;
		}
	}
	
}
