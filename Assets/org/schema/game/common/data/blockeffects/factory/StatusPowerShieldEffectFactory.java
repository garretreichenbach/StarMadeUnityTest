package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusPowerShieldEffect;

public class StatusPowerShieldEffectFactory extends StatusEffectFactory<StatusPowerShieldEffect> {

	@Override
	public StatusPowerShieldEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusPowerShieldEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
