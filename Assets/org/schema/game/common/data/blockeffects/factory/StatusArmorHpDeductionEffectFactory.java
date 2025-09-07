package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusArmorHpHardeningBonusEffect;

public class StatusArmorHpDeductionEffectFactory extends StatusEffectFactory<StatusArmorHpHardeningBonusEffect> {

	@Override
	public StatusArmorHpHardeningBonusEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusArmorHpHardeningBonusEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
