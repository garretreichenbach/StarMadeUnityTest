package org.schema.game.common.controller.rules.rules.actions.faction;

import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionList;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.network.TopLevelType;

public class FactionActionList extends ActionList<Faction, FactionAction>{

	private static final long serialVersionUID = 1L;

	@Override
	public TopLevelType getEntityType() {
		return TopLevelType.FACTION;
	}
	@Override
	public void add(Action<?> c) {
		add((FactionAction)c);
	}
}
