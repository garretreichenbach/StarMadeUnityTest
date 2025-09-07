package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public class TutorialEndedTextState extends TextState {

	/**
	 *
	 */
	

	public TutorialEndedTextState(AiEntityStateInterface gObj,
	                              GameClientState state, String message, int durationInMs) {
		super(gObj, state, message, durationInMs);
	}

}
