package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusPiercingProtectionEffect;

public class StatusPiercingProtectionEffectFactory extends StatusEffectFactory<StatusPiercingProtectionEffect> {

	@Override
	public StatusPiercingProtectionEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusPiercingProtectionEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
