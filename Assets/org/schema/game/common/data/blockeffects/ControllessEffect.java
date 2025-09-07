package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.schine.graphicsengine.core.Timer;

public class ControllessEffect extends BlockEffect {

	public ControllessEffect(SendableSegmentController controller) {
		super(controller, BlockEffectTypes.CONTROLLESS);

		durationMS = 3000;
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
		return true;
	}

}
