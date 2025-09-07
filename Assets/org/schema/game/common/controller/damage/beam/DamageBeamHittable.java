package org.schema.game.common.controller.damage.beam;

import org.schema.game.common.controller.damage.Hittable;

public interface DamageBeamHittable extends Hittable{
//	public int handleBeamDamage(BeamState beam, int hits, BeamHandlerContainer<? extends SimpleTransformableSendableObject> owner, Vector3f from, Vector3f to, CubeRayCastResult cubeResult, boolean ignoreShields, Timer timer);
	
	public DamageBeamHitHandler getDamageBeamHitHandler();
}
