package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusArmorHpHardeningBonusEffect extends StatusBlockEffect {
	public StatusArmorHpHardeningBonusEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_ARMOR_HARDEN, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.armorHPDeductionBonus = Math.min(getEffectCap(), ratio);
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.armorHPDeductionBonus;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
