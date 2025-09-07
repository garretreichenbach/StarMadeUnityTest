package org.schema.game.client.controller.tutorial.states;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;

public class TutorialEnded extends State {

	/**
	 *
	 */
	

	public TutorialEnded(AiEntityStateInterface gObj) {
		super(gObj);

	}

	@Override
	public boolean onEnter() {
		return true;
	}

	@Override
	public boolean onExit() {
		return true;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		return true;
	}

}
