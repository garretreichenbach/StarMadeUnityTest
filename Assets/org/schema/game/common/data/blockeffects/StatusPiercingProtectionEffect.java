package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusPiercingProtectionEffect extends StatusBlockEffect {

	public StatusPiercingProtectionEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_PIERCING_PROTECTION, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);

	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.pierchingProtection = Math.min(getEffectCap(), ratio);
		;
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.pierchingProtection;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
