package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusArmorHardenEffect extends StatusBlockEffect {
	public StatusArmorHardenEffect(SendableSegmentController controller, int blockCount, long pos, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_ARMOR_HARDEN, blockCount, pos, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.armorHarden = Math.min(getEffectCap(), ratio);
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.armorHarden;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
