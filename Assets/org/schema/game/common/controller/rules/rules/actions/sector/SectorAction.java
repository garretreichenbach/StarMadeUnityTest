package org.schema.game.common.controller.rules.rules.actions.sector;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.schine.network.TopLevelType;

public abstract class SectorAction extends Action<RemoteSector> {

	public SectorAction() {
		super();
	}
	@Override
	public void onTrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		RemoteSector s = (RemoteSector)sendable;
		onTrigger(s);
		
	}


	
	@Override
	public void onUntrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		RemoteSector s = (RemoteSector)sendable;
		onUntrigger(s);
	}
}
