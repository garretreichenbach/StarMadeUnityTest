package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusShieldHardenEffect extends StatusBlockEffect {
	public StatusShieldHardenEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_SHIELD_HARDEN, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.shieldHarden = Math.min(getEffectCap(), ratio);
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.shieldHarden;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
