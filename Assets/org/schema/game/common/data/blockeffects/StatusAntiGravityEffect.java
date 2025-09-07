package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusAntiGravityEffect extends StatusBlockEffect {
	public StatusAntiGravityEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_ANTI_GRAVITY, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.antiGravity = Math.min(getEffectCap(), ratio);
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.antiGravity;
	}
	@Override
	public boolean affectsMother() {
		return true;
	}
}
