package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionInFlightMode extends TutorialCondition {

	public TutorialConditionInFlightMode(State toObserve, State establishing) {
		super(toObserve, establishing);
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		return state.getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().
						getInShipControlManager().getShipControlManager().getShipExternalFlightController()
				.isActive();
	}

	@Override
	public String getNotSactifiedText() {
		return "Tutorial Condition failed\nExited flight Mode";
	}

	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_IN_FLIGHT_MODE;
	}

}
