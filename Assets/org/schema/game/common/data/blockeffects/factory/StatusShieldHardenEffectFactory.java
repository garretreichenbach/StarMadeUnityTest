package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusShieldHardenEffect;

public class StatusShieldHardenEffectFactory extends StatusEffectFactory<StatusShieldHardenEffect> {

	@Override
	public StatusShieldHardenEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusShieldHardenEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
