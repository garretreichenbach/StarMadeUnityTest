package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusGravityEffectIgnoranceEffect;

public class StatusGravityEffectsIgnoranceEffectFactory extends StatusEffectFactory<StatusGravityEffectIgnoranceEffect> {

	@Override
	public StatusGravityEffectIgnoranceEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusGravityEffectIgnoranceEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
