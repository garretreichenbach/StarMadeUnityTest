package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionInBuildMode extends TutorialCondition {

	public TutorialConditionInBuildMode(State toObserve, State establishing) {
		super(toObserve, establishing);
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		return state.getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().
						getInShipControlManager().getShipControlManager().getSegmentBuildController()
				.isActive();
	}

	@Override
	public String getNotSactifiedText() {
		return "Tutorial Condition failed\nExited build Mode";
	}
	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_IN_BUILD_MODE;
	}
}
