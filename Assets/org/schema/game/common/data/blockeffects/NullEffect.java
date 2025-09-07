package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.schine.graphicsengine.core.Timer;

public class NullEffect extends BlockEffect {

	public NullEffect(SendableSegmentController controller) {
		super(controller, BlockEffectTypes.NULL_EFFECT);
	}

	@Override
	public void update(Timer timer, FastSegmentControllerStatus status) {

	}

	@Override
	public boolean needsDeadUpdate() {
		return false;
	}
	@Override
	public boolean affectsMother() {
		return false;
	}
}
