package org.schema.game.common.controller.damage.beam;

import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class DamageBeamHitHandlerNonBlock<E extends SimpleGameObject> implements DamageBeamHitHandler{
	
	public abstract int onBeamDamage(E e, BeamState hittingBeam, int hits, CubeRayCastResult cubeResult, Timer timer);
}