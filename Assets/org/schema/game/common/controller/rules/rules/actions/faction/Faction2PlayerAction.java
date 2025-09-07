package org.schema.game.common.controller.rules.rules.actions.faction;

import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.actions.player.PlayerActionList;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;

public class Faction2PlayerAction extends FactionAction{

	
	@RuleValue(tag = "Actions")
	public PlayerActionList actions = new PlayerActionList();
	
	
	public Faction2PlayerAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Execute %s player actions for loaded players", actions.size());
	}

	@Override
	public void onTrigger(Faction s) {
		if(s.isOnServer()) {
			
			GameServerState state = (GameServerState) s.getState();

			for(String name : s.getMembersUID().keySet()) {
				PlayerState player = state.getPlayerFromNameIgnoreCaseWOException(name);	
				if(player != null) {
					actions.onTrigger(player);
				}
			}
		}
	}

	@Override
	public void onUntrigger(Faction s) {
		if(s.isOnServer()) {
			
			GameServerState state = (GameServerState) s.getState();

			for(String name : s.getMembersUID().keySet()) {
				PlayerState player = state.getPlayerFromNameIgnoreCaseWOException(name);	
				if(player != null) {
					actions.onUntrigger(player);
				}
			}
		}
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.FACTION_2_PLAYERS;
	}
}
