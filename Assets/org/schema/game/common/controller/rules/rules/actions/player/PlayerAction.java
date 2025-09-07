package org.schema.game.common.controller.rules.rules.actions.player;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.schine.network.TopLevelType;

public abstract class PlayerAction extends Action<PlayerState> {

	public PlayerAction() {
		super();
	}
	@Override
	public void onTrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		PlayerState s = (PlayerState)sendable;
		onTrigger(s);
		
	}


	@Override
	public void onUntrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		PlayerState s = (PlayerState)sendable;
		onUntrigger(s);
	}
}
