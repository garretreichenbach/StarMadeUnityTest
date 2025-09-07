package org.schema.game.common.controller.rules.rules.actions.faction;

import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;

public class FactionModFactionPointsAction extends FactionAction{
	@RuleValue(tag = "FactionPoints")
	public int points;
	
	@Override
	public String getDescriptionShort() {
		return Lng.str("Add %s faction points", points);
	}

	@Override
	public void onTrigger(Faction s) {
		if(s.isOnServer()) {
			s.factionPoints += points;
			s.sendFactionPointUpdate(((GameServerState)s.getState()).getGameState());
		}
	}

	@Override
	public void onUntrigger(Faction s) {
		
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.FACTION_MOD_FACTION_POINTS;
	}


}
