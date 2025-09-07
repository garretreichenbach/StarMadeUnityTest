package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusArmorHpAbsorptionBonusEffect extends StatusBlockEffect {
	public StatusArmorHpAbsorptionBonusEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_ARMOR_HP_ABSORPTION_BONUS, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.armorHPAbsorbtionBonus = Math.min(getEffectCap(), ratio);
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.armorHPAbsorbtionBonus;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
