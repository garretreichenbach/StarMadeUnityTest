package org.schema.game.common.controller.rules.rules.actions.player;

import org.schema.game.common.controller.rules.rules.actions.RecurringAction;
import org.schema.game.common.data.player.PlayerState;

public abstract class PlayerRecurringAction extends PlayerAction implements RecurringAction<PlayerState> {



	private long time;

	@Override
	public void onTrigger(PlayerState s) {
		if(s.isOnServer()) {
			s.getRuleEntityManager().addRecurringAction(this);
		}
	}

	@Override
	public void onUntrigger(PlayerState s) {
		if(s.isOnServer()) {
			s.getRuleEntityManager().removeRecurringAction(this);
		}
	}

	@Override
	public long getLastActivate() {
		return time;
	}

	@Override
	public void setLastActivate(long time) {
		this.time = time;
	}



}
