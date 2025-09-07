package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.schine.graphicsengine.core.Timer;

public class PowerRegenDownEffect extends BlockEffect {

	private float force;

	public PowerRegenDownEffect(SendableSegmentController controller, float slowAmount) {
		super(controller, BlockEffectTypes.NO_POWER_RECHARGE);
		this.force = slowAmount;
	}

	@Override
	public void update(Timer timer, FastSegmentControllerStatus status) {
		status.powerRegenPercent = force;
	}

	@Override
	public boolean needsDeadUpdate() {
		return true;
	}

	@Override
	public OffensiveEffects getMessage() {
		return OffensiveEffects.NO_POWER_RECHARGE;
	}

	public float getForce() {
		return force;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
