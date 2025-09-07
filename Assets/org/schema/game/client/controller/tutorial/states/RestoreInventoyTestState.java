package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.schine.ai.AiEntityStateInterface;

public class RestoreInventoyTestState extends TimedState {

	/**
	 *
	 */
	

	public RestoreInventoyTestState(AiEntityStateInterface gObj, String message, GameClientState state) {
		super(gObj, message, state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.TimedState#onEnter()
	 */
	@Override
	public boolean onEnter() {

		getGameState().getPlayer().sendSimpleCommand(SimplePlayerCommands.RESTORE_INVENTORY);

		return super.onEnter();
	}

	@Override
	public int getDurationInMs() {
		return 5000;
	}

}
