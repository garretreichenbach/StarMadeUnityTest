package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusAntiGravityEffect;

public class StatusAntiGravityEffectFactory extends StatusEffectFactory<StatusAntiGravityEffect> {

	@Override
	public StatusAntiGravityEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusAntiGravityEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
