package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;

public class StatusGravityEffectIgnoranceEffect extends StatusBlockEffect {
	public StatusGravityEffectIgnoranceEffect(SendableSegmentController controller, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, BlockEffectTypes.STATUS_GRAVITY_EFFECT_IGNORANCE, blockCount, idServer, effectCap, powerConsumption, basicMultiplier);
	}

	@Override
	public void setRatio(FastSegmentControllerStatus status, float ratio) {
		status.gravEffectIgnorance = Math.min(getEffectCap(), ratio);
		;
	}

	@Override
	public float getRatio(FastSegmentControllerStatus status) {
		return status.gravEffectIgnorance;
	}

	@Override
	public boolean affectsMother() {
		return true;
	}

}
