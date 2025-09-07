package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.StatusArmorHpAbsorptionBonusEffect;
import org.schema.game.common.data.blockeffects.StatusArmorHpRegenBonusEffect;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class StatusArmorHpRegenBonusEffectFactory extends StatusEffectFactory<StatusArmorHpRegenBonusEffect> {

	@Override
	public StatusArmorHpRegenBonusEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StatusArmorHpRegenBonusEffect(controller, blockCount, idServer, effectCap, powerConsumption, multiplier);
	}

	@Override
	public void setFrom(StatusArmorHpRegenBonusEffect to) {

	}
}
