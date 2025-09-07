package org.schema.game.common.controller.damage.beam;

import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.schine.graphicsengine.core.Timer;

public class DamageBeamHitHandlerCharacter extends DamageBeamHitHandlerNonBlock<AbstractCharacter<?>>{

	@Override
	public void reset() {
				
	}

	@Override
	public int onBeamDamage(AbstractCharacter<?> e, BeamState hittingBeam, int hits, CubeRayCastResult cubeResult,
			Timer timer) {
		if(e.isOnServer() && e.checkAttack(hittingBeam.getHandler().getBeamShooter(), true, true)) {
			e.damage(hittingBeam.getPowerByBeamLength()*hits, hittingBeam.getHandler().getBeamShooter());
			return hits;
		}else {
			return hits;
		}
	}
	
}