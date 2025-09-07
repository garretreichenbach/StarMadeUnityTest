package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusPowerShieldEffect extends StatusBlockEffect {
	public StatusPowerShieldEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_POWER_SHIELD, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.powerShield = Math.min(getEffectCap(), ratio);
		;
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.powerShield;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
