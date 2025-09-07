package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;

public class TutorialFailedState extends State {

	/**
	 *
	 */
	
	private GameClientState gameState;

	public TutorialFailedState(AiEntityStateInterface gObj, GameClientState gameState) {
		super(gObj);
		this.gameState = gameState;

	}

	@Override
	public boolean onEnter() {
		gameState.getController().popupAlertTextMessage(Lng.str("ERROR\nTutorial failed!\nSomething went wrong.\nPlease restart the game or exit tutorial!"), 0);

		return true;
	}

	@Override
	public boolean onExit() {
		return true;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		stateTransition(Transition.TUTORIAL_FAILED);
		return true;
	}

}
