package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusArmorHardenEffect;

public class StatusArmorHardenEffectFactory extends StatusEffectFactory<StatusArmorHardenEffect> {

	@Override
	public StatusArmorHardenEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusArmorHardenEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
