package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public class TextState extends TimedState {

	/**
	 *
	 */
	
	private int durationInMs;

	public TextState(AiEntityStateInterface gObj, GameClientState state, String message, int durationInMs) {
		super(gObj, message, state);
		this.durationInMs = durationInMs;
	}

	@Override
	public int getDurationInMs() {
		return durationInMs;
	}

}
