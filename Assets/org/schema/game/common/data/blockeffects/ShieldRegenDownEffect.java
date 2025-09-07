package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.schine.graphicsengine.core.Timer;

public class ShieldRegenDownEffect extends BlockEffect {

	private float force;

	public ShieldRegenDownEffect(SendableSegmentController controller, float slowAmount) {
		super(controller, BlockEffectTypes.NO_SHIELD_RECHARGE);
		this.force = slowAmount;
	}

	@Override
	public void update(Timer timer, FastSegmentControllerStatus status) {
		status.shieldRegenPercent = force;
	}

	@Override
	public boolean needsDeadUpdate() {
		return true;
	}

	@Override
	public OffensiveEffects getMessage() {
		return OffensiveEffects.NO_SHIELD_RECHARGE;
	}

	public float getForce() {
		return force;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
