package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.schine.ai.AiEntityStateInterface;

public class InstantiateInventoyTestState extends TimedState {

	/**
	 *
	 */
	
	private boolean clear;

	public InstantiateInventoyTestState(AiEntityStateInterface gObj, String message, GameClientState state, boolean clear) {
		super(gObj, message, state);
		this.clear = clear;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.TimedState#onEnter()
	 */
	@Override
	public boolean onEnter() {

		getGameState().getPlayer().sendSimpleCommand(SimplePlayerCommands.BACKUP_INVENTORY, clear);

		return super.onEnter();
	}

	@Override
	public int getDurationInMs() {
		return 5000;
	}

}
