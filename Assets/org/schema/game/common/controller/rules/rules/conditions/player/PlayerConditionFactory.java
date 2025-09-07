package org.schema.game.common.controller.rules.rules.conditions.player;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.conditions.ConditionFactory;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.schine.network.TopLevelType;

	public abstract class PlayerConditionFactory implements ConditionFactory<PlayerState, PlayerCondition> {

		@Override
		public TopLevelType getType() {
			return TopLevelType.PLAYER;
		}
		
		
}
