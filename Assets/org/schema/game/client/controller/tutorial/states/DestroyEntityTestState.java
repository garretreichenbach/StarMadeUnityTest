package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.schine.ai.AiEntityStateInterface;

public class DestroyEntityTestState extends TimedState {

	/**
	 *
	 */
	
	private String uid;

	public DestroyEntityTestState(AiEntityStateInterface gObj, String message, GameClientState state, String uid) {
		super(gObj, message, state);
		this.uid = uid;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.TimedState#onEnter()
	 */
	@Override
	public boolean onEnter() {

		getGameState().getPlayer().sendSimpleCommand(SimplePlayerCommands.DESTROY_TUTORIAL_ENTITY, uid);

		return super.onEnter();
	}

	@Override
	public int getDurationInMs() {
		return 5000;
	}

}
