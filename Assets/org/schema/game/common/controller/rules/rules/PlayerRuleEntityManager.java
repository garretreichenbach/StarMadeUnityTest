package org.schema.game.common.controller.rules.rules;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.actions.player.PlayerRecurringAction;
import org.schema.game.common.controller.rules.rules.conditions.player.PlayerCondition;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;

public class PlayerRuleEntityManager  extends RuleEntityManager<PlayerState>{

	public PlayerRuleEntityManager(PlayerState entity) {
		super(entity);
	}

	@Override
	public byte getEntitySubType() {
		return 120;
	}
	public void triggerPlayerChat() {
		trigger(PlayerCondition.TRIGGER_ON_PLAYER_CHAT);		
	}
	
	public void triggerPlayerCreditsChanged() {
		trigger(PlayerCondition.TRIGGER_ON_PLAYER_CREDITS_CHANGED);		
	}

	
	
}
