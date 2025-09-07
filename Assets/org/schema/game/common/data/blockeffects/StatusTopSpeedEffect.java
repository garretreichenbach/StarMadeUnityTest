package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusTopSpeedEffect extends StatusBlockEffect {
	public StatusTopSpeedEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_TOP_SPEED, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.topSpeed = Math.min(getEffectCap(), ratio);
		;
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.topSpeed;
	}
	@Override
	public boolean affectsMother() {
		return true;
	}
}
