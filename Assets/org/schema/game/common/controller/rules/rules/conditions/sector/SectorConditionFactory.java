package org.schema.game.common.controller.rules.rules.conditions.sector;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.conditions.ConditionFactory;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.schine.network.TopLevelType;

	public abstract class SectorConditionFactory implements ConditionFactory<RemoteSector, SectorCondition> {

		@Override
		public TopLevelType getType() {
			return TopLevelType.SECTOR;
		}
		
		
}
