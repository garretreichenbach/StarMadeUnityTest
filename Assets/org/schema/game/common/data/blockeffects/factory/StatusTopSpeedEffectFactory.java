package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusTopSpeedEffect;

public class StatusTopSpeedEffectFactory extends StatusEffectFactory<StatusTopSpeedEffect> {

	@Override
	public StatusTopSpeedEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusTopSpeedEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
