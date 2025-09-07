package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.schine.graphicsengine.core.Timer;

public class ThrusterOutageEffect extends BlockEffect {

	private float force;

	public ThrusterOutageEffect(SendableSegmentController controller, float slowAmount) {
		super(controller, BlockEffectTypes.THRUSTER_OUTAGE);
		this.force = slowAmount;
	}

	@Override
	public void update(Timer timer, FastSegmentControllerStatus status) {
		status.thrustPercent = force;
	}

	@Override
	public boolean needsDeadUpdate() {
		return true;
	}

	@Override
	public OffensiveEffects getMessage() {
		return OffensiveEffects.THRUSTER_OUTAGE;
	}

	public float getForce() {
		return force;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
