package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.actions.ActionFactory;
import org.schema.schine.network.TopLevelType;
public abstract class SegmentControllerActionFactory implements ActionFactory<SegmentController, SegmentControllerAction> {

	@Override
	public TopLevelType getType() {
		return TopLevelType.SEGMENT_CONTROLLER;
	}

}
