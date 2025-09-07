package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public class SkipTestState extends SatisfyingCondition {

	/**
	 *
	 */
	

	public SkipTestState(AiEntityStateInterface gObj, GameClientState state) {
		super(gObj, "", state);
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		return true;
	}

}
