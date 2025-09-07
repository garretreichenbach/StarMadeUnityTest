package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusArmorHpAbsorptionBonusEffect;

public class StatusArmorHpAbsorbtionEffectFactory extends StatusEffectFactory<StatusArmorHpAbsorptionBonusEffect> {

	@Override
	public StatusArmorHpAbsorptionBonusEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusArmorHpAbsorptionBonusEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

}
