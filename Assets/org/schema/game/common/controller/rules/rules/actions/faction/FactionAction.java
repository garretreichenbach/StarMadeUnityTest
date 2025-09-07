package org.schema.game.common.controller.rules.rules.actions.faction;

import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.RuleEntityContainer;
import org.schema.schine.network.TopLevelType;

public abstract class FactionAction extends Action<Faction> {

	public FactionAction() {
		super();
	}
	@Override
	public void onTrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		Faction s = (Faction)sendable;
		onTrigger(s);
		
	}


	@Override
	public void onUntrigger(RuleEntityContainer sendable, TopLevelType topLevelType) {
		assert(getEntityType() == (topLevelType));
		Faction s = (Faction)sendable;
		onUntrigger(s);
	}
}
