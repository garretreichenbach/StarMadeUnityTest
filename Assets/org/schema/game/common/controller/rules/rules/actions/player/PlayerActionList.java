package org.schema.game.common.controller.rules.rules.actions.player;

import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionList;
import org.schema.game.common.controller.rules.rules.actions.seg.SegmentControllerAction;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.TopLevelType;

public class PlayerActionList extends ActionList<PlayerState, PlayerAction>{

	private static final long serialVersionUID = 1L;

	@Override
	public TopLevelType getEntityType() {
		return TopLevelType.PLAYER;
	}
	@Override
	public void add(Action<?> c) {
		add((PlayerAction)c);
	}
}
