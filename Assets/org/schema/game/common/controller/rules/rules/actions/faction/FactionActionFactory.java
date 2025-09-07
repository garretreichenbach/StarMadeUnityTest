package org.schema.game.common.controller.rules.rules.actions.faction;

import org.schema.game.common.controller.rules.rules.actions.ActionFactory;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.network.TopLevelType;

public abstract class FactionActionFactory implements ActionFactory<Faction, FactionAction> {

	@Override
	public TopLevelType getType() {
		return TopLevelType.FACTION;
	}
	
}
