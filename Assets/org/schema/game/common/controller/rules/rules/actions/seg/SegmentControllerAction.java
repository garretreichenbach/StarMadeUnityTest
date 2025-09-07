package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.schine.network.TopLevelType;

public abstract class SegmentControllerAction extends Action<SegmentController> {

	public SegmentControllerAction() {
		super();
	}
	@Override
	public void onTrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		SegmentController s = (SegmentController)sendable;
		onTrigger(s);
		
	}


	@Override
	public void onUntrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		SegmentController s = (SegmentController)sendable;
		onUntrigger(s);
	}
}
