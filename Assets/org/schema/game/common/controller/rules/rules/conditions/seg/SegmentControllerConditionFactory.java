package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.conditions.ConditionFactory;
import org.schema.schine.network.TopLevelType;

	public abstract class SegmentControllerConditionFactory implements ConditionFactory<SegmentController, SegmentControllerCondition> {

		@Override
		public TopLevelType getType() {
			return TopLevelType.SEGMENT_CONTROLLER;
		}
		
		
}
